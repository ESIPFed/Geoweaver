# Using Geoweaver over SSH and in the browser at the same time

A common setup on a cluster: you log in with SSH (yourself, or a coding agent such as Claude Code working in that SSH session) and run workflows with the `geoweaver` command, and you keep the Geoweaver page open in a browser, for example in an Open OnDemand desktop or a port-forwarded session, to watch progress. All of these can use the same database at the same time.

## How it works

Geoweaver keeps everything in one H2 database file (`~/h2/gw.mv.db` by default). Several Geoweaver processes may use it at once: the server that serves the browser page and the Agent API, every `geoweaver ...` command you run, and a second server on another machine that shares your home folder.

The first process to open the file holds it and serves it to the others over TCP. H2 calls this `AUTO_SERVER`, and it is on in the default settings. The holder's address is written to `~/h2/gw.lock.db`. When the holder exits, another process takes over within a few seconds. Nothing has to be started in a particular order, and the Geoweaver server does not need to be running for commands to work.

Do not copy the database file to run a second Geoweaver on the copy. Do not remove `AUTO_SERVER` or set `FILE_LOCK=NO`. Two processes writing the same file without H2's lock would corrupt it.

## What you can do

| | Works |
| --- | --- |
| Run `geoweaver run workflow <id>` or `geoweaver run process <id>` in SSH while the browser page is open | yes |
| See it in the browser as it happens: a "Running now" line, a notice when it starts and ends, each step's status on the open workflow, and the log | yes, within about a second |
| See processes and workflows that an agent creates or edits through another Geoweaver server | yes: the lists update, and an open workflow reloads |
| Press **Stop** in the browser (or `POST /api/v1/runs/{id}/stop`) for a run started from the command line | yes: the command stops the run within about a second, and steps not yet started do not run |
| Run commands while the server is down | yes |
| Start the server while a command runs, or stop it | yes |
| Use the Agent API of the server while commands run | yes |
| Create and change processes and workflows from the command line while a page is open | yes: the page updates within about a second |

## Watching an agent from another Geoweaver

Each Geoweaver server keeps its open pages in step with everything else on the database. Once a second it looks for runs that started, changed status or ended, whichever process started them, and for changes that other servers recorded in a small shared table. So an agent can work through a Geoweaver on the login node (its Agent API, or `geoweaver` commands) while you watch from a second Geoweaver in Open OnDemand:

- the green **Running now** line names each running workflow or process, and a notice appears when one starts or ends;
- a workflow open on the canvas shows each step as running, done or failed;
- the process and workflow lists, and the history tables, refresh by themselves;
- if the agent changes the workflow you have open, the canvas reloads (or asks first when you have unsaved changes).

`GEOWEAVER_WORKSPACE_SYNC=false` turns this off for a server.

## Running workflows from SSH

```bash
geoweaver list --workflow
geoweaver run workflow <workflow-id> -h 100001     # 100001 is "localhost"
geoweaver run process <process-id> -h <host-id> -p '<ssh password>'
```

`localhost` (host `100001`) means the machine where that Geoweaver process runs. A command typed on a login node runs localhost steps on the login node. On most clusters, heavy work belongs on compute nodes: add the compute host as an SSH host, or run the command inside a batch or interactive job.

A command runs its workflow inside its own process. If the SSH connection drops, the run ends with it. For long runs, use `tmux`, `screen` or `nohup`:

```bash
nohup geoweaver run workflow <workflow-id> -h 100001 > run.log 2>&1 &
```

If the command is killed outright (`kill -9`, node reboot), its run stays "Running" in the history. Press **Stop** in the browser to close it.

## Opening the browser page

- **Open OnDemand:** start Geoweaver in your session (desktop app or `java -jar geoweaver.jar`) and open `http://localhost:8070/Geoweaver` there. It uses the same home folder, and so the same database.
- **SSH port forwarding** to a Geoweaver server running on the login node:

  ```bash
  ssh -L 8070:localhost:8070 you@login.cluster.edu
  # then open http://localhost:8070/Geoweaver on your laptop
  ```

## Coding agents: Geoweaver through commands only

An agent (for example Claude Code) can do everything through `geoweaver` commands, with no Geoweaver server on its side and no token. Add `--json` and standard output carries exactly one JSON document, in the same shapes as the [Agent API](agent-api.md); messages go to standard error. Errors print `{"error": "...", "status": 404}` and exit with 1.

