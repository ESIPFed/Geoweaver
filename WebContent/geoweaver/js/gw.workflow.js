/**
*
* Geoweaver Process
* 
* @author Ziheng Sun
*
*/     

GW.workflow = {
		
	loaded_workflow: null,
	
	new_frame: null,
	
	connection_cache: [{"w":"xxxx", "phs": {"hosts":"", "mode":"" }}],
	
	clearCache: function(){
		
		this.connection_cache = [];
		
	},
	
	setCache: function(wid, phs){
		
		var is = false;
		
		for(var i=0;i<this.connection_cache.length;i++){
			
			if(this.connection_cache[i].w == wid){
				
				this.connection_cache[i].phs = phs;
				
				is = true;
				
				break;
				
			}
			
		}
		
		if(!is){
			
			this.connection_cache.push({"w": wid, "phs": phs});
			
		}
		
		
	},
	
	display: function(msg){
		
		var content = "<div class=\"modal-body\">";
		
		content += "<div class=\"row\" style=\"font-size: 12px;\">";
		
		var workflowid = null;
		
		var workflowname = null;
		
		jQuery.each(msg, function(i, val) {
			
			if(val!=null&&val!="null"&&val!=""){
				
					if(typeof val =='object')
					{
					  val = JSON.stringify(val);
					}
					
					if(i=="id"){
						
						workflowid = val;
						
					}else if(i=="name"){
						
						workflowname = val;
						
					}
					
					
					content += "<div class=\"col col-md-3\">"+i+"</div>"+
					"<div class=\"col col-md-7\">"+val+"</div>";
					
			}

		});
		
		content += "</div>"+
		
		"<p align=\"right\">"+
		
		"<i class=\"fa fa-history subalignicon\" onclick=\"GW.workflow.history('"+
    	
		workflowid+"', '" + workflowname+"')\" data-toggle=\"tooltip\" title=\"List history logs\"></i> "+
		
		"<i class=\"fa fa-plus subalignicon\" data-toggle=\"tooltip\" title=\"Show/Add this workflow\" onclick=\"GW.workflow.add('"+
    	
		workflowid+"')\"></i> "+
		
		"<i class=\"fa fa-minus subalignicon\" style=\"color:red;\" data-toggle=\"tooltip\" title=\"Delete this workflow\" onclick=\"GW.menu.del('"+
    	
		workflowid+"','workflow')\"></i>"+
		
		"</p>"+
		
		"</div>";
		
		$("#main-workflow-content").html(content);
		
//		switchTab(document.getElementById("main-workflow-tab"), "main-workflow-info");
		GW.general.switchTab("workflow")
		
	},

	findCache: function(wid){
		
		var phs = null;
		
		for(var i=0;i<this.connection_cache.length;i++){
			
			if(this.connection_cache[i].w == wid){
				
				phs = this.connection_cache[i].phs;
				
				break;
				
			}
			
		}
		
		return phs;
		
	},
		
	newDialog: function(createandrun){
		
		//check if there is more than one processes in the workspace
		
		if(GW.workspace.checkIfWorkflow()){
			
			var content =  '<div class="modal-body"  style="font-size: 12px;">'+
			   '<form>'+
		       '   <div class="form-group row required">'+
		       '     <label for="processcategory" class="col-sm-4 col-form-label control-label">Input Workflow Name : </label>'+
		       '     <div class="col-sm-8">'+
		       '		<input type="text" class="form-control" id="workflow_name" placeholder="New Workflow Name" />'+
		       '     </div>'+
		       '   </div>'+
		       '</form></div>';
			
			content += '<div class="modal-footer">' +
			"<button type=\"button\" id=\"new-workflow-confirm\" class=\"btn btn-outline-primary\">Confirm</button> "+
			'</div>';
			
			GW.workflow.new_frame = GW.process.createJSFrameDialog(520, 450, content, "Authorization")
			
//			var width = 520; var height = 450;
//			
//			GW.workflow.new_frame = GW.workspace.jsFrame.create({
//	    		title: 'Authorization',
//	    	    left: 0, 
//	    	    top: 0, 
//	    	    width: width, 
//	    	    height: height,
//	    	    appearanceName: 'yosemite',
//	    	    style: {
//                    backgroundColor: 'rgb(255,255,255)',
//		    	    fontSize: 12,
//                    overflow:'auto'
//                },
//	    	    html: content
//	    	});
//	    	
//			GW.workflow.new_frame.setControl({
//                styleDisplay:'inline',
//                maximizeButton: 'zoomButton',
//                demaximizeButton: 'dezoomButton',
//                minimizeButton: 'minimizeButton',
//                deminimizeButton: 'deminimizeButton',
//                hideButton: 'closeButton',
//                animation: true,
//                animationDuration: 150,
//
//            });
//	    	
//			GW.workflow.new_frame.on('closeButton', 'click', (_frame, evt) => {
//                _frame.closeFrame();
//                
//            });
//            
//	    	//Show the window
//			GW.workflow.new_frame.show();
//	    	
//			GW.workflow.new_frame.setPosition((window.innerWidth - width) / 2, (window.innerHeight -height) / 2, 'LEFT_TOP');
			
			$("#new-workflow-confirm").click(function(){
				
            	$("#new-workflow-confirm").prop('disabled', true);
				
				//save the new workflow
				
				var workflow = {
					
					"name": $("#workflow_name").val(), 
					
					"type": "workflow",
					
					"nodes": JSON.stringify(GW.workspace.theGraph.nodes), 
					
					"edges": JSON.stringify(GW.workspace.theGraph.edges)
					
				};
				
				
				
				$.ajax({
					
					url: "add",
		    		
		    		method: "POST",
		    		
		    		data: workflow
		    		
				}).done(function(msg){
					
					msg = $.parseJSON(msg);
					
					GW.workflow.new_frame.closeFrame()
					
					GW.workflow.addMenuItem(msg);
					
					console.log("the workflow is added");
					
					GW.workflow.loaded_workflow = msg.id;
					
					if(createandrun){
						
						GW.workflow.run(msg.id);
						
					}
					
				}).fail(function(jqXHR, textStatus){
					
					console.error("fail to add workflow");
					
					alert("Fail to create new workflow");
					
					$("#new-workflow-confirm").prop('disabled', false);
					
				});
				
			});
			
//			BootstrapDialog.show({
//				
//				title: "New workflow",
//				
//				message: content,
//				
//				buttons: [{
//					
//					label: "Confirm",
//					
//					id: "workflow-new-confirm",
//					
//					action: function(dialog){
//						
//						var $button = this;
//	                	
//	                	$button.spin();
//	                	
////	                	dialog.enableButtons(false);
//	                	$("#workflow-new-confirm").prop('disabled', true);
//						
//						//save the new workflow
//						
//						var workflow = {
//							
//							"name": $("#workflow_name").val(), 
//							
//							"type": "workflow",
//							
//							"nodes": JSON.stringify(GW.workspace.theGraph.nodes), 
//							
//							"edges": JSON.stringify(GW.workspace.theGraph.edges)
//							
//						};
//						
//						$.ajax({
//							
//							url: "add",
//				    		
//				    		method: "POST",
//				    		
//				    		data: workflow
//				    		
//						}).done(function(msg){
//							
//							msg = $.parseJSON(msg);
//							
//							GW.workflow.addMenuItem(msg);
//							
//							console.log("the workflow is added");
//							
//							GW.workflow.loaded_workflow = msg.id;
//							
//							if(createandrun){
//								
//								GW.workflow.run(msg.id);
//								
//							}
//							
//							dialog.close();
//							
//						}).fail(function(jqXHR, textStatus){
//							
//							console.error("fail to add workflow");
//							
//							$button.stopSpin();
//	                		
//	        				dialogItself.enableButtons(true);
//							
//						});
//						
//					}
//					
//				},{
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
//				
//			});
			
		}else{
			
			alert("There are no adequate processes in the workspace!");
			
		}
		
		
		
	},
	
	/**
	 * render the workflow as a new process
	 */
	showProcess: function(wid){
		
		alert("not support yet");
		
	},
	
	save: function(nodes, edges){
		
		if(this.loaded_workflow!=null){
			
			var req = {
					
					"type": "workflow",
					
					"id": this.loaded_workflow,
					
					"nodes": JSON.stringify(nodes), 
					
					"edges": JSON.stringify(edges)
					
			};
			
			$.ajax({
				
				url: "edit",
				
				method: "POST",
				
				data: req
				
			}).done(function(msg){
				
				alert("Saved!!!");
				
			}).fail(function(jxr, status){
				
				alert("Error!!! Fail to save.");
				
			});
			
		}else{
			
			//add a new workflow
			
			this.newDialog(false);
			
		}
		
	},
	
	/**
	 * render the original processes in the workflow
	 */
	showWorkflow: function(wid){
		
		$.ajax({
			
			url: "detail",
			
			method: "POST",
			
			data: "type=workflow&id=" + wid
			
		}).done(function(msg){
			
			msg = $.parseJSON(msg);
			
			GW.workflow.loaded_workflow = msg.id;
			
			GW.workspace.theGraph.load(msg);
			
		}).fail(function(jxr, status){
			
			alert("fail to get workflow info");
			
			console.error("fail to get workflow info");
			
		});
	},
	
	/**
	 * Allow users to choose how to add the workflow into the workspace in two ways: 
	 * one process or the original workflow of processes
	 */
	add: function(wid){
		
		//pop up a dialog to ask which they would like to show it
		
		var req = "<div class=\"modal-body\"><div class=\"row\"> "+
		"		 <div class=\"col-md-12 col-sm-12 col-xs-12 form-group\">"+
		"		      <label class=\"labeltext\">How do you want to load the workflow?</label><br/>"+
		"		      <div class=\"form-check-inline\">"+
		"					<label class=\"customradio\"><span class=\"radiotextsty\">show all child processes and edges</span>"+
		"					  <input type=\"radio\" checked=\"checked\" name=\"addway\" value=\"all\">"+
		"					  <span class=\"checkmark\"></span>"+
		"					</label>"+
		//this is not supported yet
//		"					<label class=\"customradio\"><span class=\"radiotextsty\">show one single process</span>"+
//		"					  <input type=\"radio\" name=\"addway\" value=\"one\">"+
//		"					  <span class=\"checkmark\"></span>"+
//		"					</label>"+
		"			  </div>"+
		"		  </div>"+
		"	</div></div>";
		
		req += '<div class="modal-footer">' +
		"	<button type=\"button\" id=\"workflow-confirm\" class=\"btn btn-outline-primary\">Confirm</button> "+
		"	<button type=\"button\" id=\"workflow-cancel\" class=\"btn btn-outline-secondary\">Cancel</button>"+
		'</div>';
		
		var frame = GW.process.createJSFrameDialog(320, 210, req, "Show a Way");
		
		frame.on('#workflow-confirm', 'click', (_frame, evt) => {
			
			//get workflow details by the selected way
			
			var selValue = $('input[name=addway]:checked').val(); 
			
			console.log("selected way: " + selValue);
			
			if(selValue == "one"){
				
				GW.workflow.showProcess(wid);
				
			}else if(selValue == "all"){
				
				GW.workflow.showWorkflow(wid);
				
			}
			
			// switch to the workflow tab
//			switchTab(document.getElementById("main-workspace-tab"), "workspace");
			
			GW.general.switchTab("workspace")
			
        	_frame.closeFrame();
        	
        });
		
		frame.on('#workflow-cancel', 'click', (_frame, evt) => {
        	_frame.closeFrame();
        });
		
		
		
//		BootstrapDialog.show({
//			
//			title: "Show a way",
//			
//			message: req,
//			
//			buttons: [{
//				
//				label: "Confirm",
//				
//				action: function(dialog){
//					
//					//get workflow details by the selected way
//					
//					var selValue = $('input[name=addway]:checked').val(); 
//					
//					console.log("selected way: " + selValue);
//					
//					if(selValue == "one"){
//						
//						GW.workflow.showProcess(wid);
//						
//					}else if(selValue == "all"){
//						
//						GW.workflow.showWorkflow(wid);
//						
//					}
//					
//					dialog.close();
//					
//				}
//				
//			},{
//				
//				label: "Cancel",
//				
//				action: function(dialog){					
//					
//					dialog.close();
//					
//				}
//				
//			}]
//			
//		});
		
	},
	
	/**
	 * Start to collect information to run the workflow
	 */
	run: function(id, name){
		
		var phs = this.findCache(id);
		
		if(phs==null){
			
			//get all the nodes in the workflow
			
			//first, get the workflow details
			
			//second, choose host for each process
			
			//third, send the process-host pairs to backend
			
			//fourth, start the monitoring mode to get real-time status
			
			//fifth, trigger rendering module to pop up the results
			
			$.ajax({
				
				url: "detail",
				
				method: "POST",
				
				data: "type=workflow&id=" + id
				
			}).done(function(msg){
				
				msg = $.parseJSON(msg);
				
				var nodes = msg.nodes;
				
				var content = '<div  class=\"modal-body\"><form>'+
			       '   <div class=\"panel-body\"><div class="form-group row required">'+
			       '     <label for="hostselector" class="col-md-4 col-form-label control-label">Mode: </label>'+
			       '     <div class="col-md-8">'+
				   '        <div class="col-md-6"> '+
				   '            <input type="radio" '+
				   '                   name="modeswitch" value="one"  checked/> '+
				   '            <label>One host</label> '+
				   '        </div>'+
			       '		<div class="col-md-6"> '+
				   '            <input type="radio" '+
				   '                   name="modeswitch" value="different" /> '+
				   '	        <label>Multiple host</label> '+
				   '        </div> '+
			       '     </div>'+
			       '   </div></div>';
				
				content += "<div class=\"panel-body\" id=\"selectarea\">";
				
//				for(var i=0;i<nodes.length;i++){
//					
//					content += '   <div class="form-group row required" id="hostselectlist_'+i+'">'+
//				       '     <label for="hostselector" class="col-sm-4 col-form-label control-label">Run <mark>'+nodes[i].title+'</mark> on: </label>'+
//				       '     <div class="col-sm-8">'+
//				       '		<select class="form-control hostselector" id="hostforprocess_'+i+'">'+
//				       '  		</select>'+
//				       '     </div>'+
//				       '   </div>';
//					
//				}
				
				content += "</div>";
				
				content += '<div class="col-sm-12 form-check">'+
			       '		<input type="checkbox" class="form-check-input" id="remember">'+
			       '		<label class="form-check-label" for="remember">Remember this workflow-host connection</label>'+
			       '     </div>';
				
				content+= '</form></div>';
				
				content += '<div class="modal-footer">' +
				"	<button type=\"button\" id=\"host-select-run\" class=\"btn btn-outline-primary\">Run</button> "+
				"	<button type=\"button\" id=\"host-select-cancel\" class=\"btn btn-outline-secondary\">Cancel</button>"+
				'</div>';
				
				var frame = GW.process.createJSFrameDialog(480, 500, content, "Select Host");
				
				GW.host.refreshHostList();
				
				$("#selectarea").append('   <div class="form-group row required" id="hostselectlist">'+
					       '     <label for="hostselector" class="col-sm-4 col-form-label control-label">Select one host: </label>'+
					       '     <div class="col-sm-8">'+
					       '		<select class="form-control hostselector" id="hostforworkflow">'+
					       '  		</select>'+
					       '     </div>'+
					       '   </div>');
				
				$("input[name='modeswitch']").change(function(e){
					
			    	$("#selectarea").empty();
					
				    if($(this).val() == 'one') {
				    
				    	//only show one host selector
				    	
				    	$("#selectarea").append('   <div class="form-group row required" id="hostselectlist_'+i+'">'+
					       '     <label for="hostselector" class="col-sm-4 col-form-label control-label">Select one host: </label>'+
					       '     <div class="col-sm-8">'+
					       '		<select class="form-control hostselector" id="hostforworkflow">'+
					       '  		</select>'+
					       '     </div>'+
					       '   </div>');
				    
				    } else {
				    
				    	for(var i=0;i<nodes.length;i++){
							
				    		$("#selectarea").append('   <div class="form-group row required" id="hostselectlist_'+i+'">'+
						       '     <label for="hostselector" class="col-sm-4 col-form-label control-label">Run <mark>'+nodes[i].title+'</mark> on: </label>'+
						       '     <div class="col-sm-8">'+
						       '		<select class="form-control hostselector" id="hostforprocess_'+i+'">'+
						       '  		</select>'+
						       '     </div>'+
						       '   </div>');
							
						}
				    
				    }
				    
				    GW.host.refreshHostList();
				    
				});
				
				frame.on('#host-select-run', 'click', (_frame, evt) => {
					
					var hosts = [];
					
					var mode;
					
					if($('input[name=modeswitch]:checked').val()=="one"){
						
						//all on one
						
						mode = "one";
						
						var thehost = $("#hostforworkflow").val();
						
						hosts.push({
							"name":thehost, 
							"id": $("#hostforworkflow").find('option:selected').attr('id')
						});
						
					}else{
						
						//multiple
						
						mode = "different";
						
						for(var i=0;i<nodes.length;i++){
							
							hosts.push({
								"name":$("#hostforprocess_"+i).val(), 
								"id": $("#hostforprocess_"+i).find('option:selected').attr('id')
							});
							
						}
						
					}
					
					//remember the process-host connection
                	if(document.getElementById('remember').checked) {
                	    
                		GW.workflow.setCache(id, {hosts: hosts, mode: mode}); //remember s
                		
                	}
					
					GW.workflow.execute(id, mode, hosts);
					
		        	_frame.closeFrame();
		        	
		        });
				
				frame.on('#host-select-cancel', 'click', (_frame, evt) => {
					
		        	_frame.closeFrame();
		        	
		        });
			       
				
//				BootstrapDialog.show({
//					
//					title: "Select host",
//					
//					message: content,
//					
//					onshown: function(){
//						
//						GW.host.refreshHostList();
//						
//						$("#selectarea").append('   <div class="form-group row required" id="hostselectlist">'+
//							       '     <label for="hostselector" class="col-sm-4 col-form-label control-label">Select one host: </label>'+
//							       '     <div class="col-sm-8">'+
//							       '		<select class="form-control hostselector" id="hostforworkflow">'+
//							       '  		</select>'+
//							       '     </div>'+
//							       '   </div>');
//						
//						$("input[name='modeswitch']").change(function(e){
//							
//					    	$("#selectarea").empty();
//							
//						    if($(this).val() == 'one') {
//						    
//						    	//only show one host selector
//						    	
//						    	$("#selectarea").append('   <div class="form-group row required" id="hostselectlist_'+i+'">'+
//							       '     <label for="hostselector" class="col-sm-4 col-form-label control-label">Select one host: </label>'+
//							       '     <div class="col-sm-8">'+
//							       '		<select class="form-control hostselector" id="hostforworkflow">'+
//							       '  		</select>'+
//							       '     </div>'+
//							       '   </div>');
//						    
//						    } else {
//						    
//						    	for(var i=0;i<nodes.length;i++){
//									
//						    		$("#selectarea").append('   <div class="form-group row required" id="hostselectlist_'+i+'">'+
//								       '     <label for="hostselector" class="col-sm-4 col-form-label control-label">Run <mark>'+nodes[i].title+'</mark> on: </label>'+
//								       '     <div class="col-sm-8">'+
//								       '		<select class="form-control hostselector" id="hostforprocess_'+i+'">'+
//								       '  		</select>'+
//								       '     </div>'+
//								       '   </div>');
//									
//								}
//						    
//						    }
//						    
//						    GW.host.refreshHostList();
//						    
//						});
//						
//					},
//					
//					buttons: [{
//						
//						label: "Run",
//						
//						action: function(dialog){
//							
//							var hosts = [];
//							
//							var mode;
//							
//							if($('input[name=modeswitch]:checked').val()=="one"){
//								
//								//all on one
//								
//								mode = "one";
//								
//								var thehost = $("#hostforworkflow").val();
//								
//								hosts.push({
//									"name":thehost, 
//									"id": $("#hostforworkflow").find('option:selected').attr('id')
//								});
//								
//							}else{
//								
//								//multiple
//								
//								mode = "different";
//								
//								for(var i=0;i<nodes.length;i++){
//									
//									hosts.push({
//										"name":$("#hostforprocess_"+i).val(), 
//										"id": $("#hostforprocess_"+i).find('option:selected').attr('id')
//									});
//									
//								}
//								
//							}
//							
//							//remember the process-host connection
//		                	if(document.getElementById('remember').checked) {
//		                	    
//		                		GW.workflow.setCache(id, {hosts: hosts, mode: mode}); //remember s
//		                		
//		                	}
//							
//							GW.workflow.execute(id, mode, hosts);
//							
//							dialog.close();
//							
//						}
//						
//					}]
//					
//				});
				
			}).fail(function(jxr, status){
				
				alert("fail to get workflow details");
				
			});

		}else{
			
			GW.workflow.execute(id, phs.mode, phs.hosts);
			
		}
		
	},
	
	
	execute_callback: function(req, dialogItself, button){
		
 		$.ajax({
				
				url: "executeWorkflow",
				
				type: "POST",
				
				data: req
				
		}).done(function(msg){
			
			try{
				
				console.log(msg)
				
				msg = $.parseJSON(msg);
				
				if(msg.ret == "success"){
					
					console.log("the workflow is under execution.");
					
					console.log("history id: " + msg.history_id);
					
					GW.process.showSSHOutputLog(msg); //use the same method as the single process
					
					if(GW.workflow.loaded_workflow!=null
							&&GW.workflow.loaded_workflow==req.id){
						
    					GW.monitor.startMonitor(msg.history_id);
    					
					}
					
				}else if(msg.ret == "fail"){
					
					alert("Fail to execute the workflow.");
					
					console.error("fail to execute the workflow " + msg.reason);
					
				}else{
					
					console.log("other situation: " + msg);
					
				}
				
//				if(dialogItself) dialogItself.close();
				
			}catch(e){
				
				console.error(e)
				
//				if(button) button.stopSpin();
//        		
//				if(dialogItself) dialogItself.enableButtons(true);
				
				alert("fail to execute the workflow " + req.wid + ": " + e);
				
			}
			
		}).fail(function(jxr, status){
			
			alert("Error: unable to log on. Check if your password or the configuration of host is correct.");
			
//			for(var i=0;i<newhosts.length;i++){
//
//				$("#inputpswd_" + i).val("");
//				
//			}
			
			if(button) button.stopSpin();
    		
			if(dialogItself) dialogItself.enableButtons(true);
    		
			console.error("fail to execute the process " + req.wid);
			
		});
		
	},
	
	/**
	 * Start to execute the workflow directly
	 */
	execute: function(wid, mode, hosts){
		
		var req = {
 				
 				id: wid, // workflow id
 				
 				mode: mode
 				
 		};
		
		GW.host.start_auth_multiple(hosts, req, GW.workflow.execute_callback);
		
	},
	
	recent: function(number){

		$.ajax({
			
			url: "recent",
			
			method: "POST",
			
			data: "type=workflow&number=" + number
			
		}).done(function(msg){

			if(!msg.length){
				
				alert("no history found");
				
				return;
				
			}
			
			msg = $.parseJSON(msg);
			
			var content = "<div class=\"modal-body\" style=\"font-size: 12px;\" ><table class=\"table\"> "+
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
				
				if(msg[i].end_time!=null && msg[i].end_time != msg[i].begin_time){
					
					status_col = "      <td><span class=\"label label-success\">Done</span></td> ";
					
				}else if(msg[i].end_time == msg[i].begin_time && msg[i].output != null){
					
					status_col = "      <td><span class=\"label label-danger\">Failed</span></td> ";
					
				}
				
				content += "    <tr> "+
					"      <td>"+msg[i].name+"</td> "+
					"      <td>"+msg[i].begin_time+"</td> "+
					status_col+
					"      <td><a href=\"javascript: GW.workflow.getHistoryDetails('"+msg[i].id+"')\">Check</a></td> "+
					"    </tr>";
				
			}
			
			content += "</tbody></table></div>";
			
			var frame = GW.process.createJSFrameDialog(800, 640, content, "History")
			
//			var width = 800; var height = 640;
//			
//			const frame = GW.workspace.jsFrame.create({
//		    		title: 'History',
//		    	    left: 0, 
//		    	    top: 0, 
//		    	    width: width, 
//		    	    height: height,
//		    	    appearanceName: 'yosemite',
//		    	    style: {
//	                    backgroundColor: 'rgb(255,255,255)',
//			    	    fontSize: 12,
//	                    overflow:'auto'
//	                },
//		    	    html: content
//		    	    
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
//	    	frame.show();
//	    	
//	    	frame.setPosition((window.innerWidth - width) / 2, (window.innerHeight -height) / 2, 'LEFT_TOP');
			
//			BootstrapDialog.show({
//				
//				title: "History",
//				
//				message: content,
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
			
		}).fail(function(jxr, status){
			
			console.error(status);
			
		});
		
	},
	
	getTable: function(msg){
		
		var content = "<div class=\"modal-body\" style=\"font-size:12px;\" ><table class=\"table\"> "+
		"  <thead> "+
		"    <tr> "+
		"      <th scope=\"col\">Execution Id</th> "+
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
				
			}else if(msg[i].status == "Stopped"){
				
				status_col = "      <td><span class=\"label label-primary\">Stopped</span></td> ";
				
			}else{
				
				status_col = "      <td><span class=\"label label-primary\">Unknown</span></td> ";
				
			}
			
			content += "    <tr> "+
				"      <td>"+msg[i].id+"</td> "+
				"      <td>"+msg[i].begin_time+"</td> "+
				status_col +
				"      <td><a href=\"javascript: GW.workflow.getHistoryDetails('"+msg[i].id+"')\">Check</a> &nbsp;";
			
			if(msg[i].status == "Running"){
				
				content += "		<a href=\"javascript: GW.workflow.stop('"+msg[i].id+"'), 'workflow'\">Stop</a> ";
			}
				
			content += "   </td> </tr>";
			
		}
		
		content += "</tbody></table></div>";
		
		// create an interactive chart to show all the data
		
		content = "<div id=\"workflow-chart-container\" width=\"200\" height=\"100\">"+
		"<canvas id=\"workflow-history-chart\" style=\"width:200px !important; height:100px !important;\" ></canvas>"+
		"</div>" + 
		content;
		
		return content;
		
	},
	
	history: function(wid, name){
		
		$.ajax({
			
			url: "logs",
			
			method: "POST",
			
			data: "type=workflow&id=" + wid
			
		}).done(function(msg){
			
			if(!msg.length){
				
				alert("no history found");
				
				return;
				
			}
			
			msg = $.parseJSON(msg);
			
			var content = GW.workflow.getTable(msg);

			GW.chart.renderWorkflowHistoryChart(msg);
			
			
//			var frame = GW.process.createJSFrameDialog(600, 360, content, "History")
			
//			var width = 600; var height = 360;
//			
//			const frame = GW.workspace.jsFrame.create({
//		    		title: 'History',
//		    	    left: 0, 
//		    	    top: 0, 
//		    	    width: width, 
//		    	    height: height,
//		    	    appearanceName: 'yosemite',
//		    	    style: {
//	                    backgroundColor: 'rgb(255,255,255)',
//			    	    fontSize: 12,
//	                    overflow:'auto'
//	                },
//		    	    html: content
//		    	    
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
//	    	frame.show();
//	    	
//	    	frame.setPosition((window.innerWidth - width) / 2, (window.innerHeight -height) / 2, 'LEFT_TOP');
			
//			var content = "<table class=\"table\"> "+
//			"  <thead> "+
//			"    <tr> "+
//			"      <th scope=\"col\">Execution Id</th> "+
//			"      <th scope=\"col\">Begin Time</th> "+
//			"      <th scope=\"col\">Status</th> "+
//			"      <th scope=\"col\">Action</th> "+
//			"    </tr> "+
//			"  </thead> "+
//			"  <tbody> ";
//
//			
//			for(var i=0;i<msg.length;i++){
//				
//				var status_col = "      <td><span class=\"label label-warning\">Pending</span></td> ";
//				
//				if(msg[i].end_time!=null && msg[i].end_time != msg[i].begin_time){
//					
//					status_col = "      <td><span class=\"label label-success\">Done</span></td> ";
//					
//				}else if(msg[i].end_time == msg[i].begin_time && msg[i].output != null){
//					
//					status_col = "      <td><span class=\"label label-danger\">Failed</span></td> ";
//					
//				}
//				
//				content += "    <tr> "+
//					"      <td>"+msg[i].id+"</td> "+
//					"      <td>"+msg[i].begin_time+"</td> "+
//					status_col+
//					"      <td><a href=\"javascript: GW.workflow.getHistoryDetails('"+msg[i].id+"')\">Check</a></td> "+
//					"    </tr>";
//				
//			}
//			
//			content += "</tbody>";
			
//			BootstrapDialog.show({
//				
//				title: "History",
//				
//				message: content,
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
			
		}).fail(function(jxr, status){
			
			console.error("error in getting workflow history");
			
		});
		
	},
	
	showProcessLog: function(workflow_history_id, process_id){
		
		$.ajax({
			
			url: "log",
			
			method: "POST",
			
			data: "type=workflow&id=" + workflow_history_id
			
		}).done(function(msg){
			
			msg = $.parseJSON(msg);
			
			var process_history_id = null;
			
			for(var i=0;i<msg.input.length;i++){
				
				if(process_id == msg.input[i]){
					
					process_history_id = msg.output[i];
					
					break;
					
				}
					
			}
			
			GW.process.getHistoryDetails(process_history_id);
			
		}).fail(function(){
			
			console.error("fail to get log of this process");
			
		});
		
	},
	
	getHistoryDetails: function(history_id){
		
		$.ajax({
			
			url: "log",
			
			method: "POST",
			
			data: "type=workflow&id=" + history_id
			
		}).done(function(msg){
			
			msg = $.parseJSON(msg);
			
			var content = "<div class=\"modal-body\" style=\"font-size: 12px;\"><table class=\"table\"> "+
			"  <thead> "+
			"    <tr> "+
			"      <th scope=\"col\">Process Id</th> "+
			"      <th scope=\"col\">History Id</th> "+
			"      <th scope=\"col\">Action</th> "+
			"    </tr> "+
			"  </thead> "+
			"  <tbody> ";
			
			for(var i=0;i<msg.input.length;i++){
				
				content += "    <tr> "+
					"      <td>"+msg.input[i]+"</td> "+
					"      <td>"+msg.output[i]+"</td> "+
					"      <td><a href=\"javascript: GW.process.getHistoryDetails('"+msg.output[i]+"')\">Check</a></td> "+
					"    </tr>";
				
			}
			
			content += "</tbody></table></div>";
			
			var frame = GW.process.createJSFrameDialog(800, 640, content, "History")
			
//			var width = 800; var height = 640;
//			
//			const frame = GW.workspace.jsFrame.create({
//		    		title: 'History',
//		    	    left: 0, 
//		    	    top: 0, 
//		    	    width: width, 
//		    	    height: height,
//		    	    appearanceName: 'yosemite',
//		    	    style: {
//	                    backgroundColor: 'rgb(255,255,255)',
//			    	    fontSize: 12,
//	                    overflow:'auto'
//	                },
//		    	    html: content
//		    	    
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
//	    	frame.show();
//	    	
//	    	frame.setPosition((window.innerWidth - width) / 2, (window.innerHeight -height) / 2, 'LEFT_TOP');
			
		}).fail(function(){
			
			
		});
		
		
	},
	
	addMenuItem: function(one){
		
		$("#"+GW.menu.getPanelIdByType("workflow")).append("<li class=\"workflow\" id=\"workflow-" + one.id + "\">"+
				
				"<a href=\"javascript:void(0)\" onclick=\"GW.menu.details('"+one.id+"', 'workflow')\">" + 
	    		
				one.name + "</a> "+
				
//				"<i class=\"fa fa-history subalignicon\" onclick=\"GW.workflow.history('"+
//	        	
//				one.id+"', '" + one.name+"')\" data-toggle=\"tooltip\" title=\"List history logs\"></i> "+
//				
//				"<i class=\"fa fa-plus subalignicon\" data-toggle=\"tooltip\" title=\"Show/Add this workflow\" onclick=\"GW.workflow.add('"+
//	        	
//				one.id+"')\"></i> "+
//				
//				"<i class=\"fa fa-minus subalignicon\" data-toggle=\"tooltip\" title=\"Delete this workflow\" onclick=\"GW.menu.del('"+
//	        	
//				one.id+"','workflow')\"></i>"+
				
				//removed on 1/31/2019 - it is not allowed to run from the tree.
//				" <i class=\"fa fa-play subalignicon\" onclick=\"GW.workflow.run('"+
//	        	
//				one.id+"','"+one.name+"')\" data-toggle=\"tooltip\" title=\"Run Workflow\"></i> "+
				
				"</li>");
		
	},
	
	list: function(msg){
		
		for(var i=0;i<msg.length;i++){
			
			this.addMenuItem(msg[i]);
			
			//this.addWorkspace(msg[i]);
			
		}
		
		$('#workflows').collapse("show");
		
	}
		
}