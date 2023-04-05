/**
* Geoweaver main function
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

		$(".gw_version").html(edu.gmu.csiss.geoweaver.version);
		
		// $(window).off("beforeunload", GW.main.exitWarning);
		
		GW.user.loggedInafterrefresh();

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

		$(window).on("beforeunload", GW.main.exitWarning);
		
//		this.test_websocket()
		
	}
		
};

GW.main.init();

function switchTab(ele, name){
	    		
	console.log("Turn on the tab " + name)
	  
	var i, tabcontent, tablinks;
	tabcontent = document.getElementsByClassName("tabcontent");
	for (i = 0; i < tabcontent.length; i++) {
	  tabcontent[i].style.display = "none";
	}
	tablinks = document.getElementsByClassName("tablinks");
	for (i = 0; i < tablinks.length; i++) {
	  tablinks[i].className = tablinks[i].className.replace(" active", "");
	}
	document.getElementById(name).style.display = "flex";
	ele.className += " active";

	if(name==="main-dashboard"){

	  GW.board.refresh();

	}

	if (name != "main-workflow-info") {
		GW.process.sidepanel.close();
	}
	  
}

function openCity(evt, name) {
	switchTab(evt.currentTarget, name);
}

$(document).ready(function(){

	console.log("trigger click event");
	//$("main-content-tab").trigger("click")
	switchTab(document.getElementById("main-general-tab"), "main-general");
	
});