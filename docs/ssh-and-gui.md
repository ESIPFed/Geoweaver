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
| See its progress in the browser: status, each step, and the log, refreshed every few seconds | yes |
| Press **Stop** in the browser (or `POST /api/v1/runs/{id}/stop`) for a run started from the command line | yes: the command stops the run within about a second, and steps not yet started do not run |
| Run commands while the server is down | yes |
| Start the server while a command runs, or stop it | yes |
| Use the Agent API of the server while commands run | yes |

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

## Coding agents in an SSH session

An agent working in your SSH session can use the commands above. It can also use the [Agent API](agent-api.md) of a running server with a token (`geoweaver agent-token --create --no-browser --show-token`). Either way, everything it runs shows up in your browser.

## Requirements and limits

- **Same version everywhere.** Every Geoweaver that opens the database should be the same release. A newer one may add columns that an older one does not know about.
- **The machines must reach each other.** The holder accepts the others on a TCP port picked at random. If a firewall separates, for example, an Open OnDemand compute node from the login node, a command on the login node prints which machine holds the database, and that it cannot be reached. Then use that Geoweaver's page or API, run the command on that machine, or stop that Geoweaver. To use a fixed port that you can open in the firewall, set in `~/geoweaver/application.properties`:

  ```properties
  spring.datasource.url=jdbc:h2:file:~/h2/gw;AUTO_SERVER=TRUE;AUTO_SERVER_PORT=9092;INIT=create domain if not exists jsonb as text;CACHE_SIZE=65536
  ```

- **One machine only, for example a laptop:** `export GEOWEAVER_DB_BIND_ADDRESS=127.0.0.1` keeps the database on this machine. It also keeps sharing working after the laptop joins another network. Without it, H2 announces the network address it had at start-up.
- **Private files.** Geoweaver makes `~/h2` and `~/geoweaver` readable only by you, because they hold the database and the key other processes use to connect. `GEOWEAVER_PRIVATE_FILES=false` turns this off, for example when you share a database folder on purpose.
- **Storage maintenance.** A running server compacts the database only while it holds the file itself and no other process is connected. Otherwise it tries again at its next check (see [Database](database-configuration.md)).
- **Network file systems.** H2's lock works on NFS and GPFS home folders. Keep the database on one shared file system, not on node-local disks that differ per machine.
