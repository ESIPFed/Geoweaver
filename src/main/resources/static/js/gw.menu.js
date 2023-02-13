
GW.menu = {
		
		types: ["host", "process", "workflow"],
		
		del_frame: null,
		
		init : function(){
			
			for(var i=0;i<this.types.length;i++){

				GW.menu.list(this.types[i]);
				
				GW.menu.listen(this.types[i]);
				
			}
			
			$('[data-toggle="tooltip"]').tooltip();

			$('.side_menu_collapse_btn').click(GW.menu.toggle_side_panel)

			$('.workspace_collapse_btn').click(GW.menu.toggle_side_panel)

			GW.search.init() // initialize the search bar
			
		},

		refresh: function(){
			
			GW.host.refreshHostList();

			GW.process.refreshProcessList();

			GW.workflow.refreshWorkflowList();

		},

		refreshSearchResults: function(){

			GW.host.refreshSearchList();

			GW.process.refreshSearchList();

			GW.workflow.refreshSearchList();

		},
		
		getPanelIdByType: function(type){
			
			return type + "s";
			
		},
		
		
		setFullScreen: function(dialog){
			
			dialog.getModal().css('width', '100%');
			
			dialog.getModal().css('height', '100%');
			
			dialog.getModal().css('padding', '0');
			
			dialog.getModalDialog().css('width', '100%');
			
			dialog.getModalDialog().css('height', '100%');
			
			dialog.getModalDialog().css('margin', '0');
//			
			dialog.getModalContent().css('height', '100%');
//			
			dialog.getModalBody().css('height', '85%');
			
			dialog.getModalBody().children()[0].style.height =  '100%';
			
			dialog.getModalBody().children()[0].children[0].style.height = '100%';
			
		},
		
		unescape: function(value){
			
			String.prototype.replaceAll = function(search, replacement) {
			    var target = this;
			    return target.replace(new RegExp(search, 'g'), replacement);
			};
			
			var resp = value.replaceAll("-.-", "/")
			.replaceAll("-·-", "'")
			  .replaceAll("-··-", "\"")
//			  .replaceAll("->-", "\\n")
			  .replaceAll("-!-->-", "<br/>");
			
			return resp;
			
		},
		
		details: function(id, type, detail_callback){
			
			$.ajax({
				
				url: "detail",
				
				method: "POST",
				
				data: "type=" + type + "&id=" + id
				
			}).done(function(msg){
				
				msg = $.parseJSON(msg);

				if(detail_callback == null){

					if(type=="process"){
						
						GW.process.display(msg);
						
					}else if(type=="host"){
						
						GW.host.display(msg);
						
					}else if(type=="workflow"){
						
						GW.workflow.display(msg);

						GW.workflow.add(msg.id, msg.name, false) //load it into the weaver too
						
					}

				}else{

					detail_callback(msg);

				}
				
				
			});
			
		},
		
		list: function(type){
			
			$.ajax({
				
				url: "list",
				
				method: "POST",
				
				data: "type="+type
				
			}).done(function(msg){
				
				msg = $.parseJSON(msg);
				
				if(type=="host"){
					
					GW.host.list(msg);
					
				}else if(type=="process"){
					
					GW.process.list(msg);
					
				}else if(type=="workflow"){
					
					GW.workflow.list(msg);
					
				}
				
				
			}).fail(function(jxr, status){
				
				console.error("fail to list " + type);
				
			});
			
			
		},
		
		del: function(id, type){
			
			var content = '<div class="modal-body"  style="font-size: 12px;">'+
				'Are you sure to remove this '+type+'?'+
				'</div>';
			
			content += '<div class="modal-footer">' +
				"	<button type=\"button\" id=\"del-confirm-btn\" class=\"btn btn-outline-primary\">Yes</button> "+
				"	<button type=\"button\" id=\"del-cancel-btn\" class=\"btn btn-outline-secondary\">Cancel</button>"+
				'</div>';
			
			GW.menu.del_frame = GW.process.createJSFrameDialog(320, 150, content, "Alert")
			
			$("#del-confirm-btn").click(function(){
				
				$.ajax({
    				
    				url: "del",
    				
    				method: "POST",
    				
    				//remove the database record
    				data: "id="+id + "&type=" + type
    				
    			}).done(function(msg){
    				
    				if(msg==="done"){
    					
    					//remove the menu item
    					$("#"+type+"-" + id).remove();
    					
    					if(type==="process"){
    						
    						//remove the workspace object
    						GW.workspace.theGraph.removeNodes(id);
    						

    					}
    					$("#main-"+type+"-content").empty()
    					console.log("the element is removed " + type + "-" + id);

						openCity(event, 'main-general');
    					
    				}else{
    					
    					console.error("fail to remove " + id);
    					
    				}
    				
    			}).fail(function(jxr, status){
    				
    				console.error("fail to delete " + status);
    				
    			});
            	
				GW.menu.del_frame.closeFrame();
				
			});
			
			$("#del-cancel-btn").click(function(){
				
				GW.menu.del_frame.closeFrame();
				
			});
			
		},
		
		listen: function(type){
			
			$("#new" + type).click(function(e){

				e.stopPropagation();
		        e.preventDefault();
				
				if(type=="host"){
					
					GW.host.newDialog();
					
				}else if(type=="process"){
					
					GW.process.newDialog();
					
				}else if(type=="workflow"){

					GW.workflow.newDialog();
					
				}
				
			});
			
		},

		toggle_side_panel: function(){

			if($('#sidemenu').width()!=0){
				$("#sidemenu").addClass("invisible").width(0);
				$( "#main_panel_div" ).addClass( "col-md-12" ).removeClass( "col-md-9" );
				$(".workspace_collapse_btn").css("visibility", "visible")
			}else{
				$("#sidemenu").removeClass("invisible").width("");
				$( "#main_panel_div" ).addClass( "col-md-9" ).removeClass( "col-md-12" );
				$(".workspace_collapse_btn").css("visibility", "hidden")
			}

		},
		
};