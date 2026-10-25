# OOD login for Cursor (one command)

```bash
./scripts/gw-ood-login 'https://ondemand…/rnode/HOST/PORT/Geoweaver/web/geoweaver'
```

1. Log in in the Chromium window → **press Enter** in the terminal (saves cookies).
2. When prompted, enter the **Hopper Geoweaver GUI password** (not your laptop Geoweaver password) → saves Bearer under `~/.config/geoweaver-agent/api-token`.

```bash
source ~/.config/geoweaver-agent/ood-env.sh
```

Tell Cursor that file is sourced — **do not paste cookies or token into chat**.

Re-mint only: `./scripts/gw-ood-mint-token`
