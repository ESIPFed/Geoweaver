/**
 * 
 * This class is for comparing two historical version of one process/notebook/script
 * 
 */

GW.comparison = {
		
	list_history: [],
		
	show: function(){

		this.list_history = []; //clear the current comparison
		
		var count = 0;
		
		$(".hist-checkbox:checked").each(function() {
			
			var histid = $(this).attr('id');
			
			count+=1;
			
			if(count > 2) {
				
				return;
				
			}
			
//		    console.log("Removing "+histid);
		    
//		    GW.host.deleteJupyterDirectly(histid.substring(9));
			
			$.ajax({
				
				url: "log",
				
				method: "POST",
				
				data: "type=host&id=" + histid
				
			}).done(function(msg){
				
				if(msg==""){
					
					alert("Cannot find the host history in the database.");
					
					return;
					
				}
				
				msg = $.parseJSON(msg);
				
				var code = msg.output;
				
				GW.comparison.list_history.append(code);
				
				if (GW.comparison.list_history.length==2) {
					

					if(code!=null && typeof code != 'undefined'){
						
						if(typeof code != 'object'){
						
							code = $.parseJSON(code);
						
						}
						
						var notebook = nb.parse(code.content);
						
						var rendered = notebook.render();
						
						var content = '<div class="modal-body">'+$(rendered).html()+'</div>';
						
						content += '<div class="modal-footer">' +
	    				"	<button type=\"button\" id=\"host-history-download-btn\" class=\"btn btn-outline-primary\">Download</button> "+
	    				"	<button type=\"button\" id=\"host-history-cancel-btn\" class=\"btn btn-outline-secondary\">Cancel</button>"+
	    				'</div>';
						
//						console.log(content);
						
						GW.host.his_frame = GW.process.createJSFrameDialog(800, 600, content, "History Jupyter Notebook " + history_id);
						
						$("#host-history-download-btn").click(function(){
							
							GW.host.downloadJupyter(history_id);
							
						})
						
						$("#host-history-cancel-btn").click(function(){
	        				
	        				GW.host.his_frame.closeFrame();
	        				
	        			});
						
					}
					
					
					
				}
				
				
			}).fail(function(e){
				
				console.error(e);
				
			});
		    
		});

	}
		
		
}
