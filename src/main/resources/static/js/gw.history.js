GW.history = {

    history_table_interval_id: null,

    active_process_history_list: [],

    startActiveTimer: function(){

        GW.history.history_table_interval_id = setInterval(function() {
            
            if(GW.history.active_process_history_list.length>0){

                GW.history.active_process_history_list.forEach(history_row   => {

                    console.log()
                    $("#timerBadge_"+history_row.history_id).html(
                        GW.history.calculate_duration(history_row.history_begin_time, 
                            history_row.history_end_time,
                            history_row.indicator)
                    )
    
                })

            }else{

                GW.history.stopAllTimers()

            }
            
        }, 1000);

    },

    stopAllTimers: function(){

        if(GW.history.history_table_interval_id != null){

            clearInterval(GW.history.history_table_interval_id)

            GW.history.history_table_interval_id = null

            GW.history.active_process_history_list = []
            
        }

    },

    stopOneTimer: function(history_id){

        GW.history.active_process_history_list = GW.history.active_process_history_list.filter(item => item.history_id !== history_id);

    },

    deleteAllJupyter: function(hostid, callback){

        if(confirm("WARNING: Are you sure to remove all the history? This is permanent and cannot be recovered.")){

            $.ajax({

                url: "delAllHistory",

                method: "POST",

                data: { id: hostid}

            }).done(function(msg){

                console.log("All the history has been deleted, refresh the history table");

                callback(hostid);

            }).fail(function(jxr, status){

                console.error(status + " failed to update notes, the server may lose connection. Try again. ");

            });

        }

    },

    deleteNoNotesJupyter: function(hostid, callback){

        if(confirm("WARNING: Are you sure to remove all the history without notes? This is permanent and cannot be recovered.")){

            $.ajax({

                url: "delNoNotesHistory",

                method: "POST",

                data: { id: hostid}

            }).done(function(msg){

                console.log("history without notes are deleted, refresh the history table");

                callback(hostid);

            }).fail(function(jxr, status){

                console.error(status + " failed to update notes, the server may lose connection. Try again. ");

            });

        }

    },

    /**
     * Returns an HTML status column for a process history entry based on its indicator.
     *
     * @param {string} hid - The history ID of the process entry.
     * @param {string} status - The status indicator.
     * @returns {string} - HTML content for the status column.
     */
    getProcessStatusCol: function(hid, status){

        var status_col = "      <td id=\"status_"+hid+"\" ><span class=\"label label-warning\">Pending</span></td> ";

        if(status == "Done"){

            status_col = "      <td id=\"status_"+hid+"\"><span class=\"label label-success\">Done</span></td> ";

        }else if(status == "Failed"){

            status_col = "      <td id=\"status_"+hid+"\"><span class=\"label label-danger\">Failed</span></td> ";

        }else if(status == "Running"){

            status_col = "      <td id=\"status_"+hid+"\"><span class=\"label label-warning\">Running <i class=\"fa fa-spinner fa-spin visible\" style=\"font-size:10px;color:red\"></i></span></td> ";

        }else if(status == "Stopped"){

            status_col = "      <td id=\"status_"+hid+"\"><span class=\"label label-default\">Stopped</span></td> ";

        }
        else if(status == "Skipped"){
            status_col = "      <td id=\"status_"+hid+"\"><span class=\"label label-info\">Skipped</span></td> ";
        }
        else{

            status_col = "      <td id=\"status_"+hid+"\"><span class=\"label label-primary\">Unknown</span></td> ";

        }

        return status_col;

    },

    calculate_duration: function(start_time, end_time, process_indicator){

        var startTime = new Date(start_time).getTime();
        var endTime = new Date(end_time).getTime();
        var currentTime = new Date().getTime();

        if(process_indicator != "Running"){
            currentTime = endTime;
        }

        var elapsedTime = Math.floor((currentTime - startTime) / 1000);

        var days = Math.floor(elapsedTime / (24 * 3600));
        var hours = Math.floor((elapsedTime % (24 * 3600)) / 3600);
        var minutes = Math.floor((elapsedTime % 3600) / 60);
        var seconds = elapsedTime % 60;

        // Format the time based on non-zero values
        var formattedTime = '';

        if (days > 0) {
            formattedTime += days + 'd';
        }

        if (hours > 0) {
            formattedTime += hours + 'h';
        }

        if (minutes > 0) {
            formattedTime += minutes + 'm';
        }

        if (seconds > 0) {
            formattedTime += seconds + 's';
        }else if(formattedTime == ""){
            formattedTime += '0s';
        }

        return formattedTime

    },

    padZero: function (number) {
        return number < 10 ? '0' + number : number;
    },

    
    removeFailedHistory: function(processId) {
        const formData = new FormData();
        formData.append('processId', processId);
        const options = {
            method: 'DELETE',
            body: formData,
        }
        fetch("delete-failed", options)
        // GW.process.sidepanel.history(processId, 'testing_data_integration');
    },
    /**
     * Generates an HTML table with process execution history data.
     *
     * @param {Array} msg - Array of process history data.
     * @returns {string} - HTML content of the process execution history table.
     */
    getProcessHistoryTable: function(msg){
        let hasFailedProcess;
        if (msg.length > 0) {
            hasFailedProcess = msg.some((item) => item.indicator === "Failed");
        } else {
            hasFailedProcess = false;
        }

        let content = `
        <div style="display: flex; flex-direction: row; justify-content: space-between;">
            <div id="durationFilterContainer">
                <label for="durationCondition">Duration:</label>
                <select id="durationCondition" style="color: black; max-width: 115px;">
                    <option value="greater">Greater Than</option>
                    <option value="less">Less Than</option>
                </select>
                <input type="number" id="durationValue" placeholder="Enter duration" style="color: black;">
            </div>
            <div id="statusFilterContainer">`;

            if (msg.length && hasFailedProcess) {

                content += `<button id="failed-history-rm" 
                                onclick="(function(){ GW.history.removeFailedHistory('${msg[0]['history_process']}'); 
                                window.alert('Actions will reflect on next page load. Processing.') })()" 
                                    class="history-remove-failed" 
                                    data-history-process="` + msg[0]['history_process'] + `"
                                    >
                                Remove Failed History</button>`;
            }
                content += `<label for="statusFilter">Status:</label>
                <select id="statusFilter" style="color: black;">
                        <option value="">All</option> <!-- Changed to "All" -->
                        <option value="Running ">Running</option>
                        <option value="Done">Done</option>
                        <option value="Stopped">Stopped</option>
                        <option value="Failed">Failed</option>
                        <option value="Skipped">Skipped</option>
                </select>
            </div> 
            
        </div>
        
        <table class=\"table table-color\" id=\"process_history_table\"> 
          <thead>
            <tr>
              <th scope=\"col\">Execution Id</th>
              <th scope=\"col\">Begin Time</th>
              <th scope=\"col\">Duration</th>
              <th scope=\"col\">Notes (Click to Edit)</th>
              <th scope=\"col\">Status</th>
              <th scope=\"col\">Action</th>
            </tr>
          </thead>
          <tbody> `;

        for(var i=0;i<msg.length;i++){
            
            var status_col = this.getProcessStatusCol(msg[i].history_id, msg[i].indicator);

            content += "    <tr id=\"history-row-" + msg[i].history_id + "\">" +
                "      <td>"+msg[i].history_id+"</td> "+
                "      <td>"+GW.general.toDateString(msg[i].history_begin_time)+"</td> ";

            // create duration column and make it changing if the status is active
            //content += "      <td>"+GW.general.toDateString(msg[i].history_end_time)+"</td> ";
            content += `<td><span class="badge badge-primary" id="timerBadge_`+msg[i].history_id+`">`+
                GW.history.calculate_duration(msg[i].history_begin_time, msg[i].history_end_time, msg[i].indicator)+
                `</span></td>`
            if(msg[i].indicator == "Running"){
                GW.history.active_process_history_list.push(msg[i])
            }

            content += "	   <td>"+ msg[i].history_notes  +"</td>"+ status_col;

            if(!GW.process.sidepanel.isPresent()){
                content +=  "      <td><a href=\"javascript: GW.process.showHistoryDetails('"+
                    msg[i].history_id+"')\">Details</a> &nbsp;";
            }else{
                content +=  "      <td><a href=\"javascript: GW.process.sidepanel.showHistoryDetails('"+
                    msg[i].history_id+"')\">Details</a> &nbsp;";
            }
        
            // code to display the view changes option if in case 'i' > 0
            if(i!=msg.length-1) 
                content += "  <a href=\"javascript: GW.process.showHistoryDifference('"+
                    msg[i].history_process+"','"+
                    msg[i].history_id+"','"+ 
                    msg[i+1].history_id+
                    "')\">View Changes</a> &nbsp;";

            
            content +=  `      <a href="javascript: GW.process.deleteHistory('`+
            msg[i].history_id+`')">Delete</a> &nbsp;`;

            if(msg[i].indicator == "Running"){
                content += "		<a href=\"javascript: void(0)\" id=\"stopbtn_"+msg[i].history_id+"\" onclick=\"GW.process.stop('"+msg[i].history_id+"')\">Stop</a>";
            }

            content += "	   </td> "+
                "    </tr>";

        }

        content += "</tbody>";

        // create an interactive chart to show all the data

        content =
        // "<h4 class=\"border-bottom\">History Section  <button type=\"button\" class=\"btn btn-secondary btn-sm\" id=\"closeHistory\" >close</button></h4>"+
        "<div id=\"process-chart-container\">"+
        "<canvas id=\"process-history-chart\"></canvas>"+
        "</div>" + content ;



        return content;
    },

    /**
     * Returns an HTML status column for a workflow history entry based on its indicator.
     *
     * @param {string} history_id - The history ID of the workflow entry.
     * @param {string} indicator - The indicator value indicating the status.
     * @returns {string} - HTML content for the status column.
     */


	getWorkflowStatusCol: function(history_id, indicator){

		var status_col = "      <td id=\"status_"+history_id+"\">";

		if(indicator == "Done"){

			status_col += "       <span class=\"label label-success\">Done</span>  ";

		}else if(indicator == "Failed"){

			status_col += "       <span class=\"label label-danger\">Failed</span>  ";

		}else if(indicator == "Running"){

			status_col += "       <span class=\"label label-warning\">Running</span>  ";

		}else if(indicator == "Stopped"){

			status_col += "       <span class=\"label label-default\">Stopped</span>  ";

		}
        else if(indicator == "Skipped"){
            status_col += "       <span class=\"label label-info\">Skipped</span>  ";
        }
        else{

			status_col += "       <span class=\"label label-primary\">Unknown</span>  ";

		}

		status_col += "</td>";

		return status_col;

	},

    getWorkflowHistoryTable: function(msg){

		var content = "<div class=\"modal-body\" style=\"font-size:12px;\" ><table class=\"table table-color\" id=\"workflow-history-table\" > "+
		"  <thead> "+
		"    <tr> "+
		"      <th scope=\"col\">Execution Id</th> "+
		"      <th scope=\"col\">Begin Time</th> "+
        "      <th scope=\"col\">End Time</th> "+
		"      <th scope=\"col\">Notes (Click to Edit)</th> "+
		"      <th scope=\"col\">Status</th> "+
		"      <th scope=\"col\">Action</th> "+
        "      <th scope=\"col\">Checkpoint</th> "+
		"    </tr> "+
		"  </thead> "+
		"  <tbody> ";


		for(var i=0;i<msg.length;i++){

			var status_col = GW.history.getWorkflowStatusCol(msg[i].history_id, msg[i].indicator);

			content += "    <tr> "+
				"      <td>"+msg[i].history_id+"</td> "+
				"      <td>"+GW.general.toDateString(msg[i].history_begin_time)+"</td> "+
                "      <td>"+GW.general.toDateString(msg[i].history_end_time)+"</td> "+
				"      <td>"+msg[i].history_notes+"</td> "+
				status_col +
				"      <td><a href=\"javascript: GW.workflow.getHistoryDetails('"+msg[i].history_id+"')\">Check</a> &nbsp;";

			if(msg[i].indicator == "Running"){
				content += "		<a href=\"javascript:void(0)\" id=\"stopbtn_"+msg[i].history_id+"\" onclick=\"GW.workflow.stop('"+msg[i].history_id+"')\">Stop</a> ";
            }
            console.log(msg[i]);
            content += " <td><a onclick=\"GW.workflow.restoreCheckpoint('" + msg[i].history_process + "', '" + msg[i].history_id + "')\">Restore</ad></td>"


			content += "   </td> </tr>";

		}

		content += "</tbody></table></div>";

		// create an interactive chart to show all the data

		content = "<div id=\"workflow-chart-container\" width=\"200\" height=\"100\">"+
		"<canvas id=\"workflow-history-chart\" style=\"width:200px !important; height:50px !important;\" ></canvas>"+
		"</div>" +
		content;

		return content;

	},

    applyBootstrapTable: function(table_id){

        var table = $(table_id).DataTable({
            columnDefs : [
                { type: 'time-date-sort',
                  targets: [1],
                },
                {
                    targets: '_all',
                    className: 'wrap-text'
                }
            ],
            order: [[ 1, "desc" ]],
            "bDestroy": true,

        });



        $.fn.dataTable.ext.search.push(
            function(settings, data, dataIndex) {
                const condition = $('#durationCondition').val();
                const value = parseInt($('#durationValue').val(), 10);
                const durationData = parseFloat(data[2]);

                if (isNaN(value)) {
                    return true;
                }

                if (condition === "greater") {
                    return durationData > value;
                } else if (condition === "less") {
                    return durationData < value;
                }

                return true;
            }
        );


        $(document).ready(function() {

            $(document).on('click', '#failed-history-rm', function () {
                const historyProcess = $(this).data("history-remove-failed");
                if (historyProcess) {
                    GW.history.removeFailedHistory(historyProcess);
                } else {
                    console.error('Error: history-remove-failed attribute is missing or undefined.');
                }
            });
            
            // Function to apply search filter
            $.fn.dataTable.ext.search.push(function(settings, data, dataIndex) {
                const selectedStatus = ($('#statusFilter').val() || "").toLowerCase();  // Ensure it's a string
                const rowStatus = (data[4] || "").toLowerCase();  // Ensure it's a string
                
                // If selected status is empty and the row status is "skipped", exclude this row
                if (selectedStatus === "" && rowStatus === "skipped") {
                    return false;
                }

                // If no filter is applied or the row matches the selected filter, include the row
                return selectedStatus === "" || rowStatus === selectedStatus;
            });
        
            // call the function to apply the search filter
            table.draw();
        });
        
        // Your other JavaScript code here
        


        $('#durationCondition, #durationValue').on('change', function () {
            table.draw();
        });

        $('#statusFilter').on('change', function () {
            var value = $(this).val();
            
            table.draw();
        });

        table.MakeCellsEditable({
            "onUpdate": GW.history.processHistoryTableCellUpdateCallBack,
            "columns": [3],
            "allowNulls": {
                "columns": [3],
                "errorClass": 'error'
            },
            "confirmationButton": { // could also be true
                "confirmCss": 'my-confirm-class',
                "cancelCss": 'my-cancel-class'
            },
            "inputTypes": [
                {
                    "column": 3,
                    "type": "text",
                    "options": null
                }]
        });

    },

    processHistoryTableCellUpdateCallBack: function(updatedCell, updatedRow, oldValue){

        console.log("The new value for the cell is: " + updatedCell.data());
        console.log("The old value for that cell was: " + oldValue);
        console.log("The values for each cell in that row are: " + updatedRow.data());

        // The values for each cell in that row are: <input type="checkbox" class="hist-checkbox" id="selected_3naxi3l8o52j">,http://localhost:8888/api/contents/work/GMU%20workspace/COVID/covid_win_laptop.ipynb,xyz,2021-03-03 22:00:32.913,<a href="javascript: GW.host.viewJupyter('3naxi3l8o52j')">View</a> <a href="javascript: GW.host.downloadJupyter('3naxi3l8o52j')">Download</a> <a href="javascript: GW.host.deleteJupyter('3naxi3l8o52j')">Delete</a>

        var hisid = updatedRow.data()[0]

        var newvalue = updatedRow.data()[3]

        GW.history.updateNotesOfAHistory(hisid, newvalue);

    },

    updateNotesOfAHistory: function(hisid, notes){

        $.ajax({

            url: "edit",

            method: "POST",

            data: { type: "history", id: hisid, notes: notes}

        }).done(function(msg){

            console.log("notes is updated");

        }).fail(function(jxr, status){

            console.error(status + " failed to update notes, the server may lose connection. Try again. ");

        });
    },


}
