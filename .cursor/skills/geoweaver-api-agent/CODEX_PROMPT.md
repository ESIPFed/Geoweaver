# Codex prompt — Geoweaver Agent API (local or public)

Copy everything below the line into Codex.  
Prerequisites: **the human has already started Geoweaver** and it is reachable; env vars set (do **not** paste secrets into this prompt).

---

You are controlling **classic Geoweaver** over HTTP like a human using the UI, without the GUI and without MCP.

## Environment (already configured)

- `GEOWEAVER_BASE_URL` — full base URL **including** `/Geoweaver`, e.g.
  - public / reverse proxy: `https://gw.example.org/Geoweaver`
  - Open OnDemand (same prefix as UI; strip `/web/geoweaver`):  
    `https://ondemand…/rnode/HOST/PORT/Geoweaver`
  - SSH tunnel to the **hopper** JVM (preferred for Cursor when OOD returns IdP 302):  
    `http://127.0.0.1:18070/Geoweaver`
- Bearer token for `/api/v1` — inject from a secret store (prefer not to keep it in shell env).
  Mint against **this** base (hopper DB ≠ laptop DB). Campus/GMU login cookies are **not**
  a Geoweaver Bearer; if you use the public OOD HTTPS URL you may need **both** OOD session
  cookies and the Bearer.
- If health is **302/HTML to campus login**: fix tunnel or cookies; do not substitute an
  unrelated local Geoweaver for the user’s OOD session.
- If the token is missing or expired (401): `POST $GEOWEAVER_BASE_URL/api/v1/tokens` JSON
  `{"hostPassword":"...","ttlDays":30}` (no Bearer; `ttlDays` max 180 / six months). Ask the user
  for the **Geoweaver GUI localhost password** once; never echo it back. Do not save the token
  in env files or the repo. Tokens are stored in the Geoweaver database as hashes only.

Docs: `docs/agent-api.md`  
Playbook: `.cursor/skills/geoweaver-api-agent/SKILL.md`

## Hard rules

1. **Only** call `$GEOWEAVER_BASE_URL/api/v1/...` (curl/httpx/Shell HTTP). Do **not** start, stop, kill, or restart Geoweaver (`java -jar`, IDE, `pkill`, port fights). If health fails, ask the human to start the server.
2. Use `Authorization: Bearer $GEOWEAVER_API_TOKEN` for all routes except `GET /health` and `POST /tokens`.
3. Do not use HTML `/web/*` login.
4. Process languages: **python** or **shell** only.
5. Runs use `hostId=100001` = **the Geoweaver server host** (not your laptop).
6. Never print the full token or host password.
7. After starting a run, poll until terminal. For **workflows**, also poll `GET /processes/{id}/runs` — workflow history may stay `Running` after the step is `Done`; trust the process run log for stdout.
8. Unique `clientRunKey` per logical job; new key after code edits.
9. First call `GET .../health` then `GET .../capabilities`.
10. After create / edit / successful run: always download a local checkpoint under
    `~/Documents/Geoweaver-checkpoints/{workflow-name-or-id}/{timestamp}/`
    (`workflow.json`, process JSON+code, run logs, `manifest.json`). No API tokens in that folder; do not commit it to git.
11. Complicated goals: split into small processes first; wire sequential or parallel `edges`;
    pass data via files on host `100001` (e.g. `~/gw-pipeline/...`). Do not hardcode the
    analysis universe when a prior discover step should choose candidates dynamically.
    One-process monoliths for multi-stage work are not allowed.

## API map

| Action | Call |
| --- | --- |
| Liveness | `GET /api/v1/health` (no auth) |
| Discover | `GET /api/v1/capabilities` |
| Processes | `GET\|POST /api/v1/processes` · `GET\|PUT /api/v1/processes/{id}` |
| Run process | `POST /api/v1/processes/{id}/runs` `{"hostId":"100001","clientRunKey":"..."}` |
| Workflows | `GET\|POST /api/v1/workflows` · `GET\|PUT /api/v1/workflows/{id}` |
| Run workflow | `POST /api/v1/workflows/{id}/runs` `{"mode":"one","clientRunKey":"..."}` |
| Poll / stop | `GET /api/v1/runs/{historyId}` · `POST /api/v1/runs/{historyId}/stop` |
| Process history list | `GET /api/v1/processes/{id}/runs` (best place for step stdout) |

Workflow node `id`: `{processId}-{suffix}`. Trivial one-node demos may use `"edges":"[]"`.
Multi-stage work must use real edges and file handoffs between steps.

## Your task

**[REPLACE WITH YOUR GOAL]**  
Example: Against `$GEOWEAVER_BASE_URL`, create a shell process that prints `hello-from-codex`, run it, wait until terminal, report status and log tail (redact secrets).

## Success criteria

- Used only `/api/v1` with Bearer auth; did not start/stop the JVM.
- Process run reached a terminal status (or was stopped intentionally).
- Report process/workflow id, historyId, final status, short log summary — no leaked secrets.
