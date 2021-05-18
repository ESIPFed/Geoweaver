var shell;
var sshConnected = false;
var passwordPhase = false;

var ws;
var last_prompt = null;
var password_cout = 0;
/* var host = window.location.hostname;
var port = window.location.port;
var pcol = window.location.protocol; */
var root = getContextURLPath();
var key = '${key}';
var completable = false;
var username = '<sec:authentication property="principal" />';
//alert('ID:' + key + ',USERNAME:' + username);
var special = {
  black:   "\x1b[1;30m",
  red:     "\x1b[1;31m",
  green:   "\x1b[1;32m",
  yellow:  "\x1b[1;33m",
  blue:    "\x1b[1;34m",
  magenta: "\x1b[1;35m",
  cyan:    "\x1b[1;36m",
  white:   "\x1b[1;37m",
  reset:   "\x1b[0m",
  ready:   "]0;", 
  prompt:  "$"
};
	  
function ws_onopen(e) {
  
  //shell.echo(special.white + "connected" + special.reset);
  shell.echo("socket connected");
  // link the SSH session established with spring security logon to the websocket session...
  send(token);
  send("\n");
  shell.resume();
  
}

function send(data) {
	
    shell.pause();
    
    if(ws != null){
  	
    	ws.send(data);
    	
    } else {
    
    	shell.error('not connected!');
    
    }
}

function ws_onclose(e) {
	
    try{
    
    	shell.echo(special.white + "disconnected" + special.reset);
        
    	shell.destroy();
        
    	shell.purge();
        
    }catch(e){
    	
    	console.error(e);
    }
    
	document.forms['logout'].submit();
}

function ws_onerror(e) {
	
    shell.echo(special.red + e + special.reset);
    
    shell.resume();
  
}

function resumeTerm(message){
	
	var breaks;
	
	if(message.indexOf("$ ")!=-1){
		
		breaks = message.split("$ ");
		
		last_prompt = breaks[0] + "$ ";
		
		shell.set_prompt(last_prompt);
		
	}else if(message.indexOf("# ")!=-1){
		
		breaks = message.split("# ");
		
		last_prompt = breaks[0] + "# ";
		
		shell.set_prompt(last_prompt);
		
	}
	
	if(breaks != null && breaks[1].startsWith("sudo")){
		
		shell.set_prompt("[sudo] password for " + user + ": ");	
		
		//one try failed
		//shell.set_prompt("[sudo] password for " + user + ": ");	
		
		//passwordPhase = true;
		
	}else if(message.startsWith("[sudo] password")){
		
		/*passwordPhase = true;*/
		
		send("\n");
		
		shell.set_mask(false);
		
	}
	
	/*else if(passwordPhase && (message.startsWith("[sudo] password for") || message.startsWith("Sorry"))){
		
		//one try failed
		shell.set_prompt("[sudo] password for " + user + ": ");	
		
	}else if(passwordPhase && (message.startsWith("sudo"))){
		
		//three tries failed
		passwordPhase = false;
		
	}*/
	
	shell.resume();
	
}

function ws_onmessage(e) {
  
  try {
	
    	//if(e.data.indexOf(special.prompt) == -1 && e.data.indexOf(special.ready) == -1) {
    	
    	console.log(e.data);

		/*if(completable){
			
			completable = false;
			
			
		}else{*/
			
	    	var match = /\r|\n/.exec(e.data);
	    	
	    	/*if(passwordPhase){
	    		
	    		if(e.data.startsWith("[sudo] password for") || e.data.startsWith("Sorry")){
	    			
	    			//one try failed
	    			shell.set_prompt("[sudo] password for " + user + ": ");	
	    			
	    			shell.resume();
	    			
	    		}else if(e.data.startsWith("sudo")){
	    			
	    			//three tries failed
	    			passwordPhase = false;
	    			
	    			shell.set_prompt(last_prompt);
	    			
	    			shell.resume();
	    			
	    		}else{
	    			
	    			send("\n");
	    			
	    			passwordPhase = false;
	    			
	    		}
	    		
	    	}else */
	    	
	    	if(!match && e.data.indexOf("@")!=-1 && e.data.indexOf(special.prompt)){
	    		
	    		/*var breaks = e.data.split("$ ");
	    		
	    		var last_prompt = breaks[0] + "$ ";
	    		
	    		shell.set_prompt(last_prompt);
	    		
	        	shell.resume();*/
	        	
	    		resumeTerm(e.data);
	        	
	    	}else if(e.data.startsWith("[sudo] password")){
	    		
	    		//passwordPhase = true;
	    		
	    		resumeTerm(e.data);
	    		
	    	}else{
				
	        	shell.echo(e.data);
	    		
	    	}
	    	
			
		/*}*/
    	
    //}
    
    //if (e.data.indexOf(special.ready) != -1) {
    
	
    //}
    
  } catch(err) {
  
	shell.error("** Invalid server response : " + e.data + " - " + err); 
    
	if(last_prompt) {
    
		shell.set_prompt(last_prompt);
    
	}
	
  }
  
  //shell.resume();
  
}

function getContextURLPath() {
    var rootUrl = location.protocol;
    rootUrl = rootUrl+"//"+location.host;
    var path = location.pathname;
    var tempStr = path.split('/');
    rootUrl = rootUrl+"/"+tempStr[1];
    return rootUrl;
}
 
function getWsPrefixURL (){
	
	var s = ((window.location.protocol === "https:") ? "wss://" : "ws://") + window.location.host + "/";
	
//	s +=  "Geoweaver/";
	
	console.log("Ws URL Prefix: ", s)
	
	return s
	
}

$(document).ready(function ($) {
	
//    ws = new SockJS("shell");
	
	ws = new WebSocket(getWsPrefixURL() + "Geoweaver/terminal-socket")
	
    ws.onopen = function(e) { ws_onopen(e) };
    
    ws.onclose = function(e) { ws_onclose(e) };
    
    ws.onmessage = function(e) { ws_onmessage(e) };
    
    ws.onerror = function(e) { ws_onerror(e) };
	
    shell = $('#content').terminal(function (command, term) {
    		
    		if(command.startsWith("cd")){
    			
    			command += "\n";
    			
    		}

			if(command.startsWith("sudo")){
				
				shell.set_mask(true);
				
			}
    	
    		send(command);
    		
        }, {
        	
            prompt: '['+user+'@'+host+': ~]# ',
            name: 'Geoweaver SSH on Web',
            scrollOnEcho: true,
            exit: false,
            clear: true,
            wrap: true,
            greetings: "SSH on Web started. Type 'exit' to quit. \n",
       		
			/*completion: function (command, callback) {

				console.log("Complete command is entered - " + command + ":")
				
				ws.send(command + "\t");
				
				completable = true;
				
	        },*/

			/*keypress:{
			
				""	
				
			},*/
			
			/*onCommandChange: function(command, terminal){
				
				console.log("Command is changed: ", command);
				
			},*/

            keymap: {
              
           	  "CTRL+D": function(e, original){
                   
                   console.log("ctrl+d is presesed");
                   
                   send("\u0003");
                   
                   original();
                   
              },
              
              "CTRL+C": function(e, original){
                
                console.log("ctrl+c is presesed");
                
                send("\u0003");
                
                original();
                
              },
              
              "CTRL+ALT+C": function(e, original){
                  
                  console.log("ctrl+alt+c is presesed");
                  
                  send("\u0003");
                  
                  original();
                  
              }
              
            }
        }
        
    );
	
});