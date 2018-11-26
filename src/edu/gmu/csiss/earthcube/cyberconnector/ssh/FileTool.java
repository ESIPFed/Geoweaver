package edu.gmu.csiss.earthcube.cyberconnector.ssh;

import org.apache.log4j.Logger;

import edu.gmu.csiss.earthcube.cyberconnector.utils.RandomString;
import edu.gmu.csiss.earthcube.cyberconnector.utils.SysDir;

public class FileTool {
	
	static Logger log = Logger.getLogger(FileTool.class);

	
	public static String scp_download(String hid, String password, String file_path) {
		
		String resp = null;
		
		try {
			
			//get host ip, port, user name and password
			
//			String[] hostdetails = HostTool.getHostDetailsById(hid);
			
			//establish SSH session and generate a token for it
			
			SSHSession session = new SSHSessionImpl();
			
			session.login(hid, password, null, false);
			
			String filename = new RandomString(9).nextString();
			
			String fileloc = SysDir.geoweaver_file_path + filename;
			
			log.info("download " + file_path + " to " + fileloc);
			
			session.getSsh().newSCPFileTransfer().download(file_path, fileloc);
			
//			session.getSSHJSession().newSCPFileTransfer().download("test_file", new FileSystemFile("/tmp/"));
			
//			session.runBash(code, id, false); 
			
			String file_url = null;
			
			resp = "{\"filename\": \"" + filename + "\", \"ret\": \"success\"}";
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
			throw new RuntimeException(e.getLocalizedMessage());
			
		}  finally {
			
		}
        		
		return resp;
		
	}
	
}
