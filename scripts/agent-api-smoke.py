#!/usr/bin/env python3
"""End-to-end Agent API check: can an AI agent drive this Geoweaver deployment?

Runs the loop a coding agent performs against GEOWEAVER_BASE_URL (local or remote):
health -> auth -> create python/shell processes -> run -> wait -> create a two-step
workflow (one failing step) -> per-step status/logs -> fix -> re-run -> JSON errors ->
schema -> clean up. Every object it creates is deleted at the end.

Usage:
  export GEOWEAVER_BASE_URL='https://gw.example.org/Geoweaver'
  export GEOWEAVER_API_TOKEN=...          # or let the script mint one:
  export GEOWEAVER_PASSWORD=...           # GUI localhost password (only used to mint)
  python3 scripts/agent-api-smoke.py

Exit code 0 when every check passes. The token is never printed.
"""

import json
import os
import sys
import time
import urllib.error
import urllib.request
import uuid

BASE = os.environ.get("GEOWEAVER_BASE_URL", "http://127.0.0.1:8070/Geoweaver").rstrip("/")
API = BASE + "/api/v1"
TOKEN = os.environ.get("GEOWEAVER_API_TOKEN", "")
RESULTS = []


def call(method, path, body=None, token=True, raw=None, url=None):
    data = raw if raw is not None else (None if body is None else json.dumps(body).encode())
    req = urllib.request.Request(url or API + path, data=data, method=method)
    req.add_header("Content-Type", "application/json")
    if token and TOKEN:
        req.add_header("Authorization", "Bearer " + TOKEN)
    opener = urllib.request.build_opener(NoRedirect)
    try:
        with opener.open(req, timeout=90) as res:
            text = res.read().decode()
            return res.status, (json.loads(text) if text else None)
    except urllib.error.HTTPError as e:
        text = e.read().decode() if e.fp else ""
        try:
            return e.code, json.loads(text) if text else None
        except ValueError:
            return e.code, {"_raw": text[:200]}


class NoRedirect(urllib.request.HTTPRedirectHandler):
    """Surface 302s instead of following them: an agent API must never redirect."""

    def redirect_request(self, req, fp, code, msg, headers, newurl):
        return None


def check(name, ok, detail=""):
    RESULTS.append(ok)
    print(("PASS " if ok else "FAIL ") + name + ("" if ok else "  -> " + str(detail)[:300]))
    return ok


def is_json_error(status, body, want_status):
    return status == want_status and isinstance(body, dict) and body.get("error") and body.get("message")


def main():
    global TOKEN
    tag = "smoke-" + uuid.uuid4().hex[:6]
    created_processes, created_workflows = [], []

    status, body = call("GET", "/health", token=False)
    if not check("health reachable", status == 200 and body == {"status": "up"}, (status, body)):
        return finish()

    if not TOKEN:
        pw = os.environ.get("GEOWEAVER_PASSWORD")
        if not pw:
            check("token available", False, "set GEOWEAVER_API_TOKEN or GEOWEAVER_PASSWORD")
            return finish()
        status, body = call("POST", "/tokens", {"hostPassword": pw, "ttlDays": 1}, token=False)
        check("mint token with GUI password", status == 201 and body.get("token"), (status, body and body.get("error")))
        TOKEN = (body or {}).get("token", "")
        minted_fp = (body or {}).get("fingerprint")
    else:
        minted_fp = None

    status, body = call("GET", "/processes", token=False)
    check("no token -> JSON 401", is_json_error(status, body, 401), (status, body))
    status, caps = call("GET", "/capabilities")
    if status == 401:
        check("token accepted", False,
              "401: the token was minted on a different Geoweaver (or expired/revoked). "
              "Mint against this GEOWEAVER_BASE_URL.")
        return finish()
    check("capabilities", status == 200 and "deleteWorkflow" in caps.get("operations", []), (status, caps))

    try:
        run_loop(tag, created_processes, created_workflows)
    except (KeyError, TypeError, AttributeError) as e:
        check("agent loop completed", False, "unexpected response shape: %r" % (e,))
    finally:
        cleanup(created_processes, created_workflows, minted_fp)
    return finish()


