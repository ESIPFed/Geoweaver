/**
 *
 * SSH client
 *
 * Create a shared websocket shell to stream all the results from server to the client.
 *
 * Distinguish the outputs of different sessions using tokens.
 *
 * Only one ssh session is allowed at one time.
 *
 * Only one running workflow is allowed at one time.
 *
 */

GW.ssh = {
  shell: null,

  sshConnected: false,

  passwordPhase: false,

  user: null,

  host: null,

  port: null,

  token: null,

  output_div_id: null,

  process_output_id: "process-log-window",

  ws: null,

  all_ws: null, //future websocket session for all the traffic between client and server

  last_prompt: null,

  password_cout: 0,

  key: "",

  checker_swich: false,

  current_log_length: 0, // length of log in log-window

  current_process_log_length: 0, // length of log in process and prompt log-window

  username: '<sec:authentication property="principal" />',

  special: {
    black: "\x1b[1;30m",
    red: "\x1b[1;31m",
    green: "\x1b[1;32m",
    yellow: "\x1b[1;33m",
    blue: "\x1b[1;34m",
    magenta: "\x1b[1;35m",
    cyan: "\x1b[1;36m",
    white: "\x1b[1;37m",
    reset: "\x1b[0m",
    ready: "]0;",
    prompt: "$",
  },

  echo: function (content) {
    if (content != null) {
      content = content.replace(/\n/g, "<br/>");

      this.addlog(content);

      // trigger the builtin process

      if (GW.general.isJSON(content)) {
        try {
          var returnmsg = $.parseJSON(content);

          console.log(returnmsg);

          if (returnmsg.ret == "success") {
            setTimeout(function () {
              GW.process.callback(returnmsg);
            }, 2000);
          }

          if (returnmsg.builtin) {
            GW.process.callback(returnmsg);
          } else {
            // GW.workspace.updateStatus(returnmsg); // the workflow status message should only come from the workflow-socket
          }
        } catch (errors) {
          console.error(errors);
        }
      }
    }
  },

  error: function (content) {
    content = content.replace(/\n/g, "<br/>");

    this.addlog(content);
  },

  send: function (data) {
    if (this.ws != null) {
      this.ws.send(data);
    } else {
      if (data != this.token) {
        this.error("not connected!");
      }
    }
  },

  checkSessionStatus: function () {
    // console.log("Current WS status: " + GW.ssh.all_ws.readyState);
    // return GW.ssh.all_ws.readyState;

    GW.ssh.checker_swich = true;

    GW.ssh.send("token:" + GW.general.CLIENT_TOKEN);

    setTimeout(() => {
      if (GW.ssh.checker_swich) {
        //restart the websocket if the switch is still true two seconds later
        GW.ssh.startLogSocket(GW.ssh.token);
        GW.ssh.checker_swich = false;
      }
    }, 2000);
  },

  ws_onopen: function (e) {
    console.log("WebSocket Channel is Openned");

    this.echo("connected");

    setTimeout(() => {
      GW.ssh.send("token:" + GW.general.CLIENT_TOKEN);
    }, 1000); //create a chance for the server side to register the session if it didn't when it is openned
  },

  ws_onclose: function (e) {
    try {
      this.echo("disconnected");

      GW.ssh.all_ws = null;
      GW.ssh.ws = null;
      GW.ssh.token = null;
    } catch (e) {
      console.error(e);

      this.echo("Reconnection failed. " + e);
    }

    console.log("the logging out websocket has been closed");
  },

  ws_onerror: function (e) {
    this.error("The process execution failed.");

    this.error("Reason: " + e);
  },

  // ws_onmessage: function (e) {

  //   console.log("WebSocket message received:", e.data);

  //   try {
  //     if (e.data.indexOf("Session_Status:Active") != -1) {
  //       GW.ssh.checker_swich = false;
  //     } else if (
  //       e.data.indexOf(this.special.prompt) == -1 &&
  //       e.data.indexOf(this.special.ready) == -1 &&
  //       e.data.indexOf("No SSH connection is active") == -1
  //     ) {
  //       this.echo(e.data);
  //     } else {
  //       console.log("No display to output");
  //       //the websocket is already closed. try the history query
  //       // this.echo("It ends too quickly. Go to history to check the logs out.");
  //     }

  //     //if (e.data.indexOf(special.ready) != -1) {

  //     //	shell.resume();

  //     //}
  //   } catch (err) {
  //     console.log(err);
  //     console.error("Error fetching file content", err);

  //     this.error("** Invalid server response : " + e.data);
  //   }
  // },


  ws_onmessage: function (e) {
    // console.log("WebSocket message received:", e.data);

    try {
        if (e.data.indexOf("Session_Status:Active") != -1) {
            GW.ssh.checker_swich = false;
        } else if (e.data.indexOf(this.special.prompt) == -1 &&
            e.data.indexOf(this.special.ready) == -1 &&
            e.data.indexOf("No SSH connection is active") == -1) {

            if (GW.process.sidepanel.current_workflow_history_id) {
                // Handle workflow log output
                let logContainerId = `prompt-panel-process-log-window-${GW.process.sidepanel.current_workflow_history_id}`;
                let workflowLogElement = document.getElementById(logContainerId);
                if (workflowLogElement) {
                    workflowLogElement.innerHTML += e.data.replace(/\n/g, "<br/>");
                } else {
                    // console.warn("Workflow log container not found.");
                }
            } else {
                // Handle regular process log output
                this.echo(e.data);
            }
        } else {
            // console.log("No display to output");
        }
    } catch (err) {
        // console.log(err);
        // console.error("Error fetching file content", err);

        this.error("** Invalid server response : " + e.data);
    }
},



//   ws_onmessage: function (e) {
//     console.log("WebSocket message received:", e.data);

//     try {
//         // Split the incoming WebSocket message on the delimiter '*_*' to extract history_id and content
//         let contentArray = e.data.split("*_*");
//         let log_history_id = null;
//         let logContent = e.data;

//         // If the message contains the delimiter, extract history_id and log content
//         if (contentArray.length > 1) {
//             log_history_id = contentArray[0]; // First part is the history_id
//             logContent = contentArray.slice(1).join(" "); // Remaining part is the actual log message
//         }

//         // Handle different cases based on message content
//         if (logContent.indexOf("Session_Status:Active") != -1) {
//             GW.ssh.checker_swich = false;
//         } else if (
//             logContent.indexOf(this.special.prompt) == -1 &&
//             logContent.indexOf(this.special.ready) == -1 &&
//             logContent.indexOf("No SSH connection is active") == -1
//         ) {
//             // Check if the history_id in the message matches the current running process
//             if (GW.process.history_id && log_history_id === GW.process.history_id) {
//                 // Append log content to the correct process log container
//                 this.echo(logContent); // Use the echo function to append the log content
//             } else {
//                 console.warn(`Mismatch or undefined history_id. Log not appended. Expected: ${GW.process.history_id}, Received: ${log_history_id}`);
//             }
//         } else {
//             console.log("No display to output");
//         }
//     } catch (err) {
//         console.error("Error processing WebSocket message:", err);
//         this.error("** Invalid server response: " + e.data);
//     }
// },




//////////////////// PARTIALLY WORKING ////////////////

  addlog: function (content) {
    var dt = new Date();
    var time = dt.getHours() + ":" + dt.getMinutes() + ":" + dt.getSeconds();

    cont_splits = content.split("*_*");

    log_history_id = null;

    if (cont_splits.length > 1) {
        log_history_id = cont_splits[0];  // Get the history ID from the content
        let newArray = cont_splits.slice(1);
        content = newArray.join(" ");
    }

    var style1 = "";
    if (content.includes("Start to execute")) {
        style1 = "color: blue; font-weight: bold; text-decoration: underline;";
    }

    var newline = `<p style="line-height:1.1; text-align:left; margin-top: 10px; margin-bottom: 10px;">
                     <span style="${style1}">${content}</span>
                   </p>`;

    this.current_log_length += 1;  // Increase log line count

    // Remove old logs if necessary
    if (this.current_log_length > 5000) {
        $("#log-window").find("p:first").remove();
        this.current_log_length -= 1;
    }

    // Check if the history ID matches the current process
    if (GW.process.history_id && log_history_id === GW.process.history_id) {
        // Add log to the specific container for this process
        let logContainerId = `process-log-window-${log_history_id}`;
        let logElement = document.getElementById(logContainerId);
        if (logElement) {
            logElement.appendChild($(newline)[0]);
        } else {
            // Create a new log container if not found
            logElement = document.createElement('div');
            logElement.id = logContainerId;
            logElement.className = 'process-log-window';
            document.getElementById('single-console-content').appendChild(logElement);
            logElement.appendChild($(newline)[0]);
        }
    } else {
        // console.warn(`Mismatch or undefined history_id. Log not appended.`);
    }

    this.current_process_log_length += 1;
},



//////////////////// PARTIALLY WORKING END ////////////////




  clearProcessLog: function () {
    $("#" + GW.ssh.process_output_id).html("");
  },

  clearMain: function () {
    $("#log-window").html("");
  },

  getWsPrefixURL: function () {
    var s =
      (window.location.protocol === "https:" ? "wss://" : "ws://") +
      window.location.host +
      "/Geoweaver/";

    //	    	s += "Geoweaver/"; //this is gone in spring boot

    console.log("Ws URL Prefix: ", s);

    return s;
  },

  startLogSocket: function (token) {
    if (GW.ssh.all_ws) GW.ssh.all_ws.close();

    GW.ssh.all_ws = new WebSocket(this.getWsPrefixURL() + "command-socket");

    GW.ssh.ws = GW.ssh.all_ws;

    GW.ssh.output_div_id = "log_box_id";

    GW.ssh.token = token; //token is the jsession id

    //			this.echo("Running process " + token)

    GW.ssh.all_ws.onopen = function (e) {
      GW.ssh.ws_onopen(e);
    };

    GW.ssh.all_ws.onclose = function (e) {
      GW.ssh.ws_onclose(e);
    };

    GW.ssh.all_ws.onmessage = function (e) {
      GW.ssh.ws_onmessage(e);
    };

    GW.ssh.all_ws.onerror = function (e) {
      GW.ssh.ws_onerror(e);
    };
  },

  connectWsSessionWithExecution: function (msg) {},

  openLog: function (msg) {
    //check if the websocket session is alive, otherwise, restore the connection

    if (
      GW.ssh.all_ws != null &&
      (GW.ssh.all_ws.readyState === WebSocket.CLOSED ||
        GW.ssh.all_ws.readyState === WebSocket.CLOSING)
    ) {
      // GW.ssh.all_ws.close();

      console.log(
        "The command websocket connection is detected to be closed. Try to reconnect...",
      );

      GW.ssh.startLogSocket(msg.token);

      console.log("The console websocket connection is restored..");
    } else {
      GW.ssh.checkSessionStatus();
    }

    if (msg.history_id.length == 12)
      this.addlog("=======\nStart to execute Process " + msg.history_id);
    else this.addlog("=======\nStart to execute Workflow " + msg.history_id);
  },

  openTerminal: function (token, terminal_div_id) {},
};



