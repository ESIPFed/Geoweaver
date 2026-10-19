---
name: geoweaver-api-agent
description: >-
  Drive classic Geoweaver without the GUI via /Geoweaver/api/v1 on a local or
  public URL: create/edit Python and Shell processes, build/run workflows on the
  Geoweaver server host, poll/stop history. Use when GEOWEAVER_BASE_URL /
  GEOWEAVER_API_TOKEN are set, or the user wants Codex/Cursor/Claude Code to use
  a deployed Geoweaver endpoint like a human UI. Never start or stop the
  Geoweaver JVM — the operator runs the server; the agent only calls HTTP.
version: 1.8.0
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
- Cram a multi-stage job into **one** process when the work has clear stages (discover → transform → analyze → report)

## Break complicated work into small linked processes (always)

Before coding a “big” goal (screening, forecasting pipelines, multi-source research):

1. **Split** into small processes first (one job each). Example for equities research:
   - discover candidates (live lists / screeners — **not** a hardcoded ticker list)
   - enrich / analyze those candidates (news, price, volume, filters)
   - report evidence and ranking
2. **Link** them in a workflow with `nodes` + `edges`:
   - **Sequential:** A → B → C when B needs A’s outputs.
   - **Parallel:** A → B and A → C (or B and C with no edge between them after a shared prep step) when stages are independent.
3. **Pass data on the Geoweaver host** (`hostId=100001`) via a shared directory (for example `~/gw-pipeline/<run-name>/`). Geoweaver does **not** auto-pipe stdout from one process into the next. Write JSON/files in an upstream step; read them downstream. Print a clear `HANDOFF_PATH=...` line in logs.
4. **Do not** fix the analysis universe in the analyze step when a prior discover step can choose it dynamically.
5. Node `id` format remains `{processId}-{suffix}` (example: `abc123-n0001`).
6. Edge JSON shape (stringified array), copy this pattern:

```json
[{"source":{"title":"step-a","id":"PROC_A-n0001","x":80,"y":120},"target":{"title":"step-b","id":"PROC_B-n0002","x":320,"y":120}}]
```

One-step demos may still use `"edges":"[]"`. Real multi-stage work must use real edges.

## Local checkpoint backup (always)

After **creating**, **editing**, or a **successful run** of any workflow (and after each meaningful process `PUT`), **download a local checkpoint** before ending the turn.

Default root (outside the git repo):

`~/Documents/Geoweaver-checkpoints/{workflow-name-or-id}/{YYYYMMDDTHHMMSS}/`

Pull via `/api/v1` only and write at least:

| File | Source |
| --- | --- |
| `workflow.json` | `GET /workflows/{id}` |
| `process-{id}.json` (+ `.py` / `.sh` code extract) | `GET /processes/{id}` for every node process |
| `process-runs.json` | `GET /processes/{id}/runs?logLimit=200000` (latest after a run) |
| `workflow-run.json` | `GET /runs/{workflowHistoryId}` when a workflow run exists |
| `manifest.json` | ids, names, base URL, timestamp — **no API token** |

Do **not** commit checkpoints into the Geoweaver git repo. Do **not** store tokens in the checkpoint folder.

**If health fails:** tell the user Geoweaver is not reachable and wait for them to start it with Agent API enabled. Do not start it yourself.

Operator notes (defaults are on in current Geoweaver builds):

```properties
geoweaver.agent.api-enabled=true
geoweaver.agent.allow-localhost-runs=true
geoweaver.agent.allow-non-loopback=true
# Required when the JVM bind is not loopback, or startup fails:
geoweaver.agent.acknowledge-network-risk=true
```

If health returns 503 `api_disabled`, ask the operator to enable the Agent API and restart **their** server. Do not start the server yourself.

## Environment

| Variable | Meaning |
| --- | --- |
| `GEOWEAVER_BASE_URL` | Full base including context path, e.g. `https://gw.example.org/Geoweaver`, Open OnDemand same-prefix `https://ondemand…/rnode/HOST/PORT/Geoweaver`, or SSH-tunnel `http://127.0.0.1:18070/Geoweaver` aimed at the **hopper** JVM |
| `GEOWEAVER_API_TOKEN` | Bearer token (mint with GUI localhost password **against that same base**). Stored as a hash in the Geoweaver **database**, never in a Geoweaver token file. |

### Open OnDemand: same domain and path as the UI

If the human opened:

`https://ondemand…/rnode/HOST/PORT/Geoweaver/web/geoweaver`

set:

`GEOWEAVER_BASE_URL=https://ondemand…/rnode/HOST/PORT/Geoweaver`

Do **not** fall back to a laptop-only Geoweaver when the user named an OOD session. The `PORT` changes every job—refresh the base URL.

