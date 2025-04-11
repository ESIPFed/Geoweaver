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

          if (returnmsg.builtin) {
            GW.process.callback(returnmsg);
          } else {
            if (returnmsg.workflow_status == "completed") {
              GW.monitor.stopMonitor();
            } else {
              GW.workspace.updateStatus(returnmsg);
            }
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
      // Use the communication module to send data
      // This will automatically use WebSocket or HTTP fallback as appropriate
      GW.communication.send(data);
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

  ws_onmessage: function (e) {
    try {
      if (e.data.indexOf("Session_Status:Active") != -1) {
        GW.ssh.checker_swich = false;
      } else if (
        e.data.indexOf(this.special.prompt) == -1 &&
        e.data.indexOf(this.special.ready) == -1 &&
        e.data.indexOf("No SSH connection is active") == -1
      ) {
        this.echo(e.data);
      } else {
        //the websocket is already closed. try the history query
        // this.echo("It ends too quickly. Go to history to check the logs out.");
      }

      //if (e.data.indexOf(special.ready) != -1) {

      //	shell.resume();

      //}
    } catch (err) {
      console.log(err);

      this.error("** Invalid server response : " + e.data);
    }
  },

  addlog: function (content) {
    var dt = new Date();
    var time = dt.getHours() + ":" + dt.getMinutes() + ":" + dt.getSeconds();

    // Split content by the log separator
    var cont_splits = content.split("*_*");
    var log_history_id = null;

    // Extract history ID if present
    if (cont_splits.length > 1) {
      log_history_id = cont_splits[0];
      let newArray = cont_splits.slice(1);
      content = newArray.join(" ");
    }

    console.log("Log received - history_id: " + log_history_id + ", current process history_id: " + GW.process.history_id);

    // Style based on content
    var style1 = "";
    if (content.includes("Start to execute")) {
      style1 = "color: blue; font-weight: bold; text-decoration: underline;";
      $(".dot-flashing").removeClass("invisible").addClass("visible");
    } else if (content.includes("===== Process") || content.includes("Connected to process execution")) {
      style1 = "color: blue; font-weight: bold; text-decoration: underline;";
      $(".dot-flashing").removeClass("visible").addClass("invisible");
    } else if (content == "disconnected") {
      $(".dot-flashing").removeClass("visible").addClass("invisible");
    } else if (log_history_id == GW.process.history_id) {
      // This log belongs to the current process
      style1 = "color: green;";
      $(".dot-flashing").removeClass("invisible").addClass("visible");
    } else {
      $(".dot-flashing").removeClass("visible").addClass("invisible");
    }

    // Create the HTML for the log line
    var newline =
      `<p style="line-height:1.1; text-align:left; margin-top: 10px; ` +
      `margin-bottom: 10px;"><span style="` +
      style1 +
      `">` +
      content +
      `</span></p>`;

    // Add to main log window with limit
    this.current_log_length += 1;
    if (this.current_log_length > 5000) {
      $("#log-window").find("p:first").remove();
      this.current_log_length -= 1;
    }
    $("#log-window").append(newline);

    // Add to process-specific log window if it exists and matches current process
    if ($("#" + GW.ssh.process_output_id).length) {
      // Manage process log length
      if (this.current_process_log_length > 5000) {
        $("#" + GW.ssh.process_output_id).find("p:first").remove();
        this.current_process_log_length -= 1;
      }

      // Only display logs for the current process
      if (log_history_id == null || GW.process.history_id == log_history_id) {
        $("#" + GW.ssh.process_output_id).append(newline);
        // Scroll to bottom of log window
        var logWindow = document.getElementById(GW.ssh.process_output_id);
        if (logWindow) {
          logWindow.scrollTop = logWindow.scrollHeight;
        }
        this.current_process_log_length += 1;
      }
    }
  },

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
    // Close existing connection if any
    if (GW.ssh.all_ws) GW.ssh.all_ws.close();
    
    GW.ssh.output_div_id = "log_box_id";
    GW.ssh.token = token; //token is the jsession id
    
    // Initialize the communication module with the token and message handler
    // Using polling as the primary and only communication method
    GW.communication.init(token, function(message) {
      // This is the message handler that will be called for polling
      if (message.indexOf("Session_Status:Active") != -1) {
        GW.ssh.checker_swich = false;
      } else if (
        message.indexOf(GW.ssh.special.prompt) == -1 &&
        message.indexOf(GW.ssh.special.ready) == -1 &&
        message.indexOf("No SSH connection is active") == -1
      ) {
        GW.ssh.echo(message); // this function will be used to process all polling responses
      }
    });
    
    // Set up references to the communication module
    // Maintain the same interface for backward compatibility
    GW.ssh.all_ws = {
      close: function() {
        GW.communication.close();
      },
      send: function(data) {
        GW.communication.send(data);
      },
      readyState: function() {
        return GW.communication.isConnected ? WebSocket.OPEN : WebSocket.CLOSED;
      }
    };
    
    GW.ssh.ws = GW.ssh.all_ws;
  },

  connectWsSessionWithExecution: function (msg) {
    // This function connects the WebSocket session with a process execution
    // by associating the history_id with the current session
    
    // Store the history ID for reference
    GW.process.history_id = msg.history_id;
    
    // Ensure we have an active communication connection
    if (GW.ssh.all_ws != null) {
      // Check connection and reconnect if needed
      GW.communication.checkConnection();
    } else {
      console.log("No communication connection exists. Establishing connection...");
      GW.ssh.startLogSocket(msg.token);
    }
    
    // Send a message to the server to associate this session with the execution
    setTimeout(function() {
      GW.ssh.send("execution:" + msg.history_id);
      // Clear any existing logs in the process log window
      if ($("#" + GW.ssh.process_output_id).length) {
        $("#" + GW.ssh.process_output_id).html("");
      }
      // Add initial log message to confirm connection
      GW.ssh.addlog(msg.history_id + GW.utils.BaseTool.log_separator + "=======\nConnected to process execution: " + msg.history_id);
    }, 1000);
    
    console.log("WebSocket session connected with execution ID: " + msg.history_id);
  },

  openLog: function (msg) {
    // Check if the communication connection is alive, otherwise restore it
    // This will work with both WebSocket and long polling fallback
    
    if (GW.ssh.all_ws != null) {
      // Check connection and reconnect if needed
      GW.communication.checkConnection();
    } else {
      console.log("No communication connection exists. Establishing connection...");
      GW.ssh.startLogSocket(msg.token);
    }

    if (msg.history_id.length == 12)
      this.addlog("=======\nStart to execute Process " + msg.history_id);
    else this.addlog("=======\nStart to execute Workflow " + msg.history_id);
  },

  openTerminal: function (token, terminal_div_id) {
    // This function sets up a terminal interface in the specified div
    
    // Store the token and output div ID
    GW.ssh.token = token;
    GW.ssh.output_div_id = terminal_div_id;
    
    // Clear any existing content in the terminal div
    $("#" + terminal_div_id).html("");
    
    // Add terminal-specific styling to the div
    $("#" + terminal_div_id).addClass("terminal-container");
    
    // Initialize the communication connection if it doesn't exist
    if (GW.ssh.all_ws == null) {
      GW.ssh.startLogSocket(token);
    } else {
      // Check connection and reconnect if needed
      GW.communication.checkConnection();
    }
    
    // Send a message to the server to initialize the terminal
    setTimeout(function() {
      GW.ssh.send("terminal:init");
    }, 1000);
    
    // Add a welcome message to the terminal
    GW.ssh.echo("Terminal connected. Type commands to interact with the server.");
    
    console.log("Terminal opened in div: " + terminal_div_id);
  },
};