| Task | Command |
| --- | --- |
| List, show | `geoweaver process list --json`, `geoweaver process show <id> --json` (same for `workflow`) |
| Create a process | `geoweaver process create --name clean_data --lang python --code-file clean.py --json` (`--code-file -` reads standard input) |
| Change it | `geoweaver process update <id> --code-file clean.py --json` |
| Create a workflow | `geoweaver workflow create --name daily --steps <p1>,<p2>,<p3> --json` (a chain), or `--file graph.json` with `nodes` and `edges` as in the Agent API |
| Change a workflow | `geoweaver workflow update <id> --steps ... --json` |
| Start a run and return | `geoweaver run workflow <id> --detach --json` prints `{"runId": ...}`; the run continues in its own process after the command returns, the SSH session closes or the agent's shell is stopped. Its output is in `~/geoweaver/cli-runs/<runId>.log` |
| Wait for it | `geoweaver run status <runId> --wait 600 --json` (exit 1 when it ended Failed or Stopped) |
| Read its log | `geoweaver run log <runId>`; `-f` follows it to the end, step by step for a workflow |
| Stop it | `geoweaver run stop <runId> --json`, also for runs started in a browser |
| Recent runs | `geoweaver run list --status Running --json` |
| Delete | `geoweaver process delete <id>`, `geoweaver workflow delete <id>` |

Everything these commands do shows up in every Geoweaver page open on the same database within about a second, on any machine: new and changed processes and workflows, a "Running now" line, a notice when runs start and end, and each step's status on the open workflow. The page says "The command line" changed it.

Runs on `localhost` (the default) need no password: they run on the machine where the command runs, as you. Each command takes about 3 seconds to start Java.

## Calling the cluster from your own computer

Use the bridge (the default): your Geoweaver server stays running on the cluster and the agent talks to its [Agent API](agent-api.md) through one SSH connection, which takes a fraction of a second per call. `geoweaver-ssh` is the alternative when nothing may be left running on the cluster; it starts a new `geoweaver` command for every call, which takes several seconds each. Both can be used at the same time as any other Geoweaver on the same database.

| | `scripts/geoweaver-bridge` (default) | `scripts/geoweaver-ssh` |
| --- | --- | --- |
| What runs on the cluster | your Geoweaver server, kept running, reachable only through the bridge | only `geoweaver` commands, no server |
| How the agent calls it | `geoweaver-bridge api GET /api/v1/...` | `geoweaver-ssh <command> --json` |
| Time per call | well under a second | several seconds (Java starts each time) |
| Logins | once per session (none with a key alone) | once per session (none with a key alone) |
| The Geoweaver page on your computer | always, at `http://localhost:8070/Geoweaver` | `geoweaver-ssh forward` to a server that is already running |

### The bridge

```bash
export GEOWEAVER_SSH_HOST=you@login.cluster.edu
scripts/geoweaver-bridge start       # one login; prints the page address
scripts/geoweaver-bridge api GET /api/v1/workflows
scripts/geoweaver-bridge stop        # when you are done
```

`start` logs in once, starts a Geoweaver server on the cluster (or reuses the one already running on that login node), forwards it to `http://localhost:8070/Geoweaver` (the next free port if 8070 is taken), and creates an Agent API token for the `api` command. Run `scripts/geoweaver-bridge password` once to set the password for the page.

Until you run `stop`, a health check every 30 seconds keeps the connection in use, puts the port forward back when it disappears, restarts the server if it stopped, and reconnects with your key when the cluster allows that. When logging in again needs you, it shows a desktop notification, `status` says so, and `api` prints `GEOWEAVER-BRIDGE: NOT CONNECTED` with the command to run (exit 75) instead of waiting.

The server is live: `stop` closes only the bridge, and the server keeps running, together with its runs, so the next `start` reconnects within a second or two (and `start` on a bridge that is already up returns at once). `stop` leaves the SSH connection open for `ssh`, `scp` and `geoweaver-ssh` that may share it (it closes by itself after 12 hours without use); `stop --disconnect` closes it. `stop --server` also stops your servers, on every login node it can reach; it refuses while runs are going on unless you add `--force`. `status` lists your servers.

