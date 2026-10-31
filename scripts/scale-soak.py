#!/usr/bin/env python3
"""Long-running load test for a Geoweaver server: does it stay healthy as runs pile up?

Runs many workflow runs through the Agent API and records, at regular checkpoints, the size of
the H2 database file and of the run-log folder, the server's memory, and how long common pages
take. Writes a CSV and a JSON summary with pass/fail checks.

Use only against a throwaway server with its own home folder, never a real installation:

    GEOWEAVER_BASE_URL=http://127.0.0.1:18071/Geoweaver \\
    GEOWEAVER_API_TOKEN=... \\
    python3 scripts/scale-soak.py --home /tmp/gw-soak-home --pid <server pid> \\
        --workflow-runs 200 --steps 10 --out /tmp/soak

The server must run with -Dgeoweaver.login_required=false so the dashboard can be timed.

Log sizes follow a log-normal distribution (median about 30 lines, long tail) similar to real
Geoweaver histories; --max-lines caps the longest log and --heavy-share adds occasional very long
logs.
"""

import argparse
import csv
import json
import os
import statistics
import subprocess
import sys
import time
import urllib.error
import urllib.request
import uuid

BASE = os.environ.get("GEOWEAVER_BASE_URL", "http://127.0.0.1:18071/Geoweaver").rstrip("/")
API = BASE + "/api/v1"
TOKEN = os.environ.get("GEOWEAVER_API_TOKEN", "")


def call(method, path, body=None, base=API, timeout=600):
    data = json.dumps(body).encode() if body is not None else None
    req = urllib.request.Request(base + path, data=data, method=method)
    if base == API:
        req.add_header("Authorization", "Bearer " + TOKEN)
    if data is not None:
        req.add_header("Content-Type", "application/json")
    t0 = time.perf_counter()
    try:
        with urllib.request.urlopen(req, timeout=timeout) as r:
            raw = r.read()
            status = r.status
    except urllib.error.HTTPError as e:
        raw = e.read()
        status = e.code
    ms = (time.perf_counter() - t0) * 1000
    try:
        parsed = json.loads(raw) if raw else None
    except ValueError:
        parsed = raw.decode(errors="replace")
    return status, parsed, ms


def step_code(sigma, max_lines, heavy_share, heavy_lines):
    # Log-normal number of lines; each line about 50 characters.
    return (
        "awk -v cap=%d -v hs=%f -v hl=%d 'BEGIN{srand(); "
        "u1=rand(); if(u1<1e-9)u1=1e-9; u2=rand(); z=sqrt(-2*log(u1))*cos(6.283185307*u2); "
        "n=int(exp(3.4+%f*z)); if(n>cap)n=cap; if(rand()<hs)n=hl; "
        "for(i=0;i<n;i++) printf \"epoch %%d loss=%%.6f acc=%%.4f step output\\n\", i, rand(), rand()}'\n"
        % (max_lines, heavy_share, heavy_lines, sigma)
    )


def dir_size(path):
    total = 0
    for root, _, files in os.walk(path):
        for f in files:
            try:
                total += os.path.getsize(os.path.join(root, f))
            except OSError:
                pass
    return total


def rss_mb(pid):
    if not pid:
        return None
    try:
        out = subprocess.run(["ps", "-o", "rss=", "-p", str(pid)], capture_output=True, text=True).stdout.strip()
        return round(int(out) / 1024) if out else None
    except (ValueError, OSError):
        return None


def p95(values):
    if not values:
        return None
    s = sorted(values)
    return round(s[min(len(s) - 1, int(0.95 * len(s)))], 1)


