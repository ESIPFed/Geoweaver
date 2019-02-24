/**
 * 
 * @author Ziheng Sun
 * 
 */

edu.gmu.csiss.geoweaver.settings = {
		
		clearCache: function(){
			
			if(confirm("Do you want to clear all the cached information (including passwords, the connection between process/workflow and host)?")){
				
				edu.gmu.csiss.geoweaver.host.clearCache();
				
				edu.gmu.csiss.geoweaver.process.clearCache();
				
			}
			
		},
		
		clearPasswords: function(){
			
			if(confirm("Do you want to clear the remembered passwords?")){
				
				edu.gmu.csiss.geoweaver.host.clearCache();
				
			}
			
		},
		
		clearConnections: function(){
			
			if(confirm("Do you want to clear the remembered mappings between processes/workflows and hosts?")){
				
				edu.gmu.csiss.geoweaver.process.clearCache();
				
			}
			
		},
		
		showDialog: function(){
			
			BootstrapDialog.show({
				
				title: "Settings",
				
				message: function(){
					
					var content = "<div class=\"list-group\"> "+
					"    <a class=\"list-group-item clearfix\" href=\"javascript:void(0)\"> "+
		            "        Clear Connections between Process/Workflow and Hosts Only "+
		            "        <span class=\"pull-right\"> "+
		            "            <span class=\"btn btn-xs btn-default\" onclick=\"edu.gmu.csiss.geoweaver.settings.clearConnections();\"> "+
		            "                <span class=\"glyphicon glyphicon-play\" aria-hidden=\"true\"></span> "+
		            "            </span> "+
		            "        </span> "+
		            "    </a> "+
					"    <a class=\"list-group-item clearfix\" href=\"javascript:void(0)\"> "+
		            "        Clear Passwords Only"+
		            "        <span class=\"pull-right\"> "+
		            "            <span class=\"btn btn-xs btn-default\" onclick=\"edu.gmu.csiss.geoweaver.settings.clearPasswords();\"> "+
		            "                <span class=\"glyphicon glyphicon-play\" aria-hidden=\"true\"></span> "+
		            "            </span> "+
		            "        </span> "+
		            "    </a> "+
		            "    <a class=\"list-group-item clearfix\" href=\"javascript:void(0)\"> "+
		            "        Clear All Cached Information "+
		            "        <span class=\"pull-right\"> "+
		            "            <span class=\"btn btn-xs btn-default\" onclick=\"edu.gmu.csiss.geoweaver.settings.clearCache();\"> "+
		            "                <span class=\"glyphicon glyphicon-play\" aria-hidden=\"true\"></span> "+
		            "            </span> "+
		            "        </span> "+
		            "    </a> "+
		            "</div>";
					
					return content;
					
				},
				
				buttons: [{
					
					label: "Close",
					
					action: function(dialogItself){
						
						dialogItself.close();
						
					}
					
				}]
				
			});
			
		}
		
}