The server listens only on the cluster machine itself (`127.0.0.1`), on a port picked per user, so it never takes a port another user's Geoweaver uses. If a later login lands on another login node, a second server starts there; both share the database with each other and with any Geoweaver in Open OnDemand, and none locks the others out. Each server is one Java process (about 0.5 to 1 GB of memory); check your site's rules for long-running programs on login nodes.

Any HTTP client can use the bridge. `geoweaver-bridge env` prints the address and the token:

```bash
eval "$(scripts/geoweaver-bridge env)"
curl -s -H "Authorization: Bearer $TOKEN" "$URL/api/v1/workflows"
curl -s -X POST -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  "$URL/api/v1/workflows/<id>/runs" -d '{"hostId":"100001"}'
curl -s -H "Authorization: Bearer $TOKEN" "$URL/api/v1/runs/<runId>?waitSeconds=60&includeStepLogs=true"
```

The server started by the bridge always has the Agent API on, even if your settings turn it off for other Geoweaver servers. The token is kept in `~/.geoweaver-bridge/` (readable only by you) and replaced by itself when it expires.

For the agent, waiting is cheapest on the server side: `geoweaver-bridge api GET '/api/v1/runs/<id>?waitSeconds=60'` returns as soon as the run ends.

### Commands over SSH

`scripts/geoweaver-ssh` runs `geoweaver` on the cluster over one shared SSH connection:

```bash
export GEOWEAVER_SSH_HOST=you@login.cluster.edu
scripts/geoweaver-ssh connect                 # once; answers password / two-factor prompts if any
scripts/geoweaver-ssh workflow list --json    # reuses the connection, no prompt
scripts/geoweaver-ssh forward 8070            # the cluster's Geoweaver page at http://localhost:8070/Geoweaver
```

- **With an SSH key the cluster accepts on its own,** `connect` is not even needed: the script opens the connection with the key, and opens it again after it drops, without asking anything.
- **When the cluster also wants a second factor,** you log in once with `connect`, and the connection stays open while it is used (and up to 12 hours idle, `GEOWEAVER_SSH_KEEP`). Keep-alive messages hold it through quiet periods.
- **When the connection is gone and the key alone cannot reopen it** (network change, laptop sleep, the cluster ended the session), the script never waits at a prompt. It prints `GEOWEAVER-SSH: NOT CONNECTED` with the command to run, and exits with 75. An agent passes this on to you; run `scripts/geoweaver-ssh connect` in your own terminal and let it continue.
- **Copying files:** `scripts/geoweaver-ssh put LOCAL REMOTE` and `get REMOTE LOCAL` go through the same connection. A plain `scp` or `rsync` opens a new connection and asks for the second factor again, unless `~/.ssh/config` tells it to share the same one:

  ```
  Host mycluster
    HostName login.cluster.edu
    User you
    IdentityFile ~/.ssh/id_ed25519
    ControlMaster auto
    ControlPath ~/.ssh/geoweaver-sockets/%r@%h:%p
    ControlPersist 12h
    ServerAliveInterval 30
    ServerAliveCountMax 6
  ```

  Then `mkdir -p ~/.ssh/geoweaver-sockets && chmod 700 ~/.ssh/geoweaver-sockets`, and set `GEOWEAVER_SSH_HOST=mycluster`. The first login, by `ssh mycluster` or `scripts/geoweaver-ssh connect`, asks for the second factor; `ssh`, `scp`, `rsync` and the script all reuse it afterwards. Code does not need to be copied at all for `--code-file -`: `scripts/geoweaver-ssh process update <id> --code-file - --json < clean.py`.
- Commands start a login shell on the cluster, so `module load` and `PATH` lines in your profile apply. Set `GEOWEAVER_REMOTE_CMD` if `geoweaver` is called something else there, for example `GEOWEAVER_REMOTE_CMD="java -jar ~/geoweaver/geoweaver.jar"`.

It works with any server you can reach with `ssh`; nothing in it is specific to one cluster:

