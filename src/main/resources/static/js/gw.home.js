GW.home = {

    showDialog: function(){
			
        var content = "<div style=\"padding:10px\">"+
                "<h3 class=\"text-left\">GeoWeaver workflow</h3>"+
                "<p class=\"text-left\">Geoweaver is a web system to allow users to easily compose and execute full-stack deep learning workflows in web browsers by ad hoc integrating the distributed spatial data facilities, high-performance computation platforms, and open-source deep learning libraries.</p>"+
                "<div>"+
                "<img style=\"width:80%;height:80%\"  src=\"../img/process_creation.png\">"+
                "<div class=\"text-block\">"+
                "<p>Figure 1. Creating a Process</p>"+
                "<p></p>"+
                "</div>"+
                "</div>"+
                "<div >"+
                "<img style=\"width:80%;height:80%\" src=\"../img/process_view.png\">"+
                "<div class=\"text-block\">"+
                "<p>Figure 2. Preview your process scripts</p>"+
                "</div>"+
                "</div>"+  
                "<div >"+
                "<img style=\"width:80%;height:80%\" src=\"../img/set_pass_process.png\">"+
                "<div class=\"text-block\">"+
                "<p>Figure 3. Set password for your process</p>"+
                "</div>"+
                "</div>"+
                                
                "<div >"+
                "<img style=style=\"width:80%;height:80%\" src=\"../img/process_termianal.png\">"+
                "<div class=\"text-block\">"+
                "<p>Figure 4. Run your process in terminal</p>"+
                "<p></p>"+
                "</div>"+
                "</div>"+
                "<div >"+
                "<img style=\"width:80%;height:80%\" src=\"../img/work_1.png\">"+
                "<div class=\"text-block\">"+
                "<p>Figure 5. Creating a workflow in weaver</p>"+
                "</div>"+
                "</div>"+
                "<div >"+
                "<img style=\"width:80%;height:80%\" src=\"../img/work_final.png\">"+
                "<div class=\"text-block\">"+
                "<p>Figure 6. View your workflow details , id edge nodes</p>"+
                "</div>"+
                "</div>"+
                "</div>";
        GW.process.createJSFrameDialog(720, 640, content, "Workflow");
        
    }

}
			
			 