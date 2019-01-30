/**
 * 
 * This file contains event listeners of all the top buttons
 * 
 * @author Ziheng Sun
 * 
 */

edu.gmu.csiss.geoweaver.toolbar = {
		
		init: function(){
			
			$("#toolbar-search").click(edu.gmu.csiss.geoweaver.toolbar.search);
			
			$("#toolbar-add").click(edu.gmu.csiss.geoweaver.toolbar.add);
			
			this.listenLogWindowSlider();
			
		},
		
		search: function(){
			
			edu.gmu.csiss.geoweaver.monitor.openWorkspaceIndicator();
			
		},
		
		add: function(){
			
			edu.gmu.csiss.geoweaver.monitor.closeWorkspaceIndicator();
			
		},
		
		listenLogWindowSlider: function(){
			
			$("#log-window").slideToggle(true);
			
			$(".btn-minimize").click(function(){
			    
				$(this).toggleClass('btn-plus');
			    
			    $("#log-window").slideToggle();
			
			});
			
		}
		
}
