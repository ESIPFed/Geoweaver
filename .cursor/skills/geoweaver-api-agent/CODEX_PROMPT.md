# Codex prompt — Geoweaver Agent API (local or public)

Copy everything below the line into Codex.  
Prerequisites: Geoweaver is deployed and reachable; env vars set (do **not** paste secrets into this prompt).

---

You are controlling **classic Geoweaver** over HTTP like a human using the UI, without the GUI and without MCP.

## Environment (already configured)

- `GEOWEAVER_BASE_URL` — full base URL **including** `/Geoweaver`, e.g.
  - public: `https://gw.example.org/Geoweaver`
  - local: `http://127.0.0.1:8070/Geoweaver`
- Bearer token for `/api/v1` — inject from a secret store (prefer not to keep it in shell env).
- If the token is missing or expired (401): mint with
  `gw agent-token --create --base-url "$GEOWEAVER_BASE_URL" --ttl-days 30` (opens a local
  Geoweaver browser page to copy the token into a password manager, then close the window).
  Or `POST $GEOWEAVER_BASE_URL/api/v1/tokens` JSON `{"hostPassword":"...","ttlDays":30}` (no Bearer;
  `ttlDays` max 180 / six months). Ask the user for the password once; never echo it back.
  Do not recommend saving the token in environment variables or a file. Note `expiresAt` and remint before then.
  Tokens are stored in the Geoweaver database as hashes only.

Docs: `docs/agent-api.md`  
Playbook: `.cursor/skills/geoweaver-api-agent/SKILL.md`

## Hard rules

1. Use `Authorization: Bearer $GEOWEAVER_API_TOKEN` for all routes except `GET /health` and `POST /tokens`.
2. Call `$GEOWEAVER_BASE_URL/api/v1/...` with curl/httpx/Shell. Do not use HTML `/web/*` login.
3. Process languages: **python** or **shell** only.
4. Runs use `hostId=100001` = **the Geoweaver server host** (not your laptop), even when `GEOWEAVER_BASE_URL` is a public HTTPS URL.
5. Never print the full token or host password.
6. After starting a run, poll `GET .../runs/{historyId}?logLimit=200000` with backoff 1s→2s→5s until `terminal` is true. On HTTP 404, stop.
7. Unique `clientRunKey` per logical job; new key after code edits.
8. First call `GET .../health` then `GET .../capabilities` to confirm the public/local endpoint and token work.

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

Workflow node `id`: `{processId}-{suffix}`.

## Your task

**[REPLACE WITH YOUR GOAL]**  
Example: Against `$GEOWEAVER_BASE_URL`, create a shell process that prints `hello-from-codex`, run it, wait until terminal, report status and log tail (redact secrets).

## Success criteria

- Used only `/api/v1` with Bearer auth against the configured base URL.
- Run reached a terminal status (or was stopped intentionally).
- Report process/workflow id, historyId, final status, short log summary — no leaked secrets.
