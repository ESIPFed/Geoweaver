/**
 * 
 * author: Z.S.
 * date: Mar 12 2021
 * 
 */

GW.history = {

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
            
        }else{
            
            status_col = "      <td id=\"status_"+hid+"\"><span class=\"label label-primary\">Unknown</span></td> ";
            
        }
        
        return status_col;
        
    },
    
    getProcessHistoryTable: function(msg){
		
        var content = "<table class=\"table table-color\" id=\"process_history_table\"> "+
        "  <thead> "+
        "    <tr> "+
        "      <th scope=\"col\">Execution Id</th> "+
        "      <th scope=\"col\">Begin Time</th> "+
        "      <th scope=\"col\">End Time</th> "+
        "      <th scope=\"col\">Notes (Click to Edit)</th> "+
        "      <th scope=\"col\">Status</th> "+
        "      <th scope=\"col\">Action</th> "+
        "    </tr> "+
        "  </thead> "+
        "  <tbody> ";
        
        for(var i=0;i<msg.length;i++){
            
            var status_col = this.getProcessStatusCol(msg[i].history_id, msg[i].indicator);
            
            content += "    <tr> "+
                "      <td>"+msg[i].history_id+"</td> "+
                "      <td>"+GW.general.toDateString(msg[i].history_begin_time)+"</td> "+
                "      <td>"+GW.general.toDateString(msg[i].history_end_time)+"</td> "+
                "	   <td>"+msg[i].history_notes+"</td>"+
                status_col +
                "      <td><a href=\"javascript: GW.process.showHistoryDetails('"+msg[i].history_id+"')\">Details</a> &nbsp;";
                // code to display the view changes option if in case 'i' > 0
                if(i!=msg.length-1) content += "  <a href=\"javascript: GW.process.showHistoryDifference('"+msg[i].history_id+"','"+ msg[i+1].history_id+"')\">View Changes</a> &nbsp;";

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
        "<div id=\"process-chart-container\" width=\"200\" height=\"100\">"+
        "<canvas id=\"process-history-chart\" style=\"width:200px !important; height:50px !important;\" ></canvas>"+
        "</div>" + content ;
        
        return content;
        
    },

    
	getWorkflowStatusCol: function(history_id, indicator){

		// var status_col = "      <td><span class=\"label label-warning\">Pending</span></td> ";
				
		// if(single_msg.end_time!=null && single_msg.end_time != single_msg.begin_time){
			
		// 	status_col = "      <td><span class=\"label label-success\">Done</span></td> ";
			
		// }else if(single_msg.end_time == single_msg.begin_time && single_msg.output != null){
			
		// 	status_col = "      <td><span class=\"label label-danger\">Failed</span></td> ";
			
		// }

		var status_col = "      <td id=\"status_"+history_id+"\">";
			
		if(indicator == "Done"){
			
			status_col += "       <span class=\"label label-success\">Done</span>  ";
			
		}else if(indicator == "Failed"){
			
			status_col += "       <span class=\"label label-danger\">Failed</span>  ";
			
		}else if(indicator == "Running"){
			
			status_col += "       <span class=\"label label-warning\">Running</span>  ";
			
		}else if(indicator == "Stopped"){
			
			status_col += "       <span class=\"label label-default\">Stopped</span>  ";
			
		}else{
			
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

        var table = $("#"+table_id).DataTable({
            columnDefs : [
                { type: 'time-date-sort', 
                  targets: [1],
                }
            ],
            order: [[ 1, "desc" ]]
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
