
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
			
		}
		
		
		
		
		
		
		
		
}