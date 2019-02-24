/**
 * Geoweaver Search Module
 * @author Ziheng Sun
 */

edu.gmu.csiss.geoweaver.search = {
		
		showResults: function(data){
			
			var content = "<table class=\"table\">"+
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
					"      <td><a href=\"javascript:void(0)\" onclick=\"edu.gmu.csiss.geoweaver.menu.details('"+
					data[i].id + "', '" + data[i].type + 
					"')\" >"+data[i].name+"</td>"+
					"      <td>"+data[i].desc+"</td>"+
					"      <td>"+data[i].type+"</td>"+
					"    </tr>";
				
			}
			
			content += "</tbody></table>";
			
			BootstrapDialog.show({
				
				title: "Search Results",
				
				message: content,
				
				buttons: [{
					
					label: "Close",
					
					action: function(dialogItself){
						
						dialogItself.close();
						
					}
					
				}]
				
			});
			
		},
		
		send: function(keywords, type){
			
			$.ajax({
				
				url: "search",
				
				type: "POST",
				
				data: { keywords: keywords, type: type}
				
			}).success(function(data){
				
				data = $.parseJSON(data);
				
				edu.gmu.csiss.geoweaver.search.showResults(data);
				
			}).fail(function(){
				
				alert("Fail to send search request.");
				
			});
			
		},
		
		selectType: function(){
			
			$("#resource-type-select").text($(this).text());
			
		},
		
		showDialog: function(){

			var content = "<div class=\"row\"><div class=\"md-form active-cyan-2 mb-3 col-md-8\">"+
			"	  <input class=\"form-control\" type=\"text\" placeholder=\"Search\" id=\"keywords\" aria-label=\"Search\">"+
			"	</div><div class=\"col-md-4\"><select class=\"form-control\" id=\"resource-type-select\"> "+
			"	  <option selected value=\"all\">All</option> "+
			"	  <option value=\"host\">Host</option> "+
			"	  <option value=\"process\">Process</option> "+
			"	  <option value=\"workflow\">Workflow</option> "+
			"	</select>"+
			"	</div></div></div>";
			
//			var content = "<div class=\"input-group\">"+
//			"  <input type=\"text\" class=\"form-control\" placeholder=\"Search\" id=\"keywords\"  aria-label=\"Search\">"+
//			"  <div class=\"input-group-append\">"+
//			"    <button class=\"btn btn-outline-secondary dropdown-toggle\" type=\"button\" data-toggle=\"dropdown\" aria-haspopup=\"true\" aria-expanded=\"false\"  id=\"resource-type-select\">All</button>"+
//			"    <div class=\"dropdown-menu\">"+
//			"      <a class=\"dropdown-item\" onclick=\"edu.gmu.csiss.geoweaver.search.selectType()\" href=\"javascript:void(0)\">All</a>"+
//			"      <a class=\"dropdown-item\" onclick=\"edu.gmu.csiss.geoweaver.search.selectType()\" href=\"javascript:void(0)\">Host</a>"+
//			"      <a class=\"dropdown-item\" onclick=\"edu.gmu.csiss.geoweaver.search.selectType()\" href=\"javascript:void(0)\">Process</a>"+
//			"      <div role=\"separator\" class=\"dropdown-divider\"></div>"+
//			"      <a class=\"dropdown-item\" href=\"javascript:void(0)\">Workflow</a>"+
//			"    </div>"+
//			"  </div>"+
//			"</div>";
			
			BootstrapDialog.show({
				
				title: "Search",
				
				message: content,
				
				buttons: [{
					
					label: "Search",
					
					action: function(dialogItself){
						
						edu.gmu.csiss.geoweaver.search.send($("#keywords").val(), $("#resource-type-select").val());
						
					}
					
				},{
					
					label: "Close",
					
					action: function(dialogItself){
						
						dialogItself.close();
						
					}
					
				}]
				
			});
			
		}
		
}
