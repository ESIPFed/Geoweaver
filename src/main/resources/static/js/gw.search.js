/**
 * Geoweaver Search Module
 */

GW.search = {

	keywords: "",

	init: function(){

		$("#instant_search_bar").on("input", function(){

			GW.search.keywords = $(this).val();

			console.log("search string changed to: " + GW.search.keywords);

			GW.menu.refreshSearchResults();

			if(GW.search.keywords != ""){

				$("#clean_search_field").css("visibility", "visible")

			}else{

				$("#clean_search_field").css("visibility", "hidden")

			}

		});

		$("#clean_search_field").click(function(){

			$("#instant_search_bar").val("")

			GW.search.keywords = "";

			$("#clean_search_field").css("visibility", "hidden")

			GW.menu.refreshSearchResults();

		});
	},

	filterMenuListUtil: function(folder_div_name, parent_div_name, li_class_name){

		$("#"+parent_div_name).collapse("show");

		$("#"+folder_div_name).collapse("show");

		$("#"+parent_div_name).find("li."+li_class_name).each(function(index){

			host_name_div = $( this ).find("div.col-md-8").first()

			if(host_name_div.length == 0)
				host_name_div = $( this ) //workflow doesn't have second level div

			host_name = host_name_div.text()

			if(GW.search.keywords!=""){
			
				if(host_name.toLowerCase().includes(GW.search.keywords.toLowerCase())){

					// $(this).css("background-color", "yellow")
					var re = new RegExp(GW.search.keywords,"g");
					new_host_name = host_name.replace(re,"<font style='background-color: yellow; color: black;'>"+
						GW.search.keywords+"</font>")
					host_name_div.html(new_host_name)

					$(this).show()

				}else{

					var re = new RegExp("<font style='background-color: yellow; color: black;'>"+
					GW.search.keywords+"</font>","g");
					new_host_name = host_name.replace(re, GW.search.keywords)

					host_name_div.html(new_host_name)

					$(this).hide()

				}
			
			}else{

				var re = new RegExp("<font style='background-color: yellow; color: black;'>"+
					GW.search.keywords+"</font>","g");
				
				new_host_name = host_name.replace(re, GW.search.keywords)

				host_name_div.html(new_host_name)
				
				$(this).show()

				$("#"+folder_div_name).collapse("hide");

				$("#"+parent_div_name).collapse("hide");
				
			}
		
		})

	},
		
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
		
		var frame = GW.process.createJSFrameDialog(800, 500, content, "Search Results")
		
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
		
		var frame = GW.process.createJSFrameDialog(500, 200, content, "Search")
		
		$("#search").click(function(){
			
			GW.search.send($("#keywords").val(), $("#resource-type-select").val());
			
		});
		
	}
		
}
