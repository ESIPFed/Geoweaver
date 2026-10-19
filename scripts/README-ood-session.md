# Capture Open OnDemand session for Cursor (Agent API Path B)

Campus Open OnDemand (OOD) sits in front of Geoweaver with a login page. Coding agents need the **browser session cookies** on disk (not pasted into chat), plus a separate **Geoweaver Bearer** token.

## One-time setup

```bash
pip install playwright
playwright install chromium
# optional fallback:
# pip install browser-cookie3
```

## Capture (each new OOD job — the PORT in the URL changes)

```bash
cd /path/to/Geoweaver
python3 scripts/capture-ood-session.py \
  'https://ondemand.orc.gmu.edu/rnode/HOST/PORT/Geoweaver/web/geoweaver'
```

Complete GMU/campus login in the window. The script writes (mode `0600`):

| File | Purpose |
| --- | --- |
| `~/.config/geoweaver-agent/ood-cookies.txt` | Netscape cookie jar for `curl -b` |
| `~/.config/geoweaver-agent/ood-env.sh` | `GEOWEAVER_BASE_URL` + `GEOWEAVER_OOD_COOKIE_JAR` |

```bash
source ~/.config/geoweaver-agent/ood-env.sh
curl -sS -b "$GEOWEAVER_OOD_COOKIE_JAR" "$GEOWEAVER_BASE_URL/api/v1/health"
# expect: {"status":"up"}
```

Then mint a Bearer against **the same** `GEOWEAVER_BASE_URL` (Geoweaver GUI localhost password) and store it in your secret store as `GEOWEAVER_API_TOKEN`. Tell Cursor the **paths** / that you sourced `ood-env.sh` — **never paste cookie or token values into chat**.

## Preferred alternative (no cookies): SSH tunnel

See [Agent API — Open OnDemand](../docs/agent-api.md#open-ondemand-same-url-prefix-as-the-ui) Path A.
