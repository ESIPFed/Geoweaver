# Geoweaver Agent API (`/api/v1`)

JSON HTTP API so coding agents (Cursor, Codex, Claude Code) can use Geoweaver **without the HTML UI**.

Works for:

- **Local** installs (`http://127.0.0.1:8070/Geoweaver`)
- **Deployed / public** endpoints (`https://your-host.example/Geoweaver`) as long as the Agent API is enabled and a Bearer token is configured

Base path: `{GEOWEAVER_BASE_URL}/api/v1`  
Example: `https://gw.example.org/Geoweaver/api/v1`

**Important:** `hostId=100001` means “run on the **Geoweaver server**” (that machine’s local CPU/files). It does **not** mean “run on the Codex laptop.” Remote Codex → public Geoweaver URL → jobs execute on the server.

## Security model

1. **Bearer token** required (except public `GET /health`).
2. **Localhost GUI password** still applies on the server unless `geoweaver.agent.allow-localhost-runs=true`.
3. Wrong password → HTTP **403**.
4. Process/workflow runs on this API are **server-local only** (`100001`). Remote SSH hosts are not exposed.
5. Public bind: either terminate TLS on a reverse proxy in front of loopback, **or** set `geoweaver.agent.allow-non-loopback=true` when the JVM listens beyond loopback.

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

## Operator: public / deployed endpoint (Codex-ready)

Recommended pattern (reverse proxy + loopback JVM):

```properties
# JVM stays on loopback; nginx/Caddy exposes HTTPS publicly
server.address=127.0.0.1
server.port=8070
geoweaver.agent.api-enabled=true
geoweaver.agent.allow-localhost-runs=true
geoweaver.agent.allow-non-loopback=false
```

Proxy forwards `https://gw.example.org/Geoweaver/` → `http://127.0.0.1:8070/Geoweaver/`.

If the JVM itself binds all interfaces:

```properties
# Only with network controls + HTTPS terminator + strong token hygiene
server.address=0.0.0.0
geoweaver.agent.api-enabled=true
geoweaver.agent.allow-non-loopback=true
geoweaver.agent.allow-localhost-runs=true
```

### Create the Bearer token

**A) On the server (filesystem):**

```bash
gw agent-token --create
# optional lifetime (days), max 180 / six months:
gw agent-token --create --ttl-days 30
# same CLI: geoweaver agent-token --create
# or jar:   java -jar ~/geoweaver.jar agent-token --create --ttl-days 30
```

By default the **raw token is not printed** to the terminal. Instead, `gw agent-token --create` opens the copy page on the **already running** Geoweaver (`http://127.0.0.1:8070/Geoweaver/agent-token-reveal/…`). **Start Geoweaver first** when you want the HTTP mint (`--base-url`) so the token is stored in **that server’s database**. Offline `java -jar … agent-token --create` writes into this process’s database (same working directory / H2 file). Copy the token into a password manager, then click “I copied the token…” and close the window. **Do not** store the token in a file or in `GEOWEAVER_API_TOKEN` / shell environment variables.

Every token **must expire**. Default lifetime is 30 days. Maximum lifetime is **180 days (six months)**. You can have several active tokens. `gw agent-token --list` shows fingerprints only. `gw agent-token --revoke <fingerprint>` or `DELETE /api/v1/tokens/{fingerprint}` disables one. `gw agent-token --rotate` issues a new token and revokes the others. Leftover CHG-0001 plaintext files are imported as a hash once, then deleted; auth never reads a token file.

**B) From any machine (password-gated HTTP — preferred for remote Codex ops):**

Requires `geoweaver.agent.api-enabled=true` on the server. Uses the **GUI localhost password** (same password as the Geoweaver web login for localhost). No existing Bearer token needed. Rate-limited per client IP (default 5/minute). Prefer HTTPS.

```bash
export GEOWEAVER_BASE_URL='https://gw.example.org/Geoweaver'
# Interactive (recommended): prompts for password without echoing
gw agent-token --create --base-url "$GEOWEAVER_BASE_URL" --ttl-days 30

# Or one-shot HTTP:
curl -sS -X POST "$GEOWEAVER_BASE_URL/api/v1/tokens" \
  -H "Content-Type: application/json" \
  -d '{"hostPassword":"<gui-localhost-password>","ttlDays":30}'
```

Response `201` includes `token`, `expiresAt`, and `ttlDays` once. Other tokens stay valid unless you send `"revokeOthers": true`. Prefer `gw agent-token --create --base-url …` so the browser copy page opens. Store the secret in a password manager / CI secret store — never commit, never in a file, never in shell env by default. After `expiresAt` or revoke, auth returns 401 until you mint again.

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
| `geoweaver.agent.api-enabled` | `false` | Master switch |
| `geoweaver.agent.allow-localhost-runs` | `false` | Token may skip GUI localhost password for server-local runs |
| `geoweaver.agent.allow-non-loopback` | `false` | Allow Agent API when JVM bind is not loopback |
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
| GET/PUT | `/processes/{id}` | Open / save process |
| POST | `/processes/{id}/runs` | Run on server local host |
| GET | `/processes/{id}/runs` | Process history list |
| GET/POST | `/workflows` | Workflow list / create |
| GET/PUT | `/workflows/{id}` | Open / save workflow graph |
| POST | `/workflows/{id}/runs` | Run workflow on server local host |
| GET | `/runs/{historyId}` | History detail + log |
| POST | `/runs/{historyId}/stop` | Stop |

Unknown history → **404**. Terminal statuses: Done, Failed, Stopped, Skipped.

Workflow node ids: `{processId}-{suffix}` (same as the UI).

## Codex / Cursor

- Skill: `.cursor/skills/geoweaver-api-agent/SKILL.md`
- Paste prompt: `.cursor/skills/geoweaver-api-agent/CODEX_PROMPT.md`

## Rollback

`geoweaver.agent.api-enabled=false` or `DELETE /api/v1/tokens/{fingerprint}` for each live token. Already-started jobs keep running until they finish or you stop them. Raw tokens cannot be recovered from the database (hashes only).

## Database table (`agent_api_token`)

Columns: `id`, `token_hash` (SHA-256 hex, unique), `fingerprint` (12-character id, unique), `expires_at`, `created_at`, `revoked_at`, `ttl_days`. Hibernate `ddl-auto=update` creates this table. Operators who set `ddl-auto=none` must create the table themselves before enabling the Agent API.
