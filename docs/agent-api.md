# Geoweaver Agent API (`/api/v1`)

JSON HTTP API so coding agents (Cursor, Codex, Claude Code) can use Geoweaver **without the HTML UI**.

Works for:

- **Local** installs (`http://127.0.0.1:8070/Geoweaver`)
- **Deployed / public** endpoints (`https://your-host.example/Geoweaver`) as long as the Agent API is enabled and a Bearer token is configured

Base path: `{GEOWEAVER_BASE_URL}/api/v1`  
Example: `https://gw.example.org/Geoweaver/api/v1`

**Important:** `hostId=100001` means “run on the **Geoweaver server**” (that machine’s local CPU/files). It does **not** mean “run on the Codex laptop.” Remote Codex → public Geoweaver URL → jobs execute on the server.

## Remote deployment quickstart (operator → AI agent in 5 steps)

1. **Run Geoweaver on the server** (JDK 17+), either the jar or Docker:

```bash
# jar: binds all interfaces on 8070 (Agent API defaults are on; risk ack is on in the shipped properties)
java -jar geoweaver.jar resetpassword -p '<gui-password>'   # or read the random one from the first-start log
GEOWEAVER_PORT=8070 java -jar geoweaver.jar

# Docker (build from repo root after `mvn -DskipTests package`)
docker build -t geoweaver .
docker run -d -p 8070:8070 -e GEOWEAVER_PASSWORD='<gui-password>' \
  -v geoweaver-home:/home/marsvegan --name geoweaver geoweaver
```

Put HTTPS in front (reverse proxy to `http://127.0.0.1:8070/Geoweaver/`) for anything beyond a trusted network.

2. **Check reachability from the agent machine:** `curl -sS "$GEOWEAVER_BASE_URL/api/v1/health"` → `{"status":"up"}`.
3. **Mint a token once** (GUI password, never in chat or git): `curl -sS -X POST "$GEOWEAVER_BASE_URL/api/v1/tokens" -H 'Content-Type: application/json' -d '{"hostPassword":"…","ttlDays":30}'` → store `token` in the agent's secret store as `GEOWEAVER_API_TOKEN`.
4. **Point the agent at the docs:** give it `GEOWEAVER_BASE_URL` + the token, and the skill `.cursor/skills/geoweaver-api-agent/SKILL.md` (or let it read `GET /api/v1/capabilities` and the schema at `GET /v3/api-docs/agent-api`, which needs no token).
5. **The agent loop:** create processes → create a workflow with array `nodes`/`edges` → `POST …/runs` → `GET /runs/{id}?waitSeconds=60&includeStepLogs=true` → fix the failing step with `PUT` → re-run → clean up with `DELETE`.

## Security model

**Risk callout:** By default the Agent API is **enabled**. A leaked Bearer token is full Agent API access (create/edit python|shell processes and workflows; start server-local runs). Prefer HTTPS. Harden with `geoweaver.agent.api-enabled=false` and revoke tokens. On a network-reachable JVM bind you must also set `geoweaver.agent.acknowledge-network-risk=true` or Geoweaver refuses to start.

1. **Bearer token** required (except public `GET /health` and password-gated `POST /tokens`).
2. **Localhost GUI password** still applies on the server unless `geoweaver.agent.allow-localhost-runs=true` (default true).
3. Wrong password → HTTP **403**.
4. Process/workflow runs on this API are **server-local only** (`100001`). Remote SSH hosts are not exposed.
5. Public bind: either terminate TLS on a reverse proxy in front of loopback, **or** set `geoweaver.agent.allow-non-loopback=true` **and** `geoweaver.agent.acknowledge-network-risk=true` when the JVM listens beyond loopback.
6. Open GUI browsers sync process/workflow lists live (Google-Doc-style awareness) when `geoweaver.agent.workspace-events-enabled=true` (default true). Transport is **HTTP long poll** on `/api/workspace/poll/{clientToken}` plus a revision clock on `/api/workspace/revision` (WebSocket `/workspace-socket` is optional). Uses the browser client token, not the Agent Bearer token. Agent API and human GUI saves both emit events.

