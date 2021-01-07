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
		
	getJSessionId: function (){
		
	    var jsId = document.cookie.match(/JSESSIONID=[^;]+/);
	    
	    if(jsId != null) {
	        if (jsId instanceof Array)
	            jsId = jsId[0].substring(11);
	        else
	            jsId = jsId.substring(11);
	    }
	    
	    return jsId;
	},
	
	init: function(){
		
		$("#menuheader").val("Geoweaver v" + GW.version);
		
		GW.workspace.init();
		
		GW.toolbar.init();
		
		GW.general.init();
		
		GW.menu.init();
		
		GW.ssh.startLogSocket(GW.main.getJSessionId());
		
		introJs().start();
		
	}
		
};

GW.main.init();