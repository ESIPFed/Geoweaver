package edu.gmu.csiss.earthcube.cyberconnector.ssh;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.gmu.csiss.earthcube.cyberconnector.utils.BaseTool;
import edu.gmu.csiss.earthcube.cyberconnector.utils.RandomString;
import edu.gmu.csiss.earthcube.cyberconnector.utils.SysDir;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import net.schmizz.sshj.sftp.SFTPClient;

public class FileTool {
	
	static Logger log = Logger.getLogger(FileTool.class);
	
	static Map<String, SSHSession> token2session = new HashMap();

	static Map<String, SFTPClient> token2ftpclient = new HashMap();
	
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
	
	public static void close_browser(String token) {
		
		try {
		
			SFTPClient client = token2ftpclient.get(token);
			
			client.close();
			
			token2ftpclient.remove(token);
			
			SSHSession session = token2session.get(token);
			
			session.logout();
			
			token2session.remove(token);
			
		} catch (IOException e) {
			
			e.printStackTrace();
			
		}
		
	}
	
	public static String getFolderJSON(List<RemoteResourceInfo> list, String file_path) {
		
		StringBuffer json = new StringBuffer("{ \"current\": \"").append(file_path).append("\", \"array\": [");
		
    	
    	for(int i=0;i<list.size(); i++) {
    		
    		RemoteResourceInfo r = list.get(i);
    		
    		if(i!= 0) {
    			
    			json.append(", ");
    			
    		}
    		
    		json.append("{\"name\": \"").append(r.getName().trim()).append("\", ");
    		json.append("\"atime\": \"").append(r.getAttributes().getAtime()).append("\", ");
    		json.append("\"mtime\": \"").append(r.getAttributes().getMtime()).append("\", ");
    		json.append("\"size\": \"").append(r.getAttributes().getSize()).append("\", ");
    		json.append("\"mode\": \"").append(r.getAttributes().getMode()).append("\", ");
    		json.append("\"isdirectory\": ").append(r.isDirectory()).append(" }");
    		
    	}
    	
    	json.append("]}");
		
		return json.toString();
		
	}
	
	
	
	public static String continue_browser(String token, String file_path) {
		
		String resp = null;
		
		try {
			
			//get host ip, port, user name and password
			
//			String[] hostdetails = HostTool.getHostDetailsById(hid);
			
			//establish SSH session and generate a token for it
			
			SSHSession session = token2session.get(token);
			
			SFTPClient ftpclient = token2ftpclient.get(token);
			
//			ftpclient.ls(file_path);
			
			List<RemoteResourceInfo> list = ftpclient.ls(file_path);
			
			resp = getFolderJSON(list, file_path);
	    	
//	    	ftpclient.close();
//	    	
//	    	log.info("check if the ssh is still connected : " + session.getSsh().isConnected());
//	    	
//	    	session.logout();
//	    	
//	    	log.info("check again : " + session.getSsh().isConnected());
			
//			session.getSSHJSession().newSCPFileTransfer().download("test_file", new FileSystemFile("/tmp/"));
			
//			session.runBash(code, id, false); 
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
//			throw new RuntimeException(e.getLocalizedMessage());
			
			resp = "{ \"ret\" : \"failure\", \"msg\": \""+e.getLocalizedMessage()+"\"}";
			
		}  finally {
		   
		}
		
		return resp;
		
	}
	
	public static String open_sftp_browser(String hid, String password, String file_path, String sessionid) {
		
		String resp = null;
		
		try {
			
			//establish SSH session and generate a token for it
			
			SSHSession session = new SSHSessionImpl();
			
			session.login(hid, password, null, false);
			
			SFTPClient ftpclient = session.getSsh().newSFTPClient();
			
			token2session.put(sessionid, session);
			
			token2ftpclient.put(sessionid, ftpclient);
			
	    	List<RemoteResourceInfo> list = ftpclient.ls(file_path);
	    	
	    	resp = getFolderJSON(list, file_path);
	    	
		} catch (Exception e) {
			
			e.printStackTrace();
			
			throw new RuntimeException(e.getLocalizedMessage());
			
		}  finally {
		   
		}
		
		return resp;
		
	}
	
	public static String scp_fileeditor(String filepath, String content, String sessionid) {
		
		String resp = null;
		
		try {
			
			String filename = new File(filepath).getName();
			
			String local = BaseTool.getCyberConnectorRootPath() + SysDir.upload_file_path + "/" + filename;
			
			BaseTool.writeString2File(content, local);
			
//			token2ftpclient.get(sessionid).get(filepath, dest);
			
			log.info("Writing local file " + local + " into remote file : " + filepath);
			
			token2ftpclient.get(sessionid).put(local, filepath);
			
			resp = "{\"ret\": \"success\"}";
			
		}catch(Exception e) {
			
			e.printStackTrace();
			
			resp = "{\"ret\": \"failure\", \"reason\": \"" + e.getLocalizedMessage() + "\"}";
			
		}

		return resp;
		
	}
	
	public static String scp_download(String filepath, String sessionid) {
		
		String resp = null;
		
		try {
			
			String filename = new File(filepath).getName();
			
			String dest = BaseTool.getCyberConnectorRootPath() + SysDir.upload_file_path + "/" + filename;
			
			File destfile = new File(dest);
			
			if(destfile.exists()) {
				
				destfile.delete();
				
			}
			
			log.info(filepath + " " + dest);
			
			token2ftpclient.get(sessionid).get(filepath, dest);
			
			resp = "{\"ret\": \"success\", \"path\": \"" + SysDir.upload_file_path + "/" + filename + "\"}";
			
		}catch(Exception e) {
			
			e.printStackTrace();
			
			resp = "{\"ret\": \"failure\", \"reason\": \"" + e.getLocalizedMessage() + "\"}";
			
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
