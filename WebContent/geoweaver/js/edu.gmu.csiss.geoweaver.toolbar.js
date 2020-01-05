/**
 * 
 * This file contains event listeners of all the top buttons
 * 
 * @author Ziheng Sun
 * 
 */

edu.gmu.csiss.geoweaver.toolbar = {
		
		monitor_switch: false,
		
		init: function(){
			
			$("#toolbar-search").click(edu.gmu.csiss.geoweaver.search.showDialog);
			
			$("#toolbar-add").click(this.add);
			
			$("#toolbar-monitor").click(this.monitor);
			
			$("#toolbar-history").click(this.history);
			
			$("#toolbar-settings").click(this.settings);
			
			$("#toolbar-print").click(this.print);
			
			this.listenLogWindowSlider();
			
		},
		
		settings: function(){
			
			edu.gmu.csiss.geoweaver.settings.showDialog();
			
		},
		
		print: function(){
			
			window.print();
			
		},
		
		history: function(){
			
			//list recent execution history
			
			var width = 300, height = 180;
			
	    	const frame = edu.gmu.csiss.geoweaver.workspace.jsFrame.create({
	    		title: 'Recent History',
	    	    width: width, height: height,
	    	    appearanceName: 'yosemite',
	    	    movable: true,
	    	    html:  "<div class=\"modal-body\"><div class=\"btn-group\" role=\"group\" >"+
				"  <button type=\"button\" class=\"btn btn-secondary\" id=\"history-process-d\">Process</button>"+
				"  <button type=\"button\" class=\"btn btn-secondary\"  id=\"history-workflow-d\">Workflow</button>"+
				"</div></div>"
	    	});
	    	
//	    	frame.setPosition(window.innerWidth / 2, window.innerHeight / 2, 'CENTER_BOTTOM');
	    	
	    	frame.setControl({
	            maximizeButton: 'maximizeButton',
	            demaximizeButton: 'restoreButton',
	            minimizeButton: 'minimizeButton',
	            deminimizeButton: 'deminimizeButton',
	            animation: true,
	            animationDuration: 200,

	        });
	    	
            frame.on('#history-process-d', 'click', (_frame, evt) => {
            	edu.gmu.csiss.geoweaver.process.recent(20);
            	_frame.closeFrame();
            });
            
            frame.on('#history-workflow-d', 'click', (_frame, evt) => {
            	edu.gmu.csiss.geoweaver.workflow.recent(20);
            	_frame.closeFrame();
            });
            
	    	//Show the window
	    	frame.show();
	    	
	    	frame.setPosition((window.innerWidth - width) / 2, (window.innerHeight -height) / 2, 'LEFT_TOP');
			
//			BootstrapDialog.show({
//				
//				title: "Recent History",
//				
//				message: "<div class=\"btn-group\" role=\"group\" >"+
//					"  <button type=\"button\" class=\"btn btn-secondary\" id=\"history-process-d\">Process</button>"+
//					"  <button type=\"button\" class=\"btn btn-secondary\"  id=\"history-workflow-d\">Workflow</button>"+
//					"</div>",
//					
//				onshown: function(dialogRef){
//					
//					$("#history-process-d").click(function(){
//					
//						edu.gmu.csiss.geoweaver.process.recent(20);
//						
//						dialogRef.close();
//						
//					});
//					
//					$("#history-workflow-d").click(function(){
//						
//						edu.gmu.csiss.geoweaver.workflow.recent(20);
//						
//						dialogRef.close();
//						
//					});
//					
//				},
//				
//				buttons: [{
//					
//					label: "Close",
//					
//					action: function(dialogItself){
//						
//						dialogItself.close();
//						
//					}
//					
//				}]
//				
//			});
			
			
		},
		
		monitor: function(){
			
			if(this.monitor_switch){
				
				edu.gmu.csiss.geoweaver.monitor.closeProgressIndicator();
				
				edu.gmu.csiss.geoweaver.monitor.closeWorkspaceIndicator();
				
			}else{
				
				edu.gmu.csiss.geoweaver.monitor.openProgressIndicator();
				
				edu.gmu.csiss.geoweaver.monitor.openWorkspaceIndicator();
				
			}

			//show the running processes and workflows

			edu.gmu.csiss.geoweaver.monitor.showDialog();
			
			this.monitor_switch = !this.monitor_switch;
			
		},
		
		add: function(){
			
			BootstrapDialog.show({
				
				title: "New",
				
				message: "<div class=\"btn-group\" role=\"group\" >"+
					"  <button type=\"button\" class=\"btn btn-secondary\" id=\"newhost-d\">Host</button>"+
					"  <button type=\"button\" class=\"btn btn-secondary\" id=\"newprocess-d\">Process</button>"+
					"  <button type=\"button\" class=\"btn btn-secondary\"  id=\"newworkflow-d\">Workflow</button>"+
					"</div>",
					
				onshown: function(dialogRef){
					
					$("#newhost-d").click(function(){
						
						edu.gmu.csiss.geoweaver.host.newDialog();
						
						dialogRef.close();
						
					});
					
					$("#newprocess-d").click(function(){
						
						edu.gmu.csiss.geoweaver.process.newDialog();
						
						dialogRef.close();
						
					});
					
					$("#newworkflow-d").click(function(){
						
						edu.gmu.csiss.geoweaver.workflow.newDialog();
						
						dialogRef.close();
						
					});
					
				},
				
				buttons: [{
					
					label: "Close",
					
					action: function(dialog){
						
						dialog.close();
						
					}
					
				}]
				
			});
			
		},
		
		listenLogWindowSlider: function(){
			
			$("#log-window").slideToggle(true);
			
			$(".btn-minimize").click(function(){
			    
				$(this).toggleClass('btn-plus');
			    
			    $("#log-window").slideToggle();
			
			});
			
		}
		
}
