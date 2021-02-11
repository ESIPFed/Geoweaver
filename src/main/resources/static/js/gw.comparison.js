/**
 * 
 * This class is for comparing two historical version of one process/notebook/script
 * 
 */

GW.comparison = {
	
	list_hist_ids: [],
	
	list_history: [],
	
	compare_frame: null,
		
	show: function(){

		this.list_hist_ids = [];
		
		this.list_history = []; //clear the current comparison
		
		var count = 0;
		
		$(".hist-checkbox:checked").each(function() {
			
			var histid = $(this).attr('id').substring(9);
			
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
				
				if(code!=null && typeof code != 'undefined'){
					
					if(typeof code != 'object'){
					
						code = $.parseJSON(code);
					
					}
				
					GW.comparison.list_history.push(code);
					
					GW.comparison.list_hist_ids.push(histid);
				
					if (GW.comparison.list_history.length==2) {
						
						GW.comparison.showDialog();
						
					}
					
				}
				
				
			}).fail(function(e){
				
				console.error(e);
				
			});
		    
		});

	},
	
	showDialog: function(){
		
		var notebook1 = nb.parse(GW.comparison.list_history[0].content);
		
		var notebook2 = nb.parse(GW.comparison.list_history[1].content);
		
		var rendered1 = notebook1.render();
		
		var rendered2 = notebook2.render();
		
		var content = '<div class="modal-body">'+
				
				'	<div class="row">'+
				
				'		<div class="col-md-6">'+
				
				$(rendered1).html()+
				
				'		</div>'+
				
				'		<div class="col-md-6">'+
				
				$(rendered2).html()+
				
				'		</div>'+
				
				'	</div>'+
				
				'</div>';
		
		content += '<div class="modal-footer">' +
		"	<button type=\"button\" id=\"compare-download-btn\" class=\"btn btn-outline-primary\">Download</button> "+
		"	<button type=\"button\" id=\"compare-cancel-btn\" class=\"btn btn-outline-secondary\">Cancel</button>"+
		'</div>';
		
//		console.log(content);
		
		GW.comparison.compare_frame = GW.process.createJSFrameDialog(800, 600, content, "History Comparison " + GW.comparison.list_hist_ids[0] + 
				" vs " + GW.comparison.list_hist_ids[1]);
		
		$("#compare-download-btn").click(function(){
			
			GW.host.downloadJupyter(GW.comparison.list_hist_ids[0]);
			GW.host.downloadJupyter(GW.comparison.list_hist_ids[1]);
			
		})
		
		$("#compare-cancel-btn").click(function(){
			
			GW.comparison.compare_frame.closeFrame();
			
		});
		
	}
		
		
}
