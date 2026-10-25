---
name: geoweaver-api-agent
description: >-
  Drive Geoweaver without the GUI via /Geoweaver/api/v1 on a local or public
  URL: create/edit Python, Shell and builtin processes, build/run workflows on
  the Geoweaver server or registered SSH hosts, manage hosts and Python
  environments, history, checkpoints, export/import and result files. Use when GEOWEAVER_BASE_URL /
  GEOWEAVER_API_TOKEN are set, or the user wants Codex/Cursor/Claude Code to use
  a deployed Geoweaver endpoint like a human UI. Never start or stop the
  Geoweaver JVM — the operator runs the server; the agent only calls HTTP.
version: 2.0.0
---

# Geoweaver Agent API skill

**Goal:** Do what a human does in the Geoweaver UI (processes, workflows, hosts, environments, history, checkpoints, results), over JSON HTTP only, against **any reachable Geoweaver base URL**.

**Docs:** `docs/agent-api.md`

**Not this skill:** AuroWeaver managed API, MCP-first control, Jupyter notebook processes (Geoweaver cannot run them).

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
6. Send `nodes` and `edges` as plain JSON arrays. `title`/`x`/`y` are optional (filled from the process name and a left-to-right layout). Edge shorthand is fine:

```json
{"name":"two-step","nodes":[{"id":"PROC_A-n0001"},{"id":"PROC_B-n0002"}],
 "edges":[{"source":"PROC_A-n0001","target":"PROC_B-n0002"}]}
```

The older stringified GUI form (`"nodes":"[...]"`, edges with full `source`/`target` objects) is still accepted. Unknown process ids, bad node ids, and edges to missing nodes fail at create time with a 400 naming the bad entry.

One-step demos may still use `"edges":"[]"`. Real multi-stage work must use real edges.

## Local checkpoint backup (always)

After **creating**, **editing**, or a **successful run** of any workflow (and after each meaningful process `PUT`), **download a local checkpoint** before ending the turn.

Default root (outside the git repo):

`~/Documents/Geoweaver-checkpoints/{workflow-name-or-id}/{YYYYMMDDTHHMMSS}/`

Pull via `/api/v1` only and write at least:

