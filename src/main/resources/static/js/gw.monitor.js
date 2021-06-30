/**
 * 
 * Workflow monitor
 * 
 * @author Ziheng Sun
 * 
 */

GW.monitor = {
		
		ws: null,
		
		current_name: null, //current workflow or process name
		
		historyid: null,

		all_ws: null,
		
// 		send: function (data) {
	    	
// 	        if(this.ws != null){
	      	
// 	        	this.ws.send(data);
	        
// 	        } else {
	        
// 	        	this.error('not connected!');
	        
// 	        }
// 	    },
		
// 		ws_onopen: function(e){
			
// 			this.send(this.historyid);
			
// 		},
		
// 		ws_onclose: function(e){
			
// 			this.ws = null;
			
// 			GW.workspace.currentmode = 1;
			
// 			console.log("this workflow monitor websocket has been closed");
			
// 			GW.monitor.closeWorkspaceIndicator();
			
// 			GW.monitor.closeProgressIndicator();
			
// 		},
		
// 		ws_onmessage: function(e){
			
// 			try {
		    	
// //		        if(e.data.indexOf(GW.ssh.special.prompt) == -1 && 
// //		        		
// //		        		e.data.indexOf(GW.ssh.special.ready) == -1) {
				
// 		        	var returnmsg = $.parseJSON(e.data);
		        	
// 		        	console.log(returnmsg);
		        	
// 		        	if(returnmsg.builtin){
		        		
// 		        		GW.process.callback(returnmsg);
		        		
// 		        	}else{
		        		
// 		        		GW.workspace.updateStatus(returnmsg);
		        		
// 		        	}
		        	
// //		        }else{
// //		        	
// //		        	//the websocket is already closed. try the history query
// //		        	
// //		        	console.error("It ends too quickly. Go to history to check the logs out.");
// //		        	
// //		        }
		        
// 		      } catch(err) {
		    	
// 		    	console.error("** Invalid server response : " + err); 
		        
// 		      }
			
// 		},
		
// 		ws_onerror: function(e){
			
// 			console.error("error in monitoring workflow " + e );
			
// 			GW.monitor.closeWorkspaceIndicator();
			
// 			GW.monitor.closeProgressIndicator();
			
// 		},


		ws_onopen: function(e){

			//shell.echo(special.white + "connected" + special.reset);
			console.log("workflow websocket is connected");
			// link the SSH session established with spring security logon to the websocket session...
			GW.monitor.all_ws.send(this.token);
			
		},

		ws_onclose: function(e){

			console.log("workflow websocket is closed");

		},

		ws_onmessage: function(e){

			console.log(e.data); //print out everything back from server

			if(GW.monitor.IsJsonString(e.data)){

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
				
			GW.monitor.all_ws = new WebSocket(GW.ssh.getWsPrefixURL() + "workflow-socket");
			
			GW.monitor.token = token; //token is the jsession id
			
			GW.monitor.all_ws.onopen = function(e) { GW.monitor.ws_onopen(e) };
			
			GW.monitor.all_ws.onclose = function(e) { GW.monitor.ws_onclose(e) };
			
			GW.monitor.all_ws.onmessage = function(e) { GW.monitor.ws_onmessage(e) };
			
			GW.monitor.all_ws.onerror = function(e) { GW.monitor.ws_onerror(e) };

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
			
		},
		
		updateProgress: function(id, flag){
			
			var percent = 0;
			
			var barcolor = "";
			
			if(flag=="RUNNING"){
	    		  
	    		  percent = 30
	    		  barcolor = "progress-bar-success progress-bar-striped active";
	    		  
	    	}else if(flag=="DONE"){
	    		  
	    		  percent = 100;
	    		  barcolor = "progress-bar-success progress-bar-striped";
	    		  
	    	}else if(flag=="FAILED"){
	    		  
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
		
		closeWorkspaceIndicator: function(){
			
			$("#current_workflow_name").html("");
			
			$("#current_workflow_name").removeClass("visible");
			
			$("#current_workflow_name").addClass("invisible");
			
			$("#running_spinner").removeClass("visible");
			
			$("#running_spinner").addClass("invisible");
			
			console.log("workspace indicator is closed");
			
		},
		
		/**
		 * 
		 * connect with the websocket session and get message to update the workflow status in the workspace
		 * 
		 */
		startMonitor: function(historyid){

			if ( GW.monitor.all_ws == null || GW.monitor.all_ws.readyState === WebSocket.CLOSED ) {

				GW.monitor.startSocket();
			
			}
			
//			//only start when the mode is in monitor mode
//			
//			if(GW.workspace.currentmode == GW.workspace.MONITOR){
			
				GW.workspace.currentmode = 2;
				
				GW.monitor.openWorkspaceIndicator();
				
				GW.monitor.openProgressIndicator();

				
				//not used any more
				
//				GW.monitor.ws = new SockJS("task");
//		        
//				GW.monitor.historyid = historyid;
//		        
//				GW.monitor.ws.onopen = function(e) { GW.monitor.ws_onopen(e) };
//		        
//				GW.monitor.ws.onclose = function(e) { GW.monitor.ws_onclose(e) };
//		        
//				GW.monitor.ws.onmessage = function(e) { GW.monitor.ws_onmessage(e) };
//		        
//				GW.monitor.ws.onerror = function(e) { GW.monitor.ws_onerror(e) };
				
//			}
			
		},
		
		stopMonitor: function(){
			
			// if(this.ws != null){
				
				// this.ws.close();
				
				// this.ws = null;
				
				GW.workspace.currentmode = 1;

				GW.monitor.closeProgressIndicator();

				GW.monitor.closeWorkspaceIndicator();
				
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

