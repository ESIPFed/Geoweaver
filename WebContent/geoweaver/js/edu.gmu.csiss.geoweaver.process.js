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
		
		add: function(run){
			
			if(this.precheck()){
				
				var req = "type=process&lang="+$("#processcategory").val() + 
				
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
	                	
	                	$.ajax({
	        				
	        				url: "executeProcess",
	        				
	        				type: "POST",
	        				
	        				data: "processId=" + pid + "&hostId=" + hid + "&pswd=" + $("#inputpswd").val()
	        				
	        			}).done(function(msg){
	        				
	        				msg = $.parseJSON(msg);
	        				
	        				if(msg.ret == "success"){
	        					
	        					console.log("the process is successfully executed.");
	        					
	        					console.log(msg.output)
	        					
	        				}else if(msg.ret == "fail"){
	        					
	        					console.error("fail to execute the process " + msg.reason);
	        					
	        				}
	        				
	        			}).fail(function(jxr, status){
	        				
	        				console.error("fail to execute the process " + pid);
	        				
	        			});
	        			
	                    dialogItself.close();
	                    
	                }
					
				},{
					
	            	label: 'Cancel',
	                
	                action: function(dialogItself){
	                	
	                    dialogItself.close();
	                    
	                }
					
				}]
				
			});
			
		},
		
		runProcesses: function(pidList, pnameList){
			
//			var content = '<form>'+
//			   '   <div class="form-group row required">'+
//		       '     <label for="hostselector" class="col-sm-4 col-form-label control-label">Execute all processes on one host: </label>'+
//		       '     <div class="col-sm-8">'+
//		       '		<input class="form-check-input" type="checkbox" value="" id="allonone" />'+
//		       '     </div>'+
//		       '   </div>';
//			
//			for(var i=0;i<pidList.length;i++){
//				
//				content += '   <div class="form-group row required">'+
//			       '     <label for="host'+i+'selector" class="col-sm-4 col-form-label control-label">Run Process '+pnameList[i]+' on: </label>'+
//			       '     <div class="col-sm-8">'+
//			       '		<select class="form-control" id="host'+i+'selector" onchange="edu.gmu.csiss.geoweaver.host.checklive()">'+
//			       '  		</select>'+
//			       '     </div>'+
//			       '   </div>';
//				
//			}
//			
//			content += '</form>';
//			
//			BootstrapDialog.show({
//				
//				title: "Select a host",
//				
//				closable: false,
//				
//	            message: content,
//	            
//	            onshown: function(){
//	            	
//	            	$.ajax({
//	            		
//	            		url: "list",
//	            		
//	            		method: "POST",
//	            		
//	            		data: "type=host"
//	            		
//	            	}).done(function(msg){
//	            		
//	            		msg = $.parseJSON(msg);
//	            		
//	            		for(var i=0;i<pidList.length;i++){
//	            			
//	            			$("#host"+i+"selector").find('option').remove().end();
//		            		
//		            		for(var i=0;i<msg.length;i++){
//		            			
//		            			$("#host"+i+"selector").append("<option id=\""+msg[i].id+"\">"+msg[i].name+"</option>");
//		            			
//		            		}
//	            			
//	            		}
//	            		
//	            	}).fail(function(jxr, status){
//	    				
//	    				console.error("fail to list host");
//	    				
//	    			});
//	            	
//	            },
//	            
//	            buttons: [{
//		            
//	            	label: 'Execute',
//	                
//	                action: function(dialogItself){
//	                	
//	                	var hostid = $("#hostselector").children(":selected").attr("id");
//	                	
//	                	console.log("selected host: " + hostid);
//	                	
//	                	edu.gmu.csiss.geoweaver.process.executeWorkflow(pidList);
//	                	
//	                    dialogItself.close();
//	                    
//	                }
//	        
//	            },{
//		            
//	            	label: 'Cancel',
//	                
//	                action: function(dialogItself){
//	                	
//	                    dialogItself.close();
//	                    
//	                }
//	        
//	            }]
//	            
//			});
			
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