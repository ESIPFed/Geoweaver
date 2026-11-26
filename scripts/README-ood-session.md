# OOD login for Cursor (one command)

The Geoweaver on the compute node must allow Agent API calls that come through the OOD proxy. Add to its `~/geoweaver/application.properties` (by default the Agent API answers only programs on that node, and these scripts get HTTP 403 `local_only`):

```properties
geoweaver.agent.allow-non-loopback=true
geoweaver.agent.acknowledge-network-risk=true
```

Every OOD user who can reach the node's port through the proxy can then try the API, so keep tokens short-lived. Without OOD, running requests over SSH to `http://127.0.0.1:<port>` needs no setting.

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
