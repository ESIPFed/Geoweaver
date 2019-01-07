package edu.gmu.csiss.earthcube.cyberconnector.ssh;

import java.io.File;

import org.apache.log4j.Logger;

import edu.gmu.csiss.earthcube.cyberconnector.utils.BaseTool;
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
	
	public static void scp_download(String hid, String password, String file_path, String dest_path) {
		
		String resp = null;
		
		try {
			
			//get host ip, port, user name and password
			
//			String[] hostdetails = HostTool.getHostDetailsById(hid);
			
			//establish SSH session and generate a token for it
			
			SSHSession session = new SSHSessionImpl();
			
			session.login(hid, password, null, false);
			
			log.info("download " + file_path + " to " + dest_path);
			
			session.getSsh().newSCPFileTransfer().download(file_path, dest_path);
			
//			session.getSSHJSession().newSCPFileTransfer().download("test_file", new FileSystemFile("/tmp/"));
			
//			session.runBash(code, id, false); 
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
			throw new RuntimeException(e.getLocalizedMessage());
			
		}  finally {
		   
		}
		
	}

	
	public static String scp_download(String hid, String password, String file_path) {
		
		String filename = new RandomString(9).nextString();
		
		String fileloc = BaseTool.getCyberConnectorRootPath() + SysDir.upload_file_path + "/" + filename;
		
		scp_download(hid, password, file_path, fileloc);
		
		String resp = "{\"filename\": \"" + filename + "\", \"ret\": \"success\"}";
				
		return resp;
		
	}
	
}
