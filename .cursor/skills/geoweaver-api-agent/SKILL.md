---
name: geoweaver-api-agent
description: >-
  Drive classic Geoweaver without the GUI via /Geoweaver/api/v1 on a local or
  public URL: create/edit Python and Shell processes, build/run workflows on the
  Geoweaver server host, poll/stop history. Use when GEOWEAVER_BASE_URL /
  GEOWEAVER_API_TOKEN are set, or the user wants Codex/Cursor/Claude Code to use
  a deployed Geoweaver endpoint like a human UI.
version: 1.5.0
---

# Geoweaver Agent API skill

**Goal:** Do what a human does in the Geoweaver UI for **Python/Shell processes and workflows**, over JSON HTTP, against **any reachable Geoweaver base URL** (local or public).

**Docs:** `docs/agent-api.md`

**Not this skill:** AuroWeaver managed API, MCP-first control, Jupyter/builtin types on `/api/v1`, remote SSH hosts on `/api/v1`.

## Environment

| Variable | Meaning |
| --- | --- |
| `GEOWEAVER_BASE_URL` | Full base including context path, e.g. `https://gw.example.org/Geoweaver` or `http://127.0.0.1:8070/Geoweaver` |
| `GEOWEAVER_API_TOKEN` | Bearer token (mint with GUI localhost password or server CLI). Stored as a hash in the Geoweaver **database**, never in a Geoweaver token file. |

### Mint / rotate token from any machine

Operator must have enabled `geoweaver.agent.api-enabled=true`. You need the **GUI localhost password** (not a random public signup).

```bash
# Preferred CLI (prompts for password)
gw agent-token --create --base-url "$GEOWEAVER_BASE_URL" --ttl-days 30
# Prompts for GUI localhost password. Hash is stored on that server (not this CLI’s database).
# Prints the secret once (the copy web page is loopback-only on the server).
# Do NOT put the token in environment variables. Never paste into chat.

# Equivalent HTTP (no Bearer). ttlDays optional (1..180 / six months); default 30.
curl -sS -X POST "$GEOWEAVER_BASE_URL/api/v1/tokens" \
  -H "Content-Type: application/json" \
  -d '{"hostPassword":"<gui-localhost-password>","ttlDays":30}'
# → 201 includes token once; store in secret store only; do not echo in chat
# Tokens live in the Geoweaver database as hashes. They always expire (max 180 days). Remint before expiresAt or after 401.
```

Wrong password → 403. Missing password → 400. Too many tries from one IP → 429. API off → 503.  
Token create **always** requires the localhost password (`allow-localhost-runs` does not skip this).

```bash
BASE="$GEOWEAVER_BASE_URL/api/v1"
H="Authorization: Bearer $GEOWEAVER_API_TOKEN"
```

**Execution location:** `hostId=100001` is the **Geoweaver server machine**, not the agent laptop. Public URL + Codex still runs jobs on that server.

## Security (always enforce)

| Control | Behavior |
| --- | --- |
| Bearer token | Required except `GET /api/v1/health` |
| `geoweaver.agent.api-enabled` | Must be `true` or API returns 503 |
| Server-local password | If `allow-localhost-runs=false`, runs need JSON `hostPassword` (GUI localhost password **on the server**) |
| Public bind | Reverse proxy to 127.0.0.1 **or** `allow-non-loopback=true` if the JVM listens beyond loopback |
| Host scope | Only `hostId=100001`. Remote SSH → 400 |

Never print the full token or host password in chat.

## First contact (any deployment)

```bash
curl -sS "$GEOWEAVER_BASE_URL/api/v1/health"
# expect: {"status":"up"}

curl -sS "$GEOWEAVER_BASE_URL/api/v1/capabilities" -H "$H"
# expect: 200 JSON with processLanguages python/shell
```

If health fails → wrong URL / firewall / Geoweaver down.  
If capabilities is 401 → fix token.  
If 503 → ask operator to enable `geoweaver.agent.api-enabled=true`.

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

Node `id`: `{processId}-{suffix}`.

```bash
curl -sS -X POST "$BASE/workflows" -H "$H" -H "Content-Type: application/json" \
  -d '{"name":"demo-wf","nodes":"[{\"title\":\"demo\",\"id\":\"PROCESS_ID-n0001\",\"x\":100,\"y\":100}]","edges":"[]"}'

curl -sS -X POST "$BASE/workflows/WORKFLOW_ID/runs" -H "$H" -H "Content-Type: application/json" \
  -d '{"mode":"one","clientRunKey":"wf-1"}'
```

## Failure map

| Symptom | Action |
| --- | --- |
| Connection / timeout | Check `GEOWEAVER_BASE_URL` (include `/Geoweaver`), TLS, firewall |
| 401 | Fix `GEOWEAVER_API_TOKEN` (mint via `POST /tokens` + localhost password, or local `agent-token --create`) |
| 503 api_disabled | Operator enables Agent API and restarts |
| 403 on `/tokens` | Wrong GUI localhost password |
| 429 on `/tokens` | Too many password attempts from this IP — wait and retry |
| 403 on runs | Need `allow-localhost-runs=true` on server **or** correct `hostPassword` |
| 400 remote host / bad lang | Use `100001` + python/shell only |
| 404 | Bad id; do not poll forever |

## Act like a human

1. Prefer `/api/v1` over asking anyone to click the UI.
2. Wait for terminal status before claiming success.
3. On `Failed`, read `log`, fix code, re-run with a new `clientRunKey`.
4. Do not invent remote SSH or Jupyter endpoints on `/api/v1`.
