/**
*
* Geoweaver Process
* 
* @author Ziheng Sun
*
*/ 

GW.process = {
		
		editor: null,
		
		jupytercode: null,
		
		current_pid: null,
		
		env_frame: null,
		
		envlist: {},
		
		cmid: null,  //the id used to differentiate the dialogs
		
		builtin_processes: [
			
			{"operation":"ShowResultMap", "params":[{"name":"resultfile", "min_occurs": 1, "max_occurs": 1}]}, //multiple occurs are something for later
			
			{"operation":"DownloadData", "params":[{"name":"file_url", "min_occurs": 1, "max_occurs": 1}]}
		
		],
		
		connection_cache: [{"p":"xxxx", "h": "yyyyy"}],
		
		clearCache: function(){
			
			this.connection_cache = [];
			
		},
		
		setCache: function(pid, hid){
			
			var is = false;
			
			for(var i=0;i<this.connection_cache.length;i++){
				
				if(this.connection_cache[i].p == pid){
					
					this.connection_cache[i].h = hid;
					
					is = true;
					
					break;
					
				}
				
			}
			
			if(!is){
				
				this.connection_cache.push({"p": pid, "h": hid});
				
			}
			
			
		},
		

		findCache: function(pid){
			
			var h = null;
			
			for(var i=0;i<this.connection_cache.length;i++){
				
				if(this.connection_cache[i].p == pid){
					
					h = this.connection_cache[i].h;
					
					break;
					
				}
				
			}
			
			return h;
			
		},
		
		precheck: function(){
			
			var valid = false;
			
			if($("#processname-"+GW.process.cmid).val()){
					
//					&&this.editor.getValue()){
				
				valid = true;
				
			}
			
			return valid;
			
		},
		
		showShell: function(code, cmid){
			
			$("#codearea-"+GW.process.cmid).append('<textarea id="codeeditor-'+cmid+'" placeholder="Code goes here..."></textarea>');
			
        	//initiate the code editor
			
			GW.process.editor = CodeMirror.fromTextArea(document.getElementById("codeeditor-" + cmid), {
        		
        		lineNumbers: true,
        		
        		lineWrapping: true,
        		
        		extraKeys: {
        			
	    		    "Ctrl-S": function(instance) { 
	    		    	
	    		    	GW.process.update(GW.process.current_pid, cmid);
	    		    	
	    		     }
	    		}
			
        	});
			
			$(".CodeMirror").css('font-size',"10pt");
			
			GW.process.editor.setSize(null, 360);
			
			if(code!=null){
				
				GW.process.editor.setValue(GW.process.unescape(code));
            	
			}else {
			
				GW.process.editor.setValue("#!/bin/bash\n#write your bash script\n");
				
			}
        	
		},
		
		load_jupyter: function(){
			
			var root = {};
			
			var $file_input = document.querySelector("input#load_jupyter");
			var $url_input = document.querySelector("button#load_jupyter_url");
			var $holder = document.querySelector("#jupyter_area");

		    var render_notebook = function (ipynb) {
		    	GW.process.jupytercode = JSON.stringify(ipynb);
		        var notebook = root.notebook = nb.parse(ipynb);
		        while ($holder.hasChildNodes()) {
		            $holder.removeChild($holder.lastChild);
		        }
		        $holder.appendChild(notebook.render());
		        Prism.highlightAll();
		    };

		    var load_file = function (file) {
		        var reader = new FileReader();
		        reader.onload = function (e) {
		        	GW.process.jupytercode = this.result;
		            var parsed = JSON.parse(this.result);
		            render_notebook(parsed);
		        };
		        reader.readAsText(file);
		    };

		    $file_input.onchange = function (e) {
		        load_file(this.files[0]);
		    };
		    
		    $url_input.onclick = function(){
		    	var url = $("#jupyter_url").val();
		    	$.ajax({
		    		dataType: "json",
	    		  	url: url
		    	}).success(function(data){
		    		render_notebook(data);
		    	});
		    };

		    document.getElementById("controls").addEventListener('dragover', function (e) {
		        e.stopPropagation();
		        e.preventDefault();
		        e.dataTransfer.dropEffect = 'copy'; // Explicitly show this is a 
//		        root.document.body.style.opacity = 0.5;
		    }, false);

		    document.getElementById("controls").addEventListener('dragleave', function (e) {
//		        root.document.body.style.opacity = 1;
		    }, false);

		    document.getElementById("controls").addEventListener('drop', function (e) {
		        e.stopPropagation();
		        e.preventDefault();
		        load_file(e.dataTransfer.files[0]);
//		        $file_input.style.display = "none";
//		        root.document.body.style.opacity = 1;
		    }, false);

			
		},
		
		showPython: function(code, cmid){
			
//			var cmid = Math.floor(Math.random() * 100);
			
			$("#codearea-"+cmid).append('<textarea id="codeeditor-'+cmid+'" placeholder="Code goes here..."></textarea>');
			
			//initiate the code editor
			
			GW.process.editor = CodeMirror.fromTextArea(document.getElementById("codeeditor-" + cmid), {
        		
        		lineNumbers: true,
        		
        		lineWrapping: true,
        		
        		extraKeys: {
        			
	    		    "Ctrl-S": function(instance) { 
	    		    	
	    		    	GW.process.update(GW.process.current_pid, cmid);
	    		    	
	    		     }
        		}
        		
        	});
				
			$(".CodeMirror").css('font-size',"10pt");
			
			GW.process.editor.setSize(null, 360);
			
			if(code!=null){
				
            		GW.process.editor.setValue(GW.process.unescape(code));
            	
			}else {
			
				GW.process.editor.setValue("# Write first python in Geoweaver");
				
			}
			
		},
		
		showJupyter: function(code, cmid){
			
			var cont = '<div class="row"  style="font-size:12px;"><div class="col col-md-12"> <span class="required-mark">*</span> This panel is for importing and editing jupyter notebooks. The execution is by nbconvert.</div></div>'+
				'<div class="row"><div class="col col-md-6"><div id="controls" style="font-size:12px;"> '+
                '<div id="header">IPython/Jupyter Notebook Loader</div>     <input type="file" id="load_jupyter" />'+
				'</div></div><div class="col col-md-6">Or import from URL: <br/><div class="input-group col-md-12 mb-3"> '+
		        '  <input type="text" class="form-control" id="jupyter_url" placeholder="Jupyter Notebook URL" aria-label="Notebook URL" aria-describedby="basic-addon2"> '+
		        '  <div class="input-group-append"> '+
		        '    <button class="btn btn-outline-secondary" id="load_jupyter_url" type="button">Import</button> '+
		        '  </div> '+
		        '</div></div></div> <div id="jupyter_area-'+cmid+'"></div>';
			
			$("#codearea-"+cmid).append(cont);
			
			this.load_jupyter();
			
			if(code!=null && typeof code != 'undefined'){
				if(typeof code != 'object'){
					code = $.parseJSON(code);
				}
				var notebook = nb.parse(code);
				var rendered = notebook.render();
				$("#jupyter_area-"+cmid).append(rendered);
			}
			
		},
		
		showBuiltinProcess: function(code, cmid){
			
			var cont = '     <label for="builtinprocess" class="col-sm-4 col-form-label control-label" style="font-size:12px;" >Select a process: </label>'+
			'     <div class="col-sm-8"> <select class="form-control" id="builtin_processes-'+cmid+'">';
			
			for(var i=0;i<GW.process.builtin_processes.length;i++){
				
				cont += '    		<option value="'+GW.process.builtin_processes[i].operation +
					'">'+GW.process.builtin_processes[i].operation + '</option>';
				
			}
			
		   	cont += '  		</select></div>';
		   	
		   	for(var i=0;i<GW.process.builtin_processes[0].params.length;i++){
				
				cont += '     <label for="parameter" class="col-sm-4 col-form-label control-label" style="font-size:12px;" >Parameter <u>'+
				GW.process.builtin_processes[0].params[i].name+'</u>: </label>'+
				'     <div class="col-sm-8"> 	<input class="form-control parameter" id="param_'+
				GW.process.builtin_processes[0].params[i].name+'"></input>';
				cont += '</div>';
				
			}
			
			$("#codearea-"+cmid).append(cont);
			
			if(code!=null){
				
				code = $.parseJSON(code);
				
				$("#builtin_processes-"+cmid).val(code.operation);
				
				for(var i=0;i<code.params.length;i++){
					
					$("#param_" + code.params[i].name).val(code.params[i].value);
					
				}
				
			}
			
		},
		
		getCode: function(cmid){
			
			var code = null;
			
			if($("#processcategory-"+cmid).val()=="shell"){
				
				code = GW.process.editor.getValue();
				
			}else if($("#processcategory-"+cmid).val()=="builtin"){
				
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
				
			}else if($("#processcategory-"+cmid).val()=="jupyter"){
				
				code = GW.process.jupytercode;
				
			}else if($("#processcategory-"+cmid).val()=="python"){
				
				code = GW.process.editor.getValue();
//				code = $("#codeeditor-" + cmid).val();
			}
			
			return code;
			
		},
		
		newDialog: function(){
			
			var content = '<div class="modal-body">'+
				GW.process.getProcessDialogTemplate()+
				'</div>';
			
			content += '<div class="modal-footer">' +
				"<button type=\"button\" id=\"add-process-"+GW.process.cmid+"\" class=\"btn btn-outline-primary\">Add</button> "+
				"<button type=\"button\" id=\"run-process-"+GW.process.cmid+"\" class=\"btn btn-outline-secondary\">Run</button>"+
				'</div>';
			
			var frame = GW.process.createJSFrameDialog(720, 640, content, "Add new process")
			
	    	GW.process.showShell(null, GW.process.cmid);
        	
        	$("#processcategory-"+GW.process.cmid).on('change', function() {
        		
        		console.log(this.id);
        		
        		$("#codearea-"+GW.process.cmid).empty();
        		
        		if( this.value == "shell"){
        			
        			GW.process.showShell(null,GW.process.cmid);
        			  
        		}else if(this.value == "builtin"){
        			
        			GW.process.showBuiltinProcess(null,GW.process.cmid);
        			  
        		}else if(this.value == "jupyter"){
        			
        			GW.process.showJupyter(GW.process.jupytercode, GW.process.cmid);
        			
        		}else if(this.value == "python"){
        			
        			GW.process.showPython(null, GW.process.cmid);
        			
        		}
        		
        	});
        	
        	$("#add-process-" + GW.process.cmid).click(function(){
        		
        		GW.process.add(false,GW.process.cmid);
        		
        		frame.closeFrame();
        		
        	});
        	
        	$("#run-process-" + GW.process.cmid).click(function(){
        		
        		GW.process.add(true,GW.process.cmid);
        		
        		frame.closeFrame();
        		
        	});
			
		},
		
		recent: function(num){

			$.ajax({
				
				url: "recent",
				
				method: "POST",
				
				data: "type=process&number=" + num
				
			}).done(function(msg){
				
				if(!msg.length){
					
					alert("no history found");
					
					return;
					
				}
				
				msg = $.parseJSON(msg);
				
				var content = "<div class=\"modal-body\" style=\"font-size: 12px;\"><table class=\"table\"> "+
				"  <thead> "+
				"    <tr> "+
				"      <th scope=\"col\">Process</th> "+
				"      <th scope=\"col\">Begin Time</th> "+
				"      <th scope=\"col\">Status</th> "+
				"      <th scope=\"col\">Action</th> "+
				"    </tr> "+
				"  </thead> "+
				"  <tbody> ";
				
				for(var i=0;i<msg.length;i++){
					
//					var status_col = "      <td><span class=\"label label-warning\">Pending</span></td> ";
//					
//					if(msg[i].end_time!=null && msg[i].end_time != msg[i].begin_time){
//						
//						status_col = "      <td><span class=\"label label-success\">Done</span></td> ";
//						
//					}else if(msg[i].end_time == msg[i].begin_time && msg[i].output != null){
//						
//						status_col = "      <td><span class=\"label label-danger\">Failed</span></td> ";
//						
//					}
					
					var status_col = GW.process.getStatusCol(msg[i].id, msg[i].status);
					
					content += "    <tr> "+
						"      <td>"+msg[i].name+"</td> "+
						"      <td>"+msg[i].begin_time+"</td> "+
						status_col +
						"      <td><a href=\"javascript: GW.process.getHistoryDetails('"+msg[i].id+"')\">Details</a></td> "+
						"    </tr>";
					
				}
				
				content += "</tbody></div>";
				
				var width = 720; var height = 480;
				
				const frame = GW.workspace.jsFrame.create({
			    		title: 'History of ' + msg.name,
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
		    	
				frame.setControl({
		            styleDisplay:'inline',
		            maximizeButton: 'zoomButton',
		            demaximizeButton: 'dezoomButton',
		            minimizeButton: 'minimizeButton',
		            deminimizeButton: 'deminimizeButton',
		            hideButton: 'closeButton',
		            animation: true,
		            animationDuration: 150,
		
		        });
		    	
		    	//Show the window
		    	frame.show();
		    	
		    	frame.setPosition((window.innerWidth - width) / 2, (window.innerHeight -height) / 2, 'LEFT_TOP');
				
//				BootstrapDialog.show({
//					
//					title: "History",
//					
//					closable: false,
//					
//					message: content,
//					
//					buttons: [{
//						
//						label: "Close",
//						
//						action: function(dialog){
//							
//							dialog.close();
//							
//						}
//						
//					}]
//					
//				});
				
			}).fail(function(jxr, status){
				
				console.error(status);
				
			});
			
		},
		
		getStatusCol: function(hid, status){
			
			var status_col = "      <td id=\"status_"+hid+"\" ><span class=\"label label-warning\">Pending</span></td> ";
			
			if(status == "Done"){
				
				status_col = "      <td id=\"status_"+hid+"\"><span class=\"label label-success\">Done</span></td> ";
				
			}else if(status == "Failed"){
				
				status_col = "      <td id=\"status_"+hid+"\"><span class=\"label label-danger\">Failed</span></td> ";
				
			}else if(status == "Running"){
				
				status_col = "      <td id=\"status_"+hid+"\"><span class=\"label label-warning\">Running <i class=\"fa fa-spinner fa-spin visible\" style=\"font-size:10px;color:red\"></i></span></td> ";
				
			}else if(status == "Stopped"){
				
				status_col = "      <td id=\"status_"+hid+"\"><span class=\"label label-default\">Stopped</span></td> ";
				
			}else{
				
				status_col = "      <td id=\"status_"+hid+"\"><span class=\"label label-primary\">Unknown</span></td> ";
				
			}
			
			return status_col;
			
		},
		
		getTable: function(msg){
			
			var content = "<table class=\"table\" id=\"history_table\"> "+
			"  <thead> "+
			"    <tr> "+
			"      <th scope=\"col\">Execution Id</th> "+
			"      <th scope=\"col\">Begin Time</th> "+
			"      <th scope=\"col\">Status</th> "+
			"      <th scope=\"col\">Action</th> "+
			"    </tr> "+
			"  </thead> "+
			"  <tbody> ";

			
			for(var i=0;i<msg.length;i++){
				
				var status_col = this.getStatusCol(msg[i].id, msg[i].status);
				
				content += "    <tr> "+
					"      <td>"+msg[i].id+"</td> "+
					"      <td>"+msg[i].begin_time+"</td> "+
					status_col +
					"      <td><a href=\"javascript: GW.process.getHistoryDetails('"+msg[i].id+"')\">Details</a> &nbsp;";
				
				if(msg[i].status == "Running"){
					content += "		<a href=\"javascript: void(0)\" id=\"stopbtn_"+msg[i].id+"\" onclick=\"GW.process.stop('"+msg[i].id+"')\">Stop</a>";
				}
				
				content += "	   </td> "+
					"    </tr>";
				
			}
			
			content += "</tbody>";
			
			return content;
			
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
				
				var content = "<div class=\"modal-body\" style=\"font-size:12px;\">"+ GW.process.getTable(msg) + "</div>";
				
				var width = 800; var height = 500;
				
				const frame = GW.workspace.jsFrame.create({
			    		title: 'History',
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
		    	
				frame.setControl({
		            styleDisplay:'inline',
		            maximizeButton: 'zoomButton',
		            demaximizeButton: 'dezoomButton',
		            minimizeButton: 'minimizeButton',
		            deminimizeButton: 'deminimizeButton',
		            hideButton: 'closeButton',
		            animation: true,
		            animationDuration: 150,
		
		        });
		    	
		    	//Show the window
		    	frame.show();
		    	
		    	frame.setPosition((window.innerWidth - width) / 2, (window.innerHeight -height) / 2, 'LEFT_TOP');
				
//				BootstrapDialog.show({
//					
//					title: "History",
//					
//					message: "<div>" + content + "</div>",
//					
//					buttons: [{
//						
//						label: "Close",
//						
//						action: function(dialog){
//							
//							dialog.close();
//							
//						}
//						
//					}]
//					
//				});
				
			}).fail(function(jxr, status){
				
				console.error(status);
				
			});
			
		},
		
		stop: function(history_id){
			
			console.log("Send stop request to stop the running task");
			
			$.ajax({
				
				url: "stop",
				
				method: "POST",
				
				data: "type=process&id=" + history_id
				
			}).done(function(msg){
				
				msg = $.parseJSON(msg);
				
				console.log("stop process is called");

				if(msg.ret=="stopped"){
					
					$("#stopbtn_" + history_id).html("<span class=\"text-success\">Stopped</span>");
					
					$("#stopbtn_" + history_id).prop("onclick", null).off("click");
					
//					<span id=\"status_"+msg[i].id+"\" class=\"label label-warning\">Pending</span>
					
					$("#status_" + history_id).html("<span class=\"label label-default\">Stopped</span>");
					
				}else{

					alert("Fail to stop.");

				}
				
			});
			
		},
		
		getHistoryDetails: function(history_id){
			
			$.ajax({
				
				url: "log",
				
				method: "POST",
				
				data: "type=process&id=" + history_id
				
			}).done(function(msg){
				
				if(msg==""){
					
					alert("Cannot find the process history in the database.");
					
					return;
					
				}
				
				msg = $.parseJSON(msg);
				
				var output = msg.output;
				
				if(msg.output=="logfile"){
					
					output = "<div class=\"spinner-border\" role=\"status\"> "+
					"	  <span class=\"sr-only\">Loading...</span> "+
					"	</div>";
					
				}
				
				var content = "<div class=\"modal-body\" style=\"font-size:12px;\"><div class=\"form-group row\">"+
				"	<div class=\"col col-md-6\"> "+
				"		<div class=\"row\">"+
				"	    	<dt class=\"col col-md-3\">Log Id</dt>"+
				"	    	<dd class=\"col col-md-7\">"+msg.id+"</dd>"+
				"		</div>"+
				"	</div>"+
				"	<div class=\"col col-md-6\"> "+
				"		<div class=\"form-group row\">"+
				"	    	<dt class=\"col col-md-3\">Process Id</dt>"+
				"	    	<dd class=\"col col-md-7\">"+msg.process+"</dd>"+
				"		</div>"+
				"	</div>"+
				"</div>"+
				"<div class=\"row\">"+
				"	<div class=\"col col-md-6\"> "+
				"		<div class=\"row\">"+
				"	    	<dt class=\"col col-md-3\">Begin Time</dt>"+
				"	    	<dd class=\"col col-md-7\">"+msg.begin_time+"</dd>"+
				"		</div>"+
				"	</div>"+
				"	<div class=\"col col-md-6\"> "+
				"		<div class=\"row\">"+
				"	    	<dt class=\"col col-md-3\">End Time</dt>"+
				"	    	<dd class=\"col col-md-7\">"+msg.end_time+"</dd>"+
				"		</div>"+
				"	</div>"+
				"</div>"+
				"<div class=\"row\"> "+
				"	<div class=\"col col-md-6\"> "+
				"		<div class=\"row\">"+
				"	    	<dt class=\"col col-md-12\">Input</dt>"+
				"	    	<dd class=\"col col-md-12 word-wrap\">"+msg.input+"</dd>"+
				"		</div>"+
				"	</div>"+
				"	<div class=\"col col-md-6\"> "+
				"		<div class=\"row\">"+
				"	    	<dt class=\"col col-md-12\">Output</dt>"+
				"	    	<dd class=\"col col-md-12 word-wrap\" id=\"log-output\">"+output+"</dd>"+
				"		</div>"+
				"	</div>"+
				"</div></div>";
				
				content += '<div class="modal-footer">' +
					"<button type=\"button\" id=\"retrieve-result\" class=\"btn btn-outline-primary\">Retrieve Result</button> "+
					'</div>';
				
				var width = 800; var height = 560;
				
				const frame = GW.workspace.jsFrame.create({
			    		title: 'Process Log of ' + msg.name,
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
		    	
				frame.setControl({
		            styleDisplay:'inline',
		            maximizeButton: 'zoomButton',
		            demaximizeButton: 'dezoomButton',
		            minimizeButton: 'minimizeButton',
		            deminimizeButton: 'deminimizeButton',
		            hideButton: 'closeButton',
		            animation: true,
		            animationDuration: 150,
		
		        });
		    	
		    	frame.show();
		    	
		    	frame.setPosition((window.innerWidth - width) / 2, (window.innerHeight -height) / 2, 'LEFT_TOP');
		    	
		    	$("#retrieve-result").click(function(){
		    		
		    		GW.result.showDialog(history_id);
		    		
		    	});
		    	
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
				
//				BootstrapDialog.show({
//					
//					title: "Process Log",
//					
//					size: BootstrapDialog.SIZE_WIDE,
//					
//					message: content,
//					
//					onshown: function(){
//						
//						if(msg.output=="logfile"){
//							
//							$.get("../temp/" + msg.id + ".log" ).success(function(data){
//								
//								if(data!=null)
//									$("#log-output").text(data);
//								else
//									$("#log-output").text("missing log");
//								
//							}).error(function(){
//								
//								$("#log-output").text("missing log");
//								
//							});
//							
//						}
//						
//					},
//					
//					buttons: [{
//						
//						label: "Retrieve Result",
//						
//						action: function(dialog){
//							
//							GW.result.showDialog(history_id);
//							
//						}
//						
//					},{
//						
//						label: "Close",
//						
//						action: function(dialog){
//							
//							dialog.close();
//						}
//						
//					}]
//					
//				});
				
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
		
		restoreBackspace: function(event){
			event.stopPropagation();
		},
		
		getRandomId: function(){
			
			return Math.floor(Math.random() * 1000);
			
		},
		
		getProcessDialogTemplate: function(){
			
			GW.process.cmid = Math.floor(Math.random() * 1000);
			
			var content = '<div><form>'+
		       '   <div class="form-group row required">'+
		       '     <label for="processcategory" style="font-size: 12px;" class="col-sm-2 col-form-label control-label">Language</label>'+
		       '     <div class="col-sm-4">'+
		       '			<select class="form-control form-control-sm" id="processcategory-'+GW.process.cmid+'">'+
			   '    			<option value="shell">Shell</option>'+
			   '    			<option value="builtin">Built-In Process</option>'+
			   '    			<option value="jupyter">Jupyter Notebook</option>'+
			   '    			<option value="python">Python</option>'+
			   /*'    		<option value="python">Python</option>'+
			   '    			<option value="r">R</option>'+
			   '    			<option value="matlab">Matlab</option>'+*/
			   '  		</select>'+
		       '     </div>'+
//		       '   </div>'+
//		       '   <div class="form-group row required">'+
		       '     <label for="processname" style="font-size: 12px;" class="col-sm-2 col-form-label control-label">Name</label>'+
		       '     <div class="col-sm-4">'+
		       '			<input type="text" class="form-control form-control-sm" id="processname-'+GW.process.cmid+'"></input>'+
//		       '			<input type="text" class="form-control form-control-sm" ></input>'+
		       '     </div>'+
		       '   </div>'+
		       
		       '   <div class="form-group row required" id="codearea-'+GW.process.cmid+'"></div>'+
		       
		       '   <p class="h6"> <span class="badge badge-secondary">Ctrl+S</span> to save edits.</p>'+
		       ' </form></div>';
			
			return content;
			
		},
		
		createJSFrameDialog: function(width, height, content, title){
			
			var frame = GW.workspace.jsFrame.create({
	    		title: title,
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
	    	    html: "<div style=\"font-size:12px; padding: 1px;\">" + content + "</div>"
	    	});
	    	
			frame.setControl({
                styleDisplay:'inline',
                maximizeButton: 'zoomButton',
                demaximizeButton: 'dezoomButton',
                minimizeButton: 'minimizeButton',
                deminimizeButton: 'deminimizeButton',
                hideButton: 'closeButton',
                animation: true,
                animationDuration: 150,

            });
	    	
            frame.on('closeButton', 'click', (_frame, evt) => {
                _frame.closeFrame();
                
            });
            
	    	//Show the window
	    	frame.show();
	    	
	    	frame.setPosition((window.innerWidth - width) / 2, (window.innerHeight -height) / 2, 'LEFT_TOP');
	    	
	    	return frame;
			
		},
		
		edit: function(pid){
			
			this.current_pid = pid;
			
			$.ajax({
				
				url: "detail",
				
				method: "POST",
				
				data: "type=process&id=" + pid
				
			}).done(function(msg){
				
				msg = $.parseJSON(msg);
				
				var content = '<div class="modal-body">'+
					GW.process.getProcessDialogTemplate() + '</div>';
				
				content += '<div class="modal-footer">' +
					"	<button type=\"button\" id=\"edit-save-process-"+GW.process.cmid+"\" class=\"btn btn-outline-primary\">Save</button> "+
					"	<button type=\"button\" id=\"edit-run-process-"+GW.process.cmid+"\" class=\"btn btn-outline-secondary\">Run</button>"+
					'</div>';
				
				var frame = GW.process.createJSFrameDialog(720, 640, content, "Edit Process")
				
            	var old_name = msg.name;
            	
            	var old_lang = msg.description;
            	
            	var old_code = msg.code;
            	
            	$("#processcategory-"+GW.process.cmid).val(old_lang);
            	
            	$("#processname-"+GW.process.cmid).val(msg.name);
            	
            	$("#codearea-"+GW.process.cmid).empty();
        		
        		if( old_lang == "shell"){
        			
	            	GW.process.showShell(old_code, GW.process.cmid);
        			  
        		}else if(old_lang == "builtin"){
        			
        			GW.process.showBuiltinProcess(old_code, GW.process.cmid);
        			
        		}else if(old_lang == "jupyter"){
        			
        			GW.process.showJupyter(old_code, GW.process.cmid);
        			
        		}else if(old_lang == "python"){
        			
        			GW.process.showPython(old_code, GW.process.cmid);
        			
        		}
            	
            	$("#processcategory-"+GW.process.cmid).on('change', function() {
            		
            		$("#codearea-"+GW.process.cmid).empty();
            		
            		var old_code_new = null;
            		
            		if(this.value == old_lang){
            			
            			old_code_new = old_code;
            			
            		}
            		
            		if( this.value == "shell"){
            			
    	            	GW.process.showShell(old_code_new, GW.process.cmid);
            			  
            		}else if(this.value == "builtin"){
            			
            			GW.process.showBuiltinProcess(old_code_new, GW.process.cmid);
            			
            		}else if(this.value == "jupyter"){
            			
            			GW.process.showJupyter(old_code_new, GW.process.cmid);
            			
            		}else if(this.value == "python"){
            			
            			GW.process.showPython(old_code_new, GW.process.cmid);
            			
            		}
            		
            	});
            	
            	$("#edit-save-process-"+GW.process.cmid).click(function(){
            		
            		GW.process.update(msg.id, GW.process.cmid);
            		
            	});
            	
            	$("#edit-run-process-"+GW.process.cmid).click(function(){
            		
            		//not finished yet
            		
            		GW.process.runProcess(msg.id, msg.name, msg.description);
            		
            	});
				
			}).fail(function(jxr, status){
				
				alert("Fail to get process details");
				
			});
			
		},
		
		/**
		 * add a new item under the process menu
		 */
		addMenuItem: function(one, folder){
			
			var menuItem = " <li class=\"process\" id=\"process-" + one.id + "\"><a href=\"javascript:void(0)\" onclick=\"GW.menu.details('"+one.id+"', 'process')\">" + 
    		
			one.name + "</a><i class=\"fa fa-history subalignicon\" onclick=\"GW.process.history('"+
        	
			one.id+"', '" + one.name+"')\" data-toggle=\"tooltip\" title=\"List history logs\"></i> <i class=\"fa fa-plus subalignicon\" data-toggle=\"tooltip\" title=\"Add an instance\" onclick=\"GW.workspace.theGraph.addProcess('"+
        	
			one.id+"','"+one.name+"')\"></i><i class=\"fa fa-minus subalignicon\" data-toggle=\"tooltip\" title=\"Delete this process\" onclick=\"GW.menu.del('"+
        	
			one.id+"','process')\"></i><i class=\"fa fa-edit subalignicon\" onclick=\"GW.process.edit('"+
        	
			one.id+"')\" data-toggle=\"tooltip\" title=\"Edit Process\"></i> <i class=\"fa fa-play subalignicon\" onclick=\"GW.process.runProcess('"+
        	
			one.id+"', '" + one.name + "', '" + one.desc +"')\" data-toggle=\"tooltip\" title=\"Run Process\"></i> </li>";
			
			if(folder!=null){
				
				var folder_ul = $("#process_folder_" + folder + "_target");
				
				if(!folder_ul.length){
					
					$("#"+GW.menu.getPanelIdByType("process"))
						.append("<li class=\"folder\" id=\"process_folder_"+ folder +"\" data-toggle=\"collapse\" data-target=\"#process_folder_"+ folder +"_target\"> "+
					    " <a href=\"javascript:void(0)\"> "+ folder +" </a>"+
					    " </li>"+
					    " <ul class=\"sub-menu collapse\" id=\"process_folder_"+ folder +"_target\"></ul>");
					
					folder_ul = $("#process_folder_" + folder + "_target");
					
				}
				
				folder_ul.append(menuItem)
				
			}else{
				
				$("#"+GW.menu.getPanelIdByType("process")).append(menuItem);
				
			}
			
			
			
		},
		
		/**
		 * add process object to workspace
		 */
		addWorkspace: function(one){
			
			//randomly put a new object to the blank space
			
			var instanceid = GW.workspace.theGraph.addProcess(one.id, one.name);
			
		},
		
		list: function(msg){
			
			for(var i=0;i<msg.length;i++){
				
				this.addMenuItem(msg[i], msg[i].desc);
				
				//this.addWorkspace(msg[i]);
				
			}
			
			$('#processs').collapse("show");
			
		},
		
		update: function(pid, cmid){
			
			console.log("update process id: " + pid);
			
			if(this.precheck()){
				
				var req =  { 
						
						type: "process", lang: $("#processcategory-"+cmid).val(),
						
						desc: $("#processcategory-"+cmid).val(), //use the description column to store the process type
					
						name: $("#processname-"+cmid).val(), 
						
						id: pid,
		    			
						code: GW.process.getCode(cmid)
						
				};
				
//				"type=process&lang="+$("#processcategory").val() + 
//				
//					"&name=" + $("#processname").val() + 
//					
//					"&id=" + pid +
//	    			
//		    		"&code=" + GW.process.editor.getValue();
		    	
		    	$.ajax({
		    		
		    		url: "edit",
		    		
		    		method: "POST",
		    		
		    		data: req
		    		
		    	}).done(function(msg){
		    		
		    		msg = $.parseJSON(msg);
		    		
		    		console.log("Updated!!");
		    		
		    		console.log("If the process name is changed, the item in the menu should be changed at the same time. ");
		    		
		    	}).fail(function(jqXHR, textStatus){
		    		
		    		alert("Fail to update the process.");
		    		
		    	});
				
			}else{
				
				alert("Process name and code must be non-empty!");
				
			}
		},
		
		add: function(run, cmid){
			
			this.current_pid = null;
			
			if(this.precheck()){
				
				var req = { 
					
					type: "process", 
					
					lang: $("#processcategory-"+cmid).val(),
					
					desc: $("#processcategory-"+cmid).val(), //use the description column to store the process type
				
					name: $("#processname-"+cmid).val(), 
	    			
					code: GW.process.getCode(cmid)
					
				};
		    		
		    	$.ajax({
		    		
		    		url: "add",
		    		
		    		method: "POST",
		    		
		    		data: req
		    		
		    	}).done(function(msg){
		    		
		    		msg = $.parseJSON(msg);
		    		
		    		msg.desc = req.desc;
		    		
		    		GW.process.addMenuItem(msg, req.desc);
		    		
		    		if(run)
		    				
		    			GW.process.runProcess(msg.id, msg.name, $("#processcategory-"+cmid).val());
		    				
		    		
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
			
			GW.ssh.openLog(msg.token);
			
		},
		
		/**
		 * after the server side is done, this callback is called on each builtin process
		 */
		callback: function(msg){
			
			var oper = msg.operation;
			
			if(oper == "ShowResultMap"){
				
				//show the map
				GW.result.preview(msg.filename);
				
			}else if(oper == "DownloadData"){
				
				//download the map
				GW.result.download(msg.filename);
				
			}
			
		},
		
		sendExecuteRequest: function(req, dialog, button){
			
			$.ajax({
				
				url: "executeProcess",
				
				type: "POST",
				
				data: req
				
			}).done(function(msg){
				
				msg = $.parseJSON(msg);
				
				if(msg.ret == "success"){
					
					console.log("the process is under execution.");
					
					console.log("history id: " + msg.history_id);
					
					GW.process.showSSHOutputLog(msg);
					
					if(req.desc == "builtin"){
						
						GW.monitor.startMonitor(msg.history_id); //"builtin" operation like Show() might need post action in the client
						
					}
					
				}else if(msg.ret == "fail"){
					
					alert("Fail to execute the process.");
					
					console.error("fail to execute the process " + msg.reason);
					
				}
				
//				if(dialog) dialog.close();
				
				if(dialog) {
					
					try{dialog.closeFrame(); }catch(e){}
					
				}
				
			}).fail(function(jxr, status){
				
				alert("Error: unable to log on. Check if your password or the configuration of host is correct.");
				
				if($("#inputpswd").length) $("#inputpswd").val("");
				
				if(button) button.stopSpin();
	    		
				if(dialog) dialog.enableButtons(true);
	    		
				console.error("fail to execute the process " + req.processId);
				
			});
			
		},
		
		executeCallback: function(encrypt, req, dialogItself, button){
			
			req.pswd = encrypt;
			
			GW.process.sendExecuteRequest(req, dialogItself, button);
			
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
            
            if(req.desc == "python" || req.desc == "jupyter"){
            	
            	//check if there is cached environment for this host
            	
            	var cached_env = GW.host.findEnvCache(hid);
            	
            	if(cached_env!=null){
            		
            		req.env = cached_env;
            		
            	}else{

                	// retrive the environment list of a host
                	$.ajax({
                		
                		url: "env",
                		
                		method: "POST",
                		
                		data: "hid=" + hid
                		
                	}).done(function(msg){
                		
                		msg = $.parseJSON(msg);
                		
                		if(GW.process.env_frame != null){
                			
                			try{
                				
                				GW.process.env_frame.closeFrame();
                				
                			}catch(e){}
                			
                			GW.process.env_frame = null;
                			
                		}
                		
                		var envselector = "<div class=\"form-group\">"+
                			"<label for=\"env-select\">Select Environment:</label>"+
                			"<select id=\"env-select\" class=\"form-control\"> "+
                			"	<option value=\"default\">Default</option>"+
                			"	<option value=\"new\">New</option>";
                		
                		GW.process.envlist = msg;
    						
                		for(var i=0;i<msg.length;i+=1){
                			
                			envselector += "<option value=\""+msg[i].id+"\">"+msg[i].name+"</option>";
                			
                		}
                		
                		envselector += "</select>";
                		
                		var content = '<div class="modal-body" style="font-size: 12px;">'+
                			"<form> "+
	    					"    <div class=\"row\"> "+
								envselector +
							"    </div>"+
							"	<div class=\"form-group row\"> "+
							"    <label class=\"control-label col-sm-4\" for=\"bin\">Python Command:</label> "+
							"    <div class=\"col-sm-8\"> "+
							"      <input type=\"text\" class=\"form-control\" id=\"bin\" placeholder=\"python3\" disabled> "+
							"    </div> "+
							"  	</div>"+
							"	<div class=\"form-group row\"> "+
							"    <label class=\"control-label col-sm-4\" for=\"env\">Environment Name:</label> "+
							"    <div class=\"col-sm-8\"> "+
							"      <input type=\"text\" class=\"form-control\" id=\"env\" placeholder=\"my-conda-env\" disabled> "+
							"    </div> "+
							"  	</div>"+
							"	<div class=\"form-group row\"> "+
							"    <label class=\"control-label col-sm-4\" for=\"env\">Base Directory:</label> "+
							"    <div class=\"col-sm-8\"> "+
							"      <input type=\"text\" class=\"form-control\" id=\"basedir\" placeholder=\"/tmp/\" disabled> "+
							"    </div> "+
							"  	</div>"+
							"</form>"+
							"	<div class=\"form-group col-sm-10\">"+
						    "		<input type=\"checkbox\" class=\"form-check-input\" id=\"remember\">"+
						    "		<label class=\"form-check-label\" for=\"remember\">Don't ask again for this host</label>"+
						    "   </div></div>";
                		
                		content += '<div class="modal-footer">' +
        				"	<button type=\"button\" id=\"process-confirm-btn\" class=\"btn btn-outline-primary\">Confirm</button> "+
        				"	<button type=\"button\" id=\"process-cancel-btn\" class=\"btn btn-outline-secondary\">Cancel</button>"+
        				'</div>';
                		
                		var width = 520; var height = 340;
            			
            			GW.process.env_frame = GW.workspace.jsFrame.create({
            	    		title: "Set " + req.desc + " environment",
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
            	    	
            			GW.process.env_frame.setControl({
            	            styleDisplay:'inline',
            	            maximizeButton: 'zoomButton',
            	            demaximizeButton: 'dezoomButton',
            	            minimizeButton: 'minimizeButton',
            	            deminimizeButton: 'deminimizeButton',
            	            hideButton: 'closeButton',
            	            animation: true,
            	            animationDuration: 150,
            	
            	        });
            	    	
            			GW.process.env_frame.on('closeButton', 'click', (_frame, evt) => {
            	            _frame.closeFrame();
            	            
            	        });
            	        
            	    	//Show the window
            			GW.process.env_frame.show();
            	    	
            			GW.process.env_frame.setPosition((window.innerWidth - width) / 2, (window.innerHeight -height) / 2, 'LEFT_TOP');
            	    	
            			$("#env-select").change(function(e){
							
							if($(this).val() == 'default'){
								
								$("#bin").prop('disabled', true);
								
								$("#env").prop('disabled', true);
								
								$("#basedir").prop('disabled', true);
								
							}else{
								
								$("#bin").prop('disabled', false);
								
								$("#env").prop('disabled', false);
								
								$("#basedir").prop('disabled', false);
								
								if($(this).val() != 'new'){
									
									var envid = $(this).val();
									
									for(var i=0;i<GW.process.envlist.length;i+=1){
										
										var env = GW.process.envlist[i];
										
										if(env.id == envid){
											

        									$("#bin").val(env.bin);
        									
        									$("#env").val(env.pyenv);
        									
        									$("#basedir").val(env.basedir);
        									
        									break;
										}
										
										
									}
									
								}
								
							}
							
						})
						
						$("#process-confirm-btn").click(function(){
							
							if($(this).val() == 'default'){
    	                		
    	                		req.env = { bin: "default", pyenv: "default", basedir: "default" };
    	                		
    	                	}else{
    	                		
    	                		req.env = { bin: $("#bin").val(), pyenv: $("#env").val(), basedir: $("#basedir").val() };
    	                		
    	                	}
    	                	
    	                	if($("#remember").prop('checked')){
    	                		
    	                		GW.host.setEnvCache(hid, req.env);
    	                		
    	                	}
    	                	
    	                	GW.host.start_auth_single(hid, req, GW.process.executeCallback );
    	                	
    	                	GW.process.env_frame.closeFrame();
							
						});
            			
            			$("#process-cancel-btn").click(function(){
            				
            				GW.process.env_frame.closeFrame();
            				
            			});
                		
//                		BootstrapDialog.show({
//            				
//            				title: "Set " + req.desc + " environment",
//            				
//            				message: "<form> "+
//        					"    <div class=\"row\"> "+
//        						envselector +
//        					"    </div>"+
//        					"	<div class=\"form-group row\"> "+
//        					"    <label class=\"control-label col-sm-4\" for=\"bin\">Python Command:</label> "+
//        					"    <div class=\"col-sm-8\"> "+
//        					"      <input type=\"text\" class=\"form-control\" id=\"bin\" placeholder=\"python3\" disabled> "+
//        					"    </div> "+
//        					"  	</div>"+
//        					"	<div class=\"form-group row\"> "+
//        					"    <label class=\"control-label col-sm-4\" for=\"env\">Environment Name:</label> "+
//        					"    <div class=\"col-sm-8\"> "+
//        					"      <input type=\"text\" class=\"form-control\" id=\"env\" placeholder=\"my-conda-env\" disabled> "+
//        					"    </div> "+
//        					"  	</div>"+
//        					"	<div class=\"form-group row\"> "+
//        					"    <label class=\"control-label col-sm-4\" for=\"env\">Base Directory:</label> "+
//        					"    <div class=\"col-sm-8\"> "+
//        					"      <input type=\"text\" class=\"form-control\" id=\"basedir\" placeholder=\"/tmp/\" disabled> "+
//        					"    </div> "+
//        					"  	</div>"+
//        					"</form>"+
//        					"	<div class=\"form-group col-sm-10\">"+
//        				    "		<input type=\"checkbox\" class=\"form-check-input\" id=\"remember\">"+
//        				    "		<label class=\"form-check-label\" for=\"remember\">Don't ask again for this host</label>"+
//        				    "   </div>",
//        					
//        					onshown: function(){
//        						
//        						$("#env-select").change(function(e){
//        							
//        							if($(this).val() == 'default'){
//        								
//        								$("#bin").prop('disabled', true);
//        								
//        								$("#env").prop('disabled', true);
//        								
//        								$("#basedir").prop('disabled', true);
//        								
//        							}else{
//        								
//        								$("#bin").prop('disabled', false);
//        								
//        								$("#env").prop('disabled', false);
//        								
//        								$("#basedir").prop('disabled', false);
//        								
//        								if($(this).val() != 'new'){
//        									
//        									var envid = $(this).val();
//        									
//        									for(var i=0;i<GW.process.envlist.length;i+=1){
//        										
//        										var env = GW.process.envlist[i];
//        										
//        										if(env.id == envid){
//        											
//
//                									$("#bin").val(env.bin);
//                									
//                									$("#env").val(env.pyenv);
//                									
//                									$("#basedir").val(env.basedir);
//                									
//                									break;
//        										}
//        										
//        										
//        									}
//        									
//        								}
//        								
//        							}
//        							
//        						})
//        						
//        					},
//            				
//            				buttons: [{
//            					
//            	            	label: 'Confirm',
//            	                
//            	                action: function(dialog){
//            	                	
//            	                	if($(this).val() == 'default'){
//            	                		
//            	                		req.env = { bin: "default", pyenv: "default", basedir: "default" };
//            	                		
//            	                	}else{
//            	                		
//            	                		req.env = { bin: $("#bin").val(), pyenv: $("#env").val(), basedir: $("#basedir").val() };
//            	                		
//            	                	}
//            	                	
//            	                	if($("#remember").prop('checked')){
//            	                		
//            	                		GW.host.setEnvCache(hid, req.env);
//            	                		
//            	                	}
//            	                	
//            	                	GW.host.start_auth_single(hid, req, GW.process.executeCallback );
//            	                	
//            	                	dialog.close();
//            	                	
//            	                }
//            					
//            				},{
//            					
//            					label: 'Cancel',
//            					
//            					action: function(dialog){
//            						
//            						dialog.close();
//            						
//            					}
//            					
//            				}]
//            			});
                		
                	}).fail(function(jxr, status){
        				
        				console.error("fail to get the environment on this host");
        				
        			});
        			
            		
            	}
            	
    			
    		}else{
    			
    			GW.host.start_auth_single(hid, req, GW.process.executeCallback );
    			
    		}
			
		},
		
//		checkhost: function(){
//			
//			var id = $(this).attr("id");
//			
//			console.log("the select id is " + id);
//			
//			var selectedhostid = $(this).find(":selected").attr("id");
//			
//			GW.host.checklive(selectedhostid, hostcallback);
//			
//		},
		
		runProcess: function(pid, pname, desc){
			
			//select a host
			
			var h = this.findCache(pid);
			
			if(h==null){
				
				var content = '<div class="modal-body" style="font-size: 12px;">'+
				   '<form>'+
			       '   <div class="form-group row required">'+
			       '     <label for="hostselector" class="col-sm-4 col-form-label control-label">Run Process '+pname+' on: </label>'+
			       '     <div class="col-sm-8">'+
			       '		<select class="form-control" id="hostselector" >'+
			       '  		</select>'+
			       '     <div class="col-sm-12 form-check">'+
			       '		<input type="checkbox" class="form-check-input" id="remember">'+
			       '		<label class="form-check-label" for="remember">Remember this process-host connection</label>'+
			       '     </div>'+
			       '     </div>'+
			       '   </div>'+
			       '</form></div>';
				
				content += '<div class="modal-footer">' +
				"	<button type=\"button\" id=\"host-execute-btn\" class=\"btn btn-outline-primary\">Execute</button> "+
				"	<button type=\"button\" id=\"host-cancel-btn\" class=\"btn btn-outline-secondary\">Cancel</button>"+
				'</div>';
				
				if(GW.process.host_frame!=null){
					
					try{
						GW.process.host_frame.closeFrame();
					}catch(e){}
					
					GW.process.host_frame = null;
					
				}
				
				var width = 500; var height = 540;
				
				GW.process.host_frame = GW.workspace.jsFrame.create({
			    		title: 'Select a host',
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
		    	
				GW.process.host_frame.setControl({
		            styleDisplay:'inline',
		            maximizeButton: 'zoomButton',
		            demaximizeButton: 'dezoomButton',
		            minimizeButton: 'minimizeButton',
		            deminimizeButton: 'deminimizeButton',
		            hideButton: 'closeButton',
		            animation: true,
		            animationDuration: 150,
		
		        });
		    	
				GW.process.host_frame.show();
		    	
				GW.process.host_frame.setPosition((window.innerWidth - width) / 2, (window.innerHeight -height) / 2, 'LEFT_TOP');
		    	
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
		    	
		    	$("#host-execute-btn").click(function(){
		    		
		    		var hostid = $("#hostselector").children(":selected").attr("id");
                	
                	console.log("selected host: " + hostid);
                	
                	//remember the process-host connection
                	if(document.getElementById('remember').checked) {
                	    
                		GW.process.setCache(pid, hostid); //remember s
                		
                	}
                	
                	GW.process.executeProcess(pid, hostid, desc);
                	
                	GW.process.host_frame.closeFrame();
		    		
		    	});
		    	
		    	$("#host-cancel-btn").click(function(){
		    		
		    		GW.process.host_frame.closeFrame();
		    		
		    	});
				
//				BootstrapDialog.show({
//					
//					title: "Select a host",
//					
//					closable: false,
//					
//		            message: content,
//		            
//		            onshown: function(){
//		            	
//		            	$.ajax({
//		            		
//		            		url: "list",
//		            		
//		            		method: "POST",
//		            		
//		            		data: "type=host"
//		            		
//		            	}).done(function(msg){
//		            		
//		            		msg = $.parseJSON(msg);
//		            		
//		            		$("#hostselector").find('option').remove().end();
//		            		
//		            		for(var i=0;i<msg.length;i++){
//		            			
//		            			$("#hostselector").append("<option id=\""+msg[i].id+"\">"+msg[i].name+"</option>");
//		            			
//		            		}
//		            		
//		            	}).fail(function(jxr, status){
//		    				
//		    				console.error("fail to list host");
//		    				
//		    			});
//		            	
//		            },
//		            
//		            buttons: [{
//			            
//		            	label: 'Execute',
//		                
//		                action: function(dialogItself){
//		                	
//		                	var hostid = $("#hostselector").children(":selected").attr("id");
//		                	
//		                	console.log("selected host: " + hostid);
//		                	
//		                	//remember the process-host connection
//		                	if(document.getElementById('remember').checked) {
//		                	    
//		                		GW.process.setCache(pid, hostid); //remember s
//		                		
//		                	}
//		                	
//		                	GW.process.executeProcess(pid, hostid, desc);
//		                	
//		                    dialogItself.close();
//		                    
//		                }
//		        
//		            },{
//			            
//		            	label: 'Cancel',
//		                
//		                action: function(dialogItself){
//		                	
//		                    dialogItself.close();
//		                    
//		                }
//		        
//		            }]
//		            
//				});
				
			}else{
				
				GW.process.executeProcess(pid, h, desc);
				
			}
			
			
		}
		
}