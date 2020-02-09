/**
 * 
 * author: Ziheng Sun
 * 
 */
edu.gmu.csiss.geoweaver.host = {
		
		cred_cache: [{"h":"xxxx", "s": "yyyyy", "env": {"bin":"python3", "pyenv": "cdl"}}],
		
		password_frame: null,
		
		ssh_password_frame: null,
		
		new_host_frame: null,
		
		clearCache: function(){
			
			this.cred_cache = [];
			
		},
		
		setEnvCache: function(hid, env){
			
			var is = false;
			
			for(var i=0;i<edu.gmu.csiss.geoweaver.host.cred_cache.length;i++){
				
				if(edu.gmu.csiss.geoweaver.host.cred_cache[i].h == hid){
					
					edu.gmu.csiss.geoweaver.host.cred_cache[i].env = env;
					
					is = true;
					
					break;
					
				}
				
			}
			
			if(!is){
				
				edu.gmu.csiss.geoweaver.host.cred_cache.push({"h": hid, "env": env});
				
			}
			
		},
		
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
		
		findEnvCache: function(hid){
			
			var env = null;
			
			for(var i=0;i<edu.gmu.csiss.geoweaver.host.cred_cache.length;i++){
				
				if(edu.gmu.csiss.geoweaver.host.cred_cache[i].h == hid){
					
					env = edu.gmu.csiss.geoweaver.host.cred_cache[i].env;
					
					break;
					
				}
				
			}
			
			return env;
			
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
			
			if(this.password_frame != null){
				
				try{
				
					this.password_frame.closeFrame();
					
				}catch(e){}
				
				this.password_frame = null;
				
			}
			
			var content = '<div class="modal-body">'+
			   '   <div class="form-group row required" style="font-size: 12px;">'+
		       '     <label for="host password" class="col-sm-4 col-form-label control-label">Input Host User Password: </label>'+
		       '     <div class="col-sm-6">'+
		       '		<input type=\"password\" class=\"form-control\" id=\"inputpswd\" placeholder=\"Password\" >'+
		       '     </div>'+
		       '     <div class="col-sm-12 form-check">'+
		       '		<input type="checkbox" class="form-check-input" id="remember" />'+
		       '		<label class="form-check-label" for="remember">Remember password</label>'+
		       '     </div>'+
		       '   </div></div>';
			
			content += '<div class="modal-footer">' +
				"	<button type=\"button\" id=\"pswd-confirm-btn\" class=\"btn btn-outline-primary\">Confirm</button> "+
				"	<button type=\"button\" id=\"pswd-cancel-btn\" class=\"btn btn-outline-secondary\">Cancel</button>"+
				'</div>';
			
			
			var width = 520; var height = 340;
			
			this.password_frame = edu.gmu.csiss.geoweaver.workspace.jsFrame.create({
	    		title: 'Host Password',
	    	    left: 0, 
	    	    top: 0, 
	    	    width: width, 
	    	    height: height,
	    	    appearanceName: 'yosemite',
	    	    style: {
	                backgroundColor: 'rgb(255,255,255)',
		    	    fontSize: 12,
	                overflow:'auto'
	            },
	    	    html: content
	    	});
	    	
			this.password_frame.setControl({
	            styleDisplay:'inline',
	            maximizeButton: 'zoomButton',
	            demaximizeButton: 'dezoomButton',
	            minimizeButton: 'minimizeButton',
	            deminimizeButton: 'deminimizeButton',
	            hideButton: 'closeButton',
	            animation: true,
	            animationDuration: 150,
	
	        });
	    	
			this.password_frame.on('closeButton', 'click', (_frame, evt) => {
	            _frame.closeFrame();
	            
	        });
	        
	    	//Show the window
			this.password_frame.show();
	    	
			this.password_frame.setPosition((window.innerWidth - width) / 2, (window.innerHeight -height) / 2, 'LEFT_TOP');
	    	
			$("#inputpswd").on('keypress',function(e) {
				
			    if(e.which == 13) {
			    	
			    	$("#pswd-confirm-btn").click();
			    	
			    }
			    
			}); 
			
			$("#pswd-confirm-btn").click(function(){
				
//				$("#pswd-confirm-btn").spin();
            	
            	$('#pswd-confirm-btn').prop('disabled', true);
            	
//            	dialogItself.enableButtons(false);
            	
            	if(document.getElementById('remember').checked) {
            	    
            		edu.gmu.csiss.geoweaver.host.setCache(hid, $('#inputpswd').val()); //remember s
            		
            	}
            	
            	edu.gmu.csiss.geoweaver.host.encrypt(hid, $('#inputpswd').val(), req, edu.gmu.csiss.geoweaver.host.password_frame, $('#pswd-confirm-btn'), business_callback);
	            	
			});
			
			$("#pswd-cancel-btn").click(function(){
				
				edu.gmu.csiss.geoweaver.host.password_frame.closeFrame();
				
			});
			
//			BootstrapDialog.show({
//				
//				title: "Host Password",
//				
//				closable: false,
//				
//				message: content,
//				
//				onshown: function(dialog){
//					
//					$("#inputpswd").on('keypress',function(e) {
//					    if(e.which == 13) {
//					    	
//					    	$("#pswd-confirm-btn").click();
//					    	
//					    }
//					}); 
//					
//				},
//				
//				buttons: [{
//					
//	            	label: 'Confirm',
//	            	
//	            	id: 'pswd-confirm-btn',
//	                
//	                action: function(dialogItself){
//	                	
//	                	var $button = this;
//	                	
//	                	$button.spin();
//	                	
//	                	$('#pswd-confirm-btn').prop('disabled', true);
//	                	
////	                	dialogItself.enableButtons(false);
//	                	
//	                	if(document.getElementById('remember').checked) {
//	                	    
//	                		edu.gmu.csiss.geoweaver.host.setCache(hid, $('#inputpswd').val()); //remember s
//	                		
//	                	}
//	                	
//	                	edu.gmu.csiss.geoweaver.host.encrypt(hid, $('#inputpswd').val(), req, dialogItself, $button, business_callback)
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
			
			var content = '';
			
			for(var i=0;i<newhosts.length;i++){
				
				content += '<div class="form-group row required">'+
			       '     <label for="host password" class="col-sm-4 col-form-label control-label">Host '+newhosts[i].name+' Password: </label>'+
			       '     <div class="col-sm-8">'+
			       '		<input type=\"password\" class=\"form-control\" id=\"inputpswd_'+i+'\" required=\"true\" placeholder=\"Password\">'+
			       '     </div>'+
			       '   </div>';
			}
			
			content += '     <div class="form-group form-check">'+
		       '		<input type="checkbox" class="form-check-input" id="remember">'+
		       '		<label class="form-check-label" for="remember">Remember password</label>'+
		       '     </div>';
			
			
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
			
			const frame = edu.gmu.csiss.geoweaver.workspace.jsFrame.create({
		    		title: 'SSH Command Line',
		    	    left: 60, top: 60, width: 600, height: 540,
		    	    appearanceName: 'yosemite',
		    	    html: "<iframe src=\"geoweaver-ssh?token="+token+"\" style=\"height:100%;width:100%;\"></iframe>"
		    	});
		    	
		    	frame.setControl({
		            maximizeButton: 'maximizeButton',
		            demaximizeButton: 'restoreButton',
		            minimizeButton: 'minimizeButton',
		            deminimizeButton: 'deminimizeButton',
		            animation: true,
		            animationDuration: 200,
		
		    });
			
		    frame.on('closeButton', 'click', (_frame, evt) => {
		    	edu.gmu.csiss.geoweaver.host.closeSSH(token);
		        _frame.closeFrame();
		        
		    });
    
	    	//Show the window
	    	frame.show();
	    	
	    	frame.setPosition(window.innerWidth / 4, window.innerHeight / 4, 'CENTER_TOP');
			
//			var dialog = new BootstrapDialog.show({
//	            
//				title: 'SSH Command Line',
//	            
//	            message: "<iframe src=\"geoweaver-ssh?token="+token+"\" style=\"height:100%;width:100%;\"></iframe>",
//				
//	            size: BootstrapDialog.SIZE_WIDE,
//	            
//	            onhide: function(dialogRef){
//	                
////	            	edu.gmu.csiss.geoweaver.menu.closeSSH(token);
//	                
//	            },
//	            
//	            closable: false,
//	            
//	            buttons: [{
//	            	
//	            	label: 'Create Process',
//	            	
//	            	action: function(dialog){
//	            		
//	            		console.log("not ready yet");
//	            		
//	            	}
//	            	
//	            },{
//	                
//	            	label: 'Close Connection',
//	                
//	                action: function(dialog) {
//	                	
//	                	edu.gmu.csiss.geoweaver.host.closeSSH(token);
//	                	
//	                	dialog.close();
//	                	
//	                }
//	            }]
//	        
//			});
//			
//			edu.gmu.csiss.geoweaver.menu.setFullScreen(dialog);
			
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
				
				if(edu.gmu.csiss.geoweaver.host.ssh_password_frame != null){
					
					try{
						
						edu.gmu.csiss.geoweaver.host.ssh_password_frame.closeFrame();
						
					}catch(e){
						
						console.error("Probably it is closed already.");
						
					}
					
					edu.gmu.csiss.geoweaver.host.ssh_password_frame = null;
					
				}

				var cont = '<div class="modal-body" style=\"font-size: 12px;\">'+
					"<div class=\"row\">";
				
				cont += "<div class=\"col col-md-5\">IP</div><div class=\"col col-md-5\">" + hostmsg.ip + "</div>";
				
				cont += "<div class=\"col col-md-5\">Port</div><div class=\"col col-md-5\">" + hostmsg.port + "</div>";
				
				cont += "<div class=\"col col-md-5\">User</div><div class=\"col col-md-5\">" + hostmsg.username + "</div>";
				
				cont += "<div class=\"col col-md-5\">Password</div><div class=\"col col-md-5\"><input type=\"password\" id=\"passwd\" class=\"form-control\" id=\"inputpswd\" placeholder=\"Password\"></div>";
								
				cont += "</div></div>";
				
				cont += '<div class="modal-footer">' +
				"	<button type=\"button\" id=\"ssh-connect-btn\" class=\"btn btn-outline-primary\">Connect</button> "+
				"	<button type=\"button\" id=\"ssh-cancel-btn\" class=\"btn btn-outline-secondary\">Cancel</button>"+
				'</div>';
				
				var width = 500; var height = 340;
				
				edu.gmu.csiss.geoweaver.host.ssh_password_frame = edu.gmu.csiss.geoweaver.workspace.jsFrame.create({
			    		title: 'Open SSH session',
			    	    left: 0, 
			    	    top: 0, 
			    	    width: width, 
			    	    height: height,
			    	    appearanceName: 'yosemite',
			    	    style: {
		                    backgroundColor: 'rgb(255,255,255)',
				    	    fontSize: 12,
		                    overflow:'auto'
		                },
			    	    html: cont
		    	});
		    	
				edu.gmu.csiss.geoweaver.host.ssh_password_frame.setControl({
		            styleDisplay:'inline',
		            maximizeButton: 'zoomButton',
		            demaximizeButton: 'dezoomButton',
		            minimizeButton: 'minimizeButton',
		            deminimizeButton: 'deminimizeButton',
		            hideButton: 'closeButton',
		            animation: true,
		            animationDuration: 150,
		
		        });
		    	
				edu.gmu.csiss.geoweaver.host.ssh_password_frame.show();
		    	
				edu.gmu.csiss.geoweaver.host.ssh_password_frame.setPosition((window.innerWidth - width) / 2, (window.innerHeight -height) / 2, 'LEFT_TOP');
		    	
		    	$("#ssh-connect-btn").click(function(){
		    		
//		    		var $button = this;
//                	
//                	$button.spin();
//                	
//                    dialog.enableButtons(false);
		    		
		    		$("#ssh-connect-btn").prop("disabled", true);
                	
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
                        		username: hostmsg.username,
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
	                		try{
	                			edu.gmu.csiss.geoweaver.host.ssh_password_frame.closeFrame();
	                		}catch(e){}
	                		
	                		
	                	}).fail(function(status){
	                		
	                		alert("Fail to open SSH session" + status);
	                		
	                		$("#ssh-connect-btn").prop("disabled", false);
	                		
//	                		$button.stopSpin();
//	                		
//	                		dialog.enableButtons(true);
	                		
	                	});
	                	
                        
                	});
		    		
		    	});
		    	
		    	$("#ssh-cancel-btn").click(function(){
		    		
		    		edu.gmu.csiss.geoweaver.host.ssh_password_frame.closeFrame();
		    		
		    	});
				
