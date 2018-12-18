package edu.gmu.csiss.earthcube.cyberconnector.ssh;

import java.io.File;

import org.apache.log4j.Logger;

import edu.gmu.csiss.earthcube.cyberconnector.utils.RandomString;
import edu.gmu.csiss.earthcube.cyberconnector.utils.SysDir;

public class FileTool {
	
	static Logger log = Logger.getLogger(FileTool.class);
	
	public static String scp_upload(String hid, String passwd, String localPath) {
		
		String resp = null;
		
		try {
			
			//get host ip, port, user name and password
			
//			String[] hostdetails = HostTool.getHostDetailsById(hid);
			
			//establish SSH session and generate a token for it
			
			SSHSession session = new SSHSessionImpl();
			
			session.login(hid, passwd, null, false);
			
			File localfile = new File(localPath);
			
			String filename = localfile.getName();
			
			String fileloc = filename; //upload file to temporary folder
			
			log.info("upload " + localPath + " to " + fileloc);
			
//			session.getSsh().newSCPFileTransfer().download(file_path, fileloc);
			
			session.getSsh().newSCPFileTransfer().upload(localPath, fileloc);
			
			//remove the local temporal files
			
			localfile.delete();
			
//			session.getSSHJSession().newSCPFileTransfer().download("test_file", new FileSystemFile("/tmp/"));
			
//			session.runBash(code, id, false); 
			
			String file_url = null;
			
			resp = "{\"filename\": \"" + fileloc + "\", \"ret\": \"success\"}";
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
			throw new RuntimeException(e.getLocalizedMessage());
			
		}  finally {
			
		}
        		
		return resp;
		
	}

	
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
