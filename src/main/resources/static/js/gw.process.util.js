/**
 * Separate the utility function shared by process and process.sidepanel
 */

GW.process.util = {

    displayCodeArea: function(code_type, code, code_editor_container_id, process_window_container_id){
		
		$(code_editor_container_id).html("");

		$(code_editor_container_id).css({ 'overflow-y' : 'scroll'});

        $( process_window_container_id).css( "background-color", "white" );

		if(code_type == "jupyter"){

			$(code_editor_container_id).append(`<p style="margin:5px;" class="pull-right"><span class="badge badge-secondary">double click</span> to edit <span class="badge badge-secondary">Ctrl+Enter</span> to save <i class="fa fa-upload subalignicon"   data-toggle="tooltip" title="upload a new notebook to replace the current one" onclick="GW.process.uploadAndReplaceJupyterCode();"></i></p><br/>`);
			
			if(code != null && code != "null"){

				code = GW.general.parseResponse(code);
				
				GW.process.jupytercode = code;

				var notebook = nb.parse(code);
				
				var rendered = notebook.render();
				
				$(code_editor_container_id).append(rendered);

				nb.postlisten();

				var newjupyter = nb.getjupyterjson();

			}
			
		}else if(code_type=="builtin"){
			
			code = code.replace(/\\/g, '\\\\');
			
			code = GW.general.parseResponse(code)
			
			var cont = '     <label for="builtinprocess" class="col-sm-4 col-form-label control-label" style="font-size:12px;" >Select a process: </label>'+
			'     <div class="col-sm-8"> <select class="form-control builtin-process" id="builtin_processes">';
			
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

			   $(code_editor_container_id).html(cont);

			$("#builtin_processes").on("change", function(){

				GW.process.refreshBuiltinParameterList("builtin_processes", code_editor_container_id.substring(1));
				
				// GW.process.updateBuiltin();

			})

			$("#builtin_processes").val(code.operation);

			$("#builtin_processes").trigger("change");

			   for(var j=0;j<code.params.length;j+=1){
				   
				   $("#param_" + code.params[j].name).val(code.params[j].value);
				   
			   }
			
		}else{
			
			var lang = GW.general.getCodeStyleByLang(code_type);
			
			val = GW.process.unescape(code);
			
			code = val;

			$(process_window_container_id).css( "background-color", "rgb(28,28,28)" );

			$(code_editor_container_id).css({ 'overflow-y' : ''});
			
			let neweditor = CodeMirror(document.getElementById(code_editor_container_id.substring(1)), {
					lineNumbers: true,
					lineWrapping: true,
					theme: "yonce",
					mode: "python",
					readOnly: false,
					value: code,
					foldGutter: true,
					gutters: ["CodeMirror-linenumbers", "CodeMirror-foldgutter"],
					extraKeys: {
							
							"Ctrl-L": function(){
								console.log("ctrl l clicked")
							},
								
							"Ctrl-Space": "autocomplete",
							"Ctrl-B": "blockComment",
							"Ctrl-/": "toggleComment",
							"Ctrl-F-D": "foldCode",
							"Ctrl-Q": function(cm){ cm.foldCode(cm.getCursor()); }
					}
			});

			neweditor.foldCode(CodeMirror.Pos(0, 0));
			
			neweditor.on("change", function(instance, event){

				GW.process.showNonSaved();

			  });
//				$(".CodeMirror").css('font-size',"10pt");
			$(".CodeMirror").css('height',"100%");
			$(".CodeMirror").css('max-height',"100%");
			
			GW.process.util.refreshCodeEditor();

            return neweditor
		}
		
	},

    refreshCodeEditor: function(){

		// console.log("Process Code Editor is refreshed..");
		
		if(GW.process.editor!=null) GW.process.editor.refresh();
        if(GW.process.sidepanel.editor!=null) GW.process.sidepanel.editor.refresh();

	},

}
