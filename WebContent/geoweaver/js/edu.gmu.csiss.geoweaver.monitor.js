/**
 * 
 * workflow monitor
 * 
 * @author Ziheng Sun
 * 
 */

edu.gmu.csiss.geoweaver.monitor = {
		
		ws: null,
		
		current_name: null, //current workflow or process name
		
		historyid: null,
		
		send: function (data) {
	    	
	        if(this.ws != null){
	      	
	        	this.ws.send(data);
	        
	        } else {
	        
	        	this.error('not connected!');
	        
	        }
	    },
		
		ws_onopen: function(e){
			
			this.send(this.historyid);
			
		},
		
		ws_onclose: function(e){
			
			this.ws = null;
			
			edu.gmu.csiss.geoweaver.workspace.currentmode = 1;
			
			console.log("this workflow monitor websocket has been closed");
			
			edu.gmu.csiss.geoweaver.monitor.closeWorkspaceIndicator();
			
		},
		
		ws_onmessage: function(e){
			
			try {
		    	
//		        if(e.data.indexOf(edu.gmu.csiss.geoweaver.ssh.special.prompt) == -1 && 
//		        		
//		        		e.data.indexOf(edu.gmu.csiss.geoweaver.ssh.special.ready) == -1) {
				
		        	var returnmsg = $.parseJSON(e.data);
		        	
		        	console.log(returnmsg);
		        	
		        	if(returnmsg.builtin){
		        		
		        		edu.gmu.csiss.geoweaver.process.callback(returnmsg);
		        		
		        	}else{
		        		
		        		edu.gmu.csiss.geoweaver.workspace.updateStatus(returnmsg);
		        		
		        	}
		        	
//		        }else{
//		        	
//		        	//the websocket is already closed. try the history query
//		        	
//		        	console.error("It ends too quickly. Go to history to check the logs out.");
//		        	
//		        }
		        
		      } catch(err) {
		    	
		    	console.error("** Invalid server response : " + err); 
		        
		      }
			
		},
		
		ws_onerror: function(e){
			
			console.error("error in monitoring workflow " + e );
			
			edu.gmu.csiss.geoweaver.monitor.closeWorkspaceIndicator();
			
		},
		
		openWorkspaceIndicator: function(){
			
			$("#current_workflow_name").html("Current workflow : " + edu.gmu.csiss.geoweaver.workflow.loaded_workflow);
			
			$("#workspace_status_indicator").removeClass("invisible");
			
			$("#workspace_status_indicator").addClass("visible");
			
			console.log("workspace indicator is opened");
		},
		
		closeWorkspaceIndicator: function(){
			
			$("#current_workflow_name").html("");
			
			$("#workspace_status_indicator").removeClass("visible");
			
			$("#workspace_status_indicator").addClass("invisible");
			
			console.log("workspace indicator is closed");
			
		},
		
		/**
		 * 
		 * connect with the websocket session and get message to update the workflow status in the workspace
		 * 
		 */
		startMonitor: function(historyid){
			
//			//only start when the mode is in monitor mode
//			
//			if(edu.gmu.csiss.geoweaver.workspace.currentmode == edu.gmu.csiss.geoweaver.workspace.MONITOR){
			
				edu.gmu.csiss.geoweaver.workspace.currentmode = 2;
				
				edu.gmu.csiss.geoweaver.monitor.openWorkspaceIndicator();

				edu.gmu.csiss.geoweaver.monitor.ws = new SockJS("task");
		        
				edu.gmu.csiss.geoweaver.monitor.historyid = historyid;
		        
				edu.gmu.csiss.geoweaver.monitor.ws.onopen = function(e) { edu.gmu.csiss.geoweaver.monitor.ws_onopen(e) };
		        
				edu.gmu.csiss.geoweaver.monitor.ws.onclose = function(e) { edu.gmu.csiss.geoweaver.monitor.ws_onclose(e) };
		        
				edu.gmu.csiss.geoweaver.monitor.ws.onmessage = function(e) { edu.gmu.csiss.geoweaver.monitor.ws_onmessage(e) };
		        
				edu.gmu.csiss.geoweaver.monitor.ws.onerror = function(e) { edu.gmu.csiss.geoweaver.monitor.ws_onerror(e) };
				
//			}
			
		},
		
		stopMonitor: function(){
			
			if(this.ws != null){
				
				this.ws.close();
				
				this.ws = null;
				
				edu.gmu.csiss.geoweaver.workspace.currentmode = 1;
				
			}
			
		}
		
}

