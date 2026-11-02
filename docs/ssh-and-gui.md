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

When the agent runs on your laptop and Geoweaver lives on the cluster, `scripts/geoweaver-ssh` runs `geoweaver` there over one shared SSH connection:

```bash
export GEOWEAVER_SSH_HOST=you@login.cluster.edu
scripts/geoweaver-ssh connect                 # once; answers password / two-factor prompts if any
scripts/geoweaver-ssh workflow list --json    # reuses the connection, no prompt
scripts/geoweaver-ssh forward 8070            # the cluster's Geoweaver page at http://localhost:8070/Geoweaver
```

- **With an SSH key the cluster accepts on its own,** `connect` is not even needed: the script opens the connection with the key, and opens it again after it drops, without asking anything.
- **When the cluster also wants a second factor,** you log in once with `connect`, and the connection stays open while it is used (and up to 12 hours idle, `GEOWEAVER_SSH_KEEP`). Keep-alive messages hold it through quiet periods.
- **When the connection is gone and the key alone cannot reopen it** (network change, laptop sleep, the cluster ended the session), the script never waits at a prompt. It prints `GEOWEAVER-SSH: NOT CONNECTED` with the command to run, and exits with 75. An agent passes this on to you; run `scripts/geoweaver-ssh connect` in your own terminal and let it continue.
- Commands start a login shell on the cluster, so `module load` and `PATH` lines in your profile apply. Set `GEOWEAVER_REMOTE_CMD` if `geoweaver` is called something else there, for example `GEOWEAVER_REMOTE_CMD="java -jar ~/geoweaver/geoweaver.jar"`.

It works with any server you can reach with `ssh`; nothing in it is specific to one cluster:

- `GEOWEAVER_SSH_HOST` can be a `Host` entry from `~/.ssh/config`, so user name, key (`IdentityFile`), port and a jump host (`ProxyJump bastion.example.edu`) live there.
- Many sites skip the second factor on their VPN or campus network and accept a key alone; there the connection reopens by itself. Off the VPN you log in once per session with `connect`.
- Login names that rotate between several login nodes are fine: all commands go through the one connection, so they reach the same node, and runs, logs and Stop work across nodes because they live in the shared home folder.
- Use the login node for starting and watching runs. Put heavy work on compute nodes from inside the process, for example with `sbatch --wait` or `srun` in a shell process.

For Claude Code, a few lines in the project's `CLAUDE.md` are enough:

```markdown
Geoweaver runs on the cluster. Use `scripts/geoweaver-ssh <geoweaver command> --json` for every
Geoweaver action (process/workflow create, update, show; run ... --detach; run status --wait; run log; run stop).
Do not start a Geoweaver server. If a command prints GEOWEAVER-SSH: NOT CONNECTED, stop and ask me
to run `scripts/geoweaver-ssh connect`.
```

## Several Geoweaver servers at once

Any number of Geoweaver servers and commands can use one database at the same time, on one machine or on several that share the home folder (for example a server on the login node and one in an Open OnDemand session). Each server shows in its pages what all the others do. Forwarding a server's page to your laptop with SSH (`-L 8070:localhost:8070`, or `geoweaver-ssh forward`) does not change this; the page talks to its server, and the servers share the database directly.

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
