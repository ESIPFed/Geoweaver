/**
*
* Geoweaver Process
* 
* @author Ziheng Sun
*
*/ 

edu.gmu.csiss.geoweaver.workflow = {
		
	newDialog: function(){
		
		BootstrapDialog.show({
			
			title: "New workflow"
			
		});
		
	},
	
	addMenuItem: function(one){
		
		$("#"+edu.gmu.csiss.geoweaver.menu.getPanelIdByType("workflow")).append("<li id=\"workflow-" + one.id + "\"><a href=\"javascript:void(0)\" onclick=\"edu.gmu.csiss.geoweaver.menu.details('"+one.id+"', 'workflow')\">" + 
	    		
				one.name + "</a> <i class=\"fa fa-minus subalignicon\" data-toggle=\"tooltip\" title=\"Delete this workflow\" onclick=\"edu.gmu.csiss.geoweaver.menu.del('"+
	        	
				one.id+"','workflow')\"></i> <i class=\"fa fa-play subalignicon\" onclick=\"edu.gmu.csiss.geoweaver.workflow.run('"+
	        	
				one.id+"')\" data-toggle=\"tooltip\" title=\"Run Workflow\"></i> </li>");
		
	},
	
	list: function(msg){
		
		for(var i=0;i<msg.length;i++){
			
			this.addMenuItem(msg[i]);
			
			//this.addWorkspace(msg[i]);
			
		}
		
		$('#workflows').collapse("show");
		
	}
		
}