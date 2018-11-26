

edu.gmu.csiss.geoweaver.host = {
		

		closeSSH: function(token){
			
			$.ajax({
				
				url: "geoweaver-ssh-logout-inbox",
				
				method: "POST",
				
				data: "token=" + token
				
			}).done(function(msg){
				
				if(msg == "done"){
				
					console.log("SSH session is closed.");
					
				}else{
					
					console.error("Fail to close SSH.");
					
				}
				
			}).fail(function(){
				
				console.error("Fail to close SSH.");
				
			});
			
		},

		showSSHCmd: function(token){
			
			var dialog = new BootstrapDialog.show({
	            
				title: 'SSH Command Line',
	            
	            message: "<iframe src=\"geoweaver-ssh?token="+token+"\" style=\"height:100%;width:100%;\"></iframe>",
				
//				message: "<iframe src=\"geoweaver-ssh\" style=\"height:100%;width:100%;\"></iframe>",
	            
	            size: 'size-large',
	            
	            onhide: function(dialogRef){
	                
//	            	edu.gmu.csiss.geoweaver.menu.closeSSH(token);
	                
	            },
	            
	            closable: false,
	            
	            buttons: [{
	            	
	            	label: 'Create Process',
	            	
	            	action: function(dialog){
	            		
	            		console.log("not ready yet");
	            		
	            	}
	            	
	            },{
	                
	            	label: 'Close Connection',
	                
	                action: function(dialog) {
	                	
	                	edu.gmu.csiss.geoweaver.host.closeSSH(token);
	                	
	                	dialog.close();
	                	
	                }
	            }]
	        
			});
			
			edu.gmu.csiss.geoweaver.menu.setFullScreen(dialog);
			
			
		},
		
		
		openssh: function(hostid){
			
			//get the host information
			
			$.ajax({
				
				url: "detail",
				
				method: "POST",
				
				data: "type=host&id=" + hostid
				
			}).done(function(msg){
				
				//open the login page
				
				msg = $.parseJSON(msg);

				var cont = "<div class=\"row\">";
				
				cont += "<div class=\"col col-md-5\">IP</div><div class=\"col col-md-5\">" + msg.ip + "</div>";
				
				cont += "<div class=\"col col-md-5\">Port</div><div class=\"col col-md-5\">" + msg.port + "</div>";
				
				cont += "<div class=\"col col-md-5\">User</div><div class=\"col col-md-5\">" + msg.user + "</div>";
				
				cont += "<div class=\"col col-md-5\">Password</div><div class=\"col col-md-5\"><input type=\"password\" class=\"form-control\" id=\"inputpswd\" placeholder=\"Password\"></div>";
								
				cont += "</div>";
				
				BootstrapDialog.show({
		            
					title: 'Open SSH session',
		            
		            message: cont,
		            
		            closable: false,
		            
		            buttons: [{
		                
		            	label: 'Connect',
		                
		                action: function(dialog) {
		                	
		                	var $button = this;
		                	
		                	$button.spin();
		                	
		                    dialog.enableButtons(false);
		                	
		                	var req = "host=" + msg.ip + 
		                		
		                		"&port=" + msg.port + 
		                		
		                		"&username=" + msg.user + 
		                		
		                		"&password=" + $("#inputpswd").val();
		                	
		                	$.ajax({
		                		
		                		url: "geoweaver-ssh-login-inbox",
		                		
		                		method: "POST",
		                		
		                		data: req
		                		
		                	}).done(function(msg){
		                		
		                		msg = $.parseJSON(msg);
		                		
		                		if(msg.token!=null){
		                			
			                		//open a dialog to show the SSH command line interface

			                		edu.gmu.csiss.geoweaver.host.showSSHCmd(msg.token);
		                			
		                		}else{
		                			
		                			alert("Fail to open SSH session");
		                			
		                		}
			                	dialog.close();
		                		
		                	}).fail(function(status){
		                		
		                		alert("Fail to open SSH session" + status);
		                		
		                		$button.stopSpin();
		                		
		                		dialog.enableButtons(true);
		                		
		                	});
		                	
		                	
		                }
		            }, {
		            	
		                label: 'Cancel',
		                
		                action: function(dialog) {
		                
		                	dialog.close();
		                
		                }
		            
		            }]
		        
				});
				
			});
			
		},
		
		refreshHostList: function(){
			
			$.ajax({
        		
        		url: "list",
        		
        		method: "POST",
        		
        		data: "type=host"
        		
        	}).done(function(msg){
        		
        		msg = $.parseJSON(msg);
        		
        		$(".hostselector").find('option').remove().end();
        		
        		for(var i=0;i<msg.length;i++){
        			
        			$(".hostselector").append("<option id=\""+msg[i].id+"\">"+msg[i].name+"</option>");
        			
        		}
        		
        	}).fail(function(jxr, status){
				
				console.error("fail to list host");
				
			});
			
		},
		
		addMenuItem: function(one){
			
			$("#"+edu.gmu.csiss.geoweaver.menu.getPanelIdByType("host")).append("<li id=\"host-" + one.id + 
					
				"\"><a href=\"javascript:void(0)\" onclick=\"edu.gmu.csiss.geoweaver.menu.details('"+one.id+"', 'host')\">" + 
    				
				one.name + "</a> <i class=\"fa fa-external-link-square subalignicon\" onclick=\"edu.gmu.csiss.geoweaver.host.openssh('"+
            				
				one.id + "')\" data-toggle=\"tooltip\" title=\"Connect SSH\"></i> <i class=\"fa fa-minus subalignicon\" data-toggle=\"tooltip\" title=\"Delete this host\" onclick=\"edu.gmu.csiss.geoweaver.menu.del('" +
            				
				one.id+"','host')\"></i> </li>");
			
		},
		
		list: function(msg){
			
			for(var i=0;i<msg.length;i++){
				
				this.addMenuItem(msg[i]);
				
			}
			
			$('#hosts').collapse("show");
			
		},
		
//		validateIP: function(value){
//			
//			var ip = "^(?:(?:25[0-5]2[0-4][0-9][01]?[0-9][0-9]?)\.){3}(?:25[0-5]2[0-4][0-9][01]?[0-9][0-9]?)$";
//			
//            return value.match(ip);
//			
//		},
		
		validateIP: function(ipaddress) {  
			
			var valid = false;
		  
			if (/^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$/.test(ipaddress)) {  
		  
				valid =  true  
		  
			}else{
				
				alert("You have entered an invalid IP address!")  
				
			}  
			
			return valid;
		  
		},  
		
		precheck: function(){
			
			var valid = false;
			
			if($("#hostname").val()&&$("#hostip").val()&&$("#hostport").val()&&$("#username").val()
					&&this.validateIP($("#hostip").val())&&$.isNumeric($("#hostport").val())){
				
				valid = true;
				
			}
			
			return valid;
			
		},
		
		add: function(callback){
			
			if(this.precheck()){
				
				var req = "type=host&hostname="+$("#hostname").val() + 
	    		
		    		"&hostip=" + $("#hostip").val() +
		    		
		    		"&hostport=" + $("#hostport").val() + 
		    		
		    		"&username=" + $("#username").val();
		    	
		    	$.ajax({
		    		
		    		url: "add",
		    		
		    		method: "POST",
		    		
		    		data: req
		    		
		    	}).done(function(msg){
		    		
		    		msg = $.parseJSON(msg);
		    		
		    		edu.gmu.csiss.geoweaver.host.addMenuItem(msg);
		    		
		    		callback();
		    		
		    	}).fail(function(jqXHR, textStatus){
		    		
		    		alert("Fail to add the host.");
		    		
		    	});
				
			}else{
				
				alert("Invalid input");
				
			}
			
			
			
		},
		
		newDialog: function(){
			
			BootstrapDialog.show({
				
				title: "Add new host",
				
	            message: '<form>'+
				       '   <div class="form-group row required">'+
				       '     <label for="hostname" class="col-sm-2 col-form-label control-label">Host Name </label>'+
				       '     <div class="col-sm-10">'+
				       '       <input type="text" class="form-control" id="hostname" value="New Host">'+
				       '     </div>'+
				       '   </div>'+
				       '   <div class="form-group row required">'+
				       '     <label for="hostip" class="col-sm-2 col-form-label control-label">Hose IP</label>'+
				       '     <div class="col-sm-10">'+
				       '       <input type="text" class="form-control" id="hostip" placeholder="Host IP">'+
				       '     </div>'+
				       '   </div>'+
				       '   <div class="form-group row required">'+
				       '     <label for="hostport" class="col-sm-2 col-form-label control-label">Port</label>'+
				       '     <div class="col-sm-10">'+
				       '       <input type="text" class="form-control" id="hostport" placeholder="">'+
				       '     </div>'+
				       '   </div>'+
				       '   <div class="form-group row required">'+
				       '     <label for="username" class="col-sm-2 col-form-label control-label">User Name</label>'+
				       '     <div class="col-sm-10">'+
				       '       <input type="text" class="form-control" id="username" placeholder="">'+
				       '     </div>'+
				       '   </div>'+
				       ' </form>',
	            
	            cssClass: 'dialog-vertical-center',
	            
	            buttons: [{
	            	
	                label: 'Add',
	                
	                action: function(dialogItself){
	                	
	                	edu.gmu.csiss.geoweaver.host.add(function(){
	                		
		                    dialogItself.close();
		                    
	                	});
	                	
	                }
	            
	            },{
	            
	            	label: 'Close',
	                
	                action: function(dialogItself){
	                	
	                    dialogItself.close();
	                    
	                }
	        
	            }]
			
	        });
			
		},
		
}