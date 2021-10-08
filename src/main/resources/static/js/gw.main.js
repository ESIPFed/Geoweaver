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

	quiet: false,
		
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
	
	test_websocket: function(){
	
		var test1_url = GW.ssh.getWsPrefixURL() + "test-socket/xxxx/user/zsun/api/kernels/2eee232rsdfsweroeiwr/channels"
		
		var test1 = new WebSocket(test1_url);
		
		test1.onopen = function(e) { GW.ssh.ws_onopen(e) };
        
		test1.onclose = function(e) { GW.ssh.ws_onclose(e) };
        
		test1.onmessage = function(e) { GW.ssh.ws_onmessage(e) };
        
		test1.onerror = function(e) { GW.ssh.ws_onerror(e) };
		
		var test2_url = GW.ssh.getWsPrefixURL() + "test-socket"
		
		var test2 = new WebSocket(test2_url);
		
		test2.onopen = function(e) { GW.ssh.ws_onopen(e) };
        
		test2.onclose = function(e) { GW.ssh.ws_onclose(e) };
        
		test2.onmessage = function(e) { GW.ssh.ws_onmessage(e) };
        
		test2.onerror = function(e) { GW.ssh.ws_onerror(e) };
		
		
		
	},

	exitWarning: function(e){
		// this should show up when there are unsaved edits only
		if(!GW.process.isSaved || !GW.workflow.isSaved)
			return "Are you going to leave Geoweaver? Please make sure all the edits are saved."
		else
			return false

	},

	quiteExit: function(){

		// GW.main.quiet = true
		// $(window).off("beforeunload", GW.main.exitWarning);
		// $(window).on("beforeunload", function(){});

	},
	
	init: function(){

		$(window).on("beforeunload", GW.main.exitWarning);

		// $(window).off("beforeunload", GW.main.exitWarning);
		
		$("#menuheader").val("Geoweaver v" + GW.version);
		
		GW.process.init();

		GW.workspace.init();
		
		GW.toolbar.init();
		
		GW.general.init();
		
		GW.menu.init();

		//session id is a server side thing and it is not reasonable to get it on the client
		// var current_jssessionid = GW.main.getJSessionId();

		// console.log("Current JS Session ID: " + current_jssessionid);

		var current_token = GW.general.makeid(40); // this is no longer used

		console.log("Current token is: " + current_token);
		
		GW.ssh.startLogSocket(current_token);

		GW.monitor.startSocket(current_token); //this token will be saved as GW.monitor.token and can be used everywhere
		
		GW.board.init();

		introJs().start();

		GW.user.loggedInafterrefresh();
		
//		this.test_websocket()
		
	}
		
};

GW.main.init();