
/**
 * 
 * SSH client
 * @author Ziheng Sun
 * 
 */

edu.gmu.csiss.geoweaver.ssh = {
		
		shell: null,
	    
		sshConnected : false,
	    
	    passwordPhase : false,
	    
	    user : null, 
    	
	    host : null, 
    	
    	port : null, 
    	
    	token : null,
    	
    	output_div_id: null,
	    
	    ws: null,
	    
	    last_prompt: null,
	    
	    password_cout: 0,
	    
//		root : this.getContextURLPath(),
		
		key : '',
		
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
	    	
	    	content = content.replace(/\n/g,'<br/>')
	    	
//	    	$("#"+this.output_div_id).append("<p style=\"text-align: left; \">"+content+"</p>"); //show all the ssh output
	    	this.addlog(content);
	    	
	    },
	    
	    error: function(content){
	    	
	    	content = content.replace(/\n/g,'<br/>')
	    	
//	    	$("#"+this.output_div_id).append("<p style=\"color:red;text-align: left; \">"+content+"</p>"); //show all the ssh output
	    	
	    	this.addlog(content);
	    	
	    },
	    	  
	    ws_onopen: function (e) {
	    	
	      //open the indicator
//	      edu.gmu.csiss.geoweaver.monitor.openWorkspaceIndicator();
	      
	      //shell.echo(special.white + "connected" + special.reset);
	      this.echo("connected");
	      // link the SSH session established with spring security logon to the websocket session...
	      this.send(this.token);
	      
	    },
	    
	    send: function (data) {
	    	
	        if(this.ws != null){
	      	
	        	this.ws.send(data);
	        
	        } else {
	        
	        	this.error('not connected!');
	        
	        }
	    },

	    ws_onclose: function (e) {
	    	
	        try{

//	        	edu.gmu.csiss.geoweaver.monitor.closeWorkspaceIndicator();
	        	
	        	this.echo("disconnected");
	        	
//	        	this.destroy();
//	            
//	        	this.purge();
	            
	        }catch(e){
	        	
	        	console.error(e);
	        }
	        
	        console.log("the websocket has been closed");
	        //trigger the event to close the dialog
	        
//	    	document.forms['logout'].submit();
	    },

	    ws_onerror: function (e) {
	    	
	        this.error(e);
	        
	    },
	    
	    ws_onmessage: function (e) {
	      
	      try {
	    	
	        if(e.data.indexOf(this.special.prompt) == -1 && 
	        		
	        		e.data.indexOf(this.special.ready) == -1 && 
	        		
	        		e.data.indexOf("No SSH connection is active") == -1) {
	            
	        	this.echo(e.data);
	        	
	        }else{
	        	
	        	//the websocket is already closed. try the history query
	        	
	        	this.echo("It ends too quickly. Go to history to check the logs out.");
	        	
	        }
	        
	        //if (e.data.indexOf(special.ready) != -1) {
	        
	        //	shell.resume();
			
	        //}
	        
	      } catch(err) {
	    	  
	    	console.log(err);
	      
	    	this.error("** Invalid server response : " + e.data); 
	        
	      }
	      
	    },

//	    getContextURLPath: function () {
//	        var rootUrl = location.protocol;
//	        rootUrl = rootUrl+"//"+location.host;
//	        var path = location.pathname;
//	        var tempStr = path.split('/');
//	        rootUrl = rootUrl+"/"+tempStr[1];
//	        return rootUrl;
//	    },
	    
	    addlog: function(content){
	    	var dt = new Date();
	    	var time = dt.getHours() + ":" + dt.getMinutes() + ":" + dt.getSeconds();
	    	$("#log-window").append("<p style=\"line-height:1.1; text-align:left;\"><span style=\"color:green;\">"
	    			+ time + "</span> " + content + "</p>");
//	    	$("#log-window").animate({ scrollTop: $('#log-window').prop("scrollHeight")}, 1);
	    },

		openLog: function(token){
			
			$("#log-window").slideToggle(true);
			
//			BootstrapDialog.show({
//				
//				title: "SSH log",
//				
//				message: "<div id=\"log_box_id\">",
//				
//				closable: true,
//				
//				onshown: function(){
					
					edu.gmu.csiss.geoweaver.ssh.ws = new SockJS("shell");
			        
					edu.gmu.csiss.geoweaver.ssh.output_div_id = "log_box_id";
			        
					edu.gmu.csiss.geoweaver.ssh.token = token;
			        
					edu.gmu.csiss.geoweaver.ssh.ws.onopen = function(e) { edu.gmu.csiss.geoweaver.ssh.ws_onopen(e) };
			        
					edu.gmu.csiss.geoweaver.ssh.ws.onclose = function(e) { edu.gmu.csiss.geoweaver.ssh.ws_onclose(e) };
			        
					edu.gmu.csiss.geoweaver.ssh.ws.onmessage = function(e) { edu.gmu.csiss.geoweaver.ssh.ws_onmessage(e) };
			        
					edu.gmu.csiss.geoweaver.ssh.ws.onerror = function(e) { edu.gmu.csiss.geoweaver.ssh.ws_onerror(e) };
			        
//				},
//
//				onclose: function(){
//					
//					//do nothing - the websocket session should be closed
//				},
//				
//				buttons: [
//					// NO Turn Back! is the symbol of web-based system
////					{
////					
////					label: "Interrupt",
////					
////					action: function(dialog){
////						
////						//send the message to shut down the SSH session and stop the process
////						
////						edu.gmu.csiss.geoweaver.ssh.ws.send("exit");
////						
//////						dialog.close();
////						
////					}
////					
////				},
//				
//				{
//					
//					label: "Run in background",
//					
//					action: function(dialog){
//						
//						dialog.close();
//						
//					}
//					
//				},{
//					
//					label: "Close",
//					
//					action: function(dialog){
//						
//						dialog.close();
//						
//					}
//					
//				}]
//				
//			});
			
	        
			
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