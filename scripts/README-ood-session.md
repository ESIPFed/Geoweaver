# OOD login for Cursor (one command)

```bash
./scripts/gw-ood-login 'https://ondemand…/rnode/HOST/PORT/Geoweaver/web/geoweaver'
```

First run creates `~/.config/geoweaver-agent/venv` and installs Playwright there (does not touch Homebrew/system Python). Log in when the browser opens. Cookies go under `~/.config/geoweaver-agent/` (not into chat).

```bash
source ~/.config/geoweaver-agent/ood-env.sh
```

Tell Cursor the env file is ready. Still need a Geoweaver Bearer on the same `GEOWEAVER_BASE_URL` (GUI password → `POST /api/v1/tokens`) in your secret store.

Prefer no cookies? Use an SSH tunnel instead — see [Agent API docs](../docs/agent-api.md#open-ondemand-same-url-prefix-as-the-ui).
