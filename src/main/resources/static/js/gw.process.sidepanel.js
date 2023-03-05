/**
 * This file is dedicated to manage the prompt process dialog. 
 * This is a separate file to avoid messing with the gw.process.js and existing function.
 * All the DIV containers are brand new for the prompt panel. 
 * !!! Don't ever share the same Div id with the normal process window !!!
 */

GW.process.sidepanel = {

    current_workflow_history_id: null,
    current_workflow_process_id: null,
    current_process_id: null,
    current_process_name: null,

    editor: null,

    init: function(){
        
    },

    open_panel: function(workflow_history_id, workflow_process_id, process_name){

        console.log(workflow_history_id + " " + workflow_process_id + " " + process_name)
        this.current_process_id = workflow_process_id.split("-")[0];
        this.current_process_name = process_name;
        this.current_workflow_history_id = workflow_history_id;
        this.current_workflow_process_id = workflow_process_id;

        $.ajax({
				
            url: "detail",
            
            method: "POST",
            
            data: "type=process&id=" + this.current_process_id
            
        }).done(function(msg){

            msg = $.parseJSON(msg);

            GW.process.sidepanel.display(msg)

        })

    },

    /**
     * Keep consistent with gw.process
     * @param {} msg 
     */
    display: function(msg){

        let code_type = msg.lang==null?msg.description: msg.lang;

        let code = msg.code;

		if(code!=null && code.includes("\\\"")){

			code = GW.process.unescape(code);

		}

        $('#prompt-process-panel').addClass('cd-panel--is-visible');

        $("#prompt-panel-main").html("");
        
        // add process metadata
        let process_metadata_content = `
            <div style="display: flex; flex-direction: row;" >
                <p style="padding-top: 4px; padding-right: 10px;">Type: </p>
                <select class="border-box-black-not-allowed" id="prompt-process-processcategory" disabled>
                    <option value="shell">Shell</option>
                    <option value="builtin">Built-In Process</option>
                    <option value="jupyter">Jupyter Notebook</option>
                    <option value="python">Python</option>
                </select>
                <p style="margin: 0; padding-top: 4px; padding-left: 20px; padding-right: 10px;">Name:</p>
                <input class="border-box-black-allowed" id="prompt-process-processname" type="text" />
            </div>
            <div style="display: flex; flex-direction: row;">
                <p style="margin: 0; padding-top: 4px; padding-left: 20px; padding-right: 10px;">ID:</p>
                <input class="border-box-black-allowed" id="prompt-process-processid" type="text"  disabled/>
                <!--<p style="margin: 0; padding-top: 4px; padding-left: 20px; padding-right: 10px;">Confidential:</p>-->
            </div>
        `
        $("#prompt-panel-main").append(process_metadata_content)

        // add process code and history combo
        let process_code_history_content = `<div id="prompt-process-editor-history-tab-panel" style="height:100%; width:100%; margin:0; padding: 0; background-color: white;">
            <div class="subtab tab titleshadow" style="margin-top: 20px; max-width: 100%">
                <button class="tablinks-process" id="prompt-process-main-process-info-code-tab" onclick="GW.process.openCity(event, 'prompt-process-main-process-info-code')">Code</button>
                <button class="tablinks-process" id="prompt-process-main-process-info-history-tab" onclick="GW.process.openCity(event, 'prompt-process-main-process-info-history'); GW.process.sidepanel.history('`+this.current_process_id+`', '` + this.current_process_name+`')">History</button>

                <!-- TODO: play button, save button, full screen button-->
                <!--
                    <button class="btn pull-right" onclick="GW.process.sidepanel.switchFullScreen()" ><i class="glyphicon glyphicon-fullscreen"></i></button>
                    <button class="btn pull-right" onclick="GW.process.sidepanel.runProcess('`+ this.current_process_id+`', '`+this.current_process_name+`', '`+code_type+`');" ><i class="glyphicon glyphicon-play"></i></button>
                    <button class="btn pull-right" onclick="GW.process.sidepanel.editSwitch()" ><i class="glyphicon glyphicon-floppy-saved"></i></button>
                -->


                <button class="btn pull-right" onclick="GW.process.sidepanel.bottomDock()" ><i class="fas fa-window-maximize"></i></button>
                <button class="btn pull-right" onclick="GW.process.sidepanel.leftDock()" ><i class="fas fa-window-maximize fa-rotate-270"></i></i></button>
            </div>

            <div id="prompt-process-main-process-info-code" class="tabcontent-process generalshadow" style="height:28rem;left:0; margin:0; padding: 0; ">
                <div class="code__container" style="font-size: 12px; margin:0; height:100%;" id="prompt-process-code-history-section">
                    <div id="prompt-process-process_code_window" class="container__left" style="height:100%; padding:0; scrollbar-color: rgb(28, 28, 28);" >
                        <div class="col col-md-6" id="prompt-process-code-embed" style="width:100%; margin-top:5px; padding: 0px; margin: 0px; height: calc(100%-50px);" ></div>
                    </div>
                    <div class="resizer" id="prompt-process-dragMe"></div>
                    <div id="prompt-process-single-console-content" class="container__right" style="height:100%; overflow-y: scroll; scrollbar-color: rgb(28, 28, 28); background-color: rgb(28, 28, 28); color: white;">
                        <h4>Logging</h4>
                        <div id="prompt-process-process-log-window" style="overflow-wrap: break-word;"> </div>
                        <div class="row" style="padding:0px; margin:0px;" >
                            <div class="col col-md-12" id="prompt-process-console-output"  style="width:100%; padding:0px; margin:0px; height:calc(100%-50px); " >
                                <div class="d-flex justify-content-center"><div class="dot-flashing invisible"></div></div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div id="prompt-process-main-process-info-history" class="tabcontent-process generalshadow" style="height:calc(100% - 250px); overflow-y: scroll; left:0; margin:0; padding: 0; display:none;">
                <div class="row" id="prompt-process-process-history-container" style="padding:0px; color:white; margin:0px; background-color:rgb(28, 28, 28);" >
                </div>
            </div>

            <div id="prompt-process-execution_context"></div>
        </div>`
        $("#prompt-panel-main").append(process_code_history_content)

        // fill in values
        $("#prompt-process-processcategory").val(code_type);
		
		$("#prompt-process-processname").val(this.current_process_name);
		
		$("#prompt-process-processid").val(this.current_process_id);
		
		GW.process.sidepanel.editor = GW.process.util.displayCodeArea(code_type,  code, "#prompt-process-code-embed", "#prompt-process-process_code_window");
		
		// GW.process.util.displayToolbar(process_id, process_name, code_type, );

        // activate buttons

        GW.process.sidepanel.bottomDock()  // default bottomdock to save space

    },

    switchFullScreen: function(){

        GW.editor.switchFullScreenUtil('#prompt-process-editor-history-tab-panel', '#prompt-process-main-process-info-code', '#prompt-process-main-process-info-history')

    },

    leftDock: function(){

        GW.process.util.leftDock("prompt-process-code-history-section", "prompt-process-process_code_window", 
            "prompt-process-single-console-content", "prompt-process-dragMe")

    },

    bottomDock: function(){

        GW.process.util.bottomDock("prompt-process-code-history-section", "prompt-process-process_code_window", 
            "prompt-process-single-console-content", "prompt-process-dragMe")

    },

    history: function(process_id, process_name){

        GW.process.util.history(process_id, "#prompt-process-process-history-container", 'process_history_table', 
			"#closeHistory", "prompt-process-main-process-info-history-tab", "prompt-process-main-process-info-history")

    },

    close: function(){

        $('#prompt-process-panel').removeClass('cd-panel--is-visible');

    },


	hideSidenav: function() {
		let sidenav = document.getElementById('sidenav-editor');
		sidenav.style.display = 'none';
	},

    whateverthisis: function(){

        console.log("comes here 444");
		tabLinks = document.getElementsByClassName("tablinks");
		for (i = 0; i < tabLinks.length; i++) {
			tabLinks[i].className = tabLinks[i].className.replace(" active", "");
		}
		document.getElementById(name).style.display = "flex";
		ele.className += " active";

		if(name === "main-dashboard"){

			GW.board.refresh();

		}
		return
    },


	showSidenav: function() {
		let sidenav = document.getElementById('sidenav-editor');
		sidenav.style.display = 'block';

		let selectedNode = GW.workspace.theGraph.state.selectedNode;
		let execution_context = document.getElementById('execution_context');
		execution_context.innerHTML = GW.workflow.showProcessLogAsText(GW.workflow.history_id, selectedNode.id, selectedNode.title);


	},


	showProcessLogAsText: function(workflow_history_id, process_id, process_title){
		$.ajax({

			url: "workflow_process_log",

			method: "POST",

			data: "workflowid="+ GW.workflow.loaded_workflow +"&workflowhistoryid=" + workflow_history_id + "&processid=" + process_id

		}).done(function(msg) {
			msg = GW.general.parseResponse(msg);

			let msgout = msg.history_output;

			if(msgout!=null){
				msgout = msgout.replaceAll("\n", "<br/>");

				return `
					<div style="overflow: scroll">
						<div class="modal-body">
							<div class="row">
								<div class="col-md-12">
									Process Name: ` + process_title + ` <br/>
									Process ID: ` + process_id + ` <br/>
									Skip: <input type="checkbox"
												 onClick='GW.workflow.skipprocess("` + workflow_history_id + `", "` + process_id + `");'
												 id="skip_process_` + process_id + `"> <br/>
									Output: <br/>
									<p> `+ msgout +` </p>
								</div>
							</div>
						</div>
					</div>`;
			} else {
				return `<div style="overflow: scroll">
					<div class="modal-body">
						<div class="row">
							<div class="col-md-12">
								Process Name: ` + process_title + ` <br/>
								Process ID: ` + process_id + ` <br/>
								Skip: <input type="checkbox"
											 onClick='GW.workflow.skipprocess("` + workflow_history_id + `", "` + process_id + `");'
											 id="skip_process_` + process_id + `"> <br/>
								Output: <br/>
							</div>
						</div>
					</div>
				</div>`;
			}
	    });

		return `<div style="overflow: scroll">
					<div class="modal-body">
						<div class="row">
							<div class="col-md-12">
								Process Name: ` + process_title + ` <br/>
								Process ID: ` + process_id + ` <br/>
								Skip: <input type="checkbox"
											 onClick='GW.workflow.skipprocess("` + workflow_history_id + `", "` + process_id + `");'
											 id="skip_process_` + process_id + `"> <br/>
								Output: <br/>
							</div>
						</div>
					</div>
				</div>`;
	},

}