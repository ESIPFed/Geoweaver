/**
*
* Geoweaver main function
* 
* @author Ziheng Sun
* 
* @date 20 Aug 2018
* 
*/

edu.gmu.csiss.geoweaver.main = {
		
		init: function(){
			
			$("#menuheader").val("Geoweaver v" + edu.gmu.csiss.geoweaver.version);
			
			edu.gmu.csiss.geoweaver.workspace.init();
			
			edu.gmu.csiss.geoweaver.menu.init();
			
		}
		
};

edu.gmu.csiss.geoweaver.main.init();