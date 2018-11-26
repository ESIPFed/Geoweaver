/**
 * Author: ZIheng Sun
 * Date: 4 Oct 2018
 */

edu.gmu.csiss.geoweaver.menu = {
		
		types: ["host", "process", "workflow"],
		
		init : function(){
			
			for(var i=0;i<this.types.length;i++){

				edu.gmu.csiss.geoweaver.menu.list(this.types[i]);
				
				edu.gmu.csiss.geoweaver.menu.listen(this.types[i]);
				
			}
			
			$('[data-toggle="tooltip"]').tooltip();
			
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
			
//			dialog.getModalHeader().css('height', '10%');
//			
//			dialog.getModalFooter().css('height', '10%');
			
//			dialog.open();
			
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
		
		details: function(id, type){
			
			$.ajax({
				
				url: "detail",
				
				method: "POST",
				
				data: "type=" + type + "&id=" + id
				
			}).done(function(msg){
				
				msg = $.parseJSON(msg);
				
				var content = "<dl class=\"row\">";
				
				jQuery.each(msg, function(i, val) {
					
					if(val!=null&&val!="null"&&val!=""){
						
//						if(i=="code"){
//							
//							val = edu.gmu.csiss.geoweaver.menu.unescape(val);
//							
//						}
						
						if(typeof val =='object')
						{
						  val = JSON.stringify(val);
						}
						
						content += "    <dt class=\"col col-md-3\">"+i+"</dt>"+
						"    <dd class=\"col col-md-7\">"+val+"</dd>";
					}

				});
				
				content += "</dl>";
				
				BootstrapDialog.show({
		            
					title: 'Details',
		            
		            message: content,
		            
		            buttons: [{
		                
		            	label: 'Ok',
		                
		                action: function(dialog) {
		                	
		                	dialog.close();
		                	
		                }
		            }, {
		            	
		                label: 'Cancel',
		                
		                action: function(dialog) {
		                
		                	dialog.close();
		                
		                }
		            
		            }]
		        
				});
				
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
					
					edu.gmu.csiss.geoweaver.host.list(msg);
					
				}else if(type=="process"){
					
					edu.gmu.csiss.geoweaver.process.list(msg);
					
				}else if(type=="workflow"){
					
					edu.gmu.csiss.geoweaver.workflow.list(msg);
					
				}
				
				
			}).fail(function(jxr, status){
				
				console.error("fail to list " + type);
				
			});
			
			
		},
		
		del: function(id, type){
			
			BootstrapDialog.show({
	            
				title: 'Alert',
	            
	            message: 'Are you sure to remove this '+type+'?',
	            
	            buttons: [{
	                
	            	label: 'Yes',
	                
	                action: function(dialog) {
	                	
	                	$.ajax({
	        				
	        				url: "del",
	        				
	        				method: "POST",
	        				
	        				//remove the database record
	        				data: "id="+id + "&type=" + type
	        				
	        			}).done(function(msg){
	        				
	        				if(msg=="done"){
	        					
	        					//remove the menu item
	        					$("#"+type+"-" + id).remove();
	        					
	        					if(type=="process"){
	        						
	        						//remove the workspace object
	        						edu.gmu.csiss.geoweaver.workspace.theGraph.removeNodes(id);
	        						
	        					}
	        					
	        					console.log("the element is removed " + type + "-" + id);
	        					
	        				}else{
	        					
	        					console.error("fail to remove " + id);
	        					
	        				}
	        				
	        			}).fail(function(jxr, status){
	        				
	        				console.error("fail to delete " + status);
	        				
	        			});
	                	
	                	dialog.close();
	                	
	                }
	            }, {
	            	
	                label: 'Cancel',
	                
	                action: function(dialog) {
	                
	                	dialog.close();
	                
	                }
	            
	            }]
	        
			});
			
		},
		
		listen: function(type){
			
			$("#new" + type).click(function(){
				
				if(type=="host"){
					
					edu.gmu.csiss.geoweaver.host.newDialog();
					
				}else if(type=="process"){
					
					edu.gmu.csiss.geoweaver.process.newDialog();
					
				}else if(type=="workflow"){

					edu.gmu.csiss.geoweaver.workflow.newDialog();
					
				}
				
			});
			
//			$("#testhost").click(function(){
//				
//				edu.gmu.csiss.geoweaver.menu.showSSHCmd("test111");
//				
//			});
			
		}
		
};