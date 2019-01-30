/**
*
* Geoweaver Process
* 
* @author Ziheng Sun
*
*/ 

edu.gmu.csiss.geoweaver.process = {
		
		editor: null,
		
		builtin_processes: [
			
			{"operation":"ShowResultMap", "params":[{"name":"resultfile", "min_occurs": 1, "max_occurs": 1}]}, //multiple occurs are something for later
			
			{"operation":"DownloadData", "params":[{"name":"file_url", "min_occurs": 1, "max_occurs": 1}]}
		
		],
		
		precheck: function(){
			
			var valid = false;
			
			if($("#processname").val()){
					
//					&&this.editor.getValue()){
				
				valid = true;
				
			}
			
			return valid;
			
		},
		
		showShell: function(code){
			
			$("#codearea").append('<textarea id="codeeditor" placeholder="Code goes here..."></textarea>');
			
        	//initiate the code editor
			
			edu.gmu.csiss.geoweaver.process.editor = CodeMirror.fromTextArea(document.getElementById("codeeditor"), {
        		
        		lineNumbers: true
        		
        	});
			
			if(code!=null){
				
            	edu.gmu.csiss.geoweaver.process.editor.setValue(edu.gmu.csiss.geoweaver.process.unescape(code));
            	
			}else {
			
				edu.gmu.csiss.geoweaver.process.editor.setValue("#!/bin/bash\n#write your bash script\n");
				
			}
        	
		},
		
		showBuiltinProcess: function(code){
			
			var cont = '     <label for="builtinprocess" class="col-sm-4 col-form-label control-label">Select a process: </label>'+
	       '     <div class="col-sm-8"> <select class="form-control" id="builtin_processes">';
			
			for(var i=0;i<edu.gmu.csiss.geoweaver.process.builtin_processes.length;i++){
				
				cont += '    		<option value="'+edu.gmu.csiss.geoweaver.process.builtin_processes[i].operation +
					'">'+edu.gmu.csiss.geoweaver.process.builtin_processes[i].operation + '</option>';
				
			}
			
		   	cont += '  		</select></div>';
		   	
		   	for(var i=0;i<edu.gmu.csiss.geoweaver.process.builtin_processes[0].params.length;i++){
				
				cont += '     <label for="parameter" class="col-sm-4 col-form-label control-label">Parameter <u>'+
				edu.gmu.csiss.geoweaver.process.builtin_processes[0].params[i].name+'</u>: </label>'+
				'     <div class="col-sm-8"> 	<input class="form-control parameter" id="param_'+
				edu.gmu.csiss.geoweaver.process.builtin_processes[0].params[i].name+'"></input>';
				cont += '</div>';
				
			}
			
			$("#codearea").append(cont);
			
			if(code!=null){
				
				code = $.parseJSON(code);
				
				$("#builtin_processes").val(code.operation);
				
				for(var i=0;i<code.params.length;i++){
					
					$("#param_" + code.params[i].name).val(code.params[i].value);
					
				}
				
			}
			
		},
		
		getCode: function(){
			
			var code = null;
			
			if($("#processcategory").val()=="shell"){
				
				code = edu.gmu.csiss.geoweaver.process.editor.getValue();
				
			}else if($("#processcategory").val()=="builtin"){
				
				var params = [];
				
				$(".parameter").each(function(){
					
					var newparam = {
							
							name: $(this).attr('id').split("param_")[1],
							
							value: $(this).val()
							
					}
					
					params.push(newparam);
					
				});
				
				code = {
						
						"operation" : $("#builtin_processes").val(),
						
						"params": params
						
				}
				
			}
			
			return code;
			
		},
		
		newDialog: function(){
			
			var content = edu.gmu.csiss.geoweaver.process.getProcessDialogTemplate();
			
			var dialog = new BootstrapDialog.show({
				
				title: "Add new process",
				
				closable: false,
				
	            message: content,
	            
	            cssClass: 'dialog-vertical-center',
	            
	            size: BootstrapDialog.SIZE_WIDE,
	            
	            onshown: function(){
	            	
	            	edu.gmu.csiss.geoweaver.process.showShell();
	            	
	            	$("#processcategory").on('change', function() {
	            		
	            		$("#codearea").empty();
	            		
	            		if( this.value == "shell"){
	            			
	    	            	edu.gmu.csiss.geoweaver.process.showShell();
	            			  
	            		}else if(this.value == "builtin"){
	            			
	            			edu.gmu.csiss.geoweaver.process.showBuiltinProcess();
	            			  
	            		}
	            		
	            	});
	            	
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
			
//			edu.gmu.csiss.geoweaver.menu.setFullScreen(dialog);
			
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
				
				console.error(status);
				
			});
			
		},
		
		getHistoryDetails: function(history_id){
			
			$.ajax({
				
				url: "log",
				
				method: "POST",
				
				data: "type=process&id=" + history_id
				
			}).done(function(msg){
				
				msg = $.parseJSON(msg);
				
				var output = msg.output;
				
				if(msg.output=="logfile"){
					
					output = "<div class=\"spinner-border\" role=\"status\"> "+
					"	  <span class=\"sr-only\">Loading...</span> "+
					"	</div>";
					
				}
				
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
				"	    <dd class=\"col col-md-7 word-wrap\" id=\"log-output\">"+output+"</dd>"+
				"	  </div>";
				
				BootstrapDialog.show({
					
					title: "Process Log",
					
					size: BootstrapDialog.SIZE_WIDE,
					
					message: content,
					
					onshown: function(){
						
						if(msg.output=="logfile"){
							
							$.get("../temp/" + msg.id + ".log" ).success(function(data){
								
								if(data!=null)
									$("#log-output").text(data);
								else
									$("#log-output").text("missing log");
								
							}).error(function(){
								
								$("#log-output").text("missing log");
								
							});
							
						}
						
					},
					
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
		
		getProcessDialogTemplate: function(){
			
			var content = '<form>'+
		       '   <div class="form-group row required">'+
		       '     <label for="processcategory" class="col-sm-4 col-form-label control-label">Your Process Type </label>'+
		       '     <div class="col-sm-8">'+
		       '		<select class="form-control" id="processcategory">'+
			   '    		<option value="shell">Shell</option>'+
			   '    		<option value="builtin">Built-In Process</option>'+
			   /*'    		<option value="python">Python</option>'+
			   '    		<option value="r">R</option>'+
			   '    		<option value="matlab">Matlab</option>'+*/
			   '  		</select>'+
		       '     </div>'+
		       '   </div>'+
		       '   <div class="form-group row required">'+
		       '     <label for="processname" class="col-sm-4 col-form-label control-label">Process Name </label>'+
		       '     <div class="col-sm-8">'+
		       '		<input class="form-control" id="processname"></input>'+
		       '     </div>'+
		       '   </div>'+
		       '   <div class="form-group row required" id="codearea">'+
		       
		       '   </div>'+
		       ' </form>';
			
			return content;
			
		},
		
		edit: function(pid){
			
			$.ajax({
				
				url: "detail",
				
				method: "POST",
				
				data: "type=process&id=" + pid
				
			}).done(function(msg){
				
				msg = $.parseJSON(msg);
				
				var content = edu.gmu.csiss.geoweaver.process.getProcessDialogTemplate();
				
				var dialog = new BootstrapDialog.show({
					
					title: "Edit process",
					
					closable: false,
					
					size: BootstrapDialog.SIZE_WIDE,
					
		            message: content,
		            
		            cssClass: 'dialog-vertical-center',
		            
		            onshown: function(){
		            	
		            	var old_name = msg.name;
		            	
		            	var old_lang = msg.description;
		            	
		            	var old_code = msg.code;
		            	
		            	$("#processcategory").val(old_lang);
		            	
		            	$("#processname").val(msg.name);

		            	$("#codearea").empty();
	            		
	            		if( old_lang == "shell"){
	            			
	    	            	edu.gmu.csiss.geoweaver.process.showShell(old_code);
	            			  
	            		}else if(old_lang == "builtin"){
	            			
	            			edu.gmu.csiss.geoweaver.process.showBuiltinProcess(old_code);
	            			  
	            		}
		            	
		            	$("#processcategory").on('change', function() {
		            		
		            		$("#codearea").empty();
		            		
		            		var old_code_new = null;
		            		
		            		if(this.value == old_lang){
		            			
		            			old_code_new = old_code;
		            			
		            		}
		            		
		            		if( this.value == "shell"){
		            			
		    	            	edu.gmu.csiss.geoweaver.process.showShell(old_code_new);
		            			  
		            		}else if(this.value == "builtin"){
		            			
		            			edu.gmu.csiss.geoweaver.process.showBuiltinProcess(old_code_new);
		            			
		            		}
		            		
		            	});
		            	
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
				
//				edu.gmu.csiss.geoweaver.menu.setFullScreen(dialog);
				
			}).fail(function(jxr, status){
				
				alert("Fail to get process details");
				
			});
			
		},
		
		/**
		 * add a new item under the process menu
		 */
		addMenuItem: function(one, folder){
			
			var menuItem = "<li class=\"process\" id=\"process-" + one.id + "\"><a href=\"javascript:void(0)\" onclick=\"edu.gmu.csiss.geoweaver.menu.details('"+one.id+"', 'process')\">" + 
    		
			one.name + "</a><i class=\"fa fa-history subalignicon\" onclick=\"edu.gmu.csiss.geoweaver.process.history('"+
        	
			one.id+"', '" + one.name+"')\" data-toggle=\"tooltip\" title=\"List history logs\"></i> <i class=\"fa fa-plus subalignicon\" data-toggle=\"tooltip\" title=\"Add an instance\" onclick=\"edu.gmu.csiss.geoweaver.workspace.theGraph.addProcess('"+
        	
			one.id+"','"+one.name+"')\"></i><i class=\"fa fa-minus subalignicon\" data-toggle=\"tooltip\" title=\"Delete this process\" onclick=\"edu.gmu.csiss.geoweaver.menu.del('"+
        	
			one.id+"','process')\"></i><i class=\"fa fa-edit subalignicon\" onclick=\"edu.gmu.csiss.geoweaver.process.edit('"+
        	
			one.id+"')\" data-toggle=\"tooltip\" title=\"Edit Process\"></i> <i class=\"fa fa-play subalignicon\" onclick=\"edu.gmu.csiss.geoweaver.process.runProcess('"+
        	
			one.id+"', '" + one.name + "', '" + one.desc +"')\" data-toggle=\"tooltip\" title=\"Run Process\"></i> </li>";
			
			if(folder!=null){
				
				var folder_ul = $("#process_folder_" + folder + "_target");
				
				if(!folder_ul.length){
					
					$("#"+edu.gmu.csiss.geoweaver.menu.getPanelIdByType("process"))
						.append("<li class=\"folder\" id=\"process_folder_"+ folder +"\" data-toggle=\"collapse\" data-target=\"#process_folder_"+ folder +"_target\"> "+
					    " <a href=\"javascript:void(0)\"> "+ folder +" </a>"+
					    " </li>"+
					    " <ul class=\"sub-menu collapse\" id=\"process_folder_"+ folder +"_target\"></ul>");
					
					folder_ul = $("#process_folder_" + folder + "_target");
					
				}
				
				folder_ul.append(menuItem)
				
			}else{
				
				$("#"+edu.gmu.csiss.geoweaver.menu.getPanelIdByType("process")).append(menuItem);
				
			}
			
			
			
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
				
				this.addMenuItem(msg[i], "public");
				
				//this.addWorkspace(msg[i]);
				
			}
			
			$('#processs').collapse("show");
			
		},
		
		update: function(pid){
			
			if(this.precheck()){
				
				var req =  { 
						
						type: "process", lang: $("#processcategory").val(),
						
						desc: $("#processcategory").val(), //use the description column to store the process type
					
						name: $("#processname").val(), 
						
						id: pid,
		    			
						code: edu.gmu.csiss.geoweaver.process.getCode()
						
					};
				
//				"type=process&lang="+$("#processcategory").val() + 
//				
//					"&name=" + $("#processname").val() + 
//					
//					"&id=" + pid +
//	    			
//		    		"&code=" + edu.gmu.csiss.geoweaver.process.editor.getValue();
		    	
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
				
//				var req = "type=process&lang="+$("#processcategory").val() +
//					
//					"&desc=" + $("#processcategory").val() + //use the description column to store the process type
//				
//					"&name=" + $("#processname").val() + 
//	    			
////		    		"&code=" + edu.gmu.csiss.geoweaver.process.editor.getValue();
//					"&code=" + edu.gmu.csiss.geoweaver.process.getCode()
					
				var req = { 
					
					type: "process", lang: $("#processcategory").val(),
					
					desc: $("#processcategory").val(), //use the description column to store the process type
				
					name: $("#processname").val(), 
	    			
					code: edu.gmu.csiss.geoweaver.process.getCode()
					
				};
		    	
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
		 * after the server side is done, this callback is called on each builtin process
		 */
		callback: function(msg){
			
			var oper = msg.operation;
			
			if(oper == "ShowResultMap"){
				
				//show the map
				edu.gmu.csiss.geoweaver.result.preview(msg.filename);
				
			}else if(oper == "DownloadData"){
				
				//download the map
				edu.gmu.csiss.geoweaver.result.download(msg.filename);
				
			}
			
		},
		
		executeCallback: function(encrypt, req, dialogItself, button){
			
			req.pswd = encrypt;
			
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
					
					if(req.desc == "builtin"){
						
						edu.gmu.csiss.geoweaver.monitor.startMonitor(msg.history_id); //"builtin" operation like Show() might need post action in the client
						
					}
					
				}else if(msg.ret == "fail"){
					
					alert("Fail to execute the process.");
					
					console.error("fail to execute the process " + msg.reason);
					
				}
				
				if(dialogItself) dialogItself.close();
				
			}).fail(function(jxr, status){
				
				alert("Error: unable to log on. Check if your password or the configuration of host is correct.");
				
				if($("#inputpswd").length) $("#inputpswd").val("");
				
				if(button) button.stopSpin();
	    		
				if(dialogItself) dialogItself.enableButtons(true);
	    		
				console.error("fail to execute the process " + req.processId);
				
			});
			
		},
		
		/**
		 * Execute one process
		 */
		executeProcess: function(pid, hid, desc){
			
            var req = {
		    		
		    		processId: pid,
		    		
		    		hostId: hid,
		    		
		    		desc: desc
		    		
		    }
			
			edu.gmu.csiss.geoweaver.host.start_auth_single(hid, req, edu.gmu.csiss.geoweaver.process.executeCallback );
			
//			var content = '<form>'+
//			   '   <div class="form-group row required">'+
//		       '     <label for="host password" class="col-sm-4 col-form-label control-label">Input Host User Password: </label>'+
//		       '     <div class="col-sm-8">'+
//		       '		<input type=\"password\" class=\"form-control\" id=\"inputpswd\" placeholder=\"Password\">'+
//		       '     </div>'+
//		       '   </div>';
//			
//			BootstrapDialog.show({
//				
//				title: "Host Password",
//				
//				closable: false,
//				
//				message: content,
//				
//				buttons: [{
//					
//	            	label: 'Confirm',
//	                
//	                action: function(dialogItself){
//	                	
//	                	var $button = this;
//	                	
//	                	$button.spin();
//	                	
//	                	dialogItself.enableButtons(false);
//	                	
//	                	//Two-step encryption is applied here. 
//	                	//First, get public key from server.
//	                	//Second, encrypt the password and sent the encypted string to server. 
//	                	$.ajax({
//	                		
//	                		url: "key",
//	                		
//	                		type: "POST",
//	                		
//	                		data: ""
//	                		
//	                	}).done(function(msg){
//	                		
//	                		//encrypt the password using the received rsa key
//	                		
//	                		msg = $.parseJSON(msg);
//	                		
//	                		var encrypt = new JSEncrypt();
//	                		
//	                        encrypt.setPublicKey(msg.rsa_public);
//	                        
//	                        var encrypted = encrypt.encrypt($('#inputpswd').val());
//	                        
//	                        var req = {
//	                        		
//	                        		processId: pid,
//	                        		
//	                        		hostId: hid,
//	                        		
//	                        		pswd: encrypted
//	                        		
//	                        }
//	                		
//	                		$.ajax({
//		        				
//		        				url: "executeProcess",
//		        				
//		        				type: "POST",
//		        				
//		        				data: req
//		        				
//		        			}).done(function(msg){
//		        				
//		        				msg = $.parseJSON(msg);
//		        				
//		        				if(msg.ret == "success"){
//		        					
//		        					console.log("the process is under execution.");
//		        					
//		        					console.log("history id: " + msg.history_id);
//		        					
//		        					edu.gmu.csiss.geoweaver.process.showSSHOutputLog(msg);
//		        					
//		        					if(desc == "builtin"){
//		        						
//		        						edu.gmu.csiss.geoweaver.monitor.startMonitor(msg.history_id); //"builtin" operation like Show() might need post action in the client
//		        						
//		        					}
//		        					
//		        				}else if(msg.ret == "fail"){
//		        					
//		        					alert("Fail to execute the process.");
//		        					
//		        					console.error("fail to execute the process " + msg.reason);
//		        					
//		        				}
//		        				
//	        					dialogItself.close();
//		        				
//		        			}).fail(function(jxr, status){
//		        				
//		        				alert("Error: unable to log on. Check if your password or the configuration of host is correct.");
//		        				
//		        				$("#inputpswd").val("");
//		        				
//		        				$button.stopSpin();
//		                		
//		        				dialogItself.enableButtons(true);
//		                		
//		        				console.error("fail to execute the process " + pid);
//		        				
//		        			});
//	                		
//	                	}).fail(function(jxr, status){
//	                		
//	                	});
//	                	
//	                }
//					
//				},{
//					
//	            	label: 'Cancel',
//	                
//	                action: function(dialogItself){
//	                	
//	                    dialogItself.close();
//	                    
//	                }
//					
//				}]
//				
//			});
			
		},
		
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
		
		runProcess: function(pid, pname, desc){
			
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
	                	
	                	edu.gmu.csiss.geoweaver.process.executeProcess(pid, hostid, desc);
	                	
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