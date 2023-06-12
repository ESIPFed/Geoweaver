
/**
 * 
 * SSH client
 * 
 * Create a shared websocket shell to stream all the results from server to the client. 
 * 
 * Distinguish the outputs of different sessions using tokens.
 * 
 * Only one ssh session is allowed at one time. 
 * 
 * Only one running workflow is allowed at one time. 
 * 
 */

GW.ssh = {
		
		shell: null,
	    
		sshConnected : false,
	    
	    passwordPhase : false,
	    
	    user : null, 
    	
	    host : null, 
    	
    	port : null, 
    	
    	token : null,
    	
    	output_div_id: null,

		process_output_id: "process-log-window",
	    
	    ws: null,
	    
	    all_ws: null, //future websocket session for all the traffic between client and server
	    
	    last_prompt: null,
	    
	    password_cout: 0,
	    
		key : '',

		checker_swich: false,

		current_log_length: 0,

		current_process_log_length: 0,
		
		username : '<sec:authentication property="principal" />',
		
	    special : {
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
	    },
	    
	    echo: function(content){
		
			if(content!=null){

				// if(content.indexOf("Warning: Websocket Channel is going to close")!=-1){

				// 	console.error("The WebSocket is going to close..");

				// }
				
		    	content = content.replace(/\n/g,'<br/>')
		    	
		    	this.addlog(content);
		    	
		    	// trigger the builtin process
		    	
		    	if(GW.general.isJSON(content)){
		    		
			    	try{
						
						var returnmsg = $.parseJSON(content);
			      	
						console.log(returnmsg);

						if(returnmsg.ret == "success"){
							
							setTimeout(function (){

								GW.process.callback(returnmsg);
							  
							  }, 2000);
							

						}
				      	
				    	if(returnmsg.builtin){

							GW.process.callback(returnmsg);
				      		
				      	}else{
				      		
				      		// GW.workspace.updateStatus(returnmsg); // the workflow status message should only come from the workflow-socket
				      		
				      	}
				
					}catch(errors){
						
						console.error(errors)
						
					}
		    		
		    	}
				
			}
	    	
	    },
	    
	    error: function(content){
	    	
	    	content = content.replace(/\n/g,'<br/>')
	    	
	    	this.addlog(content);
	    	
	    },
	    	  
	    send: function (data) {
	    	
	        if(this.ws != null){
	      	
	        	this.ws.send(data);
	        
	        } else {
	        
	        	if(data!=this.token){
	        		
	        		this.error('not connected!');
	        		
	        	}
	        	
	        }
	    },

		checkSessionStatus: function(){
			// console.log("Current WS status: " + GW.ssh.all_ws.readyState);
			// return GW.ssh.all_ws.readyState;

			GW.ssh.checker_swich = true;

			GW.ssh.send("token:"+GW.general.CLIENT_TOKEN);

			setTimeout(() => {  
				
				if(GW.ssh.checker_swich){

					//restart the websocket if the switch is still true two seconds later
					GW.ssh.startLogSocket(GW.ssh.token);
					GW.ssh.checker_swich = false;

				}

			}, 2000);

		},
	    
	    ws_onopen: function (e) {
			
		  	console.log("WebSocket Channel is Openned");
	      	
			this.echo("connected");

			setTimeout(() => {  GW.ssh.send("token:" + GW.general.CLIENT_TOKEN) }, 1000); //create a chance for the server side to register the session if it didn't when it is openned
	      
	    },
	    

	    ws_onclose: function (e) {
	    	
	        try{

	        	this.echo("disconnected");

				GW.ssh.all_ws = null;
				GW.ssh.ws = null;
				GW.ssh.token = null;
	        	
	        }catch(e){
	        	
	        	console.error(e);
	        	
	        	this.echo("Reconnection failed. " + e)
	        	
	        }
	        
	        console.log("the logging out websocket has been closed");

	    },

	    ws_onerror: function (e) {
	    	
	        this.error("The process execution failed.")

	        this.error("Reason: " + e);
	        
	    },
	    
	    ws_onmessage: function (e) {
	      
	      try {
	    	if(e.data.indexOf("Session_Status:Active")!=-1){
				GW.ssh.checker_swich = false;
			}else if(e.data.indexOf(this.special.prompt) == -1 && 
	        		
	        		e.data.indexOf(this.special.ready) == -1 && 
	        		
	        		e.data.indexOf("No SSH connection is active") == -1) {
	            
	        	this.echo(e.data);
	        	
	        }else{
	        	
	        	//the websocket is already closed. try the history query
	        	
	        	// this.echo("It ends too quickly. Go to history to check the logs out.");
				
	        }

			
	        
	        //if (e.data.indexOf(special.ready) != -1) {
	        
	        //	shell.resume();
			
	        //}
	        
	      } catch(err) {
	    	  
	    	console.log(err);
	      
	    	this.error("** Invalid server response : " + e.data); 
	        
	      }
	      
	    },
		
	    addlog: function(content){
	    	var dt = new Date();
	    	var time = dt.getHours() + ":" + dt.getMinutes() + ":" + dt.getSeconds();

			var style1 = "";
			if(content.includes("Start to execute") ){

				style1 = "color: blue; font-weight: bold; text-decoration: underline;";

				$(".dot-flashing").removeClass("invisible")
				$(".dot-flashing").addClass("visible")

			}else if(content.includes("===== Process")){
				
				style1 = "color: blue; font-weight: bold; text-decoration: underline;";

				$(".dot-flashing").removeClass("visible")
				$(".dot-flashing").addClass("invisible")

				

			}else if(content=="disconnected"){
				$(".dot-flashing").removeClass("visible")
				$(".dot-flashing").addClass("invisible")
			}
			
			var newline = "<p style=\"line-height:1.1; text-align:left;\"><span style=\""+
				style1+"\">" + content + "</span></p>";

			this.current_log_length += 1  //line number plus 1

			if(this.current_log_length > 5000){
				$("#log-window").find('p:first').remove();
				this.current_log_length -= 1
			}
			
	    	$("#log-window").append(newline);

			//don't output log to process log if the current executed is workflow
			if(GW.process.last_executed_process_id==GW.process.process_id){

				if($("#"+GW.ssh.process_output_id).length && GW.workspace.currentmode == 1){
				
					if(this.current_process_log_length > 5000){
			
						$("#"+GW.ssh.process_output_id).find('p:first').remove();
			
						this.current_process_log_length -= 1
			
					}
			
					$("#"+GW.ssh.process_output_id).append(newline);
			
					this.current_process_log_length += 1
					
				}
			
			}
			

	    },

		clearProcessLog: function(){

			$("#"+GW.ssh.process_output_id).html("");

		},

		clearMain: function(){

			$("#log-window").html("");

		},
	    
	    getWsPrefixURL: function(){
	    	
	    	var s = ((window.location.protocol === "https:") ? "wss://" : "ws://") + window.location.host + "/Geoweaver/";
	    	
//	    	s += "Geoweaver/"; //this is gone in spring boot
	    	
	    	console.log("Ws URL Prefix: ", s)
	    	
	    	return s;
	    	
	    },
	    
	    startLogSocket: function(token){
	    	
			if(GW.ssh.all_ws) GW.ssh.all_ws.close();
	    	
	    	GW.ssh.all_ws = new WebSocket(this.getWsPrefixURL() + "command-socket");

			GW.ssh.ws = GW.ssh.all_ws;
	        
			GW.ssh.output_div_id = "log_box_id";
	        
			GW.ssh.token = token; //token is the jsession id
			
//			this.echo("Running process " + token)
	        
			GW.ssh.all_ws.onopen = function(e) { GW.ssh.ws_onopen(e) };
	        
			GW.ssh.all_ws.onclose = function(e) { GW.ssh.ws_onclose(e) };
	        
			GW.ssh.all_ws.onmessage = function(e) { GW.ssh.ws_onmessage(e) };
	        
			GW.ssh.all_ws.onerror = function(e) { GW.ssh.ws_onerror(e) };
	    	
	    },

		connectWsSessionWithExecution: function(msg){

			

		},

		openLog: function(msg){
			
			//check if the websocket session is alive, otherwise, restore the connection

			if (GW.ssh.all_ws!=null && (GW.ssh.all_ws.readyState === WebSocket.CLOSED || GW.ssh.all_ws.readyState === WebSocket.CLOSING) ) {

				// GW.ssh.all_ws.close();
				
				console.log("The command websocket connection is detected to be closed. Try to reconnect...");
				
				GW.ssh.startLogSocket(msg.token);
				
				console.log("The console websocket connection is restored..");
				
			}else{
				
				GW.ssh.checkSessionStatus();
			
			}
			
//			$("#log-window").slideToggle(true);
//			switchTab(document.getElementById("main-console-tab"), "main-console");
			// GW.general.switchTab("console");

			// this.send("history_id:" + msg.history_id);

			// this.send("token:" + msg.token); //the websocket is still in connecting state
			if(msg.history_id.length==12)
				this.addlog("=======\nStart to execute Process " + msg.history_id);
			else
				this.addlog("=======\nStart to execute Workflow " + msg.history_id);
			
	    },
	    
	    openTerminal: function(token, terminal_div_id){
	    	
//	        shell = $('#content').terminal(function (command, term) {
//	        		
//	        		send(command);
//	        		
//	            }, {
//	            	
//	                prompt: '['+user+'@'+host+': ~]# ',
//	                
//	                name: 'Geoweaver SSH on Web',
//	                
//	                scrollOnEcho: true,
//	                
//	                greetings: "SSH on Web started. Type 'exit' to quit. \nThis system is funded by National Science Foundation (https://nsf.gov)."
//	                
//	            }
//	            
//	        );
	    	
	    }
		
}