- `GEOWEAVER_SSH_HOST` can be a `Host` entry from `~/.ssh/config`, so user name, key (`IdentityFile`), port and a jump host (`ProxyJump bastion.example.edu`) live there.
- Many sites skip the second factor on their VPN or campus network and accept a key alone; there the connection reopens by itself. Off the VPN you log in once per session with `connect`.
- Login names that rotate between several login nodes are fine: all commands go through the one connection, so they reach the same node, and runs, logs and Stop work across nodes because they live in the shared home folder.
- Use the login node for starting and watching runs. Put heavy work on compute nodes from inside the process, for example with `sbatch --wait` or `srun` in a shell process.

For Claude Code, a few lines in the project's `CLAUDE.md` are enough:

```markdown
Geoweaver runs on the cluster behind `scripts/geoweaver-bridge`. Call its Agent API with
`scripts/geoweaver-bridge api METHOD /api/v1/... [JSON]` (see docs/agent-api.md); wait for runs with
`GET /api/v1/runs/<id>?waitSeconds=60`. If it prints GEOWEAVER-BRIDGE: NOT CONNECTED, stop and ask
me to run `scripts/geoweaver-bridge start`.
```

Without a server, through commands:

```markdown
Use `scripts/geoweaver-ssh <geoweaver command> --json` for every Geoweaver action. If a command
prints GEOWEAVER-SSH: NOT CONNECTED, stop and ask me to run `scripts/geoweaver-ssh connect`.
```

## Several Geoweaver servers at once

Any number of Geoweaver servers and commands can use one database at the same time, on one machine or on several that share the home folder (for example a server on the login node and one in an Open OnDemand session). Each server shows in its pages what all the others do. Forwarding a server's page to your laptop with SSH (`-L 8070:localhost:8070`, or `geoweaver-ssh forward`) does not change this; the page talks to its server, and the servers share the database directly.

## Left-over Geoweaver processes and the database lock

No Geoweaver blocks another one, including one that was left running or crashed:

- The first Geoweaver to open the database holds it and serves it to all others over TCP; they all work at the same time. A server left running is simply that holder.
- When the holder exits or is killed (even with `kill -9`, or when its machine goes away), another Geoweaver takes the file over within a few seconds and the rest carry on. A lock file left by a crashed process is taken over the same way at the next start.
- Servers started by `geoweaver-bridge` accept browser and API connections only on their own machine, but share the database on the network address, so a Geoweaver in Open OnDemand can always reach them.
- The one case that blocks others is a holder that is still running on a machine the others cannot reach (a firewall, or `GEOWEAVER_DB_BIND_ADDRESS=127.0.0.1` with Geoweaver on several machines). Then the others print which machine holds the database; stop that Geoweaver, or open the port (see below).

## Requirements and limits

- **Same version everywhere.** Every Geoweaver that opens the database should be the same release. A newer one may add columns that an older one does not know about.
- **The machines must reach each other.** The holder accepts the others on a TCP port picked at random. If a firewall separates, for example, an Open OnDemand compute node from the login node, a command on the login node prints which machine holds the database, and that it cannot be reached. Then use that Geoweaver's page or API, run the command on that machine, or stop that Geoweaver. To use a fixed port that you can open in the firewall, set in `~/geoweaver/application.properties`:

  ```properties
  spring.datasource.url=jdbc:h2:file:~/h2/gw;AUTO_SERVER=TRUE;AUTO_SERVER_PORT=9092;INIT=create domain if not exists jsonb as text;CACHE_SIZE=65536
  ```

- **Servers on several machines:** leave `GEOWEAVER_DB_BIND_ADDRESS` unset (or set it to an address the other machines can reach); `127.0.0.1` would shut them out.
- **One machine only, for example a laptop:** `export GEOWEAVER_DB_BIND_ADDRESS=127.0.0.1` keeps the database on this machine. It also keeps sharing working after the laptop joins another network. Without it, H2 announces the network address it had at start-up.
- **Private files.** Geoweaver makes `~/h2` and `~/geoweaver` readable only by you, because they hold the database and the key other processes use to connect. `GEOWEAVER_PRIVATE_FILES=false` turns this off, for example when you share a database folder on purpose.
- **Storage maintenance.** A running server compacts the database only while it holds the file itself and no other process is connected. Otherwise it tries again at its next check (see [Database](database-configuration.md)).
- **Network file systems.** H2's lock works on NFS and GPFS home folders. Keep the database on one shared file system, not on node-local disks that differ per machine.
