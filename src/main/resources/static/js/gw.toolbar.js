/**
 * 
 * This file contains event listeners of all the top buttons
 * 
 */

GW.toolbar = {
		
		monitor_switch: false, 
		
		init: function(){
			
			$("#toolbar-search").click(GW.search.showDialog);
			
			$("#toolbar-add").click(this.add);
			
			$("#toolbar-monitor").click(this.monitor);
			
			$("#toolbar-history").click(this.history);
			
			$("#toolbar-settings").click(this.settings);
			
			$("#toolbar-print").click(this.print);
			
			this.listenLogWindowSlider();
			
			$('[data-toggle="tooltip"]').tooltip();   
			
		},
		
		settings: function(){
			
			GW.settings.showDialog();
			
		},
		
		print: function(){
			
			window.print();
			
		},
		
		history: function(){
			
			//list recent execution history
			
			var content = "<div class=\"modal-body\"><div class=\"btn-group\" role=\"group\" >"+
			"  <button type=\"button\" class=\"btn btn-secondary\" id=\"history-process-d\">Process</button>"+
			"  <button type=\"button\" class=\"btn btn-secondary\"  id=\"history-workflow-d\">Workflow</button>"+
			"</div></div>"
			
			var frame = GW.process.createJSFrameDialog(300, 180, content, "Recent History");
			
            frame.on('#history-process-d', 'click', (_frame, evt) => {
            	GW.process.recent(20, true);
            	_frame.closeFrame();
            });
            
            frame.on('#history-workflow-d', 'click', (_frame, evt) => {
            	GW.workflow.recent(20, true);
            	_frame.closeFrame();
            });
            
		},
		
		monitor: function(){
			
			// if(this.monitor_switch){
				
			// 	GW.monitor.closeProgressIndicator();
				
			// 	GW.monitor.closeWorkspaceIndicator();
				
			// }else{
				
			// 	GW.monitor.openProgressIndicator();
				
			// 	GW.monitor.openWorkspaceIndicator();
				
			// }

			//show the running processes and workflows

			GW.monitor.showDialog();
			
			this.monitor_switch = !this.monitor_switch;
			
		},
		
		add: function(){
			
			var dialogid = GW.process.getRandomId();
			
			var content = "<div style=\"padding:15px; text-align:center;\" class=\"btn-group\" role=\"group\" >"+
			"  <button type=\"button\" class=\"btn btn-secondary\" id=\"newhost-d\">Host</button>"+
			"  <button type=\"button\" class=\"btn btn-secondary\" id=\"newprocess-d\">Process</button>"+
			"  <button type=\"button\" class=\"btn btn-secondary\"  id=\"newworkflow-d\">Workflow</button>"+
			"</div>"
			
			var frame = GW.process.createJSFrameDialog(320, 180, content, "New");
			
			$("#newhost-d").click(function(){
				
				GW.host.newDialog();
				
				frame.closeFrame();
				
			});
			
			// for new dialogue
			$("#newprocess-d").click(function(){
				
				GW.process.newDialog();
				
				frame.closeFrame();
				
			});
			
			$("#newworkflow-d").click(function(){
				
				GW.workflow.newDialog();
				
				frame.closeFrame();
				
			});
			
		},
		
		listenLogWindowSlider: function(){
			
//			$("#log-window").slideToggle(true);
//			
//			$(".btn-minimize").click(function(){
//			    
//				$(this).toggleClass('btn-plus');
//			    
//			    $("#log-window").slideToggle();
//			
//			});
			
		}
		
}
