
GW.filebrowser = {
		
		current_path: null,
		
		current_hid: null,
		
		editor: null,
		
		edit_file: 0,
		
		openFileEditor: function(file_name){
			
			$.ajax({
				
				url: "retrievefile",
				
				method: "POST",
				
				data: { "filepath" : GW.filebrowser.current_path + file_name}
				
			}).done(function(msg){
				
				msg = $.parseJSON(msg);
				
				if(msg.ret == "success"){
					
					GW.filebrowser.edit_file = 1;
					
					var content = "<div class=\"modal-body\" style=\"font-size:12px;\" ><div id=\"codearea\" class=\"form-group row required\" ></div>"+
					"<button id=\"loading_btn\" class=\"btn btn-sm btn-warning\"><span class=\"glyphicon glyphicon-refresh glyphicon-refresh-animate\"></span> Loading...</button></div>" + 
					'<div class="modal-footer">' +
					"	<button type=\"button\" id=\"browser-save\" class=\"btn btn-outline-primary\">Save</button> "+
					"	<button type=\"button\" id=\"browser-run\" class=\"btn btn-outline-primary\">Run</button> "+
					'</div>'
					
					var frame = GW.process.createJSFrameDialog(800, 640, content, 'File Editor')
					
					$("#codearea").append('<textarea id="code_editor" placeholder=""></textarea>');
	            	
					GW.filebrowser.editor = CodeMirror.fromTextArea(document.getElementById("code_editor"), {
		        		
		        		lineNumbers: true,
		        		lineWrapping: true
		        	});
					
					var url_path = msg.path;
					
					//prevent it loading from cache
					$.ajaxSetup ({
					    // Disable caching of AJAX responses
					    cache: false
					});
					
					$.get( "../" + url_path, function( data ) {
						
						GW.filebrowser.editor.setValue(data);
						
						$("#loading_btn").hide();
						
					});
					
					$.ajaxSetup ({
					    // Enable caching of AJAX responses
					    cache: true
					});
					
					frame.on('closeButton', 'click', (_frame, evt) => {
						
	                	$.ajax({
		            		
		            		url: "closefilebrowser",
		            		
		            		method: "POST"
		            		
		            	}).done(function(msg){
		            		
		            		console.log(msg);
		            		
		            	});
	                	
		                _frame.closeFrame();
		                
		            });
					
					$("#browser-save").click(function(){
						
						$.ajax({
	                		
	                		url: "updatefile",
	                		
	                		method: "POST",
	                		
	                		data: { filepath: GW.filebrowser.current_path + file_name, 
	                			content: GW.filebrowser.editor.getValue()}
	                		
	                	}).done(function(msg){
	                		
	                		msg = $.parseJSON(msg);
	                		
	                		if(msg.ret == "success"){
	                			
		                		console.log("file updated");
	                			
		                		alert("Saved!!");
		                		
	                		}else{
	                			
	                			alert("Failed!!" + msg.reason);
	                		}
	                		
	                	});
	                	
						
					});
					
					$("#browser-run").click(function(){

	                	var patt1 = /\.([0-9a-z]+)(?:[\?#]|$)/i;
	        			
	        			var suffix = file_name.match(patt1);
	                	
	                	if(GW.filebrowser.isIn(suffix[1],["py", "sh"])){
	                		
	                		//step 1: add the file as a new process
	                		
	                		//step 2: pop-up the run dialog of the process
	                		
	                		var type = "shell";
	                		
	                		if("py"==suffix[1]){
	                			
	                			type = "python";
	                			
	                		}
	                		
		                	var req = {
		                			
		                			name: file_name,
		                			
		                			filepath: GW.filebrowser.current_path + file_name,
		                			
		                			hid: GW.filebrowser.current_hid,
		                			
		                			type: type,
		                			
		                			content: GW.filebrowser.editor.getValue()
		                			
		                	};
		                	
		                	$.ajax({
		                		
		                		url: "addLocalFile",
		                		
		                		method: "POST",
		                		
		                		data: req
		                		
		                	}).done(function(msg){
		                		
		                		msg = $.parseJSON(msg);
		                		
		                		var pid = msg.id;
		                		
		                		GW.process.addMenuItem(msg, type);
		                		
		                		GW.process.executeProcess(pid, GW.filebrowser.current_hid, type);
		                		
		                		GW.ssh.addlog("The process " + msg.name + " is added to the process list.");
		                		GW.ssh.addlog("Pop up authorization dialog to initiate the run of the process : " + pid);
		                		
		                	});
		                	
	                	}else{
	                		
	                		alert("Only Python and Shell script can run!");
	                		
	                	}
						
						
					});
					
					
				}else{
					
					alert("Fail to retrieve file: " + msg.reason);
					
				}
				
			});
			
		},
		
		downloadFile: function(file_name){
			
			var path = GW.filebrowser.current_path + file_name;
			
			var req = {filepath : path};
			
			$.ajax({
				
				url: "retrievefile",
				
				data: req,
				
				method: "POST"
					
			}).done(function(msg){
				
				msg = $.parseJSON(msg);
				
				if(msg.ret == "success"){
					
					var fileurl = msg.path;
					
					GW.result.download_path("../" + fileurl, file_name); //remove web from Geoweaver/web
					
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
			
			GW.general.showToasts("Preparing the file, please wait..");
			
			var patt1 = /\.([0-9a-z]+)(?:[\?#]|$)/i;
			
			var suffix = file_name.match(patt1);

			if(suffix !== null){
				if(Number(file_size) < 10*1024*1024 && GW.filebrowser.isIn(suffix[1],["txt", "py", "sh", "java", "log", "js", "r", "c", "cpp", "f", "go", "sql", "php", "perl", "js"]) ){
				
					//edit the file
					GW.filebrowser.openFileEditor(file_name);
					
				}else{
					
					//directly download the file
					GW.filebrowser.downloadFile(file_name);
					
				}
			}else{
					
				//directly download the file
				GW.filebrowser.downloadFile(file_name);
				
			}
			
			
			
		},
		
		continuebrowser: function(file_name){
			
			$.ajax({
				
				url: "openfilebrowser",
				
				data: {"init_path": GW.filebrowser.current_path + file_name + "/"},
			
				method: "POST"
				
			}).done(function(msg){
				
				msg = $.parseJSON(msg);
				
				if(msg.ret!="failure")
				
					GW.filebrowser.updateBrowser(msg);
				
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
			
			GW.filebrowser.current_path = msg.current;
			
			var parentfolder = "..";
			
			if(GW.filebrowser.current_path == "/")
				
				parentfolder = ".";
			
			var cont = '<tr>'+
			  '    <td class="col-md-6" style="word-wrap:break-word;"><span><i class="pull-left fa fa-folder" style="padding-right: 5px;"></i><a style="word-wrap:break-word;" href="javascript:GW.filebrowser.continuebrowser(\''+
			  parentfolder+'\')" > '+parentfolder+' </a></span></td>'+
			  '    <td> </td>'+
			  '    <td> </td>'+
			  '    <td> </td>'+
			  '  </tr>';
			
			for(var i=0;i<msg.array.length;i++){
				
				cont += '<tr>';
				
				if(msg.array[i].isdirectory){
					
					cont += '    <td class="col-md-6 word-wrap" ><span><i class="pull-left fa fa-folder" style="padding-right: 5px;"></i><a class="word-wrap" href="javascript:GW.filebrowser.continuebrowser(\'' + 
			  			msg.array[i].name + '\')" >' +msg.array[i].name+'</a></span></td>';
						
				}else{
					
					cont += '    <td class="col-md-5 word-wrap"><span><i class="pull-left fa fa-file" style="padding-right: 5px;"></i><a  class="word-wrap" href="javascript:GW.filebrowser.operatefile(\'' + 
			  			msg.array[i].name + '\', \'' + msg.array[i].size + '\')" >' +msg.array[i].name+'</a></span></td>';
					
				}
				
				cont +=  '    <td>'+GW.filebrowser.s2Date(msg.array[i].mtime)+'</td>'+
				  '    <td>'+msg.array[i].size+'</td>'+
				  '    <td>'+msg.array[i].mode+'</td>'+
				  '  </tr>';
				
			}
			
			$("#directory_table > tbody").html(cont);
			
		},
		
		sortTable: function (n) {
			
			  var table, rows, switching, i, x, y, shouldSwitch, dir, switchcount = 0;
			  table = document.getElementById("directory_table");
			  switching = true;
			  // Set the sorting direction to ascending:
			  dir = "asc";
			  /* Make a loop that will continue until
			  no switching has been done: */
			  while (switching) {
			    // Start by saying: no switching is done:
			    switching = false;
			    rows = table.rows;
			    /* Loop through all table rows (except the
			    first, which contains table headers): */
			    for (i = 1; i < (rows.length - 1); i++) {
			      // Start by saying there should be no switching:
			      shouldSwitch = false;
			      /* Get the two elements you want to compare,
			      one from current row and one from the next: */
			      x = rows[i].getElementsByTagName("TD")[n];
			      y = rows[i + 1].getElementsByTagName("TD")[n];
			      /* Check if the two rows should switch place,
			      based on the direction, asc or desc: */
			      if (dir == "asc") {
			        if (x.innerHTML.toLowerCase() > y.innerHTML.toLowerCase()) {
			          // If so, mark as a switch and break the loop:
			          shouldSwitch = true;
			          break;
			        }
			      } else if (dir == "desc") {
			        if (x.innerHTML.toLowerCase() < y.innerHTML.toLowerCase()) {
			          // If so, mark as a switch and break the loop:
			          shouldSwitch = true;
			          break;
			        }
			      }
			    }
			    if (shouldSwitch) {
			      /* If a switch has been marked, make the switch
			      and mark that a switch has been done: */
			      rows[i].parentNode.insertBefore(rows[i + 1], rows[i]);
			      switching = true;
			      // Each time a switch is done, increase this count by 1:
			      switchcount ++;
			    } else {
			      /* If no switching has been done AND the direction is "asc",
			      set the direction to "desc" and run the while loop again. */
			      if (switchcount == 0 && dir == "asc") {
			        dir = "desc";
			        switching = true;
			      }
			    }
			  }
		},
		
		closeBrowser: function(){
			
			if(GW.filebrowser.edit_file==0){
        		
        		//only close connection when the file editor is not present
        		$.ajax({
            		
            		url: "closefilebrowser",
            		
            		method: "POST"
            		
            	}).done(function(msg){
            		
            		console.log(msg);
            		
            	});
            	
        	}
			
			$("#host-file-browser").html("");
			
		},
		
		showFolderBrowserDialog: function(msg){
			
			var cont = '<div class="modal-body" style=\"font-size: 12px;\">'+
			
			'<div class=\"row\"  style="padding:10px;">';
			
			cont += '<table class="table table-sm table-dark col-md-12" id="directory_table"> '+
				'  <thead> '+
				'    <tr> '+
				'      <th class="col-md-5 word-wrap"  onclick="GW.filebrowser.sortTable(0)" >Name</th> '+
				'      <th  onclick="GW.filebrowser.sortTable(1)" >Last Modified</th> '+
				'      <th  onclick="GW.filebrowser.sortTable(2)" >Size</th> '+
				'      <th  onclick="GW.filebrowser.sortTable(3)" >Mode</th> '+
				'    </tr> '+
				'  </thead> '+
				'  <tbody>'+ 
				'  </tbody></table></div>'+
				
				'</div>';
			
			cont ="<h4 class=\"border-bottom\">File Browser Section  <button type=\"button\" class=\"btn btn-secondary btn-sm\" id=\"closeFileBrowser\" >close</button></h4>"+ cont;
			
			
//			var frame = GW.process.createJSFrameDialog(800, 640, cont, 'File Browser')
			
			$("#host-file-browser").html(cont);
			
	    	GW.filebrowser.updateBrowser(msg);
        	
        	GW.filebrowser.edit_file = 0;
        	
        	$("#closeFileBrowser").click(this.closeBrowser);
        	
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
					
					GW.filebrowser.showFolderBrowserDialog(msg);
					
				}else{
					
					alert("Fail to open file browser");
					
				}

				dialog.closeFrame();
				
			}).fail(function(error){
				
				alert("Fail to send folder open request");
				
				button.enable();
				
				button.stopSpin();
				
			});
			
		},
		
		start: function(hid){
			
			var req = { hid: hid, init_path: "/home/"}
			
			if(this.current_hid == hid){
				
				req.init_path = this.current_path;
				
			}
			
			this.current_hid = hid;
			
			GW.host.start_auth_single(hid, req, GW.filebrowser.connect_folder);
			
		}
		
}
