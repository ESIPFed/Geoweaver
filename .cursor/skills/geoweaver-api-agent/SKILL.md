---
name: geoweaver-api-agent
description: >-
  Drive classic Geoweaver without the GUI via /Geoweaver/api/v1 on a local or
  public URL: create/edit Python and Shell processes, build/run workflows on the
  Geoweaver server host, poll/stop history. Use when GEOWEAVER_BASE_URL /
  GEOWEAVER_API_TOKEN are set, or the user wants Codex/Cursor/Claude Code to use
  a deployed Geoweaver endpoint like a human UI. Never start or stop the
  Geoweaver JVM — the operator runs the server; the agent only calls HTTP.
version: 1.6.0
---

# Geoweaver Agent API skill

**Goal:** Do what a human does in the Geoweaver UI for **Python/Shell processes and workflows**, over JSON HTTP only, against **any reachable Geoweaver base URL**.

**Docs:** `docs/agent-api.md`

**Not this skill:** AuroWeaver managed API, MCP-first control, Jupyter/builtin types on `/api/v1`, remote SSH hosts on `/api/v1`.

## Hard boundary (non-negotiable)

The agent **only** talks to Geoweaver through `{GEOWEAVER_BASE_URL}/api/v1` (curl/httpx/fetch).

**Never:**

- Start, stop, kill, or restart the Geoweaver JVM (`java -jar`, IDE run, `pkill`, freeing port 8070)
- Edit `application.properties` / system properties to “bring the server up”
- Use `gw` / `geoweaver` CLI except when the **operator** asks you to mint a token on their machine (prefer HTTP `POST /api/v1/tokens`)
- Use HTML `/web/*` login, WebSocket UI, or scrape the GUI
- Write Bearer tokens into repo files or print them in chat

**If health fails:** tell the user Geoweaver is not reachable and wait for them to start it with Agent API enabled. Do not start it yourself.

Operator enable (their job, not the agent’s):

```properties
geoweaver.agent.api-enabled=true
geoweaver.agent.allow-localhost-runs=true   # optional: Bearer may run localhost without GUI password
```

## Environment

| Variable | Meaning |
| --- | --- |
| `GEOWEAVER_BASE_URL` | Full base including context path, e.g. `https://gw.example.org/Geoweaver` or `http://127.0.0.1:8070/Geoweaver` |
| `GEOWEAVER_API_TOKEN` | Bearer token (mint with GUI localhost password). Stored as a hash in the Geoweaver **database**, never in a Geoweaver token file. |

### Mint / rotate token (HTTP preferred for agents)

Operator must have enabled `geoweaver.agent.api-enabled=true`. You need the **GUI localhost password** (ask once; never echo it).

```bash
# HTTP (no Bearer). ttlDays optional (1..180 / six months); default 30.
curl -sS -X POST "$GEOWEAVER_BASE_URL/api/v1/tokens" \
  -H "Content-Type: application/json" \
  -d '{"hostPassword":"<gui-localhost-password>","ttlDays":30}'
# → 201 includes token once; store in secret store only; do not echo in chat
```

Wrong password → 403. Missing password → 400. Too many tries from one IP → 429. API off → 503.  
Token create **always** requires the localhost password (`allow-localhost-runs` does not skip this).

```bash
BASE="$GEOWEAVER_BASE_URL/api/v1"
H="Authorization: Bearer $GEOWEAVER_API_TOKEN"
```

**Execution location:** `hostId=100001` is the **Geoweaver server machine**, not the agent laptop.

## Security (always enforce)

| Control | Behavior |
| --- | --- |
| Bearer token | Required except `GET /api/v1/health` and `POST /api/v1/tokens` |
| `geoweaver.agent.api-enabled` | Must be `true` or API returns 503 |
| Server-local password | If `allow-localhost-runs=false`, runs need JSON `hostPassword` |
| Host scope | Only `hostId=100001`. Remote SSH → 400 |

Never print the full token or host password in chat.

## First contact

