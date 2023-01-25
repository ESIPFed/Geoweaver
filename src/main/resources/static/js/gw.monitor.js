/**
 * 
 * Workspace monitor
 * 
 */

GW.monitor = {
		
		ws: null,
		
		current_name: null, //current workflow or process name
		
		historyid: null,

		all_ws: null,
		
		ws_onopen: function(e){

			//shell.echo(special.white + "connected" + special.reset);
			console.log("workflow websocket is connected");
			// if(this.token==null || this.token == "null") this.token = GW.main.getJSessionId();
			// link the SSH session established with spring security logon to the websocket session...
			GW.monitor.all_ws.send("token:" + GW.general.CLIENT_TOKEN);
			
		},

		ws_onclose: function(e){

			console.log("workflow websocket is closed");

		},

		ws_onmessage: function(e){

			// console.log(e.data); //print out everything back from server

			if(e.data.indexOf("Session_Status:Active")!=-1){

				GW.monitor.checker_swich = false;

			}else if(GW.monitor.IsJsonString(e.data)){

				var returnmsg = $.parseJSON(e.data)

				if(returnmsg.workflow_status=="completed"){

					GW.monitor.stopMonitor();

				}else{

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

		ws_onerror: function(e){

			console.error(e.data);

		},

		startSocket: function(token){

			console.log("WebSocket Channel is Openned");
			
			GW.monitor.token = token; //token is the jsession id
			
			GW.monitor.all_ws = new WebSocket(GW.ssh.getWsPrefixURL() + "workflow-socket");
			
			
			GW.monitor.all_ws.onopen = function(e) { GW.monitor.ws_onopen(e) };
			
			GW.monitor.all_ws.onclose = function(e) { GW.monitor.ws_onclose(e) };
			
			GW.monitor.all_ws.onmessage = function(e) { GW.monitor.ws_onmessage(e) };
			
			GW.monitor.all_ws.onerror = function(e) { GW.monitor.ws_onerror(e) };


			// setTimeout(function () {
			// 	GW.monitor.all_ws.send("token:"+token);
			// }, 3000);

			console.log("token has been sent to server");

		},
		
		clearProgressIndicator: function(){
			
			$("#workspace_progress_indicator").empty(); //empty the progress bar
			
		},
		
		openProgressIndicator: function(){
			
			$("#workspace_progress_indicator").removeClass("invisible");
			
			$("#workspace_progress_indicator").addClass("visible");
			
		},
		
		closeProgressIndicator: function(){
			
			$("#workspace_progress_indicator").removeClass("visible");
			
			$("#workspace_progress_indicator").addClass("invisible");

			this.clearProgressIndicator(); //after the workflow is done, clear the progress bar. 
			
		},
		
		updateProgress: function(id, flag){
			
			var percent = 0;
			
			var barcolor = "";
			
			if(flag=="Running"){
	    		  
	    		  percent = 30
	    		  barcolor = "progress-bar-success progress-bar-striped active";
	    		  
	    	}else if(flag=="Done"){
	    		  
	    		  percent = 100;
	    		  barcolor = "progress-bar-success progress-bar-striped";
	    		  
	    	}else if(flag=="Failed"){
	    		  
	    		  percent = 100;
				  
	    		  barcolor = "progress-bar-danger progress-bar-striped";
	    		  
	    	}
			
			if(!$("#progress-" + id).length){
				
				$("#workspace_progress_indicator").append("<div id=\"progress-"+id+"\">	</div>");
				
			}
			
			$("#progress-" + id).html("		Task "+ id +" "+
					"		<div class=\"progress\"> "+
					"		  <div class=\"progress-bar "+barcolor+"\" role=\"progressbar\" "+
					"		  aria-valuenow=\""+percent+"\" aria-valuemin=\"0\" aria-valuemax=\"100\" style=\"width:"+percent+"%\"> "+
					"		    "+percent+"% "+
					"		  </div> "+
					"		</div> ");
			
		},
		
		openWorkspaceIndicator: function(){
			
			if(GW.workflow.loaded_workflow != null){
				
				$("#current_workflow_name").html("Current workflow : " + GW.workflow.loaded_workflow);
				
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
		switchPlayButton: function(play_or_stop){

			if(play_or_stop){
				$("#execute-workflow").removeClass("fa-stop")
				$("#execute-workflow").addClass("fa-play").css("color", "gray").attr("title", "execute workflow")
			}else{
				$("#execute-workflow").removeClass("fa-play")
				$("#execute-workflow").addClass("fa-stop").css("color", "red").attr("title", "stop the execution")
			}

		},
		
		closeWorkspaceIndicator: function(){
			
			$("#current_workflow_name").html("");
			
			$("#current_workflow_name").removeClass("visible");
			
			$("#current_workflow_name").addClass("invisible");
			
			$("#running_spinner").removeClass("visible");
			
			$("#running_spinner").addClass("invisible");
			
			console.log("workspace indicator is closed");
			
		},

		send: function (data) {
	    	
	        if(this.all_ws != null){
	      	
	        	this.all_ws.send(data);
	        
	        } else {
	        
	        	if(data!=this.token){
	        		
	        		this.error('not connected!');
	        		
	        	}
	        	
	        }
	    },

		checkSessionStatus: function(){
			// console.log("Current WS status: " + GW.ssh.all_ws.readyState);
			// return GW.ssh.all_ws.readyState;

			GW.monitor.checker_swich = true;

			GW.monitor.send("token:"+GW.general.CLIENT_TOKEN);

			setTimeout(() => {  
				
				if(GW.monitor.checker_swich){

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
		startMonitor: function(token){

			GW.workspace.currentmode = 2;

			if ( GW.monitor.all_ws == null || GW.monitor.all_ws.readyState === WebSocket.CLOSED ) {

				console.log("Detect there is no workflow websocket or the current one is closed, restarting..");

				GW.monitor.startSocket(token);
			
			}else{

				//check 
				GW.monitor.checkSessionStatus();

			}
			
//			//only start when the mode is in monitor mode
//			
//			if(GW.workspace.currentmode == GW.workspace.MONITOR){
			
				GW.monitor.openWorkspaceIndicator();
				
				GW.monitor.openProgressIndicator();

				GW.monitor.switchPlayButton(false);

//			}
			
		},
		
		stopMonitor: function(){
			
			// if(this.ws != null){
				
				// this.ws.close();
				
				// this.ws = null;
				
				GW.workspace.currentmode = 1;

				GW.monitor.closeProgressIndicator();

				GW.monitor.closeWorkspaceIndicator();

				GW.monitor.switchPlayButton(true);
				
			// }
			
		},
		
		refresh: function(){
			
			//get the current executing processes
			
			$.ajax({
				
				url: "logs",
				
				method: "POST",
				
				data: "type=process&isactive=true"
				
			}).done(function(msg){
				
				if(!msg.length){
					
					$("#running_process_table").html("no running process found");
					
					return;
					
				}else{
					
					msg = $.parseJSON(msg);
					
					var content = GW.process.getTable(msg);
					
					$("#running_process_table").html(content);
					
				}
				
				
			});
			
			//get the current executing workflows
			
			$.ajax({
				
				url: "logs",
				
				method: "POST",
				
				data: "type=workflow&isactive=true"
				
			}).done(function(msg){
				
				if(!msg.length){
					
					$("#running_workflow_table").html("no running workflow found");
					
					return;
					
				}
				
				msg = $.parseJSON(msg);
				
				var content = GW.workflow.getTable(msg);
				
				$("#running_workflow_table").html(content);
				
			});
			
		},

		showDialog: function(){
			
			var content = "<div class=\"modal-body\"><div class=\"row\"><div class=\"col col-md-12\"><h3>Running Processes</h3></div></div>"+
			
					"<div id=\"running_process_table\" style=\"font-size: 12px;\"></div>" +
					
					"<div class=\"row\"><div  class=\"col col-md-12\"><h3>Running Workflows</h3></div></div>" +
					
					"<div id=\"running_workflow_table\" style=\"font-size: 12px;\"></div></div>";

			content += '<div class="modal-footer">' +
				"<button type=\"button\" id=\"refresh-monitor\" class=\"btn btn-outline-primary\">Refresh</button> "+
				'</div>';
			
			var frame = GW.process.createJSFrameDialog(720, 480, content, "Activity Monitor")
			
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

		}
		
}

