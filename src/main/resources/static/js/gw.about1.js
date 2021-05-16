/**
 * 
 * About Geoweaver
 * 
 * @author Ziheng Sun
 * 
 */

edu.gmu.csiss.geoweaver.about1 = {
		
		dependency: "d3.js, bootstrap, jquery, codemirror, directed-graph-creator, dmuploader",
		
		content: "thanks to Colorado Reed (https://github.com/cjrd) for making the fantastic D3.js graph creator.",
		
		showDialog: function(){
			
			var content = "<div style=\"padding:10px\">"+
			"<h3 class=\"text-left\">GeoWeaver workflow</h3>"+
					"<p class=\"text-left\">Geoweaver is a web system to allow users to easily compose and execute full-stack deep learning workflows in web browsers by ad hoc integrating the distributed spatial data facilities, high-performance computation platforms, and open-source deep learning libraries.</p>"+
					
					
					
					"<div  >"+
  "<img style=\"width:80%;height:80%\"  src=\"../img/process_creation.png\">"+
  "<div class=\"text-block\">"+
    "<h3>Creating a Process</h3>"+
    "<p></p>"+
  "</div>"+
"</div>"+
"<br>"+"<br>"+"<br>"+"<br>"+


		"<div >"+
  "<img style=\"width:80%;height:80%\" src=\"../img/process_view.png\">"+
  "<div class=\"text-block\">"+
    "<h3>Preview your process scripts</h3>"+
    "<p></p>"+
  "</div>"+
"</div>"+
					
					
					"<br>"+"<br>"+"<br>"+"<br>"+
					
					
							"<div >"+
  "<img style=\"width:80%;height:80%\" src=\"../img/set_pass_process.png\">"+
  "<div class=\"text-block\">"+
    "<h3>Set password for your process</h3>"+
    "<p></p>"+
  "</div>"+
"</div>"+
					
					
					"<br>"+"<br>"+"<br>"+"<br>"+
					
					
					
							"<div >"+
  "<img style=style=\"width:80%;height:80%\" src=\"../img/process_termianal.png\">"+
  "<div class=\"text-block\">"+
    "<h3>Run your process in terminal</h3>"+
    "<p></p>"+
  "</div>"+
"</div>"+
					"<br>"+"<br>"+"<br>"+"<br>"+
					
								"<div >"+
  "<img style=\"width:80%;height:80%\" src=\"../img/work_1.png\">"+
  "<div class=\"text-block\">"+
    "<h3>Creating a workflow in weaver</h3>"+
    "<p></p>"+
  "</div>"+
"</div>"+
			
		"<br>"+"<br>"+"<br>"+
			
			
						"<div >"+
  "<img style=\"width:80%;height:80%\" src=\"../img/work_final.png\">"+
  "<div class=\"text-block\">"+
    "<h3>View your workflow details , id edge nodes</h3>"+
    "<p></p>"+
  "</div>"+
"</div>"+
			"<br>"+"<br>"+"<br>"+"<br>"+
			
			
			
					
					
					
	            	"<p class=\"text-left\">Geoweaver is a community effort and welcome all contributors. If you have any questions, please create a new issue in GitHub or directly <a href=\"mailto:zsun@gmu.edu\">contact us</a></p></div>";
			
			GW.process.createJSFrameDialog(720, 640, content, "WorkFLow");
			
//			BootstrapDialog.show({
//				
//				title: "About Geoweaver",
//				
//				message: function(dialog){
//	            	
//	            	$content = $("<p class=\"text-left\">Geoweaver (version "+edu.gmu.csiss.geoweaver.version+") is initially proposed, developed and maitained by <a href=\"http://csiss.gmu.edu\">Center for Spatial Information Science and Sysmtems (CSISS)</a> in <a href=\"http://gmu.edu\">George Mason University</a>. This project is funded by ESIPLab incubator project. The source code is open on <a href=\"http://github.com/ESIPFed/Geoweaver\">Github</a>.  </p>"+
//	            	
//	            	"<p class=\"text-left\">Geoweaver is a web system to allow users to easily compose and execute full-stack deep learning workflows in web browsers by taking advantage of the online spatial data facilities, high-performance computation platforms, and open-source deep learning libraries.</p>"+
//	            	
//	            	"<p class=\"text-left\">Geoweaver logo is designed by Dr. Annie Burgess <a href=\"mailto:annieburgess@esipfed.org\">annieburgess@esipfed.org</a>.</p>"+
//	            		            	
//	            	"<h3 class=\"text-left\">Principal Investigators:</h3>"+
//	            	
//	            	"<ul><li>Ziheng Sun <a href=\"mailto:zsun@gmu.edu\">zsun@gmu.edu</a></li>"+
//	            	
//	            	"<li>Liping Di <a href=\"mailto:ldi@gmu.edu\">ldi@gmu.edu</a></li></ul>");
//	            	
//	            	return $content;
//	            	
//	            },
//				
//				buttons: [{
//				
//					label: 'Close',
//	                
//	                action: function(dialog) {
//	                	
//	                	dialog.close();
//	                	
//	                }
//					
//				}]
//				
//					
//			});
			
		}
		
}