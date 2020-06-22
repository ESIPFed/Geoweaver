
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
				
			}else if(name=="general" || name == null){
				
				switchTab(document.getElementById("main-general-tab"), "main-general");
				
			}
			
			
		}
		
		
		
		
		
		
		
		
}