```bash
curl -sS "$GEOWEAVER_BASE_URL/api/v1/health"
# expect: {"status":"up"}

curl -sS "$GEOWEAVER_BASE_URL/api/v1/capabilities" -H "$H"
# expect: 200 JSON with processLanguages python/shell
```

| Result | Action |
| --- | --- |
| Connection refused / timeout | Ask user to start Geoweaver; **do not** start it yourself |
| 401 | Fix / remint token via `POST /tokens` |
| 503 `api_disabled` | Ask operator to set `geoweaver.agent.api-enabled=true` and restart **their** server |

## Agent playbook (UI parity)

### 1) Inventory

```bash
curl -sS "$BASE/processes" -H "$H"
curl -sS "$BASE/workflows" -H "$H"
```

### 2) Create / edit process

```bash
curl -sS -X POST "$BASE/processes" -H "$H" -H "Content-Type: application/json" \
  -d '{"name":"demo","lang":"python","code":"print(1+1)"}'

curl -sS -X PUT "$BASE/processes/PROCESS_ID" -H "$H" -H "Content-Type: application/json" \
  -d '{"code":"print(2+2)"}'
```

Languages: **python** or **shell** only.

### 3) Run on the Geoweaver host

```bash
curl -sS -X POST "$BASE/processes/PROCESS_ID/runs" -H "$H" -H "Content-Type: application/json" \
  -d '{"hostId":"100001","clientRunKey":"unique-job-key"}'
```

New `clientRunKey` after code edits. Same key → `reusedExisting: true`.

### 4) Poll

```bash
curl -sS "$BASE/runs/HISTORY_ID?logLimit=200000" -H "$H"
```

Backoff 1s → 2s → 5s until `terminal` is true. HTTP **404** → stop. `truncated: true` → partial log.

### 5) Stop

```bash
curl -sS -X POST "$BASE/runs/HISTORY_ID/stop" -H "$H" -H "Content-Type: application/json" -d '{}'
```

### 6) Workflows

Node `id`: `{processId}-{suffix}` (example: `abc123-n0001`).

For a **one-step** workflow, use `edges` as the JSON string `"[]"` (empty array). Avoid inventing edge object shapes unless you copy them from a workflow that already runs in the UI.

```bash
curl -sS -X POST "$BASE/workflows" -H "$H" -H "Content-Type: application/json" \
  -d '{"name":"demo-wf","nodes":"[{\"title\":\"demo\",\"id\":\"PROCESS_ID-n0001\",\"x\":100,\"y\":100}]","edges":"[]"}'

curl -sS -X POST "$BASE/workflows/WORKFLOW_ID/runs" -H "$H" -H "Content-Type: application/json" \
  -d '{"mode":"one","clientRunKey":"wf-1"}'
```

**Reading workflow output:** Workflow history can stay `Running` / non-terminal even after the step finished. Always also poll:

```bash
curl -sS "$BASE/processes/PROCESS_ID/runs?logLimit=200000" -H "$H"
```

Use the latest process run’s `log` when `status` is `Done` / `Failed` / `Stopped`. That log is the trustworthy step stdout.

## Failure map

| Symptom | Action |
| --- | --- |
| Connection / timeout | Ask user to start Geoweaver; check `GEOWEAVER_BASE_URL` includes `/Geoweaver` |
| 401 | Remint via `POST /tokens` + localhost password |
| 503 api_disabled | Operator enables Agent API and restarts **their** process |
| 403 on `/tokens` | Wrong GUI localhost password |
| 429 on `/tokens` | Wait and retry |
| 403 on runs | Need `allow-localhost-runs=true` on server **or** correct `hostPassword` |
| 400 remote host / bad lang | Use `100001` + python/shell only |
| 404 | Bad id; do not poll forever |
| Workflow history stuck Running | Read `GET /processes/{id}/runs` for step Done + log |

## Act like a human

1. Use only `/api/v1`. Never start the server yourself.
2. Wait for terminal status on the **process** run before claiming success for that step.
3. On `Failed`, read `log`, fix code via `PUT`, re-run with a new `clientRunKey`.
4. Do not invent remote SSH or Jupyter endpoints on `/api/v1`.
