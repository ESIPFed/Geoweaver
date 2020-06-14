/**
 * Author: ZIheng Sun
 * Date: 4 Oct 2018
 */

GW.menu = {
		
		types: ["host", "process", "workflow"],
		
		del_frame: null,
		
		init : function(){
			
			for(var i=0;i<this.types.length;i++){

				GW.menu.list(this.types[i]);
				
				GW.menu.listen(this.types[i]);
				
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
				
				var content = "<div class=\"modal-body\"><dl class=\"row\" style=\"font-size: 12px;\">";
				
				jQuery.each(msg, function(i, val) {
					
					if(val!=null&&val!="null"&&val!=""){
						
//						if(i=="code"){
//							
//							val = GW.menu.unescape(val);
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
				
				content += "</dl></div>";
				
				console.log("type :", type)
				
				$("#main-"+type+"-content").html(content);
				
//				GW.process.createJSFrameDialog(500, 480, content, "Details");
				
//				var width = 500; var height = 480;
//				
//				const frame = GW.workspace.jsFrame.create({
//			    		title: 'Details',
//			    	    left: 0, 
//			    	    top: 0, 
//			    	    width: width, 
//			    	    height: height,
//			    	    appearanceName: 'yosemite',
//			    	    style: {
//		                    backgroundColor: 'rgb(255,255,255)',
//				    	    fontSize: 12,
//		                    overflow:'auto'
//		                },
//			    	    html: content
//		    	});
//		    	
//				frame.setControl({
//		            styleDisplay:'inline',
//		            maximizeButton: 'zoomButton',
//		            demaximizeButton: 'dezoomButton',
//		            minimizeButton: 'minimizeButton',
//		            deminimizeButton: 'deminimizeButton',
//		            hideButton: 'closeButton',
//		            animation: true,
//		            animationDuration: 150,
//		
//		        });
//		    	
//		    	frame.show();
//		    	
//		    	frame.setPosition((window.innerWidth - width) / 2, (window.innerHeight -height) / 2, 'LEFT_TOP');
				
//				BootstrapDialog.show({
//		            
//					title: 'Details',
//		            
//		            message: content,
//		            
//		            buttons: [{
//		                
//		            	label: 'Ok',
//		                
//		                action: function(dialog) {
//		                	
//		                	dialog.close();
//		                	
//		                }
//		            }, {
//		            	
//		                label: 'Cancel',
//		                
//		                action: function(dialog) {
//		                
//		                	dialog.close();
//		                
//		                }
//		            
//		            }]
//		        
//				});
				
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
			
//			var width = 320; var height = 150;
//			
//			GW.menu.del_frame = GW.workspace.jsFrame.create({
//	    		title: 'Alert',
//	    	    left: 0, 
//	    	    top: 0, 
//	    	    width: width, 
//	    	    height: height,
//	    	    appearanceName: 'yosemite',
//	    	    style: {
//                    backgroundColor: 'rgb(255,255,255)',
//		    	    fontSize: 12,
//                    overflow:'auto'
//                },
//	    	    html: content
//	    	});
//	    	
//			GW.menu.del_frame.setControl({
//                styleDisplay:'inline',
//                maximizeButton: 'zoomButton',
//                demaximizeButton: 'dezoomButton',
//                minimizeButton: 'minimizeButton',
//                deminimizeButton: 'deminimizeButton',
//                hideButton: 'closeButton',
//                animation: true,
//                animationDuration: 150,
//
//            });
//	    	
//			GW.menu.del_frame.on('closeButton', 'click', (_frame, evt) => {
//                _frame.closeFrame();
//                
//            });
//            
//	    	//Show the window
//			GW.menu.del_frame.show();
//	    	
//			GW.menu.del_frame.setPosition((window.innerWidth - width) / 2, (window.innerHeight -height) / 2, 'LEFT_TOP');
			
			$("#del-confirm-btn").click(function(){
				
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
    						GW.workspace.theGraph.removeNodes(id);
    						
    					}
    					
    					console.log("the element is removed " + type + "-" + id);
    					
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
			
//			BootstrapDialog.show({
//	            
//				title: 'Alert',
//	            
//	            message: 'Are you sure to remove this '+type+'?',
//	            
//	            buttons: [{
//	                
//	            	label: 'Yes',
//	                
//	                action: function(dialog) {
//	                	
//	                	$.ajax({
//	        				
//	        				url: "del",
//	        				
//	        				method: "POST",
//	        				
//	        				//remove the database record
//	        				data: "id="+id + "&type=" + type
//	        				
//	        			}).done(function(msg){
//	        				
//	        				if(msg=="done"){
//	        					
//	        					//remove the menu item
//	        					$("#"+type+"-" + id).remove();
//	        					
//	        					if(type=="process"){
//	        						
//	        						//remove the workspace object
//	        						GW.workspace.theGraph.removeNodes(id);
//	        						
//	        					}
//	        					
//	        					console.log("the element is removed " + type + "-" + id);
//	        					
//	        				}else{
//	        					
//	        					console.error("fail to remove " + id);
//	        					
//	        				}
//	        				
//	        			}).fail(function(jxr, status){
//	        				
//	        				console.error("fail to delete " + status);
//	        				
//	        			});
//	                	
//	                	dialog.close();
//	                	
//	                }
//	            }, {
//	            	
//	                label: 'Cancel',
//	                
//	                action: function(dialog) {
//	                
//	                	dialog.close();
//	                
//	                }
//	            
//	            }]
//	        
//			});
			
		},
		
		listen: function(type){
			
			$("#new" + type).click(function(){
				
				if(type=="host"){
					
					GW.host.newDialog();
					
				}else if(type=="process"){
					
					GW.process.newDialog();
					
				}else if(type=="workflow"){

					GW.workflow.newDialog();
					
				}
				
			});
			
//			$("#testhost").click(function(){
//				
//				GW.menu.showSSHCmd("test111");
//				
//			});
			
		}
		
};