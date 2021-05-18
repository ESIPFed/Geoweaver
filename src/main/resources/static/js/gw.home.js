GW.home = {

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
        
    }

}
			
			 