| File | Source |
| --- | --- |
| `bundle.json` | `GET /workflows/{id}/export` (workflow + every process's code; re-importable with `POST /workflows/import`) |
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

**OOD HTTPS Path B (one command):** Operator runs `./scripts/gw-ood-login '<Connect-UI-URL>'` (cookies + mint Bearer with **Hopper** GUI password, not laptop) then `source ~/.config/geoweaver-agent/ood-env.sh`. Agents must use `-b "$GEOWEAVER_OOD_COOKIE_JAR"` plus `$GEOWEAVER_API_TOKEN`; **never** ask for cookie/token paste; **never** `cat` secret files into chat. Laptop Bearer is invalid on Hopper.

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
| SSH hosts | `hostPassword` = that host's SSH password, sent per call, never stored. Ask the user once; keep it in their secret store, never in files or chat |
| `allow-remote-hosts=false` | Host add/edit/delete and SSH runs → 403; use `100001` |
| Process names | Letters, digits, `.` `_` `-` only (they become script file names) |

Never print the full token, GUI password or SSH password in chat.

## First contact

```bash
curl -sS "$GEOWEAVER_BASE_URL/api/v1/health"
# expect: {"status":"up"}

curl -sS "$GEOWEAVER_BASE_URL/api/v1/capabilities" -H "$H"
# expect: 200 JSON with processLanguages python/shell/builtin and the operations list
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
curl -sS "$BASE/processes?q=landsat" -H "$H"    # q is optional search
curl -sS "$BASE/workflows" -H "$H"
curl -sS "$BASE/hosts" -H "$H"                  # 100001 = the Geoweaver server
curl -sS "$BASE/runs?status=Running" -H "$H"    # what is running now
```

### 2) Create / edit process

```bash
curl -sS -X POST "$BASE/processes" -H "$H" -H "Content-Type: application/json" \
  -d '{"name":"demo","lang":"python","code":"print(1+1)"}'

curl -sS -X PUT "$BASE/processes/PROCESS_ID" -H "$H" -H "Content-Type: application/json" \
  -d '{"code":"print(2+2)"}'
```

Languages: **python**, **shell**, or **builtin** (`code` is JSON like `{"operation":"AWS S3","params":[]}`).

### 3) Run on the Geoweaver host

```bash
curl -sS -X POST "$BASE/processes/PROCESS_ID/runs" -H "$H" -H "Content-Type: application/json" \
  -d '{"hostId":"100001","clientRunKey":"unique-job-key"}'
```

New `clientRunKey` after code edits. Same key → `reusedExisting: true`.

### 4) Wait for the result (one call)

```bash
curl -sS "$BASE/runs/HISTORY_ID?waitSeconds=60&logLimit=200000" -H "$H"
```

`waitSeconds` (0..60) blocks until `terminal` is true or the wait ends; repeat the same call if still not terminal. HTTP **404** → stop. `truncated: true` → partial log.

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

**Reading workflow output:** one call gives every step's status and log tail:

```bash
curl -sS "$BASE/runs/WORKFLOW_HISTORY_ID?waitSeconds=60&includeStepLogs=true" -H "$H"
```

The response has `workflowRun: true` and `steps: [{nodeId, processId, historyId, status, terminal, log}]`. A step starts after its upstream steps finish **even if one Failed**, so check every step's `status`, not just the workflow's. Use `GET /runs/{step.historyId}?logLimit=200000` for a full step log. History: `GET /workflows/{id}/runs`.

### 7) Hosts, environments, remote runs

```bash
# add an SSH server (no password field; Geoweaver never stores it)
curl -sS -X POST "$BASE/hosts" -H "$H" -H "Content-Type: application/json" \
  -d '{"name":"hpc","ip":"login.hpc.example.edu","port":"22","username":"alice"}'
# discover Python/conda environments there, then list them
curl -sS -X POST "$BASE/hosts/HOST_ID/environments/scan" -H "$H" -H "Content-Type: application/json" \
  -d '{"hostPassword":"<ssh password>"}'
curl -sS "$BASE/hosts/HOST_ID/environments" -H "$H"
# run a process there
curl -sS -X POST "$BASE/processes/PROCESS_ID/runs" -H "$H" -H "Content-Type: application/json" \
  -d '{"hostId":"HOST_ID","hostPassword":"<ssh password>","envId":"ENV_ID","clientRunKey":"hpc-1"}'
# workflow with steps on different hosts
curl -sS -X POST "$BASE/workflows/WORKFLOW_ID/runs" -H "$H" -H "Content-Type: application/json" \
  -d '{"nodeHosts":{"PROC_A-n0001":{"hostId":"100001"},"PROC_B-n0002":{"hostId":"HOST_ID","envId":"ENV_ID"}},"hostPasswords":{"HOST_ID":"<ssh password>"}}'
```

502 `host_unreachable` = the SSH server is down or the name is wrong; 403 = wrong SSH password.

### 8) Skip steps, roll back, export

```bash
curl -sS -X PUT "$BASE/workflows/WORKFLOW_ID/steps/PROC_B-n0002" -H "$H" -H "Content-Type: application/json" -d '{"skip":true}'
curl -sS "$BASE/workflows/WORKFLOW_ID/checkpoints" -H "$H"          # one per run
curl -sS -X POST "$BASE/workflows/WORKFLOW_ID/restore" -H "$H" -H "Content-Type: application/json" -d '{"runId":"RUN_ID"}'
curl -sS "$BASE/workflows/WORKFLOW_ID/export" -H "$H" > bundle.json
curl -sS -X POST "$BASE/workflows/import" -H "$H" -H "Content-Type: application/json" --data @bundle.json   # 409 → add ?replace=true only if the user agrees
curl -sS -X PUT "$BASE/runs/RUN_ID/notes" -H "$H" -H "Content-Type: application/json" -d '{"notes":"baseline run"}'
```

### 9) Result files

```bash
curl -sS "$BASE/results" -H "$H"                       # top of the results folder
curl -sS "$BASE/results?path=SUBFOLDER" -H "$H"
curl -sS "$BASE/results/file?path=SUBFOLDER/out.png" -H "$H" -o out.png
```

### 10) Clean up what you created for experiments

```bash
curl -sS -X DELETE "$BASE/workflows/WORKFLOW_ID" -H "$H"
curl -sS -X DELETE "$BASE/processes/PROCESS_ID" -H "$H"   # 409 while a workflow uses it
curl -sS -X DELETE "$BASE/runs/RUN_ID" -H "$H"             # finished runs only
curl -sS -X DELETE "$BASE/hosts/HOST_ID" -H "$H"
```

Only delete objects you created in this task. Never delete a human's processes or workflows.

### Errors

Every error is JSON `{"error": code, "message": text}` with a real status code (400 `bad_request`, 401, 403, 404 `not_found`, 409 `conflict`, 415, 429, 500 `internal_error`, 502 `host_unreachable`, 503). Read `message`: it says what to fix. Schema: `GET $GEOWEAVER_BASE_URL/v3/api-docs/agent-api` (no token needed).

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
| 400 `hostPassword … is required` | Ask the user for that host's SSH password (once), send it with the run |
| 502 host_unreachable | SSH host down or wrong `ip`; check `GET /hosts/{id}` with the user |
| 400 bad lang / name | python/shell/builtin; name of letters, digits, `.` `_` `-` |
| 404 | Bad id or path; `message` names it; do not poll forever |
| 400 on workflow create/update | `message` names the bad node/edge; fix ids from `GET /processes` |
| 409 on process delete | `message` lists workflows that use it; remove from those first |
| Workflow Failed | Find the step with `status: Failed` in `steps`; read its `log` |

## Act like a human

1. Use only `/api/v1`. Never start the server yourself.
2. Split complicated goals into small processes; link them with sequential or parallel edges; hand off via host files.
3. Wait (`waitSeconds=60`) for terminal status; for workflows check every entry in `steps` before claiming success.
4. On `Failed`, read `log`, fix code via `PUT`, re-run with a new `clientRunKey`.
5. Use only endpoints listed in `GET /capabilities`; never invent paths. Ask before deleting anything you did not create or importing with `replace=true`.
6. Always write a **local checkpoint** under `~/Documents/Geoweaver-checkpoints/` after create / edit / successful run (include **all** process ids in the workflow).
