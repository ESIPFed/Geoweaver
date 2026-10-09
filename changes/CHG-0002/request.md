# CHG-0002 — Live process log when opening Details on a running process

## Problem
Clicking Details in the history table while a process is Running freezes the process/side-panel log window. The top-level general log window continues streaming.

Follow-up: while a workflow runs, the process log window was polluted with workflow monitor JSON, sibling process output, and untagged status lines.

## Root cause
1. `GW.process.display` / `sidepanel.display` set `GW.process.history_id = null`, which caused `GW.ssh.addlog` to filter out tagged live lines from the process log element (`history_id` mismatch).
2. Opening Details only dumped a static DB snapshot into `#process-log-window` / `#prompt-panel-process-log-window` and never re-bound live streaming to that element.
3. Multi-tab: logs were only pushed to the starter tab's `CLIENT_TOKEN` (fixed via `ExecutionLogBroker` fan-out).
4. Process-panel filter treated `log_history_id == null` as “show everything,” so workflow status JSON and untagged lines leaked into the process log window.

## Fix
- Preserve `msg.hid` as `GW.process.history_id` when viewing a run.
- Add `GW.ssh.activateProcessLogStream(history_id, element_id)` (subscribe + bind element).
- On Details for Running processes, re-activate stream into the process/side-panel log window and ensure the Log switch is on.
- Process/side panel: append **only** when `log_history_id` equals the bound `GW.process.history_id` (strict). Untagged workflow/monitor lines stay in `#log-window` only.
- Server fan-out for multi-tab live logs (`ExecutionLogBroker` + long-poll `execution:` subscribe).

## Files
- `static/js/gw.ssh.js`
- `static/js/gw.process.js`
- `static/js/gw.process.sidepanel.js`
- `com/gw/utils/ExecutionLogBroker.java`
- `com/gw/server/CommandServlet.java`
- `com/gw/server/LongPollingController.java`
