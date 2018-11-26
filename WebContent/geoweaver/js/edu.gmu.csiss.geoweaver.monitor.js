/**
 * 
 * workflow monitor
 * 
 * @author Ziheng Sun
 * 
 */

edu.gmu.csiss.geoweaver.monitor = {
		
		ws: null,
		
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
			
		},
		
		ws_onmessage: function(e){
			
			try {
		    	
//		        if(e.data.indexOf(edu.gmu.csiss.geoweaver.ssh.special.prompt) == -1 && 
//		        		
//		        		e.data.indexOf(edu.gmu.csiss.geoweaver.ssh.special.ready) == -1) {
		            
		        	var statuslist = $.parseJSON(e.data);
		        	
		        	edu.gmu.csiss.geoweaver.workspace.updateStatus(statuslist);
		        	
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

				edu.gmu.csiss.geoweaver.monitor.ws = new SockJS("http://" + location.host + "/CyberConnector/web/task");
		        
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

