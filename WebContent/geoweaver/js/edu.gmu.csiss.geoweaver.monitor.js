/**
 * 
 * workflow monitor
 * 
 * @author Ziheng Sun
 * 
 */

edu.gmu.csiss.geoweaver.monitor = {
		
		ws: null,
		
		current_name: null, //current workflow or process name
		
		historyid: null,
		
		send: function (data) {
	    	
	        if(this.ws != null){
	      	
	        	this.ws.send(data);
	        
	        } else {
	        
	        	this.error('not connected!');
	        
	        }
	    },
		
		ws_onopen: function(e){
			
			this.send(this.historyid);
			
		},
		
		ws_onclose: function(e){
			
			this.ws = null;
			
			edu.gmu.csiss.geoweaver.workspace.currentmode = 1;
			
			console.log("this workflow monitor websocket has been closed");
			
			edu.gmu.csiss.geoweaver.monitor.closeWorkspaceIndicator();
			
			edu.gmu.csiss.geoweaver.monitor.closeProgressIndicator();
			
		},
		
		ws_onmessage: function(e){
			
			try {
		    	
//		        if(e.data.indexOf(edu.gmu.csiss.geoweaver.ssh.special.prompt) == -1 && 
//		        		
//		        		e.data.indexOf(edu.gmu.csiss.geoweaver.ssh.special.ready) == -1) {
				
		        	var returnmsg = $.parseJSON(e.data);
		        	
		        	console.log(returnmsg);
		        	
		        	if(returnmsg.builtin){
		        		
		        		edu.gmu.csiss.geoweaver.process.callback(returnmsg);
		        		
		        	}else{
		        		
		        		edu.gmu.csiss.geoweaver.workspace.updateStatus(returnmsg);
		        		
		        	}
		        	
//		        }else{
//		        	
//		        	//the websocket is already closed. try the history query
//		        	
//		        	console.error("It ends too quickly. Go to history to check the logs out.");
//		        	
//		        }
		        
		      } catch(err) {
		    	
		    	console.error("** Invalid server response : " + err); 
		        
		      }
			
		},
		
		ws_onerror: function(e){
			
			console.error("error in monitoring workflow " + e );
			
			edu.gmu.csiss.geoweaver.monitor.closeWorkspaceIndicator();
			
			edu.gmu.csiss.geoweaver.monitor.closeProgressIndicator();
			
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
			
			$("#current_workflow_name").html("Current workflow : " + edu.gmu.csiss.geoweaver.workflow.loaded_workflow);
			
			$("#current_workflow_name").removeClass("invisible");
			
			$("#current_workflow_name").addClass("visible");
			
			$("#running_spinner").removeClass("invisible");
			
			$("#running_spinner").addClass("visible");
			
			console.log("workspace indicator is opened");
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
			
//			//only start when the mode is in monitor mode
//			
//			if(edu.gmu.csiss.geoweaver.workspace.currentmode == edu.gmu.csiss.geoweaver.workspace.MONITOR){
			
				edu.gmu.csiss.geoweaver.workspace.currentmode = 2;
				
				edu.gmu.csiss.geoweaver.monitor.openWorkspaceIndicator();
				
				edu.gmu.csiss.geoweaver.monitor.openProgressIndicator();

				edu.gmu.csiss.geoweaver.monitor.ws = new SockJS("task");
		        
				edu.gmu.csiss.geoweaver.monitor.historyid = historyid;
		        
				edu.gmu.csiss.geoweaver.monitor.ws.onopen = function(e) { edu.gmu.csiss.geoweaver.monitor.ws_onopen(e) };
		        
				edu.gmu.csiss.geoweaver.monitor.ws.onclose = function(e) { edu.gmu.csiss.geoweaver.monitor.ws_onclose(e) };
		        
				edu.gmu.csiss.geoweaver.monitor.ws.onmessage = function(e) { edu.gmu.csiss.geoweaver.monitor.ws_onmessage(e) };
		        
				edu.gmu.csiss.geoweaver.monitor.ws.onerror = function(e) { edu.gmu.csiss.geoweaver.monitor.ws_onerror(e) };
				
//			}
			
		},
		
		stopMonitor: function(){
			
			if(this.ws != null){
				
				this.ws.close();
				
				this.ws = null;
				
				edu.gmu.csiss.geoweaver.workspace.currentmode = 1;
				
			}
			
		},

		showDialog: function(){
			
			var content = "<div class=\"row\"><div class=\"col col-md-12\"><h3>Running Processes</h3></div></div>"+
			
					"<div id=\"running_process_table\"></div>" +
					
					"<div class=\"row\"><div  class=\"col col-md-12\"><h3>Running Workflows</h3></div></div>" +
					
					"<div id=\"running_workflow_table\"></div>";

			BootstrapDialog.show({
				
				title: "Activity Monitor",
				
				message: content,
					
				onshown: function(dialogRef){
					
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
							
							var content = "<table class=\"table\"> "+
							"  <thead> "+
							"    <tr> "+
							"      <th scope=\"col\">Process</th> "+
							"      <th scope=\"col\">Begin Time</th> "+
							"      <th scope=\"col\">Status</th> "+
							"      <th scope=\"col\">Action</th> "+
							"    </tr> "+
							"  </thead> "+
							"  <tbody> ";

							
							for(var i=0;i<msg.length;i++){
								
								var status_col = "      <td><span class=\"label label-warning\">Pending</span></td> ";
								
								if(msg[i].status == "Done"){
									
									status_col = "      <td><span class=\"label label-success\">Done</span></td> ";
									
								}else if(msg[i].status == "Failed"){
									
									status_col = "      <td><span class=\"label label-danger\">Failed</span></td> ";
									
								}else if(msg[i].status == "Running"){
									
									status_col = "      <td><span class=\"label label-warning\">Running</span></td> ";
									
								}else{
									
									status_col = "      <td><span class=\"label label-primary\">Unknown</span></td> ";
									
								}
								
								content += "    <tr> "+
									"      <td>"+msg[i].id+"</td> "+
									"      <td>"+msg[i].begin_time+"</td> "+
									status_col +
									"      <td><a href=\"javascript: edu.gmu.csiss.geoweaver.process.getHistoryDetails('"+msg[i].id+"')\">Check</a> &nbsp;"+
									"		<a href=\"javascript: edu.gmu.csiss.geoweaver.process.stop('"+msg[i].id+"')\">Stop</a></td> "+
									"    </tr>";
								
							}
							
							content += "</tbody>";
							
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
						
						var content = "<table class=\"table\"> "+
						"  <thead> "+
						"    <tr> "+
						"      <th scope=\"col\">Workflow</th> "+
						"      <th scope=\"col\">Begin Time</th> "+
						"      <th scope=\"col\">Status</th> "+
						"      <th scope=\"col\">Action</th> "+
						"    </tr> "+
						"  </thead> "+
						"  <tbody> ";

						
						for(var i=0;i<msg.length;i++){
							
							var status_col = "      <td><span class=\"label label-warning\">Pending</span></td> ";
							
							if(msg[i].status == "Done"){
								
								status_col = "      <td><span class=\"label label-success\">Done</span></td> ";
								
							}else if(msg[i].status == "Failed"){
								
								status_col = "      <td><span class=\"label label-danger\">Failed</span></td> ";
								
							}else if(msg[i].status == "Running"){
								
								status_col = "      <td><span class=\"label label-warning\">Running</span></td> ";
								
							}else{
								
								status_col = "      <td><span class=\"label label-primary\">Unknown</span></td> ";
								
							}
							
							content += "    <tr> "+
								"      <td>"+msg[i].id+"</td> "+
								"      <td>"+msg[i].begin_time+"</td> "+
								status_col +
								"      <td><a href=\"javascript: edu.gmu.csiss.geoweaver.process.getHistoryDetails('"+msg[i].id+"')\">Check</a> &nbsp;"+
								"		<a href=\"javascript: edu.gmu.csiss.geoweaver.process.stop('"+msg[i].id+"')\">Stop</a></td> "+
								"    </tr>";
							
						}
						
						content += "</tbody>";
						
						$("#running_workflow_table").html(content);
						
					});
					
//					$("#newhost-d").click(function(){
//						
//						edu.gmu.csiss.geoweaver.host.newDialog();
//						
//						dialogRef.close();
//						
//					});
//					
//					$("#newprocess-d").click(function(){
//						
//						edu.gmu.csiss.geoweaver.process.newDialog();
//						
//						dialogRef.close();
//						
//					});
//					
//					$("#newworkflow-d").click(function(){
//						
//						edu.gmu.csiss.geoweaver.workflow.newDialog();
//						
//						dialogRef.close();
//						
//					});
					
				},
				
				buttons: [{
					
					label: "Close",
					
					action: function(dialog){
						
						dialog.close();
						
					}
					
				}]
				
			});

		}
		
}

