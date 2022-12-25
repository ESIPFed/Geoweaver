
GW.result = {
		
		preview: function(filename){
			
//			BootstrapDialog.closeAll();
			
//			$('.imagepreview').attr('src', "../temp/" + filename);
//			
//			$('#resultmodal').modal('show');
			
			var viewer = ImageViewer(); //options is optional parameter
			
			viewer.show("../temp/" + filename); //second paramter is optional
			
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
			
			var url = "../temp/" + filename;
			
			GW.result.download_path(url, filename);
			
		},
		
		showDialog: function(process_history_id){
			
			var dialogid = GW.process.getRandomId();
			
			var content = '<form>'+
		       '   <div class="form-group row required">'+
		       '     <label for="filepath" class="col-sm-4 col-form-label control-label">Data File Path: </label>'+
		       '     <div class="col-sm-8">'+
		       '		<input type="text"  class="form-control" id="filepath-'+dialogid+'" placeholder="/temp/output.tif" >'+
		       '  		</input>'+
		       '     </div>'+
		       '   </div>'+
		       '   <div class="form-group row required">'+
		       '     <label for="hostselector" class="col-sm-4 col-form-label control-label">Which host: </label>'+
		       '     <div class="col-sm-8">'+
		       '		<select class="form-control" id="hostselector-'+dialogid+'" >'+
		       '  		</select>'+
		       '     </div>'+
		       '   </div>'+
		       '   <div class="form-group row required">'+
		       '     <label for="pswd" class="col-sm-4 col-form-label control-label">Password: </label>'+
		       '     <div class="col-sm-8">'+
		       '		<input type="password"  class="form-control" id="pswd-'+dialogid+'" >'+
		       '  		</input>'+
		       '     </div>'+
		       '   </div>'+
		       '</form>';
			
			content = '<div class="modal-body">'+ content + '</div>';
		
			content += '<div class="modal-footer">' +
				"	<button type=\"button\" id=\"result-download-"+dialogid+"\" class=\"btn btn-outline-primary\">Download</button> "+
				"	<button type=\"button\" id=\"result-preview-"+dialogid+"\" class=\"btn btn-outline-secondary\">Preview</button>"+
				"	<button type=\"button\" id=\"result-cancel-"+dialogid+"\" class=\"btn btn-outline-secondary\">Cancel</button>"+
				'</div>';
			
			var frame = GW.process.createJSFrameDialog(720, 640, content, "Result")
			
			$.ajax({
	            		
        		url: "list",
        		
        		method: "POST",
        		
        		data: "type=host"
        		
        	}).done(function(msg){
        		
        		msg = $.parseJSON(msg);
        		
        		$("#hostselector-" + dialogid).find('option').remove().end();
        		
        		for(var i=0;i<msg.length;i++){
        			
        			$("#hostselector-" + dialogid).append("<option id=\""+msg[i].id+"\">"+msg[i].name+"</option>");
        			
        		}
        		
        	}).fail(function(jxr, status){
				
				console.error("fail to list host");
				
			});
			
			$("#result-cancel-" + dialogid).click(function(){
				
				frame.closeFrame();
				
			});
			
			$("#result-preview-" + dialogid).click(function(){
				
            	var hostid = $("#hostselector-" + dialogid).children(":selected").attr("id");
            	
            	console.log("selected host: " + hostid);
            	
            	var filepath = $("#filepath-" + dialogid).val();
            	
            	var pswd = $("#pswd-" + dialogid).val();
            	
            	if(hostid=="" || filepath=="" || pswd == ""){
            		
            		alert("Please input all the fields.");
            		
            		return;
            	}
            	
            	$button = $(this)
            	
            	$button.button('loading');
            	
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
                			
                			GW.result.preview(msg.filename);
                			
                		}
                		
                		$button.button('reset');
                		
                	}).fail(function(jqXHR, textStatus, errorThrown){
                    	
                    	alert("fail to preview the file" + errorThrown);
                    	
                    	$button.button('reset')
                    	
                    });
                    
            	});
				
			});
			
			$("#result-download-" + dialogid).click(function(){
				
	        	var hostid = $("#hostselector-" + dialogid).children(":selected").attr("id");
	        	
	        	console.log("selected host: " + hostid);
	        	
	        	var filepath = $("#filepath-" + dialogid).val();
	        	
	        	var pswd = $("#pswd-" + dialogid).val();
	        	
	        	if(hostid=="" || filepath=="" || pswd == ""){
            		
            		alert("Please input all the fields.");
            		
            		return;
            	}
	        	
	        	$button = $(this)
				
				$button.button('loading');
	        	
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
	            			
	            			GW.result.download(msg.filename);
	            			
	            		}
	            		
	            		$button.button('reset');
	            		
	            	}).fail(function(jqXHR, textStatus, errorThrown){
	                	
	                	alert("fail to retrieve the file " + errorThrown);
	                	
	                	$button.button('reset');
	                	
	                });
	                
	        	});
	        	
	        })
			
		}
		
}