Never commit tokens. Prefer HTTPS on public endpoints. The Geoweaver **database** stores only a SHA-256 hash of each token (never the raw secret, never a token file). The GUI localhost password is unchanged (still the workspace password file).

## Operator: local enable

```properties
server.address=127.0.0.1
geoweaver.agent.api-enabled=true
geoweaver.agent.allow-localhost-runs=true
```

```bash
gw agent-token --create
# aliases: geoweaver agent-token --create
# or jar:  java -jar ~/geoweaver.jar agent-token --create
# Opens a local Geoweaver browser page — copy the token into a password manager / secret store,
# then click “I copied…” and close the window. Do NOT put the token in an environment variable.
export GEOWEAVER_BASE_URL='http://127.0.0.1:8070/Geoweaver'   # URL only is fine in env
```

## Operator: two machines (caller ≠ Geoweaver host)

The Agent API is ordinary HTTP. A valid Bearer token is accepted from **any reachable client**. `hostId=100001` still means “run on the Geoweaver server,” not on the caller.

**Do not** set `server.address=127.0.0.1` if other computers must open a TCP connection to the JVM with no reverse proxy — loopback is only this computer.

### A) Preferred: HTTPS reverse proxy, JVM on loopback

```properties
server.address=127.0.0.1
server.port=8070
geoweaver.agent.api-enabled=true
geoweaver.agent.allow-localhost-runs=true
geoweaver.agent.allow-non-loopback=false
```

Proxy forwards `https://gw.example.org/Geoweaver/` → `http://127.0.0.1:8070/Geoweaver/`. Clients use `GEOWEAVER_BASE_URL=https://gw.example.org/Geoweaver`.

### B) JVM listens on the network

Required when there is no proxy and the caller is another machine:

```properties
# Do not set server.address=127.0.0.1
server.address=0.0.0.0
geoweaver.agent.api-enabled=true
geoweaver.agent.allow-non-loopback=true
geoweaver.agent.acknowledge-network-risk=true
geoweaver.agent.allow-localhost-runs=true
```

Without `allow-non-loopback=true` **and** `acknowledge-network-risk=true`, Geoweaver **refuses to start** if the API is on and the bind is not loopback.

Open firewall TCP 8070 only to trusted networks. Prefer HTTPS in front of this bind.

The one-time **token copy web page** stays on `127.0.0.1` (it contains the raw secret). Remote mint uses `POST /api/v1/tokens` or `gw agent-token --create --base-url …` (prints the secret once on the caller).

### Create the Bearer token

**A) On the server (filesystem):**

```bash
gw agent-token --create
# optional lifetime (days), max 180 / six months:
gw agent-token --create --ttl-days 30
# same CLI: geoweaver agent-token --create
# or jar:   java -jar ~/geoweaver.jar agent-token --create --ttl-days 30
```

On the **server machine**, default mint does **not** print the raw token. `gw agent-token --create` (no `--base-url`) opens the copy page on the **already running** local Geoweaver (`http://127.0.0.1:8070/Geoweaver/agent-token-reveal/…`). **Start Geoweaver first**. Offline `java -jar … agent-token --create` writes into this process’s database. Copy the token into a password manager, then click “I copied the token…” and close the window. **Do not** store the token in a file or in `GEOWEAVER_API_TOKEN` / shell environment variables.

Every token **must expire**. Default lifetime is 30 days. Maximum lifetime is **180 days (six months)**. You can have several active tokens. `gw agent-token --list` shows fingerprints only. `gw agent-token --revoke <fingerprint>` or `DELETE /api/v1/tokens/{fingerprint}` disables one. `gw agent-token --rotate` issues a new token and revokes the others. Leftover CHG-0001 plaintext files are imported as a hash once, then deleted; auth never reads a token file.

**B) From any machine (password-gated HTTP — preferred for remote Codex ops):**

Requires `geoweaver.agent.api-enabled=true` on the server. Uses the **GUI localhost password** (same password as the Geoweaver web login for localhost). No existing Bearer token needed. Rate-limited per client IP (default 5/minute). Prefer HTTPS.

