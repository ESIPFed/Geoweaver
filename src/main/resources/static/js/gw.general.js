
/**
 * 
 * Show welcome page
 * 
 * @author Ziheng Sun
 * 
 */

GW.general = {
		
		process_code_editor: null, 
		
		init: function(){
			
			//add hot keys for the entire app
			$(window).bind('keydown', function(event) {
				if (event.ctrlKey || event.metaKey) {
					switch (String.fromCharCode(event.which).toLowerCase()) {
						case 's':
							//need to check if the current page to call the correct method
							event.preventDefault();
							GW.host.editSwitch();
							GW.process.editSwitch();
							GW.workspace.saveWorkflow();
							// alert('ctrl-s');
							break;
					}
				}
			});
			
		},

		makeid: function (length) {
			var result           = '';
			var characters       = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
			var charactersLength = characters.length;
			for ( var i = 0; i < length; i++ ) {
			  result += characters.charAt(Math.floor(Math.random() * 
		 charactersLength));
		   }
		   return result;
		},
		
		
		displayObject: function(type, msg){
			
			if(type=="process"){
				
				this.displayProcess(msg);
				
			}else if(type="host"){
				
				this.displayHost(msg);
				
			}else if(type=="workflow"){
				
				this.displayWorkflow(msg);
				
			}
			
		},
		
		showToasts:function(message) {
		  
		  // Get the snackbar DIV
		  var x = document.getElementById("snackbar");

		  // Add the "show" class to DIV
		  x.className = "show";
		  
		  $("#snackbar").html(message);

		  // After 3 seconds, remove the show class from DIV
		  setTimeout(function(){ x.className = x.className.replace("show", ""); }, 2000);
		  
		},
		
		getCodeStyleByLang: function(lang){
			
			var codestyle = "text/x-shell";
			
			if(lang == "shell"){
				
				codestyle = "";
				
			}else if(lang == "python"){
				
				codestyle = "";
				
			}
			
			return codestyle;
			
		},
		
		switchTab: function(name){
			
			if(name=="host"){
				
				switchTab(document.getElementById("main-host-tab"), "main-host-info");
				
			}else if(name=="process"){
				
				switchTab(document.getElementById("main-process-tab"), "main-process-info");
				
			}else if(name=="workflow"){
				
				switchTab(document.getElementById("main-workflow-tab"), "main-workflow-info");
				
			}else if(name=="workspace"){
				
				switchTab(document.getElementById("main-workspace-tab"), "workspace");
				
			}else if(name=="console"){
				
				switchTab(document.getElementById("main-console-tab"), "main-console");
				
			}else if(name=="dashboard"){
				
				switchTab(document.getElementById("main-dashboard-tab"), "main-dashboard");
				
			}else if(name=="general" || name == null){
				
				switchTab(document.getElementById("main-general-tab"), "main-general");
				
			}
			
			
		},
		
		
		isJSON: function(text){
			
			var isjson = false;

			if(text !=null){

				text = text.trim();
			
				if (/^[\],:{}\s]*$/.test(text.replace(/\\["\\\/bfnrtu]/g, '@').
					replace(/"[^"\\\n\r]*"|true|false|null|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?/g, ']').
					replace(/(?:^|:|,)(?:\s*\[)+/g, ''))) {
	
				  //the json is ok
							
					isjson = true;
	
				}

			}
			
		
			return isjson;
			
		}
		
		
		
		
		
		
		
		
}