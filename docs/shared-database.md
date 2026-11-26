# Several Geoweaver instances on one database

A common setup on a cluster: one Geoweaver server runs on the login node (used by you, or by a coding agent through its Agent API), and another runs in an Open OnDemand session on a separate virtual machine or compute node, where you watch in the browser. Both use the same home folder, and so the same database. They can run at the same time, in any order, and each one shows what the others do within about a second.

## How it works

Geoweaver keeps everything in one H2 database file (`~/h2/gw.mv.db` by default). The first Geoweaver to open it holds the file and serves it to all the others over TCP. H2 calls this `AUTO_SERVER`, and it is on in the default settings. The holder's address is written to `~/h2/gw.lock.db`. When the holder exits or its machine goes away, another Geoweaver takes the file over within a few seconds and the rest carry on.

Each Geoweaver server keeps its open pages in step with everything else on the database. Once a second it looks for runs that started, changed status or ended, whichever Geoweaver started them, and for changes that other instances recorded in a small shared table. So in every page, on every machine:

- the green **Running now** line names each running workflow or process, and a notice appears when one starts or ends;
- a workflow open on the canvas shows each step as running, done or failed;
- the process and workflow lists, and the history tables, refresh by themselves;
- if someone else changes the workflow you have open, the canvas reloads (or asks first when you have unsaved changes);
- **Stop** works for any run, wherever it was started.

The notice says who made the change: a person, an AI agent (Agent API) or the command line. `GEOWEAVER_WORKSPACE_SYNC=false` turns this off for a server.

Do not copy the database file to run a second Geoweaver on the copy. Do not remove `AUTO_SERVER` or set `FILE_LOCK=NO`. Two processes writing the same file without H2's lock would corrupt it.

## Setting it up

1. **Same release everywhere.** Install the same Geoweaver version on the login node and in the Open OnDemand image. A newer one may add columns an older one does not know.
2. **Same home folder.** Both machines must see the same `~/h2` on a shared file system (NFS, GPFS, Lustre). H2's lock works there. Not on node-local disks.
3. **The machines must reach each other over TCP.** The holder accepts the others on a port picked at random, on its network address. Geoweaver offers the database on the address the machine's name resolves to; when that is a loopback address (many Linux VMs map their own name to `127.0.1.1`), it uses the machine's first network address instead. To choose the address yourself, set `GEOWEAVER_DB_BIND_ADDRESS` to an address of that machine the others can reach. For a fixed port that can be opened in a firewall, set in `~/geoweaver/application.properties`:

   ```properties
   spring.datasource.url=jdbc:h2:file:~/h2/gw;AUTO_SERVER=TRUE;AUTO_SERVER_PORT=9092;INIT=create domain if not exists jsonb as text;CACHE_SIZE=65536
   ```

4. Start Geoweaver on each machine as usual. No order is needed.

If a Geoweaver cannot reach the holder, it does not start and prints which machine holds the database and what to change, instead of H2's "Database may be already in use".

## Left-over Geoweaver processes and the lock

No Geoweaver blocks another one, including one that was left running or crashed:

- A server left running is simply the holder; the others work through it.
- When the holder exits or is killed (even with `kill -9`, by a login-node process limit, or when its VM is shut down), another Geoweaver takes the file over within a few seconds. A lock file left by a crashed process is taken over the same way at the next start.
- The one case that blocks others is a holder that is still running on a machine the others cannot reach (a firewall, or `GEOWEAVER_DB_BIND_ADDRESS=127.0.0.1` with Geoweaver on several machines). Stop that Geoweaver, or fix the address or port as above.

Other notes:

- **One machine only, for example a laptop:** `export GEOWEAVER_DB_BIND_ADDRESS=127.0.0.1` keeps the database on this machine and keeps working after the laptop joins another network.
- **Private files.** Geoweaver makes `~/h2` and `~/geoweaver` readable only by you, because they hold the database and the key other processes use to connect. `GEOWEAVER_PRIVATE_FILES=false` turns this off.
- **Storage maintenance.** A server compacts the database only while it holds the file itself and no other process is connected; otherwise it tries again later (see [Database](database-configuration.md)).
- `localhost` (host `100001`) means the machine where that Geoweaver runs. On most clusters, heavy work belongs on compute nodes: add them as SSH hosts, or submit jobs from inside a process (`sbatch --wait`, `srun`).

## Coding agents on the login node

An agent on your own computer reaches the login node with your SSH key (`ssh -o BatchMode=yes you@login.example.edu true` must work without a password). Whatever it changes there shows up in the Open OnDemand page within about a second.

### Through the Agent API (fastest)

Keep a Geoweaver server running on the login node and forward its port:

```bash
ssh you@login.example.edu 'GEOWEAVER_PORT=8071 SERVER_ADDRESS=127.0.0.1 nohup geoweaver > ~/geoweaver/server.log 2>&1 &'
TOKEN=$(ssh you@login.example.edu geoweaver agent-token --create --no-browser --show-token | grep -o "gwt_[A-Za-z0-9_-]*")
ssh -f -N -L 8071:127.0.0.1:8071 you@login.example.edu
curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8071/Geoweaver/api/v1/workflows
```

`SERVER_ADDRESS=127.0.0.1` keeps the page and the API on the login node itself; the database is still shared on the machine's network address, so the Open OnDemand Geoweaver joins it as described above. Pick a port no one else on the login node uses. See [Agent API](agent-api.md) for the calls. A Java server takes about 0.5 to 1 GB of memory; check your site's rules for long-running programs on login nodes.

### Through commands

Without any server on the login node, `geoweaver` commands work on the database directly and also show up in every open page. `--json` puts exactly one JSON document on standard output, in the same shapes as the Agent API; errors print `{"error": "...", "status": 404}` and exit with 1. Each command takes a few seconds to start Java, so the Agent API is better for many calls.

```bash
ssh you@login.example.edu geoweaver workflow list --json
```

| Task | Command |
| --- | --- |
| List, show | `geoweaver process list --json`, `geoweaver process show <id> --json` (same for `workflow`) |
| Create a process | `geoweaver process create --name clean_data --lang python --code-file clean.py --json` (`--code-file -` reads standard input) |
| Change it | `geoweaver process update <id> --code-file clean.py --json` |
| Create a workflow | `geoweaver workflow create --name daily --steps <p1>,<p2>,<p3> --json` (a chain), or `--file graph.json` with `nodes` and `edges` as in the Agent API |
| Start a run and return | `geoweaver run workflow <id> --detach --json` prints `{"runId": ...}`; the run continues after the command and the SSH session end. Its output is in `~/geoweaver/cli-runs/<runId>.log` |
| Wait for it | `geoweaver run status <runId> --wait 600 --json` (exit 1 when it ended Failed or Stopped) |
| Read its log | `geoweaver run log <runId>`; `-f` follows it to the end |
| Stop it | `geoweaver run stop <runId> --json`, also for runs started in a browser |
| Recent runs | `geoweaver run list --status Running --json` |
| Delete | `geoweaver process delete <id>`, `geoweaver workflow delete <id>` |

A run started without `--detach` lives in its command's process: if the SSH session drops, the run ends with it. If a command is killed outright, its run stays "Running" in the history; press **Stop** in the browser to close it.