```bash
export GEOWEAVER_BASE_URL='https://gw.example.org/Geoweaver'
# Interactive (recommended): prompts for password without echoing
gw agent-token --create --base-url "$GEOWEAVER_BASE_URL" --ttl-days 30
# Prompts for the GUI localhost password. Stores the hash on that server (not in this CLI’s H2 file).
# Prints the secret once (copy page cannot run on a different computer).

# Or one-shot HTTP:
curl -sS -X POST "$GEOWEAVER_BASE_URL/api/v1/tokens" \
  -H "Content-Type: application/json" \
  -d '{"hostPassword":"<gui-localhost-password>","ttlDays":30}'
```

Response `201` includes `token`, `expiresAt`, and `ttlDays` once. Other tokens stay valid unless you send `"revokeOthers": true`. Store the secret in a password manager / CI secret store — never commit, never in a file, never in shell env by default. After `expiresAt` or revoke, auth returns 401 until you mint again.

`allow-localhost-runs` does **not** apply to token create: the GUI localhost password is always required for `POST /api/v1/tokens`.

On the **Codex machine** (or CI secrets): set `GEOWEAVER_BASE_URL` in env if needed; inject the Bearer token from your secret store (not from `.bashrc`).

Smoke from anywhere that can reach the URL (token from secret store):

```bash
curl -sS "$GEOWEAVER_BASE_URL/api/v1/health"
curl -sS "$GEOWEAVER_BASE_URL/api/v1/capabilities" \
  -H "Authorization: Bearer <token-from-secret-store>"
```

| Property | Default | Meaning |
| --- | --- | --- |
| `geoweaver.agent.api-enabled` | `true` | Master switch |
| `geoweaver.agent.allow-localhost-runs` | `true` | Token may skip GUI localhost password for server-local runs |
| `geoweaver.agent.allow-non-loopback` | `true` | Allow Agent API when JVM bind is not loopback (still needs risk ack) |
| `geoweaver.agent.acknowledge-network-risk` | `false` | Required with non-loopback bind or process refuses to start |
| `geoweaver.agent.workspace-events-enabled` | `true` | Fan-out create/edit events to open GUI browsers |
| `geoweaver.agent.allow-delete` | `true` | Agents may `DELETE` processes/workflows; `false` → 403 (edit/run unaffected) |
| `geoweaver.agent.api-token-file` | `~/gw-workspace/.agent_api_token` | Leftover file path only: import hash into DB then delete. Not the live store |
| `geoweaver.agent.token-ttl-days` | `30` | Default Bearer token lifetime in days |
| `geoweaver.agent.token-max-ttl-days` | `180` | Max lifetime (hard-capped at 180 / six months) |
| `geoweaver.agent.run-rate-limit-per-minute` | `30` | Run-start rate limit |
| `geoweaver.agent.token-create-rate-limit-per-minute` | `5` | Password-gated `POST /tokens` per client IP |

## Endpoints

| Method | Path | UI analogue |
| --- | --- | --- |
| GET | `/health` | Liveness (public) |
| POST | `/tokens` | Issue Bearer token with GUI localhost password (no Bearer; rate-limited). Optional `revokeOthers` |
| GET | `/tokens` | List fingerprints / expiry (Bearer; no secrets) |
| DELETE | `/tokens/{fingerprint}` | Revoke one token (Bearer) |
| GET | `/capabilities` | What agents may call |
| GET/POST | `/processes` | Process list / create (python\|shell) |
| GET/PUT/DELETE | `/processes/{id}` | Open / save / delete process (DELETE → 409 while a workflow uses it) |
| POST | `/processes/{id}/runs` | Run on server local host |
| GET | `/processes/{id}/runs` | Process history list |
| GET/POST | `/workflows` | Workflow list / create |
| GET/PUT/DELETE | `/workflows/{id}` | Open / save / delete workflow graph (processes and history kept) |
| POST | `/workflows/{id}/runs` | Run workflow on server local host |
| GET | `/workflows/{id}/runs` | Workflow history (newest first, max 50) with per-step status |
| GET | `/runs/{historyId}` | History detail + log. `waitSeconds=0..60` blocks until terminal; `includeStepLogs=true` adds step log tails |
| POST | `/runs/{historyId}/stop` | Stop |