def time_pages(wf_id, proc_id, big_run_id, repeats=5):
    pages = {
        "dashboard": lambda: call("POST", "/web/dashboard", base=BASE),
        "workflow_runs": lambda: call("GET", "/workflows/%s/runs" % wf_id),
        "process_runs": lambda: call("GET", "/processes/%s/runs" % proc_id),
    }
    if big_run_id:
        pages["run_detail"] = lambda: call("GET", "/runs/%s?logLimit=100000" % big_run_id)
    result = {}
    for name, fn in pages.items():
        times = []
        for _ in range(repeats):
            status, _, ms = fn()
            if status == 200:
                times.append(ms)
        result[name] = round(statistics.median(times), 1) if times else None
    return result


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--home", required=True, help="the throwaway server's home folder (contains h2/ and geoweaver/)")
    ap.add_argument("--pid", type=int, help="server process id, to record memory")
    ap.add_argument("--workflow-runs", type=int, default=200)
    ap.add_argument("--steps", type=int, default=10, help="steps per workflow (independent, run in parallel)")
    ap.add_argument("--concurrency", type=int, default=4, help="workflow runs in flight at once")
    ap.add_argument("--checkpoint", type=int, default=20, help="record metrics every N workflow runs")
    ap.add_argument("--sigma", type=float, default=2.0, help="spread of log sizes (log-normal sigma)")
    ap.add_argument("--max-lines", type=int, default=20000)
    ap.add_argument("--heavy-share", type=float, default=0.0, help="share of steps printing --heavy-lines lines")
    ap.add_argument("--heavy-lines", type=int, default=200000)
    ap.add_argument("--out", required=True, help="output folder for metrics.csv and summary.json")
    args = ap.parse_args()

    if not TOKEN:
        sys.exit("set GEOWEAVER_API_TOKEN")
    os.makedirs(args.out, exist_ok=True)
    db_file = os.path.join(args.home, "h2", "gw.mv.db")
    log_dir = os.path.join(args.home, "geoweaver", "run-logs")
    tag = "soak_" + uuid.uuid4().hex[:6]

    status, proc, _ = call("POST", "/processes", {
        "name": tag, "lang": "shell",
        "code": step_code(args.sigma, args.max_lines, args.heavy_share, args.heavy_lines)})
    if status != 200:
        sys.exit("could not create process: %s %s" % (status, proc))
    pid = proc["id"]
    nodes = [{"id": "%s-s%d" % (pid, i)} for i in range(args.steps)]
    status, wf, _ = call("POST", "/workflows", {"name": tag + "-wf", "nodes": nodes, "edges": []})
    if status != 200:
        sys.exit("could not create workflow: %s %s" % (status, wf))
    wf_id = wf["id"]

    rows, start_db, start_logs = [], (os.path.getsize(db_file) if os.path.exists(db_file) else 0), dir_size(log_dir)
    done = failed = 0
    run_times = []
    big_run = None
    t_start = time.time()
    pending = []
    submitted = 0

    def record():
        pages = time_pages(wf_id, pid, big_run)
        row = {
            "workflow_runs": done + failed,
            "step_runs": (done + failed) * args.steps,
            "elapsed_s": round(time.time() - t_start),
            "db_mb": round((os.path.getsize(db_file) if os.path.exists(db_file) else 0) / 1048576, 1),
            "run_logs_mb": round(dir_size(log_dir) / 1048576, 1),
            "rss_mb": rss_mb(args.pid),
            **{"ms_" + k: v for k, v in pages.items()},
        }
        rows.append(row)
        print(json.dumps(row), flush=True)

    record()
    while done + failed < args.workflow_runs:
        while submitted < args.workflow_runs and len(pending) < args.concurrency:
            status, run, _ = call("POST", "/workflows/%s/runs" % wf_id, {"clientRunKey": "%s-%d" % (tag, submitted)})
            if status != 200:
                sys.exit("could not start run: %s %s" % (status, run))
            pending.append((run["historyId"], time.time()))
            submitted += 1
        hid, t0 = pending.pop(0)
        while True:
            status, r, _ = call("GET", "/runs/%s?waitSeconds=60&includeStepLogs=false&logLimit=0" % hid)
            if status == 200 and r.get("terminal"):
                break
        run_times.append(time.time() - t0)
        if r.get("status") == "Done":
            done += 1
        else:
            failed += 1
        steps = r.get("steps") or []
        if steps and not big_run:
            big_run = steps[0].get("historyId")
        if (done + failed) % args.checkpoint == 0:
            record()
    if rows[-1]["workflow_runs"] != done + failed:
        record()

    first, last = rows[0], rows[-1]
    step_runs = max(1, last["step_runs"])
    db_growth = last["db_mb"] * 1048576 - start_db
    summary = {
        "workflow_runs": done + failed, "done": done, "failed": failed, "steps_per_run": args.steps,
        "elapsed_s": last["elapsed_s"],
        "db_start_mb": round(start_db / 1048576, 1), "db_end_mb": last["db_mb"],
        "db_kb_per_step_run": round(db_growth / step_runs / 1024, 1),
        "run_logs_kb_per_step_run": round((last["run_logs_mb"] * 1048576 - start_logs) / step_runs / 1024, 1),
        "peak_rss_mb": max([r["rss_mb"] for r in rows if r["rss_mb"]] or [0]) or None,
        "workflow_run_seconds_p95": round(p95(run_times), 1) if run_times else None,
        "first_checkpoint": first, "last_checkpoint": last,
        "process_id": pid, "workflow_id": wf_id,
    }
    with open(os.path.join(args.out, "metrics.csv"), "w", newline="") as f:
        w = csv.DictWriter(f, fieldnames=sorted({k for r in rows for k in r}))
        w.writeheader()
        w.writerows(rows)
    with open(os.path.join(args.out, "summary.json"), "w") as f:
        json.dump(summary, f, indent=2)
    print(json.dumps(summary, indent=2))


if __name__ == "__main__":
    main()
