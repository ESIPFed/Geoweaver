/**
*
* Author: Ziheng Sun
*
*/

edu.gmu.csiss.geoweaver.result = {
		
		preview: function(filename){
			
			BootstrapDialog.closeAll();
			
			$('.imagepreview').attr('src', "file/" + filename);
			
			$('#resultmodal').modal('show');
			
		},
		
		download_path: function(filepath, filename){
			
			var url = filepath;
			
			var element = document.createElement('a');
			
			element.setAttribute('href', url);
		  
			element.setAttribute('download', filename);

			element.style.display = 'none';
		  
			document.body.appendChild(element);

			element.click();

			document.body.removeChild(element);
		},
		
		download: function(filename){
			
//			window.open("file/" + filename, '_blank');
			
			var url = "file/" + filename;
			
			edu.gmu.csiss.geoweaver.result.download_path(url, filename);
			
		},
		
		showDialog: function(process_history_id){
			
			var content = '<form>'+
		       '   <div class="form-group row required">'+
		       '     <label for="filepath" class="col-sm-4 col-form-label control-label">Data File Path: </label>'+
		       '     <div class="col-sm-8">'+
		       '		<input type="text"  class="form-control" id="filepath" placeholder="/temp/output.tif" >'+
		       '  		</input>'+
		       '     </div>'+
		       '   </div>'+
		       '   <div class="form-group row required">'+
		       '     <label for="hostselector" class="col-sm-4 col-form-label control-label">Which host: </label>'+
		       '     <div class="col-sm-8">'+
		       '		<select class="form-control" id="hostselector" >'+
		       '  		</select>'+
		       '     </div>'+
		       '   </div>'+
		       '   <div class="form-group row required">'+
		       '     <label for="pswd" class="col-sm-4 col-form-label control-label">Password: </label>'+
		       '     <div class="col-sm-8">'+
		       '		<input type="password"  class="form-control" id="pswd" >'+
		       '  		</input>'+
		       '     </div>'+
		       '   </div>'+
		       '</form>';
			
			BootstrapDialog.show({
				
				title: "Result",
				
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
		            
	            	label: 'Download',
	                
	                action: function(dialogItself){
	                	
	                	var $button = this;
	                	
	                	$button.spin();
	                	
	                	dialogItself.enableButtons(false);
	                	
	                	var hostid = $("#hostselector").children(":selected").attr("id");
	                	
	                	console.log("selected host: " + hostid);
	                	
	                	var filepath = $("#filepath").val();
	                	
	                	var pswd = $("#pswd").val();
	                	
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
	                        
	                        var encrypted = encrypt.encrypt(pswd);
	                        
	                        var req = {
	                        		
	                        		hostid : hostid,
	                        		
	                        		filepath: filepath,
	                        		
	                        		pswd: encrypted
	                        		
	                        };
	                        
	                        $.ajax({
		                		
		                		url: "retrieve",
		                		
		                		type: "POST",
		                		
		                		data: req
		                		
		                	}).done(function(msg){
		                		
		                		msg = $.parseJSON(msg);
		                		
		                		if(msg.ret=="success"){
		                			
		                			edu.gmu.csiss.geoweaver.result.download(msg.filename);
		                			
		                		}
		                		
		                		$button.stopSpin();
		                		
		        				dialogItself.enableButtons(true);
		                		
		                	}).fail(function(jqXHR, textStatus, errorThrown){
	                        	
	                        	alert("fail to retrieve the file " + errorThrown);
	                        	
	                        	$button.stopSpin();
		                		
		        				dialogItself.enableButtons(true);
	                        	
	                        });
	                        
	                	});
	                	
//	                	edu.gmu.csiss.geoweaver.process.executeProcess(pid, hostid);
	                	
//	                    dialogItself.close();
	                    
	                }
	        
	            },{
		            
	            	label: 'Preview',
	                
	                action: function(dialogItself){
	                	
	                	var $button = this;
	                	
	                	$button.spin();
	                	
	                	dialogItself.enableButtons(false);
	                	
	                	var hostid = $("#hostselector").children(":selected").attr("id");
	                	
	                	console.log("selected host: " + hostid);
	                	
	                	var filepath = $("#filepath").val();
	                	
	                	var pswd = $("#pswd").val();
	                	
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
	                        
	                        var encrypted = encrypt.encrypt(pswd);
	                        
	                        var req = {
	                        		
	                        		hostid : hostid,
	                        		
	                        		filepath: filepath,
	                        		
	                        		pswd: encrypted
	                        		
	                        };
	                        
	                        $.ajax({
		                		
		                		url: "retrieve",
		                		
		                		type: "POST",
		                		
		                		data: req
		                		
		                	}).done(function(msg){
		                		
		                		msg = $.parseJSON(msg);
		                		
		                		if(msg.ret=="success"){
		                			
		                			edu.gmu.csiss.geoweaver.result.preview(msg.filename);
		                			
		                		}
		                		
		                		$button.stopSpin();
		                		
		        				dialogItself.enableButtons(true);
		                		
		                	}).fail(function(jqXHR, textStatus, errorThrown){
	                        	
	                        	alert("fail to preview the file" + errorThrown);
	                        	
	                        	$button.stopSpin();
		                		
		        				dialogItself.enableButtons(true);
	                        	
	                        });
	                        
	                	});
	                	
//	                	edu.gmu.csiss.geoweaver.process.executeProcess(pid, hostid);
	                	
//	                    dialogItself.close();
	                    
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