Unknown history → **404**. Terminal statuses: Done, Failed, Stopped, Skipped.

Machine-readable schema (no token): `GET {GEOWEAVER_BASE_URL}/v3/api-docs/agent-api`.

### Errors

Every `/api/v1` error is JSON `{"error":"<code>","message":"<what to fix>"}` with a real status: 400 `bad_request` (including malformed JSON), 401 `unauthorized`, 403 `forbidden`, 404 `not_found` (also unknown paths/methods), 409 `conflict`, 415 `unsupported_media_type`, 429 `rate_limited`, 500 `internal_error` (details only in the server log), 503 `api_disabled`. The API never answers with an HTML login redirect.

### Workflow graph

`nodes` and `edges` may be JSON arrays (preferred) or JSON-encoded strings (the GUI storage form).

```json
{
  "name": "discover-then-analyze",
  "nodes": [{"id": "ab12cd-s1"}, {"id": "ef34gh-s2"}],
  "edges": [{"source": "ab12cd-s1", "target": "ef34gh-s2"}]
}
```

- Node `id` is `{processId}-{suffix}` (same as the UI); the process must exist. Suffixes make ids unique, so one process can appear twice.
- `title`, `x`, `y` are optional; Geoweaver fills them so the GUI canvas renders the graph.
- Edges accept `"source":"nodeId"` or the GUI form `"source":{"id":"nodeId",…}`; both are stored in GUI form.
- Bad JSON, unknown processes, malformed ids, duplicate ids, self-loops, and edges to missing nodes → 400 naming the entry.
- **Semantics:** a step starts once all its upstream steps finish, **even if one failed**. Geoweaver does not pipe stdout between steps; pass data through files on the server.

### Workflow run status

`GET /runs/{workflowHistoryId}` returns `workflowRun: true` and `steps`:

```json
{"status":"Failed","terminal":true,"workflowRun":true,
 "steps":[{"nodeId":"ab12cd-s1","processId":"ab12cd","historyId":"x1…","status":"Failed","terminal":true,"log":"Traceback …"},
          {"nodeId":"ef34gh-s2","processId":"ef34gh","historyId":"y2…","status":"Done","terminal":true,"log":"…"}]}
```

`log` per step is included only with `includeStepLogs=true` (last 20,000 characters). With `waitSeconds`, the call returns once the workflow and every step row are final.

## Codex / Cursor

- Skill: `.cursor/skills/geoweaver-api-agent/SKILL.md`
- Paste prompt: `.cursor/skills/geoweaver-api-agent/CODEX_PROMPT.md`

## Open OnDemand (same URL prefix as the UI)

Open OnDemand (OOD) is the campus web portal that starts Geoweaver on a compute node and reverse-proxies it. The Agent API uses the **same host and path prefix** as the UI—only the suffix changes.

| What you open in the browser | Set `GEOWEAVER_BASE_URL` to |
| --- | --- |
| `https://ondemand.example.edu/rnode/HOST/PORT/Geoweaver/web/geoweaver` | `https://ondemand.example.edu/rnode/HOST/PORT/Geoweaver` |

Then call `{GEOWEAVER_BASE_URL}/api/v1/health`, `/api/v1/processes`, and so on. Refresh this base **every new OOD job**—the `PORT` segment changes.

### Two different credentials (do not confuse them)

| Credential | Who issues it | What it is for |
| --- | --- | --- |
| Campus login session (browser cookies after GMU/Shibboleth or your site’s IdP) | Campus identity via OOD | Lets the OOD reverse proxy forward your HTTP to the compute-node Geoweaver |
| Geoweaver Agent API Bearer token | `POST /api/v1/tokens` with the **Geoweaver GUI localhost password** | Lets `/api/v1` authorize the agent **after** traffic reaches Geoweaver |

