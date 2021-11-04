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
		
		editOn: false, //false: disable is false, all fields are activated; true: all fields are deactivated.
		
		jsFrame: new JSFrame({parentElement: $('#jsframe-container')[0]}),

		env_frame: null,

		isSaved: true,
		
		envlist: {},
		
		cmid: null,  //the id used to differentiate the dialogs

		replace_jupyter_jsframe: null,
		
		builtin_processes: [
			
			{"operation":"ShowResultMap", "params":[{"name":"resultfile", "min_occurs": 1, "max_occurs": 1}]}, //multiple occurs are something for later
			
			{"operation":"DownloadData", "params":[{"name":"file_url", "min_occurs": 1, "max_occurs": 1}]}
		
		],
		
		connection_cache: [{"p":"xxxx", "h": "yyyyy"}],

		init: function(){

			

		},

		
		checkIfProcessPanelActive: function(){

			return document.getElementById("main-process-info").style.display=="block";

		},
		
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
			
			$("#codearea-"+GW.process.cmid).append('<textarea id="codeeditor-'+cmid+'" class=\"CodeMirror\" placeholder="Code goes here..." ></textarea>');
			
        	//initiate the code editor
			
			GW.process.editor = CodeMirror.fromTextArea(document.getElementById("codeeditor-" + cmid), {
        		
	        		lineNumbers: true,
	        		
	        		lineWrapping: true,

	        		extraKeys: {
	        			
		    		    // "Ctrl-S": function(instance) { 
		    		    	
		    		    // 		GW.process.update(GW.process.current_pid, cmid); //ctrl-s save already defined for the whole page
		    		    	
		    		    // }
		    		}
				
	        	});

			// GW.process.editor.on('change', function(instance, event){

			// 	GW.process.showNonSaved();

			// });
			
			GW.process.editor.on('focus', function(instance, event) {
				
				if(GW.process.editor.isReadOnly()){

					GW.general.showToasts("In Readonly Mode! Click the \"edit\" button to update.");
					
				}

			    console.log(instance, event);
				
			});
			
			$(".CodeMirror").css('font-size',"10pt");

			$(".CodeMirror").css('height',"100%");
			
			//$(".CodeMirror").css('height',"auto");
			
			// GW.process.editor.setSize("100%", "100%");
			
			if(code!=null){
				
				GW.process.editor.setValue(GW.process.unescape(code));
            	
			}else {
			
				GW.process.editor.setValue("#!/bin/bash\n#write your bash script\n");
				
			}

			this.refreshCodeEditor();
        	
		},

		refreshCodeEditor: function(){

			// console.log("Process Code Editor is refreshed..");
			
			if(GW.process.editor!=null)GW.process.editor.refresh();

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
		        	GW.process.jupytercode = JSON.stringify(this.result);
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
			
			$("#codearea-"+cmid).append('<textarea id="codeeditor-'+cmid+'" style="height:200px;" placeholder="Code goes here..."></textarea>');
			
			//initiate the code editor
			
			GW.process.editor = CodeMirror.fromTextArea(document.getElementById("codeeditor-" + cmid), {
        		
        		lineNumbers: true,
        		
        		lineWrapping: true,
        		
        		theme: "yonce",

        		extraKeys: {
        			
	    		    // "Ctrl-S": function(instance) { 
	    		    	
	    		    // 	GW.process.update(GW.process.current_pid, cmid);
	    		    	
	    		    //  },

					 "Ctrl-Space": "autocomplete"
        		}
        		
        	});

			// GW.process.editor.on("change", function(instance, event){

			// 	GW.process.showNonSaved();

			// });
				
			$(".CodeMirror").css('font-size',"10pt");

			// $(".CodeMirror").css('height',"auto");
			
			// GW.process.editor.setSize(null, 360);
			
			if(code!=null){
				
            	GW.process.editor.setValue(GW.process.unescape(code));
            	
			}else {
			
				GW.process.editor.setValue("# Write first python in Geoweaver");
				
			}

			GW.process.refreshCodeEditor();
			
		},

		uploadAndReplaceJupyterCode: function(){

			GW.general.closeOtherFrames(GW.process.replace_jupyter_jsframe);

			// var content = ''+
			// 	GW.process.getProcessDialogTemplate()+
			// 	'';
			// content += '';

			var content = `<div class="modal-body">
					<div class="row"  style="font-size:12px;">
						<div class="col col-md-12">
							<span class="required-mark">*</span> 
							This panel is for importing to replace the existing jupyter notebook.
						</div>
					</div>
					<div class="row">
						<div class="col col-md-6">
							<div id="controls" style="font-size:12px;"> 
								<div id="header">IPython/Jupyter Notebook Loader</div>     
								<input type="file" id="load_jupyter" />
							</div>
						</div>
						<div class="col col-md-6">Or import from URL: <br/>
							<div class="input-group col-md-12 mb-3">
								<input type="text" class="form-control" id="jupyter_url" placeholder="Jupyter Notebook URL" aria-label="Notebook URL" aria-describedby="basic-addon2"> 
								<div class="input-group-append"> 
									<button class="btn btn-outline-secondary" id="load_jupyter_url" type="button">Import</button>
								</div>
							</div>
						</div>
					</div>
					<div id="jupyter_area"></div>
				</div>
				
				<div class="modal-footer">
					<button type="button" id="upload_replace_jupyter_confirm_btn" class="btn btn-outline-primary">Confirm</button> 
					<button type="button" id="upload_replace_jupyter_cancel_btn" class="btn btn-outline-secondary">Cancel</button>
				</div>`;

			GW.process.replace_jupyter_jsframe = GW.process.createJSFrameDialog(720,300, content, "Replace Notebook");

			this.load_jupyter();

			$("#upload_replace_jupyter_confirm_btn").click(function(){

				$("#code-embed").html("");

				$("#code-embed").append(`<p><i class="fa fa-upload subalignicon pull-right" style="margin:5px;"  data-toggle="tooltip" title="upload a new notebook to replace the current one" onclick="GW.process.uploadAndReplaceJupyterCode();"></i></p>`);

				let code = GW.process.jupytercode;

				if(code!=null && typeof code != 'undefined'){
			
					code = GW.general.parseResponse(code);
					var notebook = nb.parse(code);
					var rendered = notebook.render();
					$("#code-embed").append(rendered);
				}

				GW.process.replace_jupyter_jsframe.closeFrame();

			});

			$("#upload_replace_jupyter_cancel_btn").click(function(){

				GW.process.replace_jupyter_jsframe.closeFrame();

			});

		},
		
		showJupyter: function(code, cmid){
			
			var cont = `<div class="row"  style="font-size:12px;"><div class="col col-md-12"> <span class="required-mark">*</span> This panel is for importing jupyter notebooks as new processes. The execution is by nbconvert.</div></div>
				<div class="row"><div class="col col-md-6"><div id="controls" style="font-size:12px;"> 
                <div id="header">IPython/Jupyter Notebook Loader</div>     <input type="file" id="load_jupyter" />
				</div></div><div class="col col-md-6">Or import from URL: <br/><div class="input-group col-md-12 mb-3">
		          <input type="text" class="form-control" id="jupyter_url" placeholder="Jupyter Notebook URL" aria-label="Notebook URL" aria-describedby="basic-addon2"> 
		          <div class="input-group-append"> 
		            <button class="btn btn-outline-secondary" id="load_jupyter_url" type="button">Import</button>
		          </div>
		        </div></div></div> <div id="jupyter_area"></div>`;
			
			$("#codearea-"+cmid).append(cont);
			
			this.load_jupyter();
			
			if(code!=null && typeof code != 'undefined'){
				
				code = GW.general.parseResponse(code);
				var notebook = nb.parse(code);
				var rendered = notebook.render();
				$("#jupyter_area-"+cmid).append(rendered);
			}
			
		},
		
		showBuiltinProcess: function(code, cmid){
			
			var cont = `     <label for="builtinprocess" class="col-sm-4 col-form-label control-label" style="font-size:12px;" >Select a process: </label>
			     <div class="col-sm-8"> <select class="form-control" id="builtin_processes-` + cmid + `">`;
			
			for(var i=0;i<GW.process.builtin_processes.length;i++){
				
				cont += '    		<option value="'+GW.process.builtin_processes[i].operation +
					'">'+GW.process.builtin_processes[i].operation + '</option>';
				
			}
			
		   	cont += '  		</select></div>';
		   	
		   	for(var i=0;i<GW.process.builtin_processes[0].params.length;i++){
				
				cont += '     <label for="parameter" class="col-sm-4 col-form-label control-label" style="font-size:12px;" >Parameter <u>'
					+GW.process.builtin_processes[0].params[i].name+'</u>: </label>'+
					'     <div class="col-sm-8"> 	<input class="form-control parameter" id="param_'+
					GW.process.builtin_processes[0].params[i].name+'-'+cmid+'"></input>';
					cont += '</div>';
				
			}
			
			$("#codearea-"+cmid).append(cont);
			
			if(code!=null){
				
				code = GW.general.parseResponse(code);
				
				$("#builtin_processes-"+cmid).val(code.operation);
				
				for(var i=0;i<code.params.length;i++){
					
					$("#param_" + code.params[i].name + "-" + cmid).val(code.params[i].value);
					
				}
				
			}
			
		},
		
		getCode: function(cmid){

			cmid = cmid!=null?"-"+cmid:"";

			console.log("Get code cmid:" + cmid);
			
			var code = null;
			
			if($("#processcategory"+cmid).val()=="shell"){
				
				code = GW.process.editor.getValue();
				
			}else if($("#processcategory"+cmid).val()=="builtin"){
				
				var params = [];
				
				$(".parameter").each(function(){
					
					var newparam = {
							
							name: $(this).attr('id').split("param_")[1].split(cmid)[0],
							
							value: $(this).val()
							
					}
					
					params.push(newparam);
					
				});
				
				code = {
						
						"operation" : $("#builtin_processes").val(),
						
						"params": params
						
				}

				code = JSON.stringify(code);
				
			}else if($("#processcategory"+cmid).val()=="jupyter"){
				
				code = GW.process.jupytercode;
				
			}else if($("#processcategory"+cmid).val()=="python"){
				
				code = GW.process.editor.getValue();
//				code = $("#codeeditor-" + cmid).val();
			}
			
			return code;
			
		},
		
		newDialog: function(category){
			
			var content = '<div class="modal-body">'+
				GW.process.getProcessDialogTemplate()+
				'</div>';
			
			content += '<div class="modal-footer">' +
				"<button type=\"button\" id=\"add-process-"+GW.process.cmid+"\" class=\"btn btn-outline-primary\">Add</button> "+
				"<button type=\"button\" id=\"run-process-"+GW.process.cmid+"\" class=\"btn btn-outline-secondary\">Run</button>"+
				"<button type=\"button\" id=\"cancel-process-"+GW.process.cmid+"\" class=\"btn btn-outline-secondary\">Cancel</button>"+
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
        		
        		if(GW.process.add(false,GW.process.cmid))
        		
        			frame.closeFrame();
        		
        	});
        	
        	$("#run-process-" + GW.process.cmid).click(function(){
        		
        		if(GW.process.add(true,GW.process.cmid))
        		
        			frame.closeFrame();
        		
        	});

			$("#cancel-process-" + GW.process.cmid).click(function(){
        		
				frame.closeFrame();
        		
        	});

			//change the category if it is not null
			if(category) 
				$("#processcategory-"+GW.process.cmid).val(category).trigger('change');
			
		},
		
		recent: function(num, outside){

			$.ajax({
				
				url: "recent",
				
				method: "POST",
				
				data: "type=process&number=" + num
				
			}).done(function(msg){
				
				if(!msg.length){
					
					alert("no history found");
					
					return;
					
				}
				
				msg = GW.general.parseResponse(msg);
				
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
					
					var status_col = GW.process.getStatusCol(msg[i].id, msg[i].status);
					
					var detailbtn = null;
					
					if(outside){
						
						detailbtn = "      <td><a href=\"javascript: GW.process.showHistoryDetails('"+msg[i].id+"')\">Details</a></td> ";
						
					}else{
						
						detailbtn = "      <td><a href=\"javascript: GW.process.getHistoryDetails('"+msg[i].id+"')\">Details</a></td> ";
						
					}
					
//					detailbtn = "      <td><a href=\"javascript: void(0))\">Details</a></td> ";
					
					content += "    <tr> "+
						"      <td>"+msg[i].name+"</td> "+
						"      <td>"+msg[i].begin_time+"</td> "+
						status_col +
						detailbtn + 
						"    </tr>";
					
				}
				
				content += "</tbody></div>";
				
				var frame = GW.process.createJSFrameDialog(720, 480, content, 'History of ' + msg.name)
				
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
			
			var content = "<table class=\"table\" style=\"background-color: silver;\" id=\"history_table\"> "+
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
					"      <td><a href=\"javascript: GW.process.showHistoryDetails('"+msg[i].id+"')\">Details</a> &nbsp;";
				
				if(msg[i].status == "Running"){
					content += "		<a href=\"javascript: void(0)\" id=\"stopbtn_"+msg[i].id+"\" onclick=\"GW.process.stop('"+msg[i].id+"')\">Stop</a>";
				}
				
				content += "	   </td> "+
					"    </tr>";
				
			}
			
			content += "</tbody>";
			
			// create an interactive chart to show all the data
			
			content = 
			// "<h4 class=\"border-bottom\">History Section  <button type=\"button\" class=\"btn btn-secondary btn-sm\" id=\"closeHistory\" >close</button></h4>"+
			"<div id=\"process-chart-container\" width=\"200\" height=\"100\">"+
			"<canvas id=\"process-history-chart\" style=\"width:200px !important; height:50px !important;\" ></canvas>"+
			"</div>" + content ;
			
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
				
				msg = GW.general.parseResponse(msg);
				
				$("#process-history-container").html(GW.process.getTable(msg));
				
				GW.chart.renderProcessHistoryChart(msg);
				
				$("#closeHistory").click(function(){
					
					$("#process-history-container").html("");
					
				});
				
				console.log("Scroll to the history section.")

				GW.process.switchTab(document.getElementById("main-process-info-history-tab"), "main-process-info-history");
				
				//the code has bug, when it scrolls to the location, the header toolbar is gone. Reason unknown.
//				var elmnt = document.getElementById("process-history-container");
//				 
//				elmnt.scrollIntoView(true);
				
			}).fail(function(jxr, status){
				
				console.error(status);
				
			});
			
		},
		
		stop: function(history_id){
			
			console.log("Send stop request to stop the running process");
			
			$.ajax({
				
				url: "stop",
				
				method: "POST",
				
				data: "type=process&id=" + history_id
				
			}).done(function(msg){
				
				msg = GW.general.parseResponse(msg);
				
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
		
		/**
		 * This function is called after people click on "Details" in the process history table
		 * @param {*} history_id 
		 */
		showHistoryDetails: function(history_id){
			
			console.log("history id: " + history_id);

			$.ajax({
				
				url: "log",
				
				method: "POST",
				
				data: "type=process&id=" + history_id
				
			}).done(function(msg){

				console.log("Log Message: " + msg);
				
				if(msg==""){
					
					alert("Cannot find the process history in the database.");
					
					return;
					
				}
				
				msg = GW.general.parseResponse(msg);

				msg.code = msg.input;
				
				GW.process.display(msg);
				
				GW.process.displayOutput(msg);

				GW.process.switchTab(document.getElementById("main-process-info-code-tab"), "main-process-info-code");
				
			}).fail(function(jxr, status){
				
				console.error("Fail to get log.");
			});
			
			
		},
		
		displayOutput: function(msg){
			
			var output = msg.output;

			if(output!=null){

				output = output.replaceAll("\n", "<br/>");

			}
			
			if(msg.output=="logfile"){
				
				output = "<div class=\"spinner-border\" role=\"status\"> "+
				"	  <span class=\"sr-only\">Loading...</span> "+
				"	</div>";
				
			}
			
			console.log("Update the code with the old version")
			
			if(GW.process.editor){

				GW.process.editor.setValue(GW.process.unescape(msg.input));

				GW.process.refreshCodeEditor();

			}
			
			// output = "<h4 class=\"border-bottom\">Output Log Section <button type=\"button\" class=\"btn btn-secondary btn-sm\" id=\"closeLog\">Close</button></h4>"+
			
			output = "<p> Execution started at " + msg.begin_time + "</p>"+ 
			
			"<p> Execution ended at " + msg.end_time + "</p>"+
			
			"<p> The old code used has been refreshed in the code editor.</p>"+
			
			"<div>" + 
			
			output + "</div>";
			
			// $("#console-output").html(output);
			$("#process-log-window").html(output);
			
			$("#closeLog").click(function(){
				
				$("#console-output").html("");
				
			});
			
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

			GW.process.switchTab(document.getElementById("main-process-info-code-tab"), "main-process-info-code");

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
				
				msg = GW.general.parseResponse(msg);

				GW.process.display(msg);
				
				GW.process.displayOutput(msg);
				
			}).fail(function(){
				
				
			});
			
		},
		
		unescape: function(code){
			
			if(code != null){

				// code = code.replaceAll("<br/>", "\n"); //no long needed after using StringEscapeUtils, should remove in v1.0
				var code = code.replace(/\\n/g, "\\n")
								.replace(/\\'/g, "\\'")
								.replace(/\\"/g, '\\"')
								.replace(/\\&/g, "\\&")
								.replace(/\\r/g, "\\r")
								.replace(/\\t/g, "\\t")
								.replace(/\\b/g, "\\b")
								.replace(/\\f/g, "\\f");
			}
			
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

			var confidential_field  = '     <label for="confidential" style="font-size: 12px;" class="col-sm-2 col-form-label control-label">Confidential</label>'+
			'     <div class="col-sm-4">'+
			'       <input type="radio" name="confidential-'+GW.process.cmid+'" value="FALSE" checked> '+
	 		'		<label for="confidential-'+GW.process.cmid+'">Public</label>';
			
			if(GW.user.current_userid!=null && GW.user.current_userid!="111111")
				confidential_field += '       <input type="radio" name="confidential-'+GW.process.cmid+'" value="TRUE"> '+
				'		<label for="confidential-'+GW.process.cmid+'">Private</label>';
//		       '			<input type="text" class="form-control form-control-sm" ></input>'+
			confidential_field += '     </div>';
			
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
			   confidential_field+
		       '   </div>'+
		       '   <div class="form-group row required" id="codearea-'+GW.process.cmid+'"></div>'+
		       '   <p class="h6"> <span class="badge badge-secondary">Ctrl+S</span> to save edits. Click <i class=\"fa fa-edit subalignicon\" onclick=\"GW.process.editSwitch()\" data-toggle=\"tooltip\" title=\"Enable Edit\"></i> to enable edit. </p>'+
		       ' </form></div>';
			
			return content;
			
		},
		
		createJSFrameDialog: function(width, height, content, title){
			
			var frame = GW.process.jsFrame.create({
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
				
				msg = GW.general.parseResponse(msg);
				var content = '<div class="modal-body">'+
					GW.process.getProcessDialogTemplate() + '</div>';
				
				content += '<div class="modal-footer">' +
					"	<button type=\"button\" id=\"edit-save-process-"+GW.process.cmid+"\" class=\"btn btn-outline-primary\">Save</button> "+
					"	<button type=\"button\" id=\"edit-run-process-"+GW.process.cmid+"\" class=\"btn btn-outline-secondary\">Run</button>"+
					'</div>';
				
				var frame = GW.process.createJSFrameDialog(720, 640, content, "Edit Process")
				
            	var old_name = msg.name;
            	
            	var old_lang = msg.lang==null?msg.desc : msg.lang;
            	
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
            		
            		GW.process.runProcess(msg.id, msg.name, msg.lang);
            		
            	});
				
			}).fail(function(jxr, status){
				
				alert("Fail to get process details");
				
			});
			
		},
		
		display: function(msg){
			
			GW.process.editOn = false;

			var code = null;
			
			var code_type = null;
			
			var process_id = null;
			
			var process_name = null;
			
			msg = GW.general.parseResponse(msg);

			code_type = msg.lang==null?msg.description: msg.lang;
			
			code = msg.code;

			if(code!=null && code.includes("\\\"")){

				code = GW.process.unescape(code);

			}
			
			process_id = msg.id;
			
			process_name = msg.name;

			owner = msg.owner;

			var confidential_field = '     <div style="font-size: 12px;" class="col-sm-2 col-form-label control-label">Confidential </div>'+
			'     <div class="col-sm-3" style="padding-left:30px;">';

			if(msg.confidential=="FALSE"){

				confidential_field += '       <input type="radio" name="confidential_process" value="FALSE" checked> ';

			}else{

				confidential_field += '       <input type="radio" name="confidential_process" value="FALSE"> ';

			}
			
			confidential_field += '		<label for="confidential">Public</label>';
			
			if(GW.user.current_userid==owner && GW.user.current_userid!="111111"){

				if(msg.confidential=="TRUE"){

					confidential_field += '       <input type="radio" name="confidential_process" value="TRUE" checked> '+
						'		<label for="confidential">Private</label>';

				}else{

					confidential_field += '       <input type="radio" name="confidential_process" value="TRUE" checked> '+
						'		<label for="confidential">Private</label>';
					
				}

			}
			
			confidential_field += '     </div>';
			
			var content = "<div class=\"modal-body\" style=\"height:100%;\">";
			
			content += '  <div class="row" style="padding:0px;margin:0px;"> '+
		       '     <div style="font-size: 12px;" class="col-sm-2 col-form-label control-label">Category</div>'+
		       '     <div class="col-sm-3" style="padding:0;">'+
		       '			<select class="form-control form-control-sm" id="processcategory" disabled  >'+
			   '    			<option value="shell">Shell</option>'+
			   '    			<option value="builtin">Built-In Process</option>'+
			   '    			<option value="jupyter">Jupyter Notebook</option>'+
			   '    			<option value="python">Python</option>'+
			   /*'    			<option value="r">R</option>'+
			   '    			<option value="matlab">Matlab</option>'+*/
			   '  			</select>'+
		       '     </div>'+
		       '     <div style="font-size: 12px;" class="col-sm-2 col-form-label control-label">Name</div>'+
		       '     <div class="col-sm-4" style="padding:0;">'+
		       '			<input type="text" class="form-control form-control-sm" id="processname"></input>'+
	//		       '			<input type="text" class="form-control form-control-sm" ></input>'+
		       '     </div>'+
		       '   </div>' + 
		       '   <div class="row" style="padding:0px;margin:0px;">'+
		       '     <div style="font-size: 12px;" class="col-sm-2 col-form-label control-label">ID</div>'+
		       '     <div class="col-sm-3" style="padding:0;">'+
		       '			<input type="text" class="form-control form-control-sm" id="processid" disabled></input>'+
	//		       '			<input type="text" class="form-control form-control-sm" ></input>'+
		       '     </div>'+
			   confidential_field+
		       '   </div>'+
		       '   <div class="form-group row" style="padding:0px;margin:0px;" >'+
		       '	     <div class="col-md-6" style="padding:0;" >'+
			   '			<p class=\"h6\"> <span class=\"badge badge-secondary\">Ctrl+S</span> to save edits. Click <i class=\"fa fa-edit subalignicon\" onclick=\"GW.process.editSwitch()\" data-toggle=\"tooltip\" title=\"Enable Edit\"></i> to enable edit.'+
			   '				<label class="text-primary" style="margin-left:5px;" for="log_switch">Log</label>'+
			   '				<input type="checkbox" style="margin-left:5px;" checked id="log_switch">'+
			   ' 				<button type="button" class="btn btn-secondary btn-sm" id="showCurrent">Latest Code</button>'+  
			   '				<button type="button" class="btn btn-secondary btn-sm" id="clearProcessLog">Clear Log</button>'+
			   '			</p>'+
			   '		 </div>'+
		       '	 	 <div class="col-md-6 " style="padding:0;" id="process-btn-group"><div class=\"toast align-items-right text-white bg-success border-0\" style=\" width: fit-content; float: right; \" role="alert" aria-live="assertive" aria-atomic="true"> <div class="d-flex"> <div class="toast-body"> Editing enabled! </div> </div> </div></div>'+
			   '   </div>' ;
			
			content += "<div class=\"subtab tab titleshadow\" data-intro=\"this is a tab inside the process tab panel\">"+
			"	<button class=\"tablinks-process \" id=\"main-process-info-code-tab\" onclick=\"GW.process.openCity(event, 'main-process-info-code')\">Code</button>"+
			"	<button class=\"tablinks-process \" id=\"main-process-info-history-tab\" onclick=\"GW.process.openCity(event, 'main-process-info-history'); GW.process.history('"+
			process_id+"', '" + process_name+"')\">History</button>"+
		 	" </div>";
			
			// content += "<div id=\"main-process-info-code\" class=\"tabcontent-process\" style=\"height:calc(100% - 145px); left:0; margin:0; padding: 5px;padding-bottom:25px; border: 1px solid gray;\">";
			content += "<div id=\"main-process-info-code\" class=\"tabcontent-process generalshadow\" style=\"height:calc(100% - 165px);left:0; margin:0; padding: 0; \">";
			
			content += "<div class=\"row\" style=\"font-size: 12px; margin:0; height:100%;\" id=\"process-code-history-section\">"+
				// "			<div class=\"row\">"+
				"				<div id=\"process_code_window\" class=\"col col-md-6\" style=\"height:100%; padding:0; overflow-y:scroll;\" >"+
				// "					<h4 class=\"border-bottom\">Code"+
				// "					</h4> "+
				"					<div class=\"col col-md-6\" id=\"code-embed\" style=\"width:100%; margin-top:5px; padding: 0px; margin: 0px; height: calc(100%-50px); \" ></div>"+
				"				</div> "+
				"				<div id=\"single-console-content\" class=\"col col-md-6\" style=\"height:100%;overflow-y: scroll;\"> "+
				"					<h4 style=\"color:black\">Logging</h4> "+
				"					<div id=\"process-log-window\" style=\"padding-left: 8px;padding-top: 19px; overflow-wrap: break-word; border-left:1px solid gray;\"> </div> "+
				'   				<div class="row" style="padding:0px; margin:0px;" >'+
				'						<div class="col col-md-12" id="console-output"  style="width:100%; padding:0px; margin:0px; height:calc(100%-50px); " >'+
				'							<div class="d-flex justify-content-center"><div class="dot-flashing invisible"></div></div>'+
				'						</div>'+
				'   				</div>'+
				"				</div>";
				// "			</div>";

			content += '</div>'+
				
				'</div>';

			content += "<div id=\"main-process-info-history\" class=\"tabcontent-process generalshadow\" style=\"height:calc(100% - 165px); overflow-y: scroll; left:0; margin:0; padding: 0; display:none;\">";

			content += '   <div class="row" id="process-history-container" style="padding:0px;margin:0px; " >'+
			'   </div>';
			
			content += "</div>";
			
			$("#main-process-content").html(content);

			switchTab(document.getElementById("main-process-info-code-tab"), "main-process-info-code");

			GW.general.switchTab("process");
			
			$("#processcategory").val(code_type);
			
			$("#processname").val(process_name);
			
			$("#processid").val(process_id);
			
			GW.process.displayCodeArea(process_id, process_name, code_type,  code);
			
			GW.process.displayToolbar(process_id, process_name, code_type);
			
			$("#showCurrent").click(function(){
				
				GW.menu.details(process_id, "process");

				GW.process.showSaved();

			});

			$("#log_switch").change(function(){
				if(!this.checked){
					$("#single-console-content").hide()
					$("#process_code_window").removeClass("col-md-6");
					$("#process_code_window").addClass("col-md-12");
				}else{
					$("#single-console-content").show()
					$("#process_code_window").removeClass("col-md-12");
					$("#process_code_window").addClass("col-md-6");
				}
			})

			$("#clearProcessLog").click(GW.ssh.clearProcessLog);
			
		},
		
		openCity: function(evt, name){

			GW.process.switchTab(evt.currentTarget, name);

		},
		
		switchTab: function (ele, name){
	    		
			console.log("Turn on the tab " + name)
			  
			var i, tabcontent, tablinks;
			tabcontent = document.getElementsByClassName("tabcontent-process");
			for (i = 0; i < tabcontent.length; i++) {
			  tabcontent[i].style.display = "none";
			}
			tablinks = document.getElementsByClassName("tablinks-process");
			for (i = 0; i < tablinks.length; i++) {
			  tablinks[i].className = tablinks[i].className.replace(" active", "");
			}
			document.getElementById(name).style.display = "block";
			ele.className += " active";

			GW.process.refreshCodeEditor();
			  
		},

		displayToolbar: function(process_id, process_name, code_type){
			
			var menuItem = " <p align=\"right\">"+
			
			"<i class=\"fa fa-history subalignicon\" onclick=\"GW.process.history('"+
        	
			process_id+"', '" + process_name+"')\" data-toggle=\"tooltip\" title=\"List history logs\"></i> "+
			
			"<i class=\"fa fa-edit subalignicon\" onclick=\"GW.process.editSwitch()\" data-toggle=\"tooltip\" title=\"Enable Edit\"></i> "+
			
			"<i class=\"fa fa-play subalignicon\" onclick=\"GW.process.runProcess('"+
        	
			process_id+"', '" + process_name + "', '" + code_type +"')\" data-toggle=\"tooltip\" title=\"Run Process\"></i> "+
			
			"<i class=\"fa fa-minus subalignicon\" style=\"color:red;\"  data-toggle=\"tooltip\" title=\"Delete this process\" onclick=\"GW.menu.del('"+
        	
			process_id+"','process')\"></i>"+
			
			"</p>";
			
			$("#process-btn-group").append(menuItem);
			
		},
		
		displayCodeArea: function(process_id, process_name, code_type, code){
			
			$("#code-embed").html("");

			$( "#process_code_window" ).css( "background-color", "white" );
			
			if(code_type == "jupyter"){

				$("#code-embed").append(`<p><i class="fa fa-upload subalignicon pull-right" style="margin:5px;"  data-toggle="tooltip" title="upload a new notebook to replace the current one" onclick="GW.process.uploadAndReplaceJupyterCode();"></i></p>`);
				
				if(code != null && code != "null"){

					code = GW.general.parseResponse(code);
					
					GW.process.jupytercode = code;

					var notebook = nb.parse(code);
					
					var rendered = notebook.render();
					
					$("#code-embed").append(rendered);

				}
				
			}else if(code_type=="builtin"){
				
				code = code.replace(/\\/g, '\\\\');
				
				code = GW.general.parseResponse(code)
				
				var cont = '     <label for="builtinprocess" class="col-sm-4 col-form-label control-label" style="font-size:12px;" >Select a process: </label>'+
				'     <div class="col-sm-8"> <select class="form-control builtin-process" onchange=\"GW.process.updateBuiltin()\" id="builtin_processes">';
				
				for(var i=0;i<GW.process.builtin_processes.length;i++){
					
					var se = "";
					
					if(GW.process.builtin_processes[i].operation == code.operation){
						
						se = " selected=\"selected\" ";
						
					}
					
					cont += '    		<option value="'+
						GW.process.builtin_processes[i].operation +
						'"  ' + se + ' >'+
						GW.process.builtin_processes[i].operation + 
						'</option>';
					
				}
				
			   	cont += '  		</select></div>';
			   	
			   	for(var i=0;i<GW.process.builtin_processes[0].params.length;i++){
			   		
					cont += '     <label for="parameter" class="col-sm-4 col-form-label control-label" style="font-size:12px;" >Parameter <u>'+
					GW.process.builtin_processes[0].params[i].name+'</u>: </label>'+
					'     <div class="col-sm-8"> 	<input class="form-control builtin-parameter" id="param_'+
					GW.process.builtin_processes[0].params[i].name+'" onchange=\"GW.process.updateBuiltin()\" ></input>';
					
					cont += '</div>';
					
				}
			   	
			   	$("#code-embed").html(cont);
			   	
		   		for(var j=0;j<code.params.length;j+=1){
		   			
		   			$("#param_" + code.params[j].name).val(code.params[j].value);
		   			
		   		}
				
			}else{
				
				var lang = GW.general.getCodeStyleByLang(code_type);
				
				val = GW.process.unescape(code);
				
				code = val;

				$( "#process_code_window" ).css( "background-color", "rgb(28,28,28)" );
				
				GW.process.editor = CodeMirror(document.getElementById("code-embed"), {
//			          mode: "text/html",
//			          extraKeys: {"Ctrl-Space": "autocomplete"},
						lineNumbers: true,
						lineWrapping: true,
						theme: "yonce",
						mode: "python",
						readOnly: false,
	//			          viewportMargin: Infinity,
						value: code,
						
						extraKeys: {
			        			
				    		    // "Ctrl-S": function(instance) { 
				    		    	
				    		    // 		if(!GW.process.editOn){
				    		    			
					    		//     		var process_code = GW.process.editor.getValue()
					    		    	
					    		//     		GW.process.updateRaw(process_id, process_name, code_type, code_type, process_code);

											
					    		    	
				    		    // 		}else{
				    		    			
				    		    // 			alert("Please turn on edit mode!");
				    		    			
				    		    // 		}
				    		    	
								"Ctrl-Space": "autocomplete"
						}
			    });
				
				GW.process.editor.on("change", function(instance, event){

					GW.process.showNonSaved();

			  	});
//				$(".CodeMirror").css('font-size',"10pt");
				$(".CodeMirror").css('height',"100%");
				$(".CodeMirror").css('max-height',"100%");
				
				GW.process.refreshCodeEditor();
			}
			
		},

		clearCodeEditorListener: function(){

			if(GW.process.editor!=null){

				GW.process.editor.off("change");

			}

		},

		showNonSaved:function(){
			this.isSaved = false;
			console.log("change event called")
			$("#main-process-tab").html("Process*");

		},

		showSaved: function(){
			this.isSaved = true;
			console.log("save event called")
			$("#main-process-tab").html("Process");

		},
		
		//edit switch should always be on
		editSwitch: function(){
			
			if(GW.process.checkIfProcessPanelActive()){

				console.log("Turn on/off the fields");
				
				if(typeof $("#processid").val() != undefined){

					GW.process.current_pid = $("#processid").val();
					GW.process.update(GW.process.current_pid);
					
				
					$("#processcategory").prop( "disabled", true ); //don't allow change of process category
					
					$("#processname").prop( "disabled", GW.process.editOn );
					
					$("#processid").prop( "disabled", true ); //always cannot edit id
					
					if(GW.process.editor){
						
						GW.process.editor.setOption("readOnly", GW.process.editOn)
						
					}
					
					if($(".builtin-process")){
						
						$(".builtin-process").prop( "disabled", GW.process.editOn );
						
					}
					
					if($(".builtin-parameter")){
						
						$(".builtin-parameter").prop( "disabled", GW.process.editOn );
						
					}
				}
				
			}
			
			
		},

		refreshProcessList: function(){
			
			$.ajax({
        		
        		url: "list",
        		
        		method: "POST",
        		
        		data: "type=process"
        		
        	}).done(function(msg){
        		
        		msg = GW.general.parseResponse(msg);
        		
        		console.log("Start to refresh the process list..");
        		
        		// $("#"+GW.menu.getPanelIdByType("host")).html("");
        		$("#process_folder_shell_target").html("");
        		$("#process_folder_jupyter_target").html("");
        		$("#process_folder_builtin_target").html("");
        		$("#process_folder_python_target").html("");
        		
        		GW.process.list(msg);
        		
        		// if($(".processselector")) {

            	// 	for(var i=0;i<msg.length;i++){
            			
            	// 		$(".processselector").append("<option id=\""+msg[i].id+"\">"+msg[i].name+"</option>");
            			
            	// 	}
        			
        		// }
        		
        	}).fail(function(jxr, status){
				
				console.error("fail to list process");
				
			});
			
		},
		
		/**
		 * add a new item under the process menu
		 */
		addMenuItem: function(one, folder){
			
			var menuItem = " <li class=\"process\" id=\"process-" + one.id + "\">"+
				"<a href=\"javascript:void(0)\" onclick=\"GW.menu.details('"+one.id+"', 'process')\">" + 
				one.name + "</a>"+
//				"<i class=\"fa fa-history subalignicon\" onclick=\"GW.process.history('"+
//				one.id+"', '" + one.name+"')\" data-toggle=\"tooltip\" title=\"List history logs\"></i> "+
				"<i class=\"fa fa-plus subalignicon\" data-toggle=\"tooltip\" title=\"Add an instance\" onclick=\"GW.workspace.theGraph.addProcess('"+
				one.id+"','"+one.name+"')\"></i>"+
//				"<i class=\"fa fa-minus subalignicon\" data-toggle=\"tooltip\" title=\"Delete this process\" onclick=\"GW.menu.del('"+
//				one.id+"','process')\"></i>"+
//				"<i class=\"fa fa-edit subalignicon\" onclick=\"GW.process.edit('"+
//				one.id+"')\" data-toggle=\"tooltip\" title=\"Edit Process\"></i>"+
//				" <i class=\"fa fa-play subalignicon\" onclick=\"GW.process.runProcess('"+
//				one.id+"', '" + one.name + "', '" + one.desc +"')\" data-toggle=\"tooltip\" title=\"Run Process\"></i>"+
			" </li>";
			
			if(folder!=null){
				
				var folder_ul = $("#process_folder_" + folder + "_target");
				
				if(!folder_ul.length){
					
					$("#"+GW.menu.getPanelIdByType("process"))
						.append("<li class=\"folder\" id=\"process_folder_"+ folder +
						"\" data-toggle=\"collapse\" data-target=\"#process_folder_"+ folder +"_target\"> "+
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

		expand: function(folder){
			
			console.log("EXPAND Process type")
			
			$("#process_folder_"+folder+"_target").collapse("show");
		//	$("#"+GW.menu.getPanelIdByType("process")).collapse("show");
			
		},
		
		list: function(msg){
			
			for(var i=0;i<msg.length;i++){
				
				this.addMenuItem(msg[i], msg[i].lang==null?msg[i].description:msg[i].lang);
				
				//this.addWorkspace(msg[i]);
				
			}
			
			$('#processs').collapse("show");
			
		},

		
		
		updateBuiltin: function(){
			
			var pid = $("#processid").val();
			
			var plang = $("#processcategory").val();
			
			var pname = $("#processname").val();
			
			var pdesc = $("#processcategory").val();
			
			var oper = $(".builtin-process").val()
			
			var pcode = {operation: oper, params: []}

			var confidential = $('input[name="confidential_process"]:checked').val()
			
			$('.builtin-parameter').each(function(i, obj) {
				
				var inputfield = $(this);
				
				var paramname = inputfield.attr("id")
				
				var paramval = inputfield.val()
				
				console.log(paramname + " - " +paramval);
			    
				pcode.params.push({name: paramname.substring(6), value: paramval})
				
			});

			pcode = JSON.stringify(pcode);
			
			this.updateRaw(pid, pname, plang, pdesc, pcode, confidential);
			
		},
		
		updateRaw: function(pid, pname, plang, pdesc, pcode, confidential){
			
			var req =  {
					
					type: "process", 
					
					lang: plang,
					
					desc: pdesc, //use the description column to store the process type
				
					name: pname, 
					
					id: pid,

					owner: GW.user.current_userid,

					confidential: confidential,
	    			
					code: pcode
					
			};
			
		    	$.ajax({
		    		
		    		url: "edit/process",
		    		
		    		method: "POST",
		    		
		    		contentType: 'application/json',

					dataType: 'json',
		    		
		    		data: JSON.stringify(req)
		    		
		    	}).done(function(msg){
		    		
		    		msg = GW.general.parseResponse(msg);
		    		
		    		console.log("Updated!!");
		    		
		    		GW.general.showToasts("Code updated.");
		    		
		    		console.log("If the process name is changed, the item in the menu should be changed at the same time. ");
					
					GW.process.refreshProcessList();

					GW.process.showSaved();
					
		    	}).fail(function(jqXHR, textStatus){
		    		
		    		alert("Fail to update the process.");
		    		
		    	});
			
				
		},
		
		update: function(pid, cmid){
			
			console.log("update process id: " + pid);
			
			// if(this.precheck()){
				
			var plang = $("#processcategory").val();
			
			var pname = $("#processname").val();

			var pdesc = $("#processcategory").val();
			
			var pcode =  GW.process.getCode();

			var confidential = $('input[name="confidential_process"]:checked').val()

			if(pid!=null){
				if(plang=="builtin"){
					GW.process.updateBuiltin();
				}else{
					GW.process.updateRaw(pid, pname, plang, pdesc, pcode, confidential);
				}
				
			}
			
			// GW.process.showSaved();
				
			
			
			// }else{
				
			// 	alert("Process name and code must be non-empty!");
				
			// }
		},
		
		add: function(run, cmid){
			
			this.current_pid = null;
			
			if(this.precheck()){

				var confidential = "FALSE"; //default is public

				if(typeof $('input[name="confidential-'+cmid+'"]:checked').val() != "undefined"){
					
					confidential = $('input[name="confidential-'+cmid+'"]:checked').val()
					
				}
				
				var req = { 
					
					type: "process", 
					
					lang: $("#processcategory-"+cmid).val(),
					
					description: $("#processcategory-"+cmid).val(), //use the description column to store the process type
				
					name: $("#processname-"+cmid).val(), 
	    			
					code: GW.process.getCode(cmid),

					owner: GW.user.current_userid,

					confidential: confidential
					
				};
		    		
		    	$.ajax({
		    		
		    		url: "add/process",
		    		
		    		method: "POST",

					contentType: 'application/json',

					dataType: 'json',
		    		
		    		data: JSON.stringify(req)
		    		
		    	}).done(function(msg){
		    		
		    		msg = GW.general.parseResponse(msg);
		    		
		    		// msg.desc = req.desc;
		    		
		    		GW.process.addMenuItem(msg, req.lang);

					GW.process.expand(req.lang);
		    		
		    		if(run)
		    				
		    			GW.process.runProcess(msg.id, msg.name, $("#processcategory-"+cmid).val());
		    				
		    		
		    	}).fail(function(jqXHR, textStatus){
		    		
		    		alert("Fail to add the process.");
		    		
		    	});

				return true;
				
			}else{
				
				alert("Process name and code must be non-empty!");

				return false;
				
			}
			
		},
		
		/**
		 * create a WebSocket-based dialog for outputting the log of Bash scripts
		 */
		showSSHOutputLog: function(msg){
			
			GW.ssh.openLog(msg);
			
		},

		clearProcessLogging: function(){

			if($("#process-log-window").length){

				$("#process-log-window").html("");

			}

		},
		
		/**
		 * after the server side is done, this callback is called on each builtin process
		 */
		callback: function(msg){
			
			var oper = msg.operation;
			
			console.log("Builtin Callback Triggered");
			console.log("{{GW.process.js}}: "+msg.path)
			var filename = msg.path.replace(/^.*[\\\/]/, '')

			GW.ssh.echo("<img src=\"../download/temp/"+filename+"\" width=\"100%\" > ");

			if(oper == "ShowResultMap"){
				
				//show the map
				GW.result.preview(msg.filename);
				
				
			}else if(oper == "DownloadData"){
				
				//download the map
				GW.result.download(msg.filename);
				
			}
			
		},
		
		sendExecuteRequest: function(req, dialog, button){
			
			GW.process.clearProcessLogging();

			var newhistid = GW.general.makeid(12);

			req.history_id = newhistid;

			req.token = GW.general.CLIENT_TOKEN;

			req.operation = "ShowResultMap";

			GW.process.showSSHOutputLog({token: GW.main.getJSessionId(), history_id: newhistid});

			$.ajax({
				
				url: "executeProcess",
				
				type: "POST",
				
				data: req
				
			}).done(function(msg){
				
				if(msg){
					
					msg = GW.general.parseResponse(msg);
					
					if(msg.ret == "success"){
						
						console.log("the process is under execution.");
						
						console.log("history id: " + msg.history_id);
						
						// GW.process.showSSHOutputLog(msg);

//						if(req.desc == "builtin"){
//							
//							GW.monitor.startMonitor(msg.history_id); //"builtin" operation like Show() might need post action in the client
//							
//						}
						
					}else if(msg.ret == "fail"){
						
						alert("Fail to execute the process.");
						
						console.error("fail to execute the process " + msg.reason);
						
					}
					
//					if(dialog) dialog.close();
					
					if(dialog) {
						
						try{dialog.closeFrame(); }catch(e){}
						
					}
					
				}else{
					
					console.error("Return Response is Empty");
					
				}
				
				
			}).fail(function(jxr, status){
				
				alert("Error: unable to log on. Check if your password or the configuration of host is correct.");
				
				if($("#inputpswd").length) $("#inputpswd").val("");
				
				console.error("fail to execute the process " + req.processId);
				
			});
			
		},
		
		executeCallback: function(encrypt, req, dialogItself, button){
			
			req.pswd = encrypt;
			
			GW.process.sendExecuteRequest(req, dialogItself, button);
			
		},
		
		/**
		 * This function is to directly execute one process
		 */
		executeProcess: function(pid, hid, lang){
			
            var req = {
				
				processId: pid,
				
				hostId: hid,
				
				desc: lang,

				lang: lang
		    		
		    }
            
            if(req.lang == "python" || req.lang == "jupyter"){
            	
				//check if there is cached environment for this host
				
				var cached_env = GW.host.findEnvCache(hid);
				
				if(cached_env!=null){
					
					req.env = cached_env;
					
					GW.host.start_auth_single(hid, req, GW.process.executeCallback );
					
				}else{
					
					// retrieve the environment list of a host
					
					$.ajax({
						
						url: "env",
						
						method: "POST",
						
						data: "hid=" + hid
						
					}).done(function(msg){
						
						msg = GW.general.parseResponse(msg);
						
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
							"		<input type=\"checkbox\" class=\"form-check-input\" id=\"remember\" >"+
							"		<label class=\"form-check-label\" for=\"remember\">Don't ask again for this host</label>"+
							"   </div></div>";
						
						content += '<div class="modal-footer">' +
						"	<button type=\"button\" id=\"process-confirm-btn\" class=\"btn btn-outline-primary\">Confirm</button> "+
						"	<button type=\"button\" id=\"process-cancel-btn\" class=\"btn btn-outline-secondary\">Cancel</button>"+
						'</div>';
						
						GW.process.env_frame = GW.process.createJSFrameDialog(520, 340, content, "Set " + req.lang + " environment")
						
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
						
					}).fail(function(jxr, status){
						
						console.error("fail to get the environment on this host");
						
					});
					
					
				}
				
			}else{
				
				GW.host.start_auth_single(hid, req, GW.process.executeCallback );
				
			}
			
		},
		
		/**
		 * Show a Run process dialog
		 * @param {*} pid 
		 * @param {*} pname 
		 * @param {*} lang 
		 */
		runProcess: function(pid, pname, lang){

			if(!this.isSaved){

				if(confirm("You have non-saved changes in this process. Do you want to continue?")){

					//continue

				}else{

					return;

				}

			}
			
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
			       '		<input type="checkbox" class="form-check-input" id="remember"  />'+
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
				
				GW.process.host_frame = GW.process.createJSFrameDialog(550, 280, content, 'Select a host')
				
				$.ajax({
					
					url: "list",
					
					method: "POST",
					
					data: "type=host"
					
				}).done(function(msg){
					
					msg = GW.general.parseResponse(msg);
					
					$("#hostselector").find('option').remove().end();
					
					for(var i=0;i<msg.length;i++){
						
						if(msg[i].type=="ssh"){

							if(GW.host.isLocal(msg[i])){
							
								$("#hostselector").append("<option id=\""+msg[i].id+"\" value=\""+msg[i].ip+
										"\" selected=\"selected\">"+msg[i].name+"</option>"); // default select localhost
								
							}else{
								
								$("#hostselector").append("<option id=\""+msg[i].id+"\" value=\""+msg[i].ip+
										"\" >"+msg[i].name+"</option>");
								
							}

						}
						
						
					}
					
				}).fail(function(jxr, status){
					
					console.error("fail to list host");
					
				});
				
				$("#host-execute-btn").click(function(){
					
					var hostid = $("#hostselector").children(":selected").attr("id");
					
					var hostip = $("#hostselector").children(":selected").attr("value");
					
					console.log("host ip: " + hostip);
					
					console.log("selected host: " + hostid);
					
					if(hostip=="127.0.0.1"){
						
						GW.host.local_hid = hostid;
						
					}
					
					//remember the process-host connection
					
					if(document.getElementById('remember').checked) {
						
						GW.process.setCache(pid, hostid); //remember s
						
					}
					
					GW.process.executeProcess(pid, hostid, lang);
					
					GW.process.host_frame.closeFrame();
					
				});
				
				$("#host-cancel-btn").click(function(){
					
					GW.process.host_frame.closeFrame();
					
				});
				
			}else{
				
				GW.process.executeProcess(pid, h, lang);
				
			}
			
			
		}
		
}