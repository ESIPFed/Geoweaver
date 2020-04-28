/**
 * Geoweaver Search Module
 * @author Ziheng Sun
 */

GW.search = {
		
		showResults: function(data){
			
			var content = "<div class=\"modal-body\" style=\"font-size: 12px;\"><table class=\"table\">"+
				"	  <thead>"+
				"	    <tr>"+
				"	      <th scope=\"col\">Name</th>"+
				"	      <th scope=\"col\">Description</th>"+
				"	      <th scope=\"col\">Type</th>"+
				"	    </tr>"+
				"	  </thead>"+
				"	  <tbody>";
			
			for(var i=0;i<data.length;i+=1){
				
				content += "<tr>"+
					"      <td><a href=\"javascript:void(0)\" onclick=\"GW.menu.details('"+
					data[i].id + "', '" + data[i].type + 
					"')\" >"+data[i].name+"</td>"+
					"      <td>"+data[i].desc+"</td>"+
					"      <td>"+data[i].type+"</td>"+
					"    </tr>";
				
			}
			
			content += "</tbody></table></div>";
			
			var width = 800; var height = 500;
			
			const frame = GW.workspace.jsFrame.create({
		    		title: 'Search Results',
		    	    left: 0, 
		    	    top: 0, 
		    	    width: width, 
		    	    height: height,
		    	    appearanceName: 'yosemite',
		    	    style: {
	                    backgroundColor: 'rgba(255,255,255,0.8)',
			    	    fontSize: 12,
	                    overflow:'auto'
	                },
		    	    html: content
	    	});
	    	
			frame.setControl({
	            styleDisplay:'inline',
	            maximizeButton: 'zoomButton',
	            demaximizeButton: 'dezoomButton',
	            minimizeButton: 'minimizeButton',
	            deminimizeButton: 'deminimizeButton',
	            hideButton: 'closeButton',
	            animation: true,
	            animationDuration: 150,
	
	        });
	    	
	    	frame.show();
	    	
	    	frame.setPosition((window.innerWidth - width) / 2, (window.innerHeight -height) / 2, 'LEFT_TOP');
			
		},
		
		send: function(keywords, type){
			
			$.ajax({
				
				url: "search",
				
				type: "POST",
				
				data: { keywords: keywords, type: type}
				
			}).success(function(data){
				
				data = $.parseJSON(data);
				
				GW.search.showResults(data);
				
			}).fail(function(){
				
				alert("Fail to send search request.");
				
			});
			
		},
		
		selectType: function(){
			
			$("#resource-type-select").text($(this).text());
			
		},
		
		showDialog: function(){

			var content = "<div class=\"modal-body\"><div class=\"row\"><div class=\"md-form active-cyan-2 mb-3 col-md-8\">"+
			"	  <input class=\"form-control\" type=\"text\" placeholder=\"Search\" id=\"keywords\" aria-label=\"Search\">"+
			"	</div><div class=\"col-md-4\"><select class=\"form-control\" id=\"resource-type-select\"> "+
			"	  <option selected value=\"all\">All</option> "+
			"	  <option value=\"host\">Host</option> "+
			"	  <option value=\"process\">Process</option> "+
			"	  <option value=\"workflow\">Workflow</option> "+
			"	</select>"+
			"	</div></div></div></div>";
			
			content += '<div class="modal-footer">' +
				"<button type=\"button\" id=\"search\" class=\"btn btn-outline-primary\">Search</button> "+
				'</div>';
			
			var width = 500; var height = 200;
			
			const frame = GW.workspace.jsFrame.create({
		    		title: 'Search',
		    	    left: 0, 
		    	    top: 0, 
		    	    width: width, 
		    	    height: height,
		    	    appearanceName: 'yosemite',
		    	    style: {
	                    backgroundColor: 'rgba(255,255,255,0.8)',
			    	    fontSize: 12,
	                    overflow:'auto'
	                },
		    	    html: content
	    	});
	    	
			frame.setControl({
	            styleDisplay:'inline',
	            maximizeButton: 'zoomButton',
	            demaximizeButton: 'dezoomButton',
	            minimizeButton: 'minimizeButton',
	            deminimizeButton: 'deminimizeButton',
	            hideButton: 'closeButton',
	            animation: true,
	            animationDuration: 150,
	
	        });
	    	
	    	frame.show();
	    	
	    	frame.setPosition((window.innerWidth - width) / 2, (window.innerHeight -height) / 2, 'LEFT_TOP');
	    	
	    	$("#search").click(function(){
	    		
	    		GW.search.send($("#keywords").val(), $("#resource-type-select").val());
	    		
	    	});
			
		}
		
}