There is **no** “GMU Agent API token.” Campus login is not a substitute for the Geoweaver Bearer. A Bearer minted against laptop Geoweaver is **invalid** on the hopper database—mint against the **same** base URL you will call.

### Make Cursor → campus login → Agent API work

#### Path A — Recommended for Cursor on your laptop: SSH tunnel + Bearer

Skips the OOD/IdP HTML redirect for the API hop. Still uses the **hopper** Geoweaver (not a second local install).

1. Start Geoweaver in OOD until **Connect** works. Note compute host and the port from `connection.yml` (same as `GEOWEAVER_PORT` / OOD `$port`).
2. On your laptop, forward that port (example—adjust user, host, ports):

```bash
ssh -N -L 18070:127.0.0.1:<GEOWEAVER_PORT> <you>@<compute-host>
# or via login node jump if required by the site
```

3. Point the agent at the tunnel (this TCP target **is** the hopper JVM):

```bash
export GEOWEAVER_BASE_URL='http://127.0.0.1:18070/Geoweaver'
```

4. Mint a Bearer **against that base** (ask once for the Geoweaver GUI localhost password; do not paste it into chat or git):

```bash
curl -sS -X POST "$GEOWEAVER_BASE_URL/api/v1/tokens" \
  -H "Content-Type: application/json" \
  -d '{"hostPassword":"<gui-localhost-password>","ttlDays":30}'
# store token in a secret store → GEOWEAVER_API_TOKEN for Cursor
```

5. Smoke:

```bash
curl -sS "$GEOWEAVER_BASE_URL/api/v1/health"
# expect: {"status":"up"}
curl -sS "$GEOWEAVER_BASE_URL/api/v1/capabilities" \
  -H "Authorization: Bearer <token-from-secret-store>"
```

#### Path B — Keep the public OOD HTTPS URL (one command)

Do **not** paste cookies or tokens into chat. The Hopper Geoweaver GUI password is **not** the same as a laptop Geoweaver password — `gw-ood-login` asks for the Hopper password after cookies are saved and mints a Bearer against that instance.

```bash
./scripts/gw-ood-login 'https://ondemand.example.edu/rnode/HOST/PORT/Geoweaver/web/geoweaver'
# step 1: campus login + Enter → cookies
# step 2: Hopper GUI password → ~/.config/geoweaver-agent/api-token
source ~/.config/geoweaver-agent/ood-env.sh
curl -sS -b "$GEOWEAVER_OOD_COOKIE_JAR" "$GEOWEAVER_BASE_URL/api/v1/health"
curl -sS -b "$GEOWEAVER_OOD_COOKIE_JAR" -H "Authorization: Bearer $GEOWEAVER_API_TOKEN" \
  "$GEOWEAVER_BASE_URL/api/v1/capabilities"
```

Agent calls need **both** the cookie jar and Bearer. Re-run `gw-ood-login` when the OOD `PORT` changes. Details: `scripts/README-ood-session.md`.

If health returns **302** IdP HTML, re-run login or use Path A—do not use a laptop-only Geoweaver as a stand-in for hopper.

#### Path C — Site-approved OOD proxy policy (optional)

Campus IT may, after security review, configure OOD/Apache so `/Geoweaver/api/v1/**` can be reached in a way compatible with automation (for example forwarding when an existing valid session or site-approved header is present). This is a **site** change. Do **not** “turn off campus login for everyone” as a Geoweaver workaround.

## Rollback

`geoweaver.agent.api-enabled=false` or `DELETE /api/v1/tokens/{fingerprint}` for each live token. Already-started jobs keep running until they finish or you stop them. Raw tokens cannot be recovered from the database (hashes only).

## Database table (`agent_api_token`)

Columns: `id`, `token_hash` (SHA-256 hex, unique), `fingerprint` (12-character id, unique), `expires_at`, `created_at`, `revoked_at`, `ttl_days`. Hibernate `ddl-auto=update` creates this table. Operators who set `ddl-auto=none` must create the table themselves before enabling the Agent API.
