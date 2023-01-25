
GW.settings = {
		
		clearCache: function(){
			
			if(confirm("Do you want to clear all the cached information (including passwords, the connection between process/workflow and host)?")){
				
				GW.host.clearCache();
				
				GW.process.clearCache();
				
				GW.workflow.clearCache();
				
				alert("Cache cleared.");
				
			}
			
		},
		
		clearPasswords: function(){
			
			if(confirm("Do you want to clear the remembered passwords?")){
				
				GW.host.clearCache();

				alert("Cache cleared.");
				
			}
			
		},
		
		clearConnections: function(){
			
			if(confirm("Do you want to clear the remembered mappings between processes/workflows and hosts?")){
				
				GW.process.clearCache();
				
				GW.workflow.clearCache();

				alert("Cache cleared.");
				
			}
			
		},
		
		clearProcessConnections: function(){
			
			if(confirm("Do you want to clear the remembered mappings between processes and hosts?")){
				
				GW.process.clearCache();

				alert("Cache cleared.");
			}
			
		},
		
		clearWorkflowConnections: function(){
			
			if(confirm("Do you want to clear the remembered mappings between workflows and hosts?")){
				
				GW.workflow.clearCache();
				
				alert("Cache cleared.");
				
			}
			
		},
		
		showDialog: function(){
			
			var content = "<div class=\"list-group\" style=\"padding:10px;\"> "+
			"    <a class=\"list-group-item clearfix\" href=\"javascript:void(0)\"> "+
            "        Clear Connection between Process and Host "+
            "        <span class=\"pull-right\"> "+
            "            <span class=\"btn btn-xs btn-default\" onclick=\"GW.settings.clearProcessConnections();\"> "+
            "                <span class=\"glyphicon glyphicon-play\" aria-hidden=\"true\"></span> "+
            "            </span> "+
            "        </span> "+
            "    </a> "+
            "    <a class=\"list-group-item clearfix\" href=\"javascript:void(0)\"> "+
            "        Clear Connection between Workflow and Host "+
            "        <span class=\"pull-right\"> "+
            "            <span class=\"btn btn-xs btn-default\" onclick=\"GW.settings.clearWorkflowConnections();\"> "+
            "                <span class=\"glyphicon glyphicon-play\" aria-hidden=\"true\"></span> "+
            "            </span> "+
            "        </span> "+
            "    </a> "+
			"    <a class=\"list-group-item clearfix\" href=\"javascript:void(0)\"> "+
            "        Clear Passwords Only"+
            "        <span class=\"pull-right\"> "+
            "            <span class=\"btn btn-xs btn-default\" onclick=\"GW.settings.clearPasswords();\"> "+
            "                <span class=\"glyphicon glyphicon-play\" aria-hidden=\"true\"></span> "+
            "            </span> "+
            "        </span> "+
            "    </a> "+
            "    <a class=\"list-group-item clearfix\" href=\"javascript:void(0)\"> "+
            "        Clear All Cached Information "+
            "        <span class=\"pull-right\"> "+
            "            <span class=\"btn btn-xs btn-default\" onclick=\"GW.settings.clearCache();\"> "+
            "                <span class=\"glyphicon glyphicon-play\" aria-hidden=\"true\"></span> "+
            "            </span> "+
            "        </span> "+
            "    </a> "+
            "</div>";
			
			var frame = GW.process.createJSFrameDialog(360, 320, content, "Settings");
			
//			BootstrapDialog.show({
//				
//				title: "Settings",
//				
//				message: function(){
//					
//					var content = "<div class=\"list-group\"> "+
//					"    <a class=\"list-group-item clearfix\" href=\"javascript:void(0)\"> "+
//		            "        Clear Connection between Process and Host "+
//		            "        <span class=\"pull-right\"> "+
//		            "            <span class=\"btn btn-xs btn-default\" onclick=\"GW.settings.clearProcessConnections();\"> "+
//		            "                <span class=\"glyphicon glyphicon-play\" aria-hidden=\"true\"></span> "+
//		            "            </span> "+
//		            "        </span> "+
//		            "    </a> "+
//		            "    <a class=\"list-group-item clearfix\" href=\"javascript:void(0)\"> "+
//		            "        Clear Connection between Workflow and Host "+
//		            "        <span class=\"pull-right\"> "+
//		            "            <span class=\"btn btn-xs btn-default\" onclick=\"GW.settings.clearWorkflowConnections();\"> "+
//		            "                <span class=\"glyphicon glyphicon-play\" aria-hidden=\"true\"></span> "+
//		            "            </span> "+
//		            "        </span> "+
//		            "    </a> "+
//					"    <a class=\"list-group-item clearfix\" href=\"javascript:void(0)\"> "+
//		            "        Clear Passwords Only"+
//		            "        <span class=\"pull-right\"> "+
//		            "            <span class=\"btn btn-xs btn-default\" onclick=\"GW.settings.clearPasswords();\"> "+
//		            "                <span class=\"glyphicon glyphicon-play\" aria-hidden=\"true\"></span> "+
//		            "            </span> "+
//		            "        </span> "+
//		            "    </a> "+
//		            "    <a class=\"list-group-item clearfix\" href=\"javascript:void(0)\"> "+
//		            "        Clear All Cached Information "+
//		            "        <span class=\"pull-right\"> "+
//		            "            <span class=\"btn btn-xs btn-default\" onclick=\"GW.settings.clearCache();\"> "+
//		            "                <span class=\"glyphicon glyphicon-play\" aria-hidden=\"true\"></span> "+
//		            "            </span> "+
//		            "        </span> "+
//		            "    </a> "+
//		            "</div>";
//					
//					return content;
//					
//				},
//				
//				buttons: [{
//					
//					label: "Close",
//					
//					action: function(dialogItself){
//						
//						dialogItself.close();
//						
//					}
//					
//				}]
//				
//			});
			
		}
		
}