**Two credentials:**

1. **Campus / OOD browser session** (cookies after GMU login) — only needed if you call the public OOD HTTPS URL; lets Apache forward to the node.
2. **Geoweaver Bearer** — always required for `/api/v1` (except health / `POST /tokens`). Mint with Geoweaver GUI password on **this** instance. Campus login is **not** a Geoweaver API token.

**Preferred for Cursor on a laptop:** SSH local forward to the compute node’s `GEOWEAVER_PORT`, set `GEOWEAVER_BASE_URL` to `http://127.0.0.1:<local>/Geoweaver`, mint Bearer there. See `docs/agent-api.md` (Open OnDemand section).

**OOD HTTPS Path B (one command, cookies on disk):** Operator runs `./scripts/gw-ood-login '<Connect-UI-URL>'` then `source ~/.config/geoweaver-agent/ood-env.sh`. Agents must use `-b "$GEOWEAVER_OOD_COOKIE_JAR"` plus Bearer; **never** ask for cookie paste; **never** `cat` the jar into chat.

**If health returns HTML/302 to Shibboleth/IdP:** ask operator to re-run `gw-ood-login` or use SSH tunnel; do not invent a second localhost Geoweaver as hopper.

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
| HTTP **302** / HTML campus login (Shibboleth) | OOD reverse proxy blocked the client; ask for SSH tunnel to hopper port **or** out-of-band OOD session cookies + Bearer on the **same** OOD base—do not switch to unrelated laptop Geoweaver |
| 401 | Fix / remint token via `POST /tokens` against the **same** `GEOWEAVER_BASE_URL` (laptop token ≠ hopper token) |
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

For a **trivial one-step** demo only, `edges` may be `"[]"`. For any non-trivial goal, create **multiple processes** and wire **sequential or parallel** edges (see “Break complicated work” above).

```bash
# Example: two-step sequential workflow (discover → analyze)
curl -sS -X POST "$BASE/workflows" -H "$H" -H "Content-Type: application/json" \
  -d '{"name":"demo-two-step","nodes":"[{\"title\":\"discover\",\"id\":\"PROC_A-n0001\",\"x\":80,\"y\":120},{\"title\":\"analyze\",\"id\":\"PROC_B-n0002\",\"x\":320,\"y\":120}]","edges":"[{\"source\":{\"title\":\"discover\",\"id\":\"PROC_A-n0001\",\"x\":80,\"y\":120},\"target\":{\"title\":\"analyze\",\"id\":\"PROC_B-n0002\",\"x\":320,\"y\":120}}]"}'

curl -sS -X POST "$BASE/workflows/WORKFLOW_ID/runs" -H "$H" -H "Content-Type: application/json" \
  -d '{"mode":"one","clientRunKey":"wf-1"}'
```

**Reading workflow output:** Workflow history can stay `Running` / non-terminal even after the step finished. Always also poll **each** process:

```bash
curl -sS "$BASE/processes/PROCESS_ID/runs?logLimit=200000" -H "$H"
```

Use each process run’s `log` when `status` is `Done` / `Failed` / `Stopped`. That log is the trustworthy step stdout. For multi-step graphs, wait until **every** step you care about is terminal (or until the workflow history is terminal **and** step logs show Done).

## Failure map

| Symptom | Action |
| --- | --- |
| Connection / timeout | Ask user to start Geoweaver; check `GEOWEAVER_BASE_URL` includes `/Geoweaver` |
| 302 / IdP HTML on `/api/v1` | Same-prefix URL is correct but OOD SSO blocked; tunnel or cookies—see Open OnDemand section |
| 401 | Remint via `POST /tokens` + localhost password on **this** base |
| 503 api_disabled | Operator enables Agent API and restarts **their** process |
| 403 on `/tokens` | Wrong GUI localhost password |
| 429 on `/tokens` | Wait and retry |
| 403 on runs | Need `allow-localhost-runs=true` on server **or** correct `hostPassword` |
| 400 remote host / bad lang | Use `100001` + python/shell only |
| 404 | Bad id; do not poll forever |
| Workflow history stuck Running | Read `GET /processes/{id}/runs` for step Done + log |

## Act like a human

1. Use only `/api/v1`. Never start the server yourself.
2. Split complicated goals into small processes; link them with sequential or parallel edges; hand off via host files.
3. Wait for terminal status on each **process** run before claiming success for that step.
4. On `Failed`, read `log`, fix code via `PUT`, re-run with a new `clientRunKey`.
5. Do not invent remote SSH or Jupyter endpoints on `/api/v1`.
6. Always write a **local checkpoint** under `~/Documents/Geoweaver-checkpoints/` after create / edit / successful run (include **all** process ids in the workflow).
