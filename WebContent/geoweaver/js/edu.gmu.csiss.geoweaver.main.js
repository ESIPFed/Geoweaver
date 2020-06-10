/**
*
* Geoweaver main function
* 
* @author Ziheng Sun
* 
* @date 20 Aug 2018
* 
*/

GW.main = {
	
	init: function(){
		
		$("#menuheader").val("Geoweaver v" + GW.version);
		
		GW.workspace.init();
		
		GW.toolbar.init();
		
		GW.menu.init();
		
		GW.ssh.startLogSocket();
		
	}
		
};

GW.main.init();