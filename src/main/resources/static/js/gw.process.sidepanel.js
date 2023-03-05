/**
 * 
 */

GW.process.sidepanel = {

    init: function(){

    },

    display: function(message){

        // use different div container id


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