//				BootstrapDialog.show({
//		            
//					title: 'Open SSH session',
//		            
//		            message: cont,
//		            
//		            closable: false,
//		            
//		            buttons: [{
//		                
//		            	label: 'Connect',
//		                
//		                action: function(dialog) {
//		                	
//		                	var $button = this;
//		                	
//		                	$button.spin();
//		                	
//		                    dialog.enableButtons(false);
//		                	
//		                	$.ajax({
//		                		
//		                		url: "key",
//		                		
//		                		type: "POST",
//		                		
//		                		data: ""
//		                		
//		                	}).done(function(msg){
//		                		
//		                		//encrypt the password using the received rsa key
//		                		msg = $.parseJSON(msg);
//		                		
//		                		var encrypt = new JSEncrypt();
//		                		
//		                        encrypt.setPublicKey(msg.rsa_public);
//		                        
//		                        var encrypted = encrypt.encrypt($("#passwd").val());
//		                        
//		                        var req = {
//		                        		host: hostmsg.ip,
//		                        		port: hostmsg.port,
//		                        		username: hostmsg.username,
//		                        		password: encrypted
//		                        }
//		                	
//			                	$.ajax({
//			                		
//			                		url: "geoweaver-ssh-login-inbox",
//			                		
//			                		method: "POST",
//			                		
//			                		data: req
//			                		
//			                	}).done(function(msg){
//			                		
//			                		msg = $.parseJSON(msg);
//			                		
//			                		if(msg.token!=null){
//			                			
//				                		//open a dialog to show the SSH command line interface
//	
//				                		edu.gmu.csiss.geoweaver.host.showSSHCmd(msg.token);
//			                			
//			                		}else{
//			                			
//			                			alert("Fail to open SSH session");
//			                			
//			                		}
//				                	dialog.close();
//			                		
//			                	}).fail(function(status){
//			                		
//			                		alert("Fail to open SSH session" + status);
//			                		
//			                		$button.stopSpin();
//			                		
//			                		dialog.enableButtons(true);
//			                		
//			                	});
//			                	
//		                        
//		                	});
//		                	
//		                	
//		                	
//		                }
//		            }, {
//		            	
//		                label: 'Cancel',
//		                
//		                action: function(dialog) {
//		                
//		                	dialog.close();
//		                
//		                }
//		            
//		            }]
//		        
//				});
				
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
			
			$("#"+edu.gmu.csiss.geoweaver.menu.getPanelIdByType("host")).append("<li class=\"host\" id=\"host-" + one.id + 
					
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
			
			if(edu.gmu.csiss.geoweaver.host.new_host_frame!=null){
				
				try{
					
					edu.gmu.csiss.geoweaver.host.new_host_frame.closeFrame();
					
				}catch(e){
					
					console.error("Fail to close old frame. Maybe it is already closed.");
					
				}
				
				edu.gmu.csiss.geoweaver.host.new_host_frame = null;
				
			}
			
			var content = '<div class="modal-body" style=\"font-size: 12px;\">'+
			   '<form>'+
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
		       ' </form>'+
		       '</div>';
			
			content += '<div class="modal-footer">' +
			"	<button type=\"button\" id=\"host-add-btn\" class=\"btn btn-outline-primary\">Add</button> "+
			"	<button type=\"button\" id=\"host-cancel-btn\" class=\"btn btn-outline-secondary\">Cancel</button>"+
			'</div>';
			
			var width = 500; var height = 450;
			
			edu.gmu.csiss.geoweaver.host.new_host_frame = edu.gmu.csiss.geoweaver.workspace.jsFrame.create({
		    		title: 'Add new host',
		    	    left: 0, 
		    	    top: 0, 
		    	    width: width, 
		    	    height: height,
		    	    appearanceName: 'yosemite',
		    	    style: {
	                    backgroundColor: 'rgb(255,255,255)',
			    	    fontSize: 12,
	                    overflow:'auto'
	                },
		    	    html: content
	    	});
	    	
			edu.gmu.csiss.geoweaver.host.new_host_frame.setControl({
	            styleDisplay:'inline',
	            maximizeButton: 'zoomButton',
	            demaximizeButton: 'dezoomButton',
	            minimizeButton: 'minimizeButton',
	            deminimizeButton: 'deminimizeButton',
	            hideButton: 'closeButton',
	            animation: true,
	            animationDuration: 150,
	
	        });
	    	
			edu.gmu.csiss.geoweaver.host.new_host_frame.show();
	    	
			edu.gmu.csiss.geoweaver.host.new_host_frame.setPosition((window.innerWidth - width) / 2, (window.innerHeight -height) / 2, 'LEFT_TOP');
			
			$("#host-add-btn").click(function(){
				
            	edu.gmu.csiss.geoweaver.host.add(function(){
        		
            		try{edu.gmu.csiss.geoweaver.host.new_host_frame.closeFrame();}catch(e){}
	                
	        	});
				
			});
			
			$("#host-cancel-btn").click(function(){
				
				edu.gmu.csiss.geoweaver.host.new_host_frame.closeFrame();
				
			});
			
//			BootstrapDialog.show({
//				
//				title: "Add new host",
//				
//	            message: '<form>'+
//				       '   <div class="form-group row required">'+
//				       '     <label for="hostname" class="col-sm-2 col-form-label control-label">Host Name </label>'+
//				       '     <div class="col-sm-10">'+
//				       '       <input type="text" class="form-control" id="hostname" value="New Host">'+
//				       '     </div>'+
//				       '   </div>'+
//				       '   <div class="form-group row required">'+
//				       '     <label for="hostip" class="col-sm-2 col-form-label control-label">Hose IP</label>'+
//				       '     <div class="col-sm-10">'+
//				       '       <input type="text" class="form-control" id="hostip" placeholder="Host IP">'+
//				       '     </div>'+
//				       '   </div>'+
//				       '   <div class="form-group row required">'+
//				       '     <label for="hostport" class="col-sm-2 col-form-label control-label">Port</label>'+
//				       '     <div class="col-sm-10">'+
//				       '       <input type="text" class="form-control" id="hostport" placeholder="">'+
//				       '     </div>'+
//				       '   </div>'+
//				       '   <div class="form-group row required">'+
//				       '     <label for="username" class="col-sm-2 col-form-label control-label">User Name</label>'+
//				       '     <div class="col-sm-10">'+
//				       '       <input type="text" class="form-control" id="username" placeholder="">'+
//				       '     </div>'+
//				       '   </div>'+
//				       ' </form>',
//	            
//	            cssClass: 'dialog-vertical-center',
//	            
//	            buttons: [{
//	            	
//	                label: 'Add',
//	                
//	                action: function(dialogItself){
//	                	
//	                	edu.gmu.csiss.geoweaver.host.add(function(){
//	                		
//		                    dialogItself.close();
//		                    
//	                	});
//	                	
//	                }
//	            
//	            },{
//	            
//	            	label: 'Close',
//	                
//	                action: function(dialogItself){
//	                	
//	                    dialogItself.close();
//	                    
//	                }
//	        
//	            }]
//			
//	        });
			
		},
		
}