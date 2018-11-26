/**
*
* Geoweaver Process
* 
* @author Ziheng Sun
*
*/     

edu.gmu.csiss.geoweaver.workflow = {
		
	loaded_workflow: null,
		
	newDialog: function(createandrun){
		
		//check if there is more than one processes in the workspace
		
		if(edu.gmu.csiss.geoweaver.workspace.checkIfWorkflow()){
			
			var content = '<form>'+
		       '   <div class="form-group row required">'+
		       '     <label for="processcategory" class="col-sm-4 col-form-label control-label">Input Workflow Name : </label>'+
		       '     <div class="col-sm-8">'+
		       '		<input type="text" class="form-control" id="workflow_name" placeholder="New Workflow Name" />'+
		       '     </div>'+
		       '   </div>'+
		       '</form>';
			
			BootstrapDialog.show({
				
				title: "New workflow",
				
				message: content,
				
				buttons: [{
					
					label: "Confirm",
					
					action: function(dialog){
						
						var $button = this;
	                	
	                	$button.spin();
	                	
	                	dialog.enableButtons(false);
						
						//save the new workflow
						
						var workflow = {
							
							"name": $("#workflow_name").val(), 
							
							"type": "workflow",
							
							"nodes": JSON.stringify(edu.gmu.csiss.geoweaver.workspace.theGraph.nodes), 
							
							"edges": JSON.stringify(edu.gmu.csiss.geoweaver.workspace.theGraph.edges)
							
						};
						
						$.ajax({
							
							url: "add",
				    		
				    		method: "POST",
				    		
				    		data: workflow
				    		
						}).done(function(msg){
							
							msg = $.parseJSON(msg);
							
							edu.gmu.csiss.geoweaver.workflow.addMenuItem(msg);
							
							console.log("the workflow is added");
							
							if(createandrun){
								
								edu.gmu.csiss.geoweaver.workflow.run(msg.id);
								
							}
							
							dialog.close();
							
						}).fail(function(jqXHR, textStatus){
							
							console.error("fail to add workflow");
							
							$button.stopSpin();
	                		
	        				dialogItself.enableButtons(true);
							
						});
						
					}
					
				},{
					
					label: "Close",
					
					action: function(dialog){
						
						dialog.close();
						
					}
					
				}]
				
				
			});
			
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
			
			edu.gmu.csiss.geoweaver.workflow.loaded_workflow = msg.id;
			
			edu.gmu.csiss.geoweaver.workspace.theGraph.load(msg);
			
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
		
		var req = "<div class=\"row\"> "+
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
		"	</div>";
		
		BootstrapDialog.show({
			
			title: "Show a way",
			
			message: req,
			
			buttons: [{
				
				label: "Confirm",
				
				action: function(dialog){
					
					//get workflow details by the selected way
					
					var selValue = $('input[name=addway]:checked').val(); 
					
					console.log("selected way: " + selValue);
					
					if(selValue == "one"){
						
						edu.gmu.csiss.geoweaver.workflow.showProcess(wid);
						
					}else if(selValue == "all"){
						
						edu.gmu.csiss.geoweaver.workflow.showWorkflow(wid);
						
					}
					
					dialog.close();
					
				}
				
			},{
				
				label: "Cancel",
				
				action: function(dialog){					
					
					dialog.close();
					
				}
				
			}]
			
		});
		
	},
	
	/**
	 * Start to collect information to run the workflow
	 */
	run: function(id, name){
		
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
			
			var content = '<form>'+
		       '   <div class=\"panel-body\"><div class="form-group row required">'+
		       '     <label for="hostselector" class="col-md-4 col-form-label control-label">Mode: </label>'+
		       '     <div class="col-md-8">'+
		       '		<div class="col-md-6"> '+
			   '            <input type="radio" '+
			   '                   name="modeswitch" value="different" checked /> '+
			   '	        <label>Multiple host</label> '+
			   '        </div> '+
			   '        <div class="col-md-6"> '+
			   '            <input type="radio" '+
			   '                   name="modeswitch" value="one" /> '+
			   '            <label>One host</label> '+
			   '        </div>'+
		       '     </div>'+
		       '   </div></div>';
			
			content += "<div class=\"panel-body\" id=\"selectarea\">";
			
			for(var i=0;i<nodes.length;i++){
				
				content += '   <div class="form-group row required" id="hostselectlist_'+i+'">'+
			       '     <label for="hostselector" class="col-sm-4 col-form-label control-label">Run <mark>'+nodes[i].title+'</mark> on: </label>'+
			       '     <div class="col-sm-8">'+
			       '		<select class="form-control hostselector" id="hostforprocess_'+i+'">'+
			       '  		</select>'+
			       '     </div>'+
			       '   </div>';
				
			}
			
			content += "</div>";
			
			content+= '</form>';
		       
			
			BootstrapDialog.show({
				
				title: "Select host",
				
				message: content,
				
				onshown: function(){
					
					edu.gmu.csiss.geoweaver.host.refreshHostList();
					
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
					    
					    edu.gmu.csiss.geoweaver.host.refreshHostList();

					});
					
				},
				
				buttons: [{
					
					label: "Run",
					
					action: function(dialog){
						
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
						
						edu.gmu.csiss.geoweaver.workflow.execute(id, mode, hosts);
						
						dialog.close();
						
					}
					
				}]
				
			});
			
		}).fail(function(jxr, status){
			
			alert("fail to get workflow details");
			
		});
		
	},
	
	/**
	 * Extend the list to original size
	 */
	extendList: function(shortpasswds, newhosts, hosts){
		
		var fullpasswdslist = [];
		
		for(var i=0;i<hosts.length;i++){
			
			var passwd = null;
			
			for(var j=0;j<newhosts.length;j++){
				
				if(newhosts[j].id==hosts[i].id){
					
					passwd = shortpasswds[j];
					
					break;
					
				}
				
			}
			
			fullpasswdslist.push(passwd);
			
		}
		
		return fullpasswdslist;
		
	},
	
	shrinkList: function(hosts){
		
		var newhosts = [];
		
		for(var i=0;i<hosts.length;i++){
			
			var exist = false;
			
			for(var j=0;j<newhosts.length;j++){
				
				if(hosts[i].id==newhosts[j].id){
					
					exist = true;
					
					break;
					
				}
				
			}
			
			if(!exist){
				
				newhosts.push(hosts[i]);
				
			}
			
		}
		
		return newhosts;
		
	},
	
	turnHosts2Ids: function(hosts){
		
		var ids = [];
		
		for(var i=0; i<hosts.length; i++){
			
			ids.push(hosts[i].id);
			
		}
		
		return ids;
		
	},
	
	/**
	 * Start to execute the workflow directly
	 */
	execute: function(wid, mode, hosts){
		
		////////////////////////// workflow execution
		
		var newhosts = this.shrinkList(hosts);
		
		var content = '<form>';
		
		for(var i=0;i<newhosts.length;i++){
			
			content += '   <div class="form-group row required">'+
		       '     <label for="host password" class="col-sm-4 col-form-label control-label">Host '+newhosts[i].name+' Password: </label>'+
		       '     <div class="col-sm-8">'+
		       '		<input type=\"password\" class=\"form-control\" id=\"inputpswd_'+i+'\" placeholder=\"Password\">'+
		       '     </div>'+
		       '   </div>';
		}
		
		content += "</form>";
		
		BootstrapDialog.show({
			
			title: "Host Password",
			
			closable: false,
			
			message: content,
			
			buttons: [{
				
         	label: 'Confirm',
             
             action: function(dialogItself){
             	
             	var $button = this;
             	
             	$button.spin();
             	
             	dialogItself.enableButtons(false);
             	
             	//Two-step encryption is applied here. 
             	//First, get public key from server.
             	//Second, encrypt the password and sent the encypted string to server. 
             	$.ajax({
             		
             		url: "key",
             		
             		type: "POST",
             		
             		data: ""
             		
             	}).done(function(msg){
             		
             		//encrypt the password using the received rsa key
             		
             		msg = $.parseJSON(msg);
             		
             		var encrypt = new JSEncrypt();
             		
                    encrypt.setPublicKey(msg.rsa_public);
                    
                    var shortpasswds = [];
                    
                    for(var i=0; i<newhosts.length; i++){

                        var encrypted = encrypt.encrypt($('#inputpswd_' + i).val());
                        
                        shortpasswds.push(encrypted);
                    	
                    }
                    
                    var passwds = edu.gmu.csiss.geoweaver.workflow.extendList(shortpasswds, newhosts, hosts);
                    
                    var ids = edu.gmu.csiss.geoweaver.workflow.turnHosts2Ids(hosts);
                    
                    var req = {
             				
             				id: wid, // workflow id
             				
             				mode: mode,
             				
             				hosts: ids, // host allocation
             				
             				passwords: passwds
             				
             		};
                    
             		$.ajax({
	        				
	        				url: "executeWorkflow",
	        				
	        				type: "POST",
	        				
	        				data: req
	        				
	        			}).done(function(msg){
	        				
	        				try{
	        					
	        					msg = $.parseJSON(msg);
		        				
		        				if(msg.ret == "success"){
		        					
		        					console.log("the workflow is under execution.");
		        					
		        					console.log("history id: " + msg.history_id);
		        					
		        					edu.gmu.csiss.geoweaver.process.showSSHOutputLog(msg); //use the same method as the single process
		        					
		        					if(edu.gmu.csiss.geoweaver.workflow.loaded_workflow!=null
		        							&&edu.gmu.csiss.geoweaver.workflow.loaded_workflow==wid){
		        						
			        					edu.gmu.csiss.geoweaver.monitor.startMonitor(msg.history_id);
			        					
		        					}
		        					
		        				}else if(msg.ret == "fail"){
		        					
		        					alert("Fail to execute the workflow.");
		        					
		        					console.error("fail to execute the workflow " + msg.reason);
		        					
		        				}else{
		        					
		        					console.log("other situation: " + msg);
		        					
		        				}
		        				
		        				dialogItself.close();
	        					
	        				}catch(e){
	        					
	        					$button.stopSpin();
		                		
		        				dialogItself.enableButtons(true);
		        				
		        				alert("fail to execute the workflow " + wid + ": " + e);
		        				
	        				}
	        				
	        			}).fail(function(jxr, status){
	        				
	        				alert("Error: unable to log on. Check if your password or the configuration of host is correct.");
	        				
	        				for(var i=0;i<newhosts.length;i++){

		        				$("#inputpswd_" + i).val("");
	        					
	        				}
	        				
	        				$button.stopSpin();
	                		
	        				dialogItself.enableButtons(true);
	                		
	        				console.error("fail to execute the process " + wid);
	        				
	        			});
             		
             	}).fail(function(jxr, status){
             		 
             		 console.error("fail to execute workflow");
             	});
             	
             }
				
			},{
				
				label: 'Cancel',
             
		        action: function(dialogItself){
		         	
		             dialogItself.close();
		             
		        }
				
			}]
			
		});
		
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
			
			var content = "<table class=\"table\"> "+
			"  <thead> "+
			"    <tr> "+
			"      <th scope=\"col\">Execution Id</th> "+
			"      <th scope=\"col\">Begin Time</th> "+
			"      <th scope=\"col\">Action</th> "+
			"    </tr> "+
			"  </thead> "+
			"  <tbody> ";

			
			for(var i=0;i<msg.length;i++){
				
				content += "    <tr> "+
					"      <td>"+msg[i].id+"</td> "+
					"      <td>"+msg[i].begin_time+"</td> "+
					"      <td><a href=\"javascript: edu.gmu.csiss.geoweaver.workflow.getHistoryDetails('"+msg[i].id+"')\">Check</a></td> "+
					"    </tr>";
				
			}
			
			content += "</tbody>";
			
			BootstrapDialog.show({
				
				title: "History",
				
				message: content,
				
				buttons: [{
					
					label: "Close",
					
					action: function(dialog){
						
						dialog.close();
						
					}
					
				}]
				
			});
			
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
			
			edu.gmu.csiss.geoweaver.process.getHistoryDetails(process_history_id);
			
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
			
			var content = "<table class=\"table\"> "+
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
					"      <td><a href=\"javascript: edu.gmu.csiss.geoweaver.process.getHistoryDetails('"+msg.output[i]+"')\">Check</a></td> "+
					"    </tr>";
				
			}
			
			BootstrapDialog.show({
				
				title: "Workflow Log",
				
				message: content,
				
				buttons: [{
					
					label: "Close",
					
					action: function(dialog){
						
						dialog.close();
					}
					
				}]
				
			});
			
		}).fail(function(){
			
			
		});
		
		
	},
	
	addMenuItem: function(one){
		
		$("#"+edu.gmu.csiss.geoweaver.menu.getPanelIdByType("workflow")).append("<li id=\"workflow-" + one.id + "\"><a href=\"javascript:void(0)\" onclick=\"edu.gmu.csiss.geoweaver.menu.details('"+one.id+"', 'workflow')\">" + 
	    		
				one.name + "</a> <i class=\"fa fa-history subalignicon\" onclick=\"edu.gmu.csiss.geoweaver.workflow.history('"+
	        	
				one.id+"', '" + one.name+"')\" data-toggle=\"tooltip\" title=\"List history logs\"></i> <i class=\"fa fa-plus subalignicon\" data-toggle=\"tooltip\" title=\"Show/Add this workflow\" onclick=\"edu.gmu.csiss.geoweaver.workflow.add('"+
	        	
				one.id+"')\"></i> <i class=\"fa fa-minus subalignicon\" data-toggle=\"tooltip\" title=\"Delete this workflow\" onclick=\"edu.gmu.csiss.geoweaver.menu.del('"+
	        	
				one.id+"','workflow')\"></i> <i class=\"fa fa-play subalignicon\" onclick=\"edu.gmu.csiss.geoweaver.workflow.run('"+
	        	
				one.id+"','"+one.name+"')\" data-toggle=\"tooltip\" title=\"Run Workflow\"></i> </li>");
		
	},
	
	list: function(msg){
		
		for(var i=0;i<msg.length;i++){
			
			this.addMenuItem(msg[i]);
			
			//this.addWorkspace(msg[i]);
			
		}
		
		$('#workflows').collapse("show");
		
	}
		
}