def run_loop(tag, created_processes, created_workflows):
    # Processes
    status, py = call("POST", "/processes", {"name": tag + "-py", "lang": "python",
                                             "code": "import os\nos.makedirs('/tmp/" + tag + "', exist_ok=True)\nopen('/tmp/" + tag + "/x.txt','w').write('42')\nprint('wrote 42')\n"})
    check("create python process", status == 200 and py.get("id"), (status, py))
    created_processes.append(py["id"])
    status, sh = call("POST", "/processes", {"name": tag + "-sh", "lang": "shell",
                                             "code": "set -e\nexit 3\n"})
    check("create shell process", status == 200 and sh.get("id"), (status, sh))
    created_processes.append(sh["id"])

    status, run = call("POST", "/processes/" + py["id"] + "/runs", {"clientRunKey": tag + "-1"})
    check("start process run", status == 200 and run.get("historyId"), (status, run))
    status, done = call("GET", "/runs/" + run["historyId"] + "?waitSeconds=60")
    check("wait -> python Done with log", done.get("status") == "Done" and "wrote 42" in done.get("log", ""), done)

    # Workflow: python -> failing shell, arrays + shorthand edges
    a, b = py["id"] + "-s1", sh["id"] + "-s2"
    status, wf = call("POST", "/workflows", {"name": tag + "-wf", "nodes": [{"id": a}, {"id": b}],
                                             "edges": [{"source": a, "target": b}]})
    check("create workflow from JSON arrays", status == 200 and wf.get("id"), (status, wf))
    created_workflows.append(wf["id"])
    status, wrun = call("POST", "/workflows/" + wf["id"] + "/runs", {"clientRunKey": tag + "-w1"})
    status, wdone = call("GET", "/runs/" + wrun["historyId"] + "?waitSeconds=60&includeStepLogs=true")
    steps = wdone.get("steps") or []
    check("workflow Failed with per-step status",
          wdone.get("status") == "Failed" and [s.get("status") for s in steps] == ["Done", "Failed"], wdone)

    # Agent fixes the failing step and re-runs
    status, _ = call("PUT", "/processes/" + sh["id"], {"code": "cat /tmp/" + tag + "/x.txt\necho\necho fixed\n"})
    check("update failing step", status == 200, status)
    status, wrun2 = call("POST", "/workflows/" + wf["id"] + "/runs", {"clientRunKey": tag + "-w2"})
    status, wdone2 = call("GET", "/runs/" + wrun2["historyId"] + "?waitSeconds=60&includeStepLogs=true")
    steps2 = wdone2.get("steps") or []
    check("re-run Done; step 2 read step 1's file",
          wdone2.get("status") == "Done" and len(steps2) == 2 and "42" in (steps2[1].get("log") or ""), wdone2)
    status, hist = call("GET", "/workflows/" + wf["id"] + "/runs")
    check("workflow run history", status == 200 and len(hist) >= 2, (status, hist))

    # Errors an agent will hit
    status, body = call("POST", "/workflows", {"name": "x", "nodes": [{"id": "nosuch-1"}]})
    check("unknown process in graph -> JSON 400", is_json_error(status, body, 400) and "nosuch" in body["message"], (status, body))
    status, body = call("POST", "/processes", raw=b"{not json")
    check("malformed JSON -> JSON 400", is_json_error(status, body, 400), (status, body))
    status, body = call("GET", "/does-not-exist")
    check("unknown path -> JSON 404", is_json_error(status, body, 404), (status, body))
    status, body = call("DELETE", "/processes/" + py["id"])
    check("delete in-use process -> JSON 409", is_json_error(status, body, 409), (status, body))

    status, schema = call("GET", "", token=False, url=BASE + "/v3/api-docs/agent-api")
    check("public agent OpenAPI schema", status == 200 and "/api/v1/runs/{historyId}" in (schema or {}).get("paths", {}), status)


def cleanup(created_processes, created_workflows, minted_fp):
    for wid in created_workflows:
        status, _ = call("DELETE", "/workflows/" + wid)
        check("cleanup workflow " + wid, status == 200, status)
    for pid in created_processes:
        status, _ = call("DELETE", "/processes/" + pid)
        check("cleanup process " + pid, status == 200, status)
    if minted_fp:
        call("DELETE", "/tokens/" + minted_fp)


def finish():
    passed = sum(1 for r in RESULTS if r)
    print("\n%d/%d checks passed against %s" % (passed, len(RESULTS), BASE))
    return 0 if RESULTS and all(RESULTS) else 1


if __name__ == "__main__":
    sys.exit(main())
