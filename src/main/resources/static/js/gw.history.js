GW.history = {

    history_table_interval_id: null,

    active_process_history_list: [],

    // History panel theme management (integrated with editor theme)
    getHistoryTheme: function() {
        // Get theme from editor theme system
        const editorTheme = localStorage.getItem('editorTheme') || 'vs-light';
        return editorTheme;
    },

    applyHistoryTheme: function() {
        const themeName = this.getHistoryTheme();
        const themes = {
            'vs-light': {
                containerBg: '#ffffff',
                tableBg: '#ffffff',
                tableHeaderBg: '#f8f9fa',
                tableText: '#333333',
                tableHeaderText: '#495057',
                chartBg: '#ffffff',
                emptyStateBg: '#f8f9fa',
                emptyStateText: '#6c757d',
                emptyStateTitle: '#495057'
            },
            'vs-dark': {
                containerBg: '#1e1e1e',
                tableBg: '#1e1e1e',
                tableHeaderBg: '#2d2d30',
                tableText: '#d4d4d4',
                tableHeaderText: '#d4d4d4',
                chartBg: '#1e1e1e',
                emptyStateBg: '#2d2d30',
                emptyStateText: '#a0a0a0',
                emptyStateTitle: '#d4d4d4'
            },
            'hc-black': {
                containerBg: '#000000',
                tableBg: '#000000',
                tableHeaderBg: '#1a1a1a',
                tableText: '#ffffff',
                tableHeaderText: '#ffffff',
                chartBg: '#000000',
                emptyStateBg: '#1a1a1a',
                emptyStateText: '#cccccc',
                emptyStateTitle: '#ffffff'
            }
        };
        
        const theme = themes[themeName] || themes['vs-light'];
        
        // Apply theme to existing elements
        $('.history-panel-container').css({
            'background-color': theme.containerBg,
            'color': theme.tableText
        });
        
        // Apply theme to all history tables
        $('.history-table, #process_history_table, #prompt-panel-process-history-table').css({
            'background-color': theme.tableBg,
            'color': theme.tableText
        });
        
        // Apply theme to table headers
        $('.history-table thead, #process_history_table thead, #prompt-panel-process-history-table thead').css({
            'background-color': theme.tableHeaderBg,
            'color': theme.tableHeaderText
        });
        
        // Apply theme to table body
        $('.history-table tbody, #process_history_table tbody, #prompt-panel-process-history-table tbody').css({
            'background-color': theme.tableBg,
            'color': theme.tableText
        });
        
        // Apply theme to table rows
        $('.history-table tr, #process_history_table tr, #prompt-panel-process-history-table tr').css({
            'background-color': theme.tableBg,
            'color': theme.tableText
        });
        
        $('.history-chart-container').css({
            'background-color': theme.chartBg
        });
    },

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

    
    removeFailedHistory: function(processId, history_table_id, process_history_container_id) {
        const formData = new FormData();
        formData.append('processId', processId);
        const options = {
            method: 'DELETE',
            body: formData,
        }
        fetch("delete-failed", options)
            .then(response => {
                if (response.ok) {
                    console.log('Failed history removed successfully');
                    // Refresh only the history table instead of the entire page
                    GW.history.refreshHistoryTable(history_table_id, process_history_container_id);
                } else {
                    console.error('Failed to remove failed history');
                    alert('Failed to remove failed history. Please try again.');
                }
            })
            .catch(error => {
                console.error('Error removing failed history:', error);
                alert('Error removing failed history. Please try again.');
            });
    },

    removeSkippedHistory: function(historyProcess, history_table_id, process_history_container_id) {
        // Get the current process ID from the table
        const processId = historyProcess
        
        const formData = new FormData();
        if (processId) {
            formData.append('processId', processId);
        }
        
        const options = {
            method: 'DELETE',
            body: formData,
        }
        fetch("delete-skipped", options)
            .then(response => {
                if (response.ok) {
                    console.log('Skipped history removed successfully');
                    // Refresh only the history table instead of the entire page
                    GW.history.refreshHistoryTable(history_table_id, process_history_container_id);
                } else {
                    console.error('Failed to remove skipped history');
                    alert('Failed to remove skipped history. Please try again.');
                }
            })
            .catch(error => {
                console.error('Error removing skipped history:', error);
                alert('Error removing skipped history. Please try again.');
            });
    },

    refreshHistoryTable: function(history_table_id, process_history_container_id) {
        // Get the current process ID and name from the page
        const processId = $('#'+history_table_id).data('process-id');
        const processName = $('#'+history_table_id).data('process-name');
        
        if (processId && processName) {
            // Show loading indicator
            $('#'+process_history_container_id).html('<div style="text-align: center; padding: 20px;">🔄 Refreshing history...</div>');
            
            // Make AJAX request to get fresh data
            $.ajax({
                url: "logs",
                method: "POST",
                data: "type=process&id=" + processId + "&pname=" + processName,
            })
            .done(function (msg) {
                // Hide loading indicator
                $("#"+process_history_container_id).css("display", "block");
                $("#history-tab-loader-main-detail").css("display", "none");

                if (!msg.length) {
                    $('#'+process_history_container_id).html(`
                        <div style="text-align: center; padding: 40px; color: #666; background-color: #f8f9fa; border-radius: 8px; margin: 20px;">
                            <div style="font-size: 48px; margin-bottom: 20px; color: #6c757d;">
                                <i class="glyphicon glyphicon-stats" style="font-size: 48px;"></i>
                            </div>
                            <h4 style="color: #495057; margin-bottom: 10px; font-weight: 500;">No History Available</h4>
                            <p style="color: #6c757d; font-size: 14px; margin: 0;">This process hasn't been executed yet or all history records have been cleared.</p>
                        </div>
                    `);
                    return;
                }

                msg = GW.general.parseResponse(msg);

                // Stop all existing timers
                GW.history.stopAllTimers();

                // Destroy existing DataTable if it exists
                if ($.fn.DataTable.isDataTable('#'+history_table_id)) {
                    $('#'+history_table_id).DataTable().destroy();
                }

                // Clear the entire container completely
                $('#'+process_history_container_id).empty();

                // Rebuild the table from scratch
                $('#'+process_history_container_id).html(
                    GW.history.getProcessHistoryTable(msg, processId, processName, history_table_id, process_history_container_id)
                );
                
                // Reinitialize the DataTable
                var table_selector = '#'+process_history_container_id+' #'+history_table_id;
                GW.history.applyBootstrapTable(table_selector, processId, processName);
                
                // Apply theme to the refreshed table
                setTimeout(() => {
                    GW.history.applyHistoryTheme();
                }, 100);
                
                // Restart timers
                GW.history.startActiveTimer();
                
                // Reinitialize the history chart
                GW.chart.renderProcessHistoryChart(msg, processName, '#' + process_history_container_id);
                
                console.log('History table refreshed successfully');
            })
            .fail(function (jxr, status) {
                console.error('Failed to refresh history:', status);
                $('#'+process_history_container_id).html('<div style="text-align: center; padding: 20px; color: #dc3545;">❌ Failed to refresh history. Please try again.</div>');
            });
        } else {
            // Fallback: show error message instead of reloading page
            console.warn('Cannot get process info for refresh');
            $('#'+process_history_container_id).html('<div style="text-align: center; padding: 20px; color: #dc3545;">❌ Cannot refresh: missing process information</div>');
        }
    },

    refreshWorkflowHistoryTable: function() {
        // Get the current workflow ID and name from the page
        const workflowId = $('#workflow-history-table').data('workflow-id');
        const workflowName = $('#workflow-history-table').data('workflow-name');
        
        if (workflowId && workflowName) {
            // Show loading indicator
            $('#workflow-history-container').html('<div style="text-align: center; padding: 20px;">🔄 Refreshing workflow history...</div>');
            
            // Make AJAX request to get fresh data
            $.ajax({
                url: "logs",
                method: "POST",
                data: "type=workflow&id=" + workflowId,
            })
            .done(function (msg) {
                if (!msg.length) {
                    $('#workflow-history-container').html(`
                        <div style="text-align: center; padding: 40px; color: #666; background-color: #f8f9fa; border-radius: 8px; margin: 20px;">
                            <div style="font-size: 48px; margin-bottom: 20px; color: #6c757d;">
                                <i class="glyphicon glyphicon-stats" style="font-size: 48px;"></i>
                            </div>
                            <h4 style="color: #495057; margin-bottom: 10px; font-weight: 500;">No Workflow History Available</h4>
                            <p style="color: #6c757d; font-size: 14px; margin: 0;">This workflow hasn't been executed yet or all history records have been cleared.</p>
                        </div>
                    `);
                    return;
                }

                msg = $.parseJSON(msg);

                // Destroy existing DataTable if it exists
                if ($.fn.DataTable.isDataTable('#workflow-history-table')) {
                    $('#workflow-history-table').DataTable().destroy();
                }

                // Clear the entire container completely
                $('#workflow-history-container').empty();

                // Rebuild the table from scratch
                $('#workflow-history-container').html(
                    GW.history.getWorkflowHistoryTable(msg, workflowId, workflowName)
                );
                
                // Reinitialize the DataTable
                GW.history.applyBootstrapTable("#workflow-history-table");
                
                // Apply theme to the refreshed table
                setTimeout(() => {
                    GW.history.applyHistoryTheme();
                }, 100);
                
                // Reinitialize the chart
                GW.chart.renderWorkflowHistoryChart(msg);
                
                console.log('Workflow history table refreshed successfully');
            })
            .fail(function (jxr, status) {
                console.error('Failed to refresh workflow history:', status);
                $('#workflow-history-container').html('<div style="text-align: center; padding: 20px; color: #dc3545;">❌ Failed to refresh workflow history. Please try again.</div>');
            });
        } else {
            // Fallback: show error message instead of reloading page
            console.warn('Cannot get workflow info for refresh');
            $('#workflow-history-container').html('<div style="text-align: center; padding: 20px; color: #dc3545;">❌ Cannot refresh: missing workflow information</div>');
        }
    },
    /**
     * Generates an HTML table with process execution history data.
     *
     * @param {Array} msg - Array of process history data.
     * @returns {string} - HTML content of the process execution history table.
     */
    getProcessHistoryTable: function(msg, pid, pname, tableId, containerId){
        // Set default values if not provided
        tableId = tableId || 'process_history_table';
        containerId = containerId || 'process-history-container';
        
        // Check if there are no history records
        if (!msg || msg.length === 0) {
            const themeName = GW.history.getHistoryTheme();
            const themes = {
                'vs-light': {
                    emptyStateBg: '#f8f9fa',
                    emptyStateText: '#6c757d',
                    emptyStateTitle: '#495057'
                },
                'vs-dark': {
                    emptyStateBg: '#2d2d30',
                    emptyStateText: '#a0a0a0',
                    emptyStateTitle: '#d4d4d4'
                },
                'hc-black': {
                    emptyStateBg: '#1a1a1a',
                    emptyStateText: '#cccccc',
                    emptyStateTitle: '#ffffff'
                }
            };
            const theme = themes[themeName] || themes['vs-light'];
            return `
                <div class="history-panel-container" style="text-align: center; padding: 40px; color: ${theme.emptyStateText}; background-color: ${theme.emptyStateBg}; border-radius: 8px; margin: 20px;">
                    <div style="font-size: 48px; margin-bottom: 20px; color: ${theme.emptyStateText};">
                        <i class="glyphicon glyphicon-stats" style="font-size: 48px;"></i>
                    </div>
                    <h4 style="color: ${theme.emptyStateTitle}; margin-bottom: 10px; font-weight: 500;">No History Available</h4>
                    <p style="color: ${theme.emptyStateText}; font-size: 14px; margin: 0;">This process hasn't been executed yet or all history records have been cleared.</p>
                </div>
            `;
        }
        
        let hasFailedProcess;
        if (msg.length > 0) {
            hasFailedProcess = msg.some((item) => item.indicator === "Failed");
        } else {
            hasFailedProcess = false;
        }

        const themeName = GW.history.getHistoryTheme();
        const themes = {
            'vs-light': {
                containerBg: '#ffffff',
                tableBg: '#ffffff',
                tableHeaderBg: '#f8f9fa',
                tableText: '#333333',
                tableHeaderText: '#495057',
                chartBg: '#ffffff'
            },
            'vs-dark': {
                containerBg: '#1e1e1e',
                tableBg: '#1e1e1e',
                tableHeaderBg: '#2d2d30',
                tableText: '#d4d4d4',
                tableHeaderText: '#d4d4d4',
                chartBg: '#1e1e1e'
            },
            'hc-black': {
                containerBg: '#000000',
                tableBg: '#000000',
                tableHeaderBg: '#1a1a1a',
                tableText: '#ffffff',
                tableHeaderText: '#ffffff',
                chartBg: '#000000'
            }
        };
        const theme = themes[themeName] || themes['vs-light'];
        let content = `
        <div class="history-panel-container" style="background-color: ${theme.containerBg}; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
        <div style="display: flex; flex-direction: row; justify-content: space-between; margin-bottom: 20px;">
            <div id="durationFilterContainer">
                <label for="durationCondition">Duration:</label>
                <select id="durationCondition" style="color: black; max-width: 115px;">
                    <option value="greater">Greater Than</option>
                    <option value="less">Less Than</option>
                </select>
                <input type="number" id="durationValue" placeholder="Enter duration" style="color: black;">
            </div>`+
            // `<button id="refresh-history" onclick="GW.process.openCity(event, 'main-process-info-history'); GW.process.history('${pid}','${pname}');"
            //     class="history-refresh-button" 
            //     style="margin-right: 10px;">
            //     Refresh
            // </button>`+
            `<div id="statusFilterContainer">`;

            // Check if there are any skipped history records
            // let hasSkippedProcess = false;
            // if (msg.length) {
            //     hasSkippedProcess = msg.some(record => 
            //         record.history_input === "No code saved" && 
            //         record.indicator === "Skipped"
            //     );
            // }

            if (msg.length && hasFailedProcess) {

                content += `<button id="failed-history-rm" 
                                onclick="GW.history.removeFailedHistory('${msg[0]['history_process']}', '${tableId}', '${containerId}')" 
                                    class="history-remove-failed" 
                                    data-history-process="` + msg[0]['history_process'] + `"
                                    >
                                Remove Failed History</button>`;
            }

            // if (hasSkippedProcess) {
                content += `<button id="skipped-history-rm" 
                                onclick="GW.history.removeSkippedHistory('` + msg[0]['history_process'] + `', '${tableId}', '${containerId}')" 
                                    class="history-remove-skipped" >
                                Remove Skipped History</button>`;
            // }
                content += `<label for="statusFilter">Status:</label>
                <select id="statusFilter" style="color: black;">
                        <option value="all-except-skipped">All (Except Skipped)</option>
                        <option value="">All</option>
                        <option value="Running ">Running</option>
                        <option value="Done">Done</option>
                        <option value="Stopped">Stopped</option>
                        <option value="Failed">Failed</option>
                        <option value="Skipped">Skipped</option>
                </select>
                <button id="refresh-history-btn" 
                        onclick="GW.history.refreshHistoryTable('${tableId}', '${containerId}')" 
                        style="background-color: #007bff; color: white; border: none; padding: 8px 16px; 
                               border-radius: 4px; cursor: pointer; margin-left: 10px; 
                               font-size: 14px; font-weight: bold;"
                        onmouseover="this.style.backgroundColor='#0056b3'"
                        onmouseout="this.style.backgroundColor='#007bff'"
                        title="Refresh History Table">
                    🔄 Refresh
                </button>
            </div> 
            
        </div>
        
        <table class=\"table table-striped history-table\" id=\"${tableId}\" style=\"background-color: ${theme.tableBg}; color: ${theme.tableText};\"> 
          <thead style=\"background-color: ${theme.tableHeaderBg}; color: ${theme.tableHeaderText};\">
            <tr>
              <th scope=\"col\">Execution Id</th>
              <th scope=\"col\">Begin Time</th>
              <th scope=\"col\">Duration</th>
              <th scope=\"col\">Notes (Click to Edit)</th>
              <th scope=\"col\">Status</th>
              <th scope=\"col\">Action</th>
            </tr>
          </thead>
          <tbody style=\"background-color: ${theme.tableBg}; color: ${theme.tableText};\"> `;

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

        content += "</tbody></table>";

        // create an interactive chart to show all the data
        content += "<div id=\"process-chart-container\" class=\"history-chart-container\" style=\"margin-top: 20px; background-color: ${theme.chartBg}; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);\">"+
        "<canvas id=\"process-history-chart\" style=\"display: block;height: 400px;width: 100%;\"></canvas>"+
        "</div>";

        // Close the main container
        content += "</div>";



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

    getWorkflowHistoryTable: function(msg, workflowId, workflowName){

        // Check if there are no history records
        if (!msg || msg.length === 0) {
            return `
                <div style="text-align: center; padding: 40px; color: #666; background-color: #f8f9fa; border-radius: 8px; margin: 20px;">
                    <div style="font-size: 48px; margin-bottom: 20px; color: #6c757d;">
                        <i class="glyphicon glyphicon-stats" style="font-size: 48px;"></i>
                    </div>
                    <h4 style="color: #495057; margin-bottom: 10px; font-weight: 500;">No Workflow History Available</h4>
                    <p style="color: #6c757d; font-size: 14px; margin: 0;">This workflow hasn't been executed yet or all history records have been cleared.</p>
                </div>
            `;
        }

        // Check if there are any skipped history records
        

        // Check if there are any failed history records
        let hasFailedProcess = false;
        if (msg.length) {
            hasFailedProcess = msg.some(record => 
                record.indicator === "Failed"
            );
        }

		var content = "<div class=\"modal-body\" style=\"font-size:12px;\" >";
        
        // Add control buttons
        content += `<div id="workflow-statusFilterContainer">`;
        
        if (hasFailedProcess) {
            content += `<button id="workflow-failed-history-rm" 
                            onclick="GW.history.removeFailedHistory('${msg[0]['history_process']}')" 
                                class="history-remove-failed" 
                                data-history-process="` + msg[0]['history_process'] + `"
                                >
                            Remove Failed History</button>`;
        }

        content += `<button id="workflow-skipped-history-rm" 
                        onclick="GW.history.removeSkippedHistory('`
                        +msg[0]['history_process']+
                        `')" 
                            class="history-remove-skipped" 
                            >
                        Remove Skipped History</button>`;
        
        content += `<label for="workflow-statusFilter">Status:</label>
            <select id="workflow-statusFilter" style="color: black;">
                    <option value="all-except-skipped">All (Except Skipped)</option>
                    <option value="">All</option>
                    <option value="Running ">Running</option>
                    <option value="Done">Done</option>
                    <option value="Stopped">Stopped</option>
                    <option value="Failed">Failed</option>
                    <option value="Skipped">Skipped</option>
            </select>
            <button id="workflow-refresh-history-btn" 
                    onclick="GW.history.refreshWorkflowHistoryTable()" 
                    style="background-color: #007bff; color: white; border: none; padding: 8px 16px; 
                           border-radius: 4px; cursor: pointer; margin-left: 10px; 
                           font-size: 14px; font-weight: bold;"
                    onmouseover="this.style.backgroundColor='#0056b3'"
                    onmouseout="this.style.backgroundColor='#007bff'"
                    title="Refresh Workflow History Table">
                🔄 Refresh
            </button>
        </div>`;

        content += "<table class=\"table table-color\" id=\"workflow-history-table\" > "+
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

    applyBootstrapTable: function(table_id, processId, processName, workflowId, workflowName){

        // Add data attributes to the table for refresh functionality
        if (processId && processName) {
            $(table_id).attr('data-process-id', processId);
            $(table_id).attr('data-process-name', processName);
        }
        
        if (workflowId && workflowName) {
            $(table_id).attr('data-workflow-id', workflowId);
            $(table_id).attr('data-workflow-name', workflowName);
        }

        var table = $(table_id).DataTable({
            lengthMenu: [100, 50, 10, 200, -1],
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
            // Initialize history theme on page load
            GW.history.applyHistoryTheme();

            $(document).on('click', '#failed-history-rm', function () {
                const historyProcess = $(this).data("history-remove-failed");
                if (historyProcess) {
                    GW.history.removeFailedHistory(historyProcess);
                } else {
                    console.error('Error: history-remove-failed attribute is missing or undefined.');
                }
            });

            $(document).on('click', '#skipped-history-rm', function () {
                const historyProcess = $(this).data("history-remove-failed");
                GW.history.removeSkippedHistory(historyProcess);
            });

            // Workflow history event listeners
            $(document).on('click', '#workflow-failed-history-rm', function () {
                const historyProcess = $(this).data("history-remove-failed");
                if (historyProcess) {
                    GW.history.removeFailedHistory(historyProcess);
                } else {
                    console.error('Error: workflow history-remove-failed attribute is missing or undefined.');
                }
            });

            $(document).on('click', '#workflow-skipped-history-rm', function () {
                GW.history.removeSkippedHistory();
            });
            
            // Function to apply search filter
            $.fn.dataTable.ext.search.push(function(settings, data, dataIndex) {
                // Determine which filter to use based on the table
                let selectedStatus = "";
                if (settings.nTable.id === 'process_history_table') {
                    selectedStatus = ($('#statusFilter').val() || "").toLowerCase();
                } else if (settings.nTable.id === 'workflow-history-table') {
                    selectedStatus = ($('#workflow-statusFilter').val() || "").toLowerCase();
                }
                
                const rowStatus = (data[4] || "").toLowerCase();  // Ensure it's a string
                
                // Handle "All (Except Skipped)" option
                if (selectedStatus === "all-except-skipped") {
                    return rowStatus !== "skipped";
                }
                
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

        // Workflow status filter event listener
        $('#workflow-statusFilter').on('change', function () {
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

        // Apply theme after DataTable initialization
        setTimeout(() => {
            GW.history.applyHistoryTheme();
        }, 100);

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
