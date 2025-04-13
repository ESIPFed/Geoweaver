/**
 *
 * Workspace monitor
 *
 */

//get the process and workflow table
getTable = function (data, pw_indicator) {
  var content = '<table class="table table-bordered">'
  content+= '<tr><th>'+pw_indicator+'_ID</th><th>Execution_ID</th><th>Name</th><th>Begin time</th><th>Duration</th></tr>';
  data.forEach(function (item) {
    var running_time = GW.history.calculate_duration(item.begin_time, new Date(), 'Running');
    content += '<tr><td>' + item.id + '</td><td>' + item.hid + '</td><td>' + item.name + '</td><td>' + item.begin_time + '</td><td>' + running_time + '</td></tr>';
  });
  content += '</table>';
  return content;
};


GW.monitor = {
  ws: null,

  current_name: null, //current workflow or process name

  historyid: null,

  all_ws: null,

  ws_onopen: function (e) {
    //shell.echo(special.white + "connected" + special.reset);
    console.log("workflow websocket is connected");
    // if(this.token==null || this.token == "null") this.token = GW.main.getJSessionId();
    // link the SSH session established with spring security logon to the websocket session...
    GW.monitor.all_ws.send("token:" + GW.general.CLIENT_TOKEN);
  },

  ws_onclose: function (e) {
    console.log("workflow websocket is closed");
  },

  ws_onmessage: function (e) {
    // console.log(e.data); //print out everything back from server

    if (e.data.indexOf("Session_Status:Active") != -1) {
      GW.monitor.checker_swich = false;
    } else if (GW.monitor.IsJsonString(e.data)) {
      var returnmsg = $.parseJSON(e.data);

      if (returnmsg.workflow_status == "completed") {
        GW.monitor.stopMonitor();
      } else {
        GW.workspace.updateStatus(returnmsg);
      }
    }
  },

  IsJsonString: function (str) {
    try {
      JSON.parse(str);
    } catch (e) {
      return false;
    }
    return true;
  },

  ws_onerror: function (e) {
    console.error(e.data);
  },

  startSocket: function (token) {
    console.log("Starting workflow monitoring connection");

    GW.monitor.token = token; //token is the jsession id

    // Check if we should use the communication module instead of direct WebSocket
    if (GW.communication && GW.communication.isConnected) {
      console.log("Using existing communication channel for workflow monitoring");
      // Register our message handler with the communication module
      GW.communication.send("token:" + token);
      return;
    }
    
    // If communication module isn't available or connected, check server preference
    var xhr = new XMLHttpRequest();
    xhr.open("GET", GW.path.getBasePath() + "api/config/communication-channel", false); // Synchronous request
    
    try {
      xhr.send();
      if (xhr.status === 200) {
        var response = JSON.parse(xhr.responseText);
        if (response && response.defaultChannel === "polling") {
          console.log("Server prefers HTTP long polling, not creating WebSocket");
          // Don't create WebSocket, use long polling instead
          return;
        }
      }
    } catch (error) {
      console.error("Error checking server preference:", error);
    }
    
    // If we get here, either server prefers WebSocket or we couldn't check preference
    console.log("Creating WebSocket connection for workflow monitoring");
    
    GW.monitor.all_ws = new WebSocket(
      GW.ssh.getWsPrefixURL() + "workflow-socket",
    );

    GW.monitor.all_ws.onopen = function (e) {
      GW.monitor.ws_onopen(e);
    };

    GW.monitor.all_ws.onclose = function (e) {
      GW.monitor.ws_onclose(e);
    };

    GW.monitor.all_ws.onmessage = function (e) {
      GW.monitor.ws_onmessage(e);
    };

    GW.monitor.all_ws.onerror = function (e) {
      GW.monitor.ws_onerror(e);
    };

    console.log("WebSocket connection initialized for workflow monitoring");
  },

  clearProgressIndicator: function () {
    $("#workspace_progress_indicator").empty(); //empty the progress bar
  },

  openProgressIndicator: function () {
    $("#workspace_progress_indicator").removeClass("invisible");

    $("#workspace_progress_indicator").addClass("visible");
  },

  closeProgressIndicator: function () {
    $("#workspace_progress_indicator").removeClass("visible");

    $("#workspace_progress_indicator").addClass("invisible");

    this.clearProgressIndicator(); //after the workflow is done, clear the progress bar.
  },

  updateProgress: function (id, flag) {
    var percent = 0;
    var barcolor = "";
    var statusClass = "";
    
    // Get task name from the workspace graph if available
    var taskName = "Task";
    if (GW.workspace && GW.workspace.theGraph) {
      for (var i = 0; i < GW.workspace.theGraph.nodes.length; i++) {
        if (GW.workspace.theGraph.nodes[i].id == id) {
          taskName = GW.workspace.theGraph.nodes[i].title || "Task";
          break;
        }
      }
    }

    if (flag == "Running") {
      percent = 30;
      barcolor = "progress-bar-running";
      statusClass = "status-running";
    } else if (flag == "Done") {
      percent = 100;
      barcolor = "progress-bar-done";
      statusClass = "status-done";
    } else if (flag == "Failed") {
      percent = 100;
      barcolor = "progress-bar-failed";
      statusClass = "status-failed";
    }

    // Check if the progress task already exists
    if (!$("#progress-" + id).length) {
      // Create a new progress task with modern styling
      $("#progress-tasks-container").find(".progress-empty-message").remove(); // Remove empty message if present
      
      $("#progress-tasks-container").append(
        '<div id="progress-' + id + '" class="progress-task">\t</div>'
      );
    }

    // Update the progress task with task name, status, and close button
    $("#progress-" + id).html(
      '<div class="progress-task-header">' +
      '  <span class="task-id">' + taskName + ' (' + id + ')</span>' +
      '  <div class="d-flex align-items-center">' +
      '    <span class="task-status ' + statusClass + '">' + flag + '</span>' +
      '    <span class="task-close ml-2" onclick="$(this).closest(\'#progress-' + id + '\').remove();"><i class="fa fa-times"></i></span>' +
      '  </div>' +
      '</div>' +
      '<div class="progress">' +
      '  <div class="progress-bar-custom ' + barcolor + '" style="width:' + percent + '%"></div>' +
      '</div>' +
      '<div class="progress-percentage">' + percent + '%</div>'
    );
  },

  openWorkspaceIndicator: function () {
    if (GW.workflow.loaded_workflow != null) {
      $("#current_workflow_name").html(
        "Current workflow : " + GW.workflow.loaded_workflow,
      );

      $("#current_workflow_name").removeClass("invisible");

      $("#current_workflow_name").addClass("visible");

      $("#running_spinner").removeClass("invisible");

      $("#running_spinner").addClass("visible");

      console.log("workspace indicator is opened");
    }
  },

  /**
   * Switch the mode of the button between play and stop.
   * @param {*} play_or_stop - true: play; false: stop
   */
  switchPlayButton: function (play_or_stop) {
    if (play_or_stop) {
      $("#execute-workflow").removeClass("fa-stop");
      $("#execute-workflow")
        .addClass("fa-play")
        .css("color", "gray")
        .attr("title", "execute workflow");
    } else {
      $("#execute-workflow").removeClass("fa-play");
      $("#execute-workflow")
        .addClass("fa-stop")
        .css("color", "red")
        .attr("title", "stop the execution");
    }
  },

  closeWorkspaceIndicator: function () {
    $("#current_workflow_name").html("");

    $("#current_workflow_name").removeClass("visible");

    $("#current_workflow_name").addClass("invisible");

    $("#running_spinner").removeClass("visible");

    $("#running_spinner").addClass("invisible");

    console.log("workspace indicator is closed");
  },

  send: function (data) {
    // If we're using the communication module, use that instead
    if (GW.communication && GW.communication.isConnected) {
      GW.communication.send(data);
      return;
    }
    
    // Check if WebSocket is available and open
    if (this.all_ws != null && this.all_ws.readyState === WebSocket.OPEN) {
      this.all_ws.send(data);
    } else {
      // Try HTTP fallback
      try {
        var xhr = new XMLHttpRequest();
        xhr.open("POST", GW.path.getBasePath() + "api/longpoll/send/" + GW.general.CLIENT_TOKEN, false);
        xhr.setRequestHeader("Content-Type", "text/plain");
        xhr.send(data);
      } catch (error) {
        console.error("Failed to send message via any channel:", error);
        if (data != this.token) {
          this.error("not connected!");
        }
      }
    }
  },

  checkSessionStatus: function () {
    // console.log("Current WS status: " + GW.ssh.all_ws.readyState);
    // return GW.ssh.all_ws.readyState;

    GW.monitor.checker_swich = true;
    
    // If we're using the communication module, use that instead
    if (GW.communication && GW.communication.isConnected) {
      GW.communication.send("token:" + GW.general.CLIENT_TOKEN);
      GW.monitor.checker_swich = false;
      return;
    }
    
    // Only send through WebSocket if it exists and is open
    if (GW.monitor.all_ws && GW.monitor.all_ws.readyState === WebSocket.OPEN) {
      GW.monitor.send("token:" + GW.general.CLIENT_TOKEN);
    }

    setTimeout(() => {
      if (GW.monitor.checker_swich) {
        // Check server preference before restarting WebSocket
        var xhr = new XMLHttpRequest();
        xhr.open("GET", GW.path.getBasePath() + "api/config/communication-channel", false); // Synchronous request
        
        try {
          xhr.send();
          if (xhr.status === 200) {
            var response = JSON.parse(xhr.responseText);
            if (response && response.defaultChannel === "polling") {
              console.log("Server prefers HTTP long polling, not restarting WebSocket");
              GW.monitor.checker_swich = false;
              return;
            }
          }
        } catch (error) {
          console.error("Error checking server preference:", error);
        }
        
        //restart the websocket if the switch is still true two seconds later
        GW.monitor.startSocket(GW.monitor.token);
        GW.monitor.checker_swich = false;
      }
    }, 2000);
  },

  /**
   *
   * Connect with the websocket session and get message to update the workflow status in the workspace.
   *
   * This function is only called during the workflow execution to update the workflow graph.
   *
   */
  startMonitor: function (token) {
    GW.workspace.currentmode = 2;

    // Check if we should use the communication module
    if (GW.communication && GW.communication.isConnected) {
      console.log("Using existing communication channel for workflow monitoring");
      // Register with the communication module
      GW.communication.send("token:" + token);
    } else {
      // Check server preference before creating/restarting WebSocket
      var xhr = new XMLHttpRequest();
      xhr.open("GET", GW.path.getBasePath() + "api/config/communication-channel", false); // Synchronous request
      var useWebSocket = true;
      
      try {
        xhr.send();
        if (xhr.status === 200) {
          var response = JSON.parse(xhr.responseText);
          if (response && response.defaultChannel === "polling") {
            console.log("Server prefers HTTP long polling for workflow monitoring");
            useWebSocket = false;
          }
        }
      } catch (error) {
        console.error("Error checking server preference:", error);
      }
      
      if (useWebSocket) {
        if (
          GW.monitor.all_ws == null ||
          GW.monitor.all_ws.readyState === WebSocket.CLOSED
        ) {
          console.log(
            "Detect there is no workflow websocket or the current one is closed, restarting..",
          );

          GW.monitor.startSocket(token);
        } else {
          //check
          GW.monitor.checkSessionStatus();
        }
      } else {
        console.log("Using HTTP long polling for workflow monitoring");
        // Send token through HTTP to register for polling
        var xhr = new XMLHttpRequest();
        xhr.open("POST", GW.path.getBasePath() + "api/longpoll/register/" + token, true);
        xhr.send();
      }
    }

    //			//only start when the mode is in monitor mode
    //
    //			if(GW.workspace.currentmode == GW.workspace.MONITOR){

    GW.monitor.openWorkspaceIndicator();

    GW.monitor.openProgressIndicator();

    GW.monitor.switchPlayButton(false);

    //			}
  },

  stopMonitor: function () {
    // if(this.ws != null){

    // this.ws.close();

    // this.ws = null;

    GW.workspace.currentmode = 1;

    GW.monitor.closeProgressIndicator();

    GW.monitor.closeWorkspaceIndicator();

    GW.monitor.switchPlayButton(true);

    // }
  },

  refresh: function () {
    //get the current executing processes

    $.ajax({
      url: "logs",

      method: "POST",

      data: "type=process&isactive=true",
    }).then(function (msg) {
      var process_msg=[];
      if (!msg.length) {
        $("#running_process_table").html("no running process found");

        return;
      } else {
        msg = $.parseJSON(msg);
        for (var i = 0; i < msg.length; i++) {
          var pw_id = msg[i].id;
          //get active processes id and name from execution_id
          $.ajax({
            url: "log",
            method: "POST",
            data: "type=process&id=" + pw_id,
          }).done(function (detailMsg) {
            var detailMsg = GW.general.parseResponse(detailMsg);
            process_msg.push(detailMsg);
        if (msg.length==process_msg.length){
          var content = getTable(process_msg, 'Process');

        $("#running_process_table").html(content);
        }
      });
    }
      }
    });

    //get the current executing workflows

    $.ajax({
      url: "logs",

      method: "POST",

      data: "type=workflow&isactive=true",
    }).then(function (msg) {
      if (!msg.length) {
        $("#running_workflow_table").html("no running workflow found");

        return;
      }

      msg = $.parseJSON(msg);
      var workflow_msg=[]
      for (var i = 0; i < msg.length; i++) {
        var pw_id = msg[i].id;
        $.ajax({
          url: "log",
          method: "POST",
          data: "type=workflow&id=" + pw_id,
        //get active workflows from execution_id
        }).then(function (detailMsg) {
          var detailMsg = $.parseJSON(detailMsg);
          var workflow_id = detailMsg.process;
          var begin_time = detailMsg.begin_time;
          var hid = detailMsg.hid;
          //get name of workflows from list
          $.ajax({
            url: "list",
            method: "POST",
            data: "type=workflow",
          }).then(function (workflowList) {
            workflowList = $.parseJSON(workflowList);
            var workflowObj = workflowList.find(w => w.id === workflow_id);
            workflow_msg.push({
              id: workflow_id,
              name: workflowObj.name,
              hid: hid,
              begin_time: begin_time
            });  
          var content = getTable(workflow_msg, 'Workflow');

      $("#running_workflow_table").html(content);
        })
    });
      }});
  },

  showDialog: function () {
    var content =
      '<div class="modal-body"><div class="row"><div class="col col-md-12"><h3>Running Processes</h3></div></div>' +
      '<div id="running_process_table" style="font-size: 12px;"></div>' +
      '<div class="row"><div  class="col col-md-12"><h3>Running Workflows</h3></div></div>' +
      '<div id="running_workflow_table" style="font-size: 12px;"></div></div>';

    content +=
      '<div class="modal-footer">' +
      '<button type="button" id="refresh-monitor" class="btn btn-outline-primary">Refresh</button> ' +
      "</div>";

    var frame = GW.process.createJSFrameDialog(
      720,
      480,
      content,
      "Activity Monitor",
    );

    //			var width = 720; var height = 480;
    //
    //			const frame = GW.workspace.jsFrame.create({
    //		    		title: 'Activity Monitor',
    //		    	    left: 0,
    //		    	    top: 0,
    //		    	    width: width,
    //		    	    height: height,
    //		    	    appearanceName: 'yosemite',
    //		    	    style: {
    //	                    backgroundColor: 'rgba(255,255,255,0.8)',
    //			    	    fontSize: 12,
    //	                    overflow:'auto'
    //	                },
    //		    	    html: content
    //	    	});
    //
    //			frame.setControl({
    //	            styleDisplay:'inline',
    //	            maximizeButton: 'zoomButton',
    //	            demaximizeButton: 'dezoomButton',
    //	            minimizeButton: 'minimizeButton',
    //	            deminimizeButton: 'deminimizeButton',
    //	            hideButton: 'closeButton',
    //	            animation: true,
    //	            animationDuration: 150,
    //
    //	        });
    //
    //	    	//Show the window
    //	    	frame.show();
    //
    //	    	frame.setPosition((window.innerWidth - width) / 2, (window.innerHeight -height) / 2, 'LEFT_TOP');

    GW.monitor.refresh();

    //			BootstrapDialog.show({
    //
    //				title: "Activity Monitor",
    //
    //				message: content,
    //
    //				onshown: function(dialogRef){
    //
    //					//get the current executing processes
    //
    //					$.ajax({
    //
    //						url: "logs",
    //
    //						method: "POST",
    //
    //						data: "type=process&isactive=true"
    //
    //					}).done(function(msg){
    //
    //						if(!msg.length){
    //
    //							$("#running_process_table").html("no running process found");
    //
    //							return;
    //
    //						}else{
    //
    //							msg = $.parseJSON(msg);
    //
    //							var content = GW.process.getTable(msg);
    //
    //							$("#running_process_table").html(content);
    //
    //						}
    //
    //
    //					});
    //
    //					//get the current executing workflows
    //
    //					$.ajax({
    //
    //						url: "logs",
    //
    //						method: "POST",
    //
    //						data: "type=workflow&isactive=true"
    //
    //					}).done(function(msg){
    //
    //						if(!msg.length){
    //
    //							$("#running_workflow_table").html("no running workflow found");
    //
    //							return;
    //
    //						}
    //
    //						msg = $.parseJSON(msg);
    //
    //						var content = GW.workflow.getTable(msg);
    //
    //						$("#running_workflow_table").html(content);
    //
    //					});
    //
    //				},
    //
    //				buttons: [{
    //
    //					label: "Close",
    //
    //					action: function(dialog){
    //
    //						dialog.close();
    //
    //					}
    //
    //				}]
    //
    //			});
  },
};
