/**
 * Plugin for uploading files to the remote host
 * 
 * Code comes from https://danielmg.org/demo/java-script/uploader/no-queue (by Daniel Morales) MIT license
 */

GW.fileupload = {
		
		uploader: null,
		
		hid : null,
		
		password: null,
		
		password_frame: null,
		
		clean: function(){
			
			console.log("clean everything");
			
			hid = null;
			
			encrypted = null;			
			
		},

		uploadfile: function(hid){
			
			//it is divided into two steps
			//let users input credentials of the selected host
			
			GW.fileupload.hid = hid;
			
			$.ajax({
				
				url: "detail",
				
				method: "POST",
				
				data: "type=host&id=" + hid
				
			}).done(function(msg){
				
				//open the login page
				
				msg = $.parseJSON(msg);
				
				if(GW.host.findCache(hid)==null){

					if(GW.fileupload.password_frame != null){
						
						try{
						
							GW.fileupload.password_frame.closeFrame();
							
						}catch(e){
							
							console.error("Fail to close the frame. Probably it is already closed.");
							
						}
						
						GW.fileupload.password_frame = null;
						
					}

					var content = '<div class="modal-body" style=\"font-size: 12px;\">'+
							"<div class=\"row\">"+
							"<div class=\"col col-md-5\">Input Host Password : </div>"+
							"<div class=\"col col-md-5\">"+
							"	  <input type=\"password\" class=\"form-control\" id=\"inputpswd\" placeholder=\"Password\">"+
							"</div>"+
							"     <div class=\"col-md-12 form-check\">"+
						    "		<input type=\"checkbox\" class=\"form-check-input\" id=\"upload-remember\" />"+
						    "		<label class=\"form-check-label\" for=\"upload-remember\">Remember password and don't ask again.</label>"+
						    "     </div>"+
							"</div></div>";
					
					content += '<div class="modal-footer">' +
					"	<button type=\"button\" id=\"pswd-confirm-btn\" class=\"btn btn-outline-primary\">Confirm</button> "+
					"	<button type=\"button\" id=\"pswd-cancel-btn\" class=\"btn btn-outline-secondary\">Cancel</button>"+
					'</div>';
					
					GW.fileupload.password_frame = GW.process.createJSFrameDialog(350, 250, content, "Authorization")
			    	
					GW.fileupload.password_frame.on('closeButton', 'click', (_frame, evt) => {
						
		                _frame.closeFrame();
		                
		            });
		            
			    	//Show the window
//					GW.fileupload.password_frame.show();
//			    	
//					GW.fileupload.password_frame.setPosition((window.innerWidth - width) / 2, (window.innerHeight -height) / 2, 'LEFT_TOP');
			    	
			    	$("#pswd-confirm-btn").click(function(){
			    		
//			    		var $button = this;
//	                	
//	                	$button.spin();
	                	
//			    		GW.fileupload.password_frame.enableButtons(false);
			    		
	                	GW.fileupload.password = $('#inputpswd').val()
	                	
	                	if(GW.fileupload.password == ""){
	                		
	                		alert("Please input correct password.");
	                		
	                		return;
	                		
	                	}
	                	
	                	if(document.getElementById('upload-remember').checked) {
	                		
	                		GW.host.setCache(hid, GW.fileupload.password);
	                		
	                	}
	                	
	                	GW.fileupload.showUploadDialog();
	                	
	                	GW.fileupload.password_frame.closeFrame();
			    		
			    	});
			    	
			    	$("#pswd-cancel-btn").click(function(){
			    		
			    		GW.fileupload.password_frame.closeFrame();
			    		
			    	});
					
					
				}else{
					
                	GW.fileupload.password = GW.host.findCache(hid);
                	
                	if(GW.fileupload.password == ""){
                		
                		alert("Please clean the cache and try again.");
                		
                		return;
                		
                	}
                	
                	GW.fileupload.showUploadDialog();
                	
                	GW.fileupload.password_frame.closeFrame();
		    		
				}
				
				
			});
			
		},
		
		getUploadDialogContent: function(hasdebug){

			var content = "<div class=\"modal-body\" style=\"font-size: 12px;\"><div class=\"row\"  style=\"padding:10px;\">"+
			"<div class=\"row\" style=\"margin: 5px;\"> "+
		    "    <div class=\"col-md-12 col-sm-12\"> "+
		    "      <!-- Our markup, the important part here! --> "+
		    "      <div id=\"drag-and-drop-zone\" class=\"dm-uploader p-5\"> "+
		    "        <h3 class=\"mb-5 mt-5 text-muted\">Drag &amp; drop files here</h3> "+
		
		    "        <div class=\"btn btn-primary btn-block mb-5\"> "+
		    "            <span>Open the file Browser</span> "+
		    "            <input type=\"file\" title='Click to add Files' /> "+
		    "        </div> "+
		    "      </div>"+
		    "    </div> "+
		    "    <div class=\"col-md-12 col-sm-12\"> "+
		    "      <div class=\"card h-100\"> "+
		    "        <div class=\"card-header\"> "+
		    "          File List "+
		    "        </div> "+
		    "        <ul class=\"list-unstyled p-2 d-flex flex-column col\" id=\"files\"> "+
		    "          <li class=\"text-muted text-center empty\">No files uploaded.</li> "+
		    "        </ul> "+
		    "      </div> "+
		    "    </div> "+
		    "  </div>";

			if(hasdebug)
			content += "  <div class=\"row\" style=\"margin: 5px;\" > "+
		    "    <div class=\"col-12\"> "+
		    "       <div class=\"card h-100\"> "+
		    "        <div class=\"card-header\"> "+
		    "          Debug Messages "+
		    "        </div> "+
		    "       <ul class=\"list-group list-group-flush\" id=\"debug\"> "+
		    "          <li class=\"list-group-item text-muted empty\">Loading plugin....</li> "+
		    "        </ul> "+
		    "      </div> "+
		    "    </div> "+
		    "  </div>";

			content += "  <script type=\"text/html\" id=\"files-template\">"+
		    "  	<li class=\"media\">"+
		    "    <div class=\"media-body mb-1\">"+
		    "      <p class=\"mb-2\">"+
		    "        <strong>%%filename%%</strong> - Status: <span class=\"text-muted\">Waiting</span>"+
		    "      </p>"+
		    "      <div class=\"progress mb-2\">"+
		    "        <div class=\"progress-bar progress-bar-striped progress-bar-animated bg-primary\""+ 
		    "          role=\"progressbar\""+
		    "          style=\"width: 0%\" "+
		    "          aria-valuenow=\"0\" aria-valuemin=\"0\" aria-valuemax=\"100\">"+
		    "        </div>"+
		    "      </div>"+
		    "      <hr class=\"mt-1 mb-1\" />"+
		    "    </div>"+
		    "  	</li>"+
		    "	</script>"+
		    "	<script  type=\"text/html\" id=\"debug-template\">" +
		    "		<li class=\"list-group-item text-%%color%%\"><strong>%%date%%</strong>: %%message%%</li>" +
		    "	</script>"+
		    "</div>";
			
			content += '<div class="modal-footer">' +
			"	<button type=\"button\" id=\"upload-start\" class=\"btn btn-outline-primary\">Start</button> "+
			"	<button type=\"button\" id=\"upload-stop\" class=\"btn btn-outline-primary\">Stop</button> "+
			"	<button type=\"button\" id=\"upload-reset\" class=\"btn btn-outline-primary\">Reset</button> "+
			"	<button type=\"button\" id=\"upload-close\" class=\"btn btn-outline-primary\">Close</button> "+
			'</div>';
			
			
			
			return content;

		},

		closeUploaderJSFrame: function(){

			if(this.uploader!=null){
				
				try{ this.uploader.closeFrame(); }catch(e){ console.error("Probably it is already closed."); }
				
				this.uploader = null;
				
			}

		},

		showUploadWorkflowDialog: function(){

			this.closeUploaderJSFrame();

			let content = GW.fileupload.getUploadDialogContent(false);

			this.uploader = GW.process.createJSFrameDialog(550, 450, content, "Upload Workflow");

			this.listenUploadButtons(false, true);

		},
		
		showUploadDialog: function(){
			
			//once validated, show a dialog for users to select a file or multiple files to upload
			//click the submit button, the files are first uploaded to Geoweaver host
			//once received the complete response, send another request to upload the files to remote host
			
			let content = GW.fileupload.getUploadDialogContent(true);
			
			content = "<h4 class=\"border-bottom\">File Uploader Section  <button type=\"button\" class=\"btn btn-secondary btn-sm\" id=\"closeFileUploader\" >close</button></h4>"+content;

			$("#host-file-uploader").html(content);
			
			$("#closeFileUploader").click(function(){
				
				$("#host-file-uploader").html("");
				
			});
			
			this.listenUploadButtons(true, false);
			
		},

		listenUploadButtons: function(iftransfer, ifworkflow){

			$('#drag-and-drop-zone').dmUploader({
			    url: '../FileUploadServlet',
			    maxFileSize: 3000000000, // 3000 Megs max
			    auto: false,
			    queue: true,
			    onDragEnter: function(){
			      // Happens when dragging something over the DnD area
			      this.addClass('active');
			    },
			    onDragLeave: function(){
			      // Happens when dragging something OUT of the DnD area
			      this.removeClass('active');
			    },
			    onInit: function(){
			      // Plugin is ready to use
			      GW.fileupload.ui_add_log('Uploader initialized :)', 'info');
			    },
			    onComplete: function(){
			      // All files in the queue are processed (success or error)
//			      GW.fileupload.ui_add_log('All pending tranfers finished');
			    },
			    onNewFile: function(id, file){
			      // When a new file is added using the file selector or the DnD area
			      GW.fileupload.ui_add_log('New file added #' + id);
			      GW.fileupload.ui_multi_add_file(id, file);
			    },
			    onBeforeUpload: function(id){
			      // about tho start uploading a file
			      GW.fileupload.ui_add_log('Starting the upload of #' + id);
			      GW.fileupload.ui_multi_update_file_progress(id, 0, '', true);
			      GW.fileupload.ui_multi_update_file_status(id, 'uploading', 'Uploading...');
			    },
			    onUploadProgress: function(id, percent){
			      // Updating file progress
			      GW.fileupload.ui_multi_update_file_progress(id, percent);
			    },
			    onUploadSuccess: function(id, data){
			      // A file was successfully uploaded
			      GW.fileupload.ui_add_log('Server Response for file #' + id + ': ' + data);
			      GW.fileupload.ui_add_log('Upload of file #' + id + ' COMPLETED', 'success');
				  data = $.parseJSON(data);
				  if(iftransfer){
					GW.fileupload.ui_multi_update_file_progress(id, 90, '', true);
					GW.fileupload.transfer(id, data.url);
				  }

				  if(ifworkflow){
					GW.fileupload.closeUploaderJSFrame();
					GW.workflow.parseUploadedWorkflow(id, data.filename);
				  }
			      
			    },
			    onUploadError: function(id, xhr, status, message){
			      GW.fileupload.ui_multi_update_file_status(id, 'danger', message);
			      GW.fileupload.ui_multi_update_file_progress(id, 0, 'danger', false);  
			    },
			    onFallbackMode: function(){
			      // When the browser doesn't support this plugin :(
			      GW.fileupload.ui_add_log('Plugin cant be used here, running Fallback callback', 'danger');
			    },
			    onFileSizeError: function(file){
			      GW.fileupload.ui_add_log('File \'' + file.name + '\' cannot be added: size excess limit', 'danger');
			    }
			});
			
			$("#upload-start").click(function(){
				
				$('#drag-and-drop-zone').dmUploader('start');
				
				var $button = $("#upload-start"); // 'this' here is a jQuery object that wrapping the <button> DOM element.
//                $button.disable();
                $('#upload-start').prop('disabled', true);
//                $button.spin();
//                dialog.setClosable(false);
				
			});
			
			$("#upload-stop").click(function(){
				$('#drag-and-drop-zone').dmUploader('cancel');
				$('#upload-start').prop('disabled', false);
//				dialog.getButton("btn-start").enable();
//				dialog.getButton("btn-start").stopSpin();
//				dialog.setClosable(true);
			});
			
			$("#upload-reset").click(function(){
				$("#drag-and-drop-zone").dmUploader("reset");
				$("#files").empty();
				$("#debug").empty();
				$('#upload-start').prop('disabled', false);
//				dialog.getButton("btn-start").enable();
//				dialog.getButton("btn-start").stopSpin();
//				dialog.setClosable(true);
				
			});
			
			$("#upload-close").click(function(){
				
				if(iftransfer) $("#host-file-uploader").html("");

				if(ifworkflow) GW.fileupload.closeUploaderJSFrame();

			});

		},
		
		transfer: function(id, url){
			
			GW.fileupload.ui_add_log('Start to transfer to file to remote host, please wait until it is finished...');
			
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
				
				var encrypted = encrypt.encrypt(GW.fileupload.password);
				
//                GW.fileupload.encrypted = encrypted;
				
//                GW.fileupload.showUploadDialog();
				
				var req = {
						
						hid: GW.fileupload.hid,
						
						encrypted: encrypted,
						
						filepath: url
						
				}
				
				$.ajax({
					
					url: "upload",
					
					type: "POST",
					
					data: req
					
				}).done(function(data){
					
					data = $.parseJSON(data);
					
					console.log("response: " + data);

					GW.fileupload.ui_add_log('Temporary file in Geoweaver temporary folder is removed');
					
					GW.fileupload.ui_add_log('File has been on remote host!' + data.filename);
					
					GW.fileupload.ui_multi_update_file_progress(id, 100, 'success', false);
					
					GW.fileupload.ui_multi_update_file_status(id, 'success', 'Upload Complete');
					
					$('#upload-start').prop('disabled', false);
					
//	    				var $btn = $("#btn-start");
//	    				
//	    				$btn.enable();
//	    				
//	    				$btn.stopSpin();
					
//    				GW.fileupload.uploader.setClosable(true);
					
				}).fail(function(){
					
					console.error("fail to transfer the file to remote host");
					
					GW.fileupload.ui_add_log('Fail to transfer');
					
//	    				var $btn = $("#btn-start");
//	    				
//	    				$btn.enable();
//	    				
//	    				$btn.stopSpin();
					
					$('#upload-start').prop('disabled', false);
					
//    				GW.fileupload.uploader.setClosable(true);
					
				});
				
				
			}).fail(function(jxr, status){
				
				console.error("fail to encrypt key");
				
			});
			
		},
		
		// Adds an entry to our debug area
		ui_add_log: function (message, color)
		{
		  var d = new Date();

		  var dateString = (('0' + d.getHours())).slice(-2) + ':' +
		    (('0' + d.getMinutes())).slice(-2) + ':' +
		    (('0' + d.getSeconds())).slice(-2);

		  color = (typeof color === 'undefined' ? 'muted' : color);

		  var template = $('#debug-template').text();
		  template = template.replace('%%date%%', dateString);
		  template = template.replace('%%message%%', message);
		  template = template.replace('%%color%%', color);
		  
		  $('#debug').find('li.empty').fadeOut(); // remove the 'no messages yet'
		  $('#debug').prepend(template);
		  
		},
		
		ui_multi_reset: function(id, file){
			
			$("#drop-area").dmUploader("reset");
			
		},

		// Creates a new file and add it to our list
		ui_multi_add_file: function (id, file)
		{
		  var template = $('#files-template').text();
		  template = template.replace('%%filename%%', file.name);

		  template = $(template);
		  template.prop('id', 'uploaderFile' + id);
		  template.data('file-id', id);

		  $('#files').find('li.empty').fadeOut(); // remove the 'no files yet'
		  $('#files').prepend(template);
		},

		// Changes the status messages on our list
		ui_multi_update_file_status: function (id, status, message)
		{
		  $('#uploaderFile' + id).find('span').html(message).prop('class', 'status text-' + status);
		},

		// Updates a file progress, depending on the parameters it may animate it or change the color.
		ui_multi_update_file_progress: function (id, percent, color, active)
		{
		  color = (typeof color === 'undefined' ? false : color);
		  active = (typeof active === 'undefined' ? true : active);

		  var bar = $('#uploaderFile' + id).find('div.progress-bar');

		  bar.width(percent + '%').attr('aria-valuenow', percent);
		  bar.toggleClass('progress-bar-striped progress-bar-animated', active);

		  if (percent === 0){
		    bar.html('');
		  } else {
		    bar.html(percent + '%');
		  }

		  if (color !== false){
		    bar.removeClass('bg-success bg-info bg-warning bg-danger');
		    bar.addClass('bg-' + color);
		  }
		}
		
		
}