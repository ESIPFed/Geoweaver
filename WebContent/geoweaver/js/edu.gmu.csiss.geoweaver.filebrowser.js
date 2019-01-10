/**
 * 
 * author: Ziheng Sun
 * 
 */

edu.gmu.csiss.geoweaver.filebrowser = {
		
		connect_folder: function(req, dialog, button){
			
			
			
		},
		
		start: function(hid){
			
			var req = { hid: hid, init_path: "/home/"}
			
			edu.gmu.csiss.geoweaver.host.start_auth_single(hid, req, edu.gmu.csiss.geoweaver.filebrowser.connect_folder);
			
		}
		
}
