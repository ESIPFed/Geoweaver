/**
 * 
 * author: Ziheng Sun
 * 
 */

edu.gmu.csiss.geoweaver.filebrowser = {
		
		current_path: null,
		
		editor: null,
		
		openFileEditor: function(file_name){
			
			BootstrapDialog.closeAll();
			
			$.ajax({
				
				url: "retrievefile",
				
				method: "POST",
				
				data: { "filepath" : edu.gmu.csiss.geoweaver.filebrowser.current_path + file_name}
				
			}).done(function(msg){
				
				msg = $.parseJSON(msg);
				
				if(msg.ret == "success"){
					
					BootstrapDialog.show({
						
						title: "File Editor",
						
						closable: false,
						
						size: BootstrapDialog.SIZE_WIDE,
						
						message: "<div id=\"codearea\" class=\"form-group row required\" ></div>"+
						
						"<button id=\"loading_btn\" class=\"btn btn-sm btn-warning\"><span class=\"glyphicon glyphicon-refresh glyphicon-refresh-animate\"></span> Loading...</button>",
						
						onshown: function(){
							
							$("#codearea").append('<textarea id="code_editor" placeholder=""></textarea>');
			            	
							edu.gmu.csiss.geoweaver.filebrowser.editor = CodeMirror.fromTextArea(document.getElementById("code_editor"), {
				        		
				        		lineNumbers: true
				        		
				        	});
							
							var url_path = msg.path;
							
							$.get( "../" + url_path, function( data ) {
								
								edu.gmu.csiss.geoweaver.filebrowser.editor.setValue(data);
								
								$("#loading_btn").hide();
								
							});
							
			            },
			            
			            buttons: [{
			            	
			                label: 'Save',
			                
			                action: function(dialog) {
			                	
			                	$.ajax({
			                		
			                		url: "updatefile",
			                		
			                		method: "POST",
			                		
			                		data: { filepath: edu.gmu.csiss.geoweaver.filebrowser.current_path + file_name, 
			                			content: edu.gmu.csiss.geoweaver.filebrowser.editor.getValue()}
			                		
			                	}).done(function(msg){
			                		
			                		msg = $.parseJSON(msg);
			                		
			                		if(msg.ret == "success"){
			                			
				                		console.log("file updated");
			                			
				                		alert("Saved!!");
				                		
			                		}else{
			                			
			                			alert("Failed!!" + msg.reason);
			                		}
			                		
			                	});
			                	
			                }
			            
			            },{
			            	
			                label: 'Close',
			                
			                action: function(dialog) {
			                	
			                	$.ajax({
			                		
			                		url: "closefilebrowser",
			                		
			                		method: "POST"
			                		
			                	}).done(function(msg){
			                		
			                		console.log(msg);
			                		
			                	});
			                	
			                	dialog.close();
			                
			                }
			            
			            }]
						
					});
					
				}else{
					
					alert("Fail to retrieve file: " + msg.reason);
					
				}
				
			});
			
		},
		
		downloadFile: function(file_name){
			
			var path = edu.gmu.csiss.geoweaver.filebrowser.current_path + file_name;
			
			var req = {filepath : path};
			
			$.ajax({
				
				url: "retrievefile",
				
				data: req,
				
				method: "POST"
					
			}).done(function(msg){
				
				msg = $.parseJSON(msg);
				
				if(msg.ret == "success"){
					
					var fileurl = msg.path;
					
					edu.gmu.csiss.geoweaver.result.download_path("../" + fileurl, file_name); //remove web from Geoweaver/web
					
				}else{
					
					alert("Fail to download!");
					
				}
				
			}).fail(function(jqXHR, textStatus, errorThrown){
				
				console.error(textStatus + errorThrown);
				
			});
			
		},
		
		isIn: function(target, array) {
		    for(var i=0; i<array.length; i++){
			   if(array[i] == target) 
			      return true;
		    }
		    return false;
		},
		
		operatefile: function(file_name, file_size){
			
			var patt1 = /\.([0-9a-z]+)(?:[\?#]|$)/i;
			
			var suffix = file_name.match(patt1);
			
			if(Number(file_size) < 10*1024*1024 && edu.gmu.csiss.geoweaver.filebrowser.isIn(suffix[1],["txt", "py", "sh", "java", "log", "js", "r", "c", "cpp", "f", "go", "sql", "php", "perl", "js"]) ){
				
				//edit the file
				edu.gmu.csiss.geoweaver.filebrowser.openFileEditor(file_name);
				
			}else{
				
				//directly download the file
				edu.gmu.csiss.geoweaver.filebrowser.downloadFile(file_name);
				
			}
			
		},
		
		continuebrowser: function(file_name){
			
			$.ajax({
				
				url: "openfilebrowser",
				
				data: {"init_path": edu.gmu.csiss.geoweaver.filebrowser.current_path + file_name + "/"},
			
				method: "POST"
				
			}).done(function(msg){
				
				msg = $.parseJSON(msg);
				
				if(msg.ret!="failure")
				
					edu.gmu.csiss.geoweaver.filebrowser.updateBrowser(msg);
				
				else
					
					alert("Fail to open the directory: " + msg.msg);
				
			}).fail(function(error){
				
				alert("Fail to send request to continue file browser" + error);
				
			});
			
		},
		
		s2Date: function(seconds){
			
			var m = new Date(Number(seconds)*1000);
			
			var dateString =
			    m.getUTCFullYear() + "/" +
			    ("0" + (m.getUTCMonth()+1)).slice(-2) + "/" +
			    ("0" + m.getUTCDate()).slice(-2) + " " +
			    ("0" + m.getUTCHours()).slice(-2) + ":" +
			    ("0" + m.getUTCMinutes()).slice(-2) + ":" +
			    ("0" + m.getUTCSeconds()).slice(-2);
			
			return dateString;
			
		},
		
		updateBrowser: function(msg){
			
			edu.gmu.csiss.geoweaver.filebrowser.current_path = msg.current;
			
			var parentfolder = "..";
			
			if(edu.gmu.csiss.geoweaver.filebrowser.current_path == "/")
				
				parentfolder = ".";
			
			var cont = '<tr>'+
			  '    <td class="col-md-6" style="word-wrap:break-word;"><span><i class="pull-left fa fa-folder"></i><a style="word-wrap:break-word;" href="javascript:edu.gmu.csiss.geoweaver.filebrowser.continuebrowser(\''+
			  parentfolder+'\')" >'+parentfolder+'</a></span></td>'+
			  '    <td> </td>'+
			  '    <td> </td>'+
			  '    <td> </td>'+
			  '  </tr>';
			
			for(var i=0;i<msg.array.length;i++){
				
				cont += '<tr>';
				
				if(msg.array[i].isdirectory){
					
					cont += '    <td class="col-md-6 word-wrap" ><span><i class="pull-left fa fa-folder"></i><a class="word-wrap" href="javascript:edu.gmu.csiss.geoweaver.filebrowser.continuebrowser(\'' + 
			  			msg.array[i].name + '\')" >' +msg.array[i].name+'</a></span></td>';
						
				}else{
					
					cont += '    <td class="col-md-5 word-wrap"><span><i class="pull-left fa fa-file"></i><a  class="word-wrap" href="javascript:edu.gmu.csiss.geoweaver.filebrowser.operatefile(\'' + 
			  			msg.array[i].name + '\', \'' + msg.array[i].size + '\')" >' +msg.array[i].name+'</a></span></td>';
					
				}
				
				cont +=  '    <td>'+edu.gmu.csiss.geoweaver.filebrowser.s2Date(msg.array[i].mtime)+'</td>'+
				  '    <td>'+msg.array[i].size+'</td>'+
				  '    <td>'+msg.array[i].mode+'</td>'+
				  '  </tr>';
				
			}
			
			$("#directory_table > tbody").html(cont);
			
		},
		
		showFolderBrowserDialog: function(msg){
			
			var cont = '<div class=\"row\"  style="padding:10px;">';
			
			cont += '<table class="table table-sm table-dark col-md-12" id="directory_table"> '+
				'  <thead> '+
				'    <tr> '+
				'      <th class="col-md-5 word-wrap">Name</th> '+
				'      <th>Last Modified</th> '+
				'      <th>Size</th> '+
				'      <th>Mode</th> '+
				'    </tr> '+
				'  </thead> '+
				'  <tbody>'+ 
				'  </tbody></table></div>';
			
			BootstrapDialog.show({
				
				title: 'File Browser',
	            
	            message: cont,
	            
	            size: BootstrapDialog.SIZE_WIDE,
	            
	            closable: false,
	            
	            onshown: function(){
	            	
	            	edu.gmu.csiss.geoweaver.filebrowser.updateBrowser(msg);
	            	
	            },
	            
	            buttons: [{
	            	
	                label: 'Close',
	                
	                action: function(dialog) {
	                	
	                	$.ajax({
	                		
	                		url: "closefilebrowser",
	                		
	                		method: "POST"
	                		
	                	}).done(function(msg){
	                		
	                		console.log(msg);
	                		
	                	});
	                	
	                	dialog.close();
	                
	                }
	            
	            }]
				
			});
			
		},
		
		connect_folder: function(encrypt, req, dialog, button){
			
			req.pswd = encrypt;
			
			$.ajax({
				
				url: "openfilebrowser",
				
				data: req,
				
				method: "POST"
				
			}).done(function(msg){
				
				msg = $.parseJSON(msg);
				
				if(msg.current.length){
					
					edu.gmu.csiss.geoweaver.filebrowser.showFolderBrowserDialog(msg);
					
				}else{
					
					alert("Fail to open file browser");
					
				}

				dialog.close();
				
			}).fail(function(error){
				
				alert("Fail to send folder open request");
				
				button.enable();
				
				button.stopSpin();
				
			});
			
		},
		
		start: function(hid){
			
			var req = { hid: hid, init_path: "/home/"}
			
			edu.gmu.csiss.geoweaver.host.start_auth_single(hid, req, edu.gmu.csiss.geoweaver.filebrowser.connect_folder);
			
		}
		
}
