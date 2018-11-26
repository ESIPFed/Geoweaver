/**
*
* Geoweaver Process
* 
* @author Ziheng Sun
*
*/ 

edu.gmu.csiss.geoweaver.process = {
		
		editor: null,
		
		precheck: function(){
			
			var valid = false;
			
			if($("#processname").val()&&this.editor.getValue()){
				
				valid = true;
				
			}
			
			return valid;
			
		},
		
		newDialog: function(){
			
			var content = '<form>'+
		       '   <div class="form-group row required">'+
		       '     <label for="processcategory" class="col-sm-4 col-form-label control-label">Your Process Type </label>'+
		       '     <div class="col-sm-8">'+
		       '		<select class="form-control" id="processcategory">'+
			   '    		<option>Shell</option>'+
			   /*'    		<option>Python</option>'+
			   '    		<option>R</option>'+
			   '    		<option>Matlab</option>'+*/
			   '  		</select>'+
		       '     </div>'+
		       '   </div>'+
		       '   <div class="form-group row required">'+
		       '     <label for="processname" class="col-sm-4 col-form-label control-label">Process Name </label>'+
		       '     <div class="col-sm-8">'+
		       '		<input class="form-control" id="processname"></input>'+
		       '     </div>'+
		       '   </div>'+
		       '   <div class="form-group row required" >'+
		       '	 <textarea  id="codeeditor" placeholder="Code goes here..."></textarea>'+
		       '   </div>'+
		       ' </form>';
			
			var dialog = new BootstrapDialog.show({
				
				title: "Add new process",
				
				closable: false,
				
	            message: content,
	            
	            cssClass: 'dialog-vertical-center',
	            
	            onshown: function(){
	            	
	            	//initiate the code editor
	            	
	            	edu.gmu.csiss.geoweaver.process.editor = CodeMirror.fromTextArea(document.getElementById("codeeditor"), {
	            		
	            		lineNumbers: true
	            		
	            	});
	            	
	            	edu.gmu.csiss.geoweaver.process.editor.setValue("#!/bin/bash\n#write your bash script\n");
	            	
	            },
	            
	            buttons: [{
	            
	            	label: 'Add',
	                
	                action: function(dialogItself){
	                	
	                	edu.gmu.csiss.geoweaver.process.add(false);
	                	
	                    dialogItself.close();
	                    
	                }
	        
	            },{
//	            
//	            	label: 'Run',
//	                
//	                action: function(dialogItself){
//	                	
//	                	edu.gmu.csiss.geoweaver.process.add(true);
//	                	
//	                    dialogItself.close();
//	                    
//	                }
//	        
//	            }, {
	            
	            	label: 'Close',
	                
	                action: function(dialogItself){
	                	
	                    dialogItself.close();
	                    
	                }
	        
	            }]
				
			});
			
			edu.gmu.csiss.geoweaver.menu.setFullScreen(dialog);
			
		},
		
		/**
		 * list all the history execution of the process
		 */
		history: function(pid, pname){
			
			$.ajax({
				
				url: "logs",
				
				method: "POST",
				
				data: "type=process&id=" + pid
				
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
						"      <td><a href=\"javascript: edu.gmu.csiss.geoweaver.process.getHistoryDetails('"+msg[i].id+"')\">Check</a></td> "+
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
				
				
			});
			
		},
		
		getHistoryDetails: function(history_id){
			
			$.ajax({
				
				url: "log",
				
				method: "POST",
				
				data: "type=process&id=" + history_id
				
			}).done(function(msg){
				
				msg = $.parseJSON(msg);
				
				var content = "<div class=\"form-group row\"> "+
				"	    <dt class=\"col col-md-3\">Log Id</dt>"+
				"	    <dd class=\"col col-md-7\">"+msg.id+"</dd>"+
				"	  </div>"+
				"<div class=\"form-group row\"> "+
				"	    <dt class=\"col col-md-3\">Process Id</dt>"+
				"	    <dd class=\"col col-md-7\">"+msg.process+"</dd>"+
				"	  </div>"+
				"<div class=\"form-group row\"> "+
				"	    <dt class=\"col col-md-3\">Begin Time</dt>"+
				"	    <dd class=\"col col-md-7\">"+msg.begin_time+"</dd>"+
				"	  </div>"+
				"<div class=\"form-group row\"> "+
				"	    <dt class=\"col col-md-3\">End Time</dt>"+
				"	    <dd class=\"col col-md-7\">"+msg.end_time+"</dd>"+
				"	  </div>"+
				"<div class=\"form-group row\"> "+
				"	    <dt class=\"col col-md-3\">Input</dt>"+
				"	    <dd class=\"col col-md-7\">"+msg.input+"</dd>"+
				"	  </div>"+
				"<div class=\"form-group row\"> "+
				"	    <dt class=\"col col-md-3\">Output</dt>"+
				"	    <dd class=\"col col-md-7\">"+msg.output+"</dd>"+
				"	  </div>";
				
				BootstrapDialog.show({
					
					title: "Process Log",
					
					message: content,
					
					buttons: [{
						
						label: "Retrieve Result",
						
						action: function(dialog){
							
							edu.gmu.csiss.geoweaver.result.showDialog(history_id);
							
						}
						
					},{
						
						label: "Close",
						
						action: function(dialog){
							
							dialog.close();
						}
						
					}]
					
				});
				
			}).fail(function(){
				
				
			});
			
		},
		
		unescape: function(code){
			
			String.prototype.replaceAll = function(search, replacement) {
			    var target = this;
			    return target.replace(new RegExp(search, 'g'), replacement);
			};
			
			code = code.replaceAll("<br/>", "\n");
			
			return code;
			
		},
		
		edit: function(pid){
			
			$.ajax({
				
				url: "detail",
				
				method: "POST",
				
				data: "type=process&id=" + pid
				
			}).done(function(msg){
				
				msg = $.parseJSON(msg);
				
				var content = '<form>'+
			       '   <div class="form-group row required">'+
			       '     <label for="processcategory" class="col-sm-4 col-form-label control-label">Your Process Type </label>'+
			       '     <div class="col-sm-8">'+
			       '		<select class="form-control" id="processcategory">'+
				   '    		<option>Shell</option>'+
				   /*'    		<option>Python</option>'+
				   '    		<option>R</option>'+
				   '    		<option>Matlab</option>'+*/
				   '  		</select>'+
			       '     </div>'+
			       '   </div>'+
			       '   <div class="form-group row required">'+
			       '     <label for="processname" class="col-sm-4 col-form-label control-label">Process Name </label>'+
			       '     <div class="col-sm-8">'+
			       '		<input class="form-control" id="processname" ></input>'+
			       '     </div>'+
			       '   </div>'+
			       '   <div class="form-group row required" >'+
			       '	 <textarea  id="codeeditor" placeholder="Code goes here..." ></textarea>'+
			       '   </div>'+
			       ' </form>';
				
				var dialog = new BootstrapDialog.show({
					
					title: "Edit process",
					
					closable: false,
					
		            message: content,
		            
		            cssClass: 'dialog-vertical-center',
		            
		            onshown: function(){
		            	
		            	//initiate the code editor
		            	
		            	edu.gmu.csiss.geoweaver.process.editor = CodeMirror.fromTextArea(document.getElementById("codeeditor"), {
		            		
		            		lineNumbers: true
		            		
		            	});
		            	
		            	$("#processname").val(msg.name);
		            	
		            	edu.gmu.csiss.geoweaver.process.editor.setValue(edu.gmu.csiss.geoweaver.process.unescape(msg.code));
		            	
		            },
		            
		            buttons: [{
		            
		            	label: 'Update',
		                
		                action: function(dialogItself){
		                	
		                	edu.gmu.csiss.geoweaver.process.update(msg.id);
		                	
		                    dialogItself.close();
		                    
		                }
		        
		            },{
		            	
		            	label: 'Close',
		                
		                action: function(dialogItself){
		                	
		                    dialogItself.close();
		                    
		                }
		        
		            }]
					
				});
				
				edu.gmu.csiss.geoweaver.menu.setFullScreen(dialog);
				
			}).fail(function(jxr, status){
				
				alert("Fail to get process details");
				
			});
			
		},
		
		/**
		 * add a new item under the process menu
		 */
		addMenuItem: function(one){
			
			$("#"+edu.gmu.csiss.geoweaver.menu.getPanelIdByType("process")).append("<li id=\"process-" + one.id + "\"><a href=\"javascript:void(0)\" onclick=\"edu.gmu.csiss.geoweaver.menu.details('"+one.id+"', 'process')\">" + 
    		
				one.name + "</a><i class=\"fa fa-history subalignicon\" onclick=\"edu.gmu.csiss.geoweaver.process.history('"+
	        	
				one.id+"', '" + one.name+"')\" data-toggle=\"tooltip\" title=\"List history logs\"></i> <i class=\"fa fa-plus subalignicon\" data-toggle=\"tooltip\" title=\"Add an instance\" onclick=\"edu.gmu.csiss.geoweaver.workspace.theGraph.addProcess('"+
	        	
				one.id+"','"+one.name+"')\"></i><i class=\"fa fa-minus subalignicon\" data-toggle=\"tooltip\" title=\"Delete this process\" onclick=\"edu.gmu.csiss.geoweaver.menu.del('"+
	        	
				one.id+"','process')\"></i><i class=\"fa fa-edit subalignicon\" onclick=\"edu.gmu.csiss.geoweaver.process.edit('"+
	        	
				one.id+"')\" data-toggle=\"tooltip\" title=\"Edit Process\"></i> <i class=\"fa fa-play subalignicon\" onclick=\"edu.gmu.csiss.geoweaver.process.runProcess('"+
	        	
				one.id+"', '" + one.name+"')\" data-toggle=\"tooltip\" title=\"Run Process\"></i> </li>");
			
		},
		
		/**
		 * add process object to workspace
		 */
		addWorkspace: function(one){
			
			//randomly put a new object to the blank space
			
			var instanceid = edu.gmu.csiss.geoweaver.workspace.theGraph.addProcess(one.id, one.name);
			
		},
		
		list: function(msg){
			
			for(var i=0;i<msg.length;i++){
				
				this.addMenuItem(msg[i]);
				
				//this.addWorkspace(msg[i]);
				
			}
			
			$('#processs').collapse("show");
			
		},
		
		update: function(pid){
			
			if(this.precheck()){
				
				var req = "type=process&lang="+$("#processcategory").val() + 
				
					"&name=" + $("#processname").val() + 
					
					"&id=" + pid +
	    			
		    		"&code=" + edu.gmu.csiss.geoweaver.process.editor.getValue();
		    	
		    	$.ajax({
		    		
		    		url: "edit",
		    		
		    		method: "POST",
		    		
		    		data: req
		    		
		    	}).done(function(msg){
		    		
		    		msg = $.parseJSON(msg);
		    		
		    		alert("Updated!!");
		    		
		    	}).fail(function(jqXHR, textStatus){
		    		
		    		alert("Fail to update the process.");
		    		
		    	});
				
			}else{
				
				alert("Process name and code must be non-empty!");
				
			}
		},
		
		add: function(run){
			
			if(this.precheck()){
				
				var req = "type=process&lang="+$("#processcategory").val() +
					
					"&desc=" + $("#processcategory").val() + //use the description column to store the process type
				
					"&name=" + $("#processname").val() + 
	    			
		    		"&code=" + edu.gmu.csiss.geoweaver.process.editor.getValue();
		    	
		    	$.ajax({
		    		
		    		url: "add",
		    		
		    		method: "POST",
		    		
		    		data: req
		    		
		    	}).done(function(msg){
		    		
		    		msg = $.parseJSON(msg);
		    		
		    		edu.gmu.csiss.geoweaver.process.addMenuItem(msg);
		    		
		    		if(run)
		    				
		    			edu.gmu.csiss.geoweaver.process.run(msg.id);
		    				
		    		
		    	}).fail(function(jqXHR, textStatus){
		    		
		    		alert("Fail to add the process.");
		    		
		    	});
				
			}else{
				
				alert("Process name and code must be non-empty!");
				
			}
			
		},
		
		/**
		 * create a WebSocket-based dialog for outputting the log of Bash scripts
		 */
		showSSHOutputLog: function(msg){
			
			edu.gmu.csiss.geoweaver.ssh.openLog(msg.token);
			
		},
		
		/**
		 * Execute one process
		 */
		executeProcess: function(pid, hid){
			
			var content = '<form>'+
			   '   <div class="form-group row required">'+
		       '     <label for="host password" class="col-sm-4 col-form-label control-label">Input Host User Password: </label>'+
		       '     <div class="col-sm-8">'+
		       '		<input type=\"password\" class=\"form-control\" id=\"inputpswd\" placeholder=\"Password\">'+
		       '     </div>'+
		       '   </div>';
			
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
	                        
	                        var encrypted = encrypt.encrypt($('#inputpswd').val());
	                        
	                        var req = {
	                        		
	                        		processId: pid,
	                        		
	                        		hostId: hid,
	                        		
	                        		pswd: encrypted
	                        		
	                        }
	                		
	                		$.ajax({
		        				
		        				url: "executeProcess",
		        				
		        				type: "POST",
		        				
		        				data: req
		        				
		        			}).done(function(msg){
		        				
		        				msg = $.parseJSON(msg);
		        				
		        				if(msg.ret == "success"){
		        					
		        					console.log("the process is under execution.");
		        					
		        					console.log("history id: " + msg.history_id);
		        					
		        					edu.gmu.csiss.geoweaver.process.showSSHOutputLog(msg);
		        					
		        				}else if(msg.ret == "fail"){
		        					
		        					alert("Fail to execute the process.");
		        					
		        					console.error("fail to execute the process " + msg.reason);
		        					
		        				}
		        				
	        					dialogItself.close();
		        				
		        			}).fail(function(jxr, status){
		        				
		        				alert("Error: unable to log on. Check if your password or the configuration of host is correct.");
		        				
		        				$("#inputpswd").val("");
		        				
		        				$button.stopSpin();
		                		
		        				dialogItself.enableButtons(true);
		                		
		        				console.error("fail to execute the process " + pid);
		        				
		        			});
	                		
	                	}).fail(function(jxr, status){
	                		
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
		
//		hostcallback: function(live, ){
//			
//			
//			
//		},
//		
//		checkhost: function(){
//			
//			var id = $(this).attr("id");
//			
//			console.log("the select id is " + id);
//			
//			var selectedhostid = $(this).find(":selected").attr("id");
//			
//			edu.gmu.csiss.geoweaver.host.checklive(selectedhostid, hostcallback);
//			
//		},
		
		runProcess: function(pid, pname){
			
			//select a host
			
			var content = '<form>'+
		       '   <div class="form-group row required">'+
		       '     <label for="hostselector" class="col-sm-4 col-form-label control-label">Run Process '+pname+' on: </label>'+
		       '     <div class="col-sm-8">'+
		       '		<select class="form-control" id="hostselector" >'+
		       '  		</select>'+
		       '     </div>'+
		       '   </div>'+
		       '</form>';
			
			BootstrapDialog.show({
				
				title: "Select a host",
				
				closable: false,
				
	            message: content,
	            
	            onshown: function(){
	            	
	            	$.ajax({
	            		
	            		url: "list",
	            		
	            		method: "POST",
	            		
	            		data: "type=host"
	            		
	            	}).done(function(msg){
	            		
	            		msg = $.parseJSON(msg);
	            		
	            		$("#hostselector").find('option').remove().end();
	            		
	            		for(var i=0;i<msg.length;i++){
	            			
	            			$("#hostselector").append("<option id=\""+msg[i].id+"\">"+msg[i].name+"</option>");
	            			
	            		}
	            		
	            	}).fail(function(jxr, status){
	    				
	    				console.error("fail to list host");
	    				
	    			});
	            	
	            },
	            
	            buttons: [{
		            
	            	label: 'Execute',
	                
	                action: function(dialogItself){
	                	
	                	var hostid = $("#hostselector").children(":selected").attr("id");
	                	
	                	console.log("selected host: " + hostid);
	                	
	                	edu.gmu.csiss.geoweaver.process.executeProcess(pid, hostid);
	                	
	                    dialogItself.close();
	                    
	                }
	        
	            },{
		            
	            	label: 'Cancel',
	                
	                action: function(dialogItself){
	                	
	                    dialogItself.close();
	                    
	                }
	        
	            }]
	            
			});
			
		}
		
}