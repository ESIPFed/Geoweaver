/**
 * 
 * author: Ziheng Sun
 * 
 */
edu.gmu.csiss.geoweaver.host = {
		
		cred_cache: [{"h":"xxxx", "s": "yyyyy"}],
		
		setCache: function(hid, s){
			
			var is = false;
			
			for(var i=0;i<edu.gmu.csiss.geoweaver.host.cred_cache.length;i++){
				
				if(edu.gmu.csiss.geoweaver.host.cred_cache[i].h == hid){
					
					edu.gmu.csiss.geoweaver.host.cred_cache[i].s = s;
					
					is = true;
					
					break;
					
				}
				
			}
			
			if(!is){
				
				edu.gmu.csiss.geoweaver.host.cred_cache.push({"h": hid, "s": s});
				
			}
			
		},
		
		findCache: function(hid){
			
			var s = null;
			
			for(var i=0;i<edu.gmu.csiss.geoweaver.host.cred_cache.length;i++){
				
				if(edu.gmu.csiss.geoweaver.host.cred_cache[i].h == hid){
					
					s = edu.gmu.csiss.geoweaver.host.cred_cache[i].s;
					
					break;
					
				}
				
			}
			
			return s;
			
		},
		
		encrypt: function(hid, pstext, req, dialog, button, business_callback){
			
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
                
                var encrypted = encrypt.encrypt(pstext);
                
//                msg.pswd = encrypted;
                
                business_callback(encrypted, req, dialog, button);
        		
        	}).fail(function(jxr, status){
        		
        	});
			
		},
		
		
		enter_password: function(hid, req, business_callback){
			
			var content = '<form>'+
			   '   <div class="form-group row required">'+
		       '     <label for="host password" class="col-sm-4 col-form-label control-label">Input Host User Password: </label>'+
		       '     <div class="col-sm-8">'+
		       '		<input type=\"password\" class=\"form-control\" id=\"inputpswd\" placeholder=\"Password\">'+
		       '     </div>'+
		       '     <div class="col-sm-12 form-check">'+
		       '		<input type="checkbox" class="form-check-input" id="remember">'+
		       '		<label class="form-check-label" for="remember">Remember password</label>'+
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
	                	
	                	if(document.getElementById('remember').checked) {
	                	    
	                		edu.gmu.csiss.geoweaver.host.setCache(hid, $('#inputpswd').val()); //remember s
	                		
	                	}
	                	
	                	edu.gmu.csiss.geoweaver.host.encrypt(hid, $('#inputpswd').val(), req, dialogItself, $button, business_callback)
	                	
	                }
					
				},{
					
	            	label: 'Cancel',
	                
	                action: function(dialogItself){
	                	
	                    dialogItself.close();
	                    
	                }
					
				}]
				
			});
			
			
		},
		
		start_auth_single: function(hid, req, business_callback){
			
			var s = edu.gmu.csiss.geoweaver.host.findCache(hid);
			
			if(s==null){
				
				edu.gmu.csiss.geoweaver.host.enter_password(hid, req, business_callback);
				
			}else{
				
				edu.gmu.csiss.geoweaver.host.encrypt(hid, s, req, null, null, business_callback);
				
			}
			
		},
		
		encrypt_m : function(hosts, pswds, req, dialogItself, button, business_callback){
			
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
                
                var encrypt_passwds = [];
                
                for(var i=0; i<hosts.length; i++){

                    var encrypted = encrypt.encrypt(pswds[i]);//$('#inputpswd_' + i).val());
                    
                    encrypt_passwds.push(encrypted);
                	
                }
                
                var ids = edu.gmu.csiss.geoweaver.host.turnHosts2Ids(hosts);
                
                req.hosts = ids;
                
                req.passwords = encrypt_passwds;
                
                business_callback(req, dialogItself, button);
                
         	}).fail(function(jxr, status){
         		 
         		console.error("fail to execute workflow");
         		
         	});
			
		},
		
		enter_pswd_m : function(newhosts, hosts, req, business_callback){
			
			var content = '<form>';
			
			for(var i=0;i<newhosts.length;i++){
				
				content += '   <div class="form-group row required">'+
			       '     <label for="host password" class="col-sm-4 col-form-label control-label">Host '+newhosts[i].name+' Password: </label>'+
			       '     <div class="col-sm-8">'+
			       '		<input type=\"password\" class=\"form-control\" id=\"inputpswd_'+i+'\" required=\"true\" placeholder=\"Password\">'+
			       '     </div>'+
			       '   </div>';
			}
			
			content += '     <div class="form-group row form-check">'+
		       '		<input type="checkbox" class="form-check-input" id="remember">'+
		       '		<label class="form-check-label" for="remember">Remember password</label>'+
		       '     </div>';
			
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
	             	
	             	var shortpasswds = [];
	             	
	             	for(var i=0;i<newhosts.length;i++){
	             		
	             		shortpasswds.push($("#inputpswd_" + i).val());
	             		
	             		if(document.getElementById('remember').checked) {
	             			
	             			edu.gmu.csiss.geoweaver.host.setCache(newhosts[i].id, $("#inputpswd_" + i).val());
	             			
	             		}
	             		
	             	}
	             	
	             	var passwds = edu.gmu.csiss.geoweaver.host.extendList(shortpasswds, newhosts, hosts);
	             	
	             	edu.gmu.csiss.geoweaver.host.encrypt_m(hosts, passwds, req, dialogItself, $button, business_callback);
	             	
	             }
					
				},{
					
					label: 'Cancel',
	             
			        action: function(dialogItself){
			         	
			             dialogItself.close();
			             
			        }
					
				}]
				
			});
			
		},
		
		start_auth_multiple: function(hosts, req, business_callback){
			
			var newhosts = this.shrinkList(hosts);
			
			if(newhosts.length>0){
				
				edu.gmu.csiss.geoweaver.host.enter_pswd_m(newhosts, hosts, req, business_callback);
				
			}else{
				
				var passwds = edu.gmu.csiss.geoweaver.host.extendList([], newhosts, hosts);
				
				edu.gmu.csiss.geoweaver.host.encrypt_m(hosts, passwds, req, null, null, business_callback);
			}
			
			
			
		},
		
		turnHosts2Ids: function(hosts){
			
			var ids = [];
			
			for(var i=0; i<hosts.length; i++){
				
				ids.push(hosts[i].id);
				
			}
			
			return ids;
			
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
				
				if(passwd!=null)
				
					fullpasswdslist.push(passwd);
				
				else
					
					fullpasswdslist.push(edu.gmu.csiss.geoweaver.host.findCache(hosts[i].id));
				
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
				
				if(!exist && edu.gmu.csiss.geoweaver.host.findCache(hosts[i].id)==null){ //the p is not cached
					
					newhosts.push(hosts[i]);
					
				}
				
			}
			
			return newhosts;
			
		},
		

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
				
	            size: BootstrapDialog.SIZE_WIDE,
	            
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
				
				hostmsg = $.parseJSON(msg);

				var cont = "<div class=\"row\">";
				
				cont += "<div class=\"col col-md-5\">IP</div><div class=\"col col-md-5\">" + hostmsg.ip + "</div>";
				
				cont += "<div class=\"col col-md-5\">Port</div><div class=\"col col-md-5\">" + hostmsg.port + "</div>";
				
				cont += "<div class=\"col col-md-5\">User</div><div class=\"col col-md-5\">" + hostmsg.user + "</div>";
				
				cont += "<div class=\"col col-md-5\">Password</div><div class=\"col col-md-5\"><input type=\"password\" id=\"passwd\" class=\"form-control\" id=\"inputpswd\" placeholder=\"Password\"></div>";
								
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
		                	
		                	$.ajax({
		                		
		                		url: "key",
		                		
		                		type: "POST",
		                		
		                		data: ""
		                		
		                	}).done(function(msg){
		                		
		                		//encrypt the password using the received rsa key
		                		msg = $.parseJSON(msg);
		                		
		                		var encrypt = new JSEncrypt();
		                		
		                        encrypt.setPublicKey(msg.rsa_public);
		                        
		                        var encrypted = encrypt.encrypt($("#passwd").val());
		                        
		                        var req = {
		                        		host: hostmsg.ip,
		                        		port: hostmsg.port,
		                        		username: hostmsg.user,
		                        		password: encrypted
		                        }
		                	
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
            				
				one.id + "')\" data-toggle=\"tooltip\" title=\"Connect SSH\"></i><i class=\"fa fa-upload subalignicon\" onclick=\"edu.gmu.csiss.geoweaver.fileupload.uploadfile('"+
            				
				one.id + "')\" data-toggle=\"tooltip\" title=\"Upload File\"></i> <i class=\"fa fa-sitemap subalignicon\" onclick=\"edu.gmu.csiss.geoweaver.filebrowser.start('"+
            				
				one.id + "')\" data-toggle=\"tooltip\" title=\"Browser File Hierarchy\"></i> <i class=\"fa fa-minus subalignicon\" data-toggle=\"tooltip\" title=\"Delete this host\" onclick=\"edu.gmu.csiss.geoweaver.menu.del('" +
            				
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