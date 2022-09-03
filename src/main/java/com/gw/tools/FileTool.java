package com.gw.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.gw.ssh.SSHSession;
import com.gw.ssh.SSHSessionImpl;
import com.gw.utils.BaseTool;
import com.gw.utils.RandomString;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import net.schmizz.sshj.sftp.RemoteResourceInfo;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.xfer.FilePermission;
import net.schmizz.sshj.xfer.scp.SCPFileTransfer;

@Service
@Scope("prototype")
public class FileTool {
	
	Logger log = Logger.getLogger(FileTool.class);
	
	Map<String, SSHSession> token2session = new HashMap();

	Map<String, SFTPClient> token2ftpclient = new HashMap();
	
	@Value("${geoweaver.upload_file_path}")
	String upload_file_path;
	
	@Value("${geoweaver.workspace}")
	String workspace;
	
	@Autowired
	BaseTool bt;
	
	@Autowired
	SSHSession session;
	
	
	
	
	/**
	 * Move a localfile to the public folder for downloading
	 * @param localPath
	 * @param newfilename
	 * @return
	 */
	public String download_local(String localPath, String newfilename) {
		
		String resp = null;
		
		try {
			
			String dest = newfilename;
			
			File destfile = new File(dest);
			
			if(destfile.exists()) {
				
				destfile.delete();
				
			}
			
			copy(localPath, dest);
			
			resp = "{\"ret\": \"success\", \"path\": \"" + StringEscapeUtils.escapeJson(dest) + "\"}";
			
		}catch(Exception e) {
			
			e.printStackTrace();
			
			resp = "{\"ret\": \"failure\", \"reason\": \"" + e.getLocalizedMessage() + "\"}";
			
		}

		return resp;
		
	}
	
	public void copy(String srcpath, String destpath) throws IOException {
		
		log.info("Copying " + srcpath + " to " + destpath);

        InputStream is = null;
        
        OutputStream os = null;
        
		try {
			
			File src = new File(srcpath);
			
			File dest = new File(destpath);
            
			is = new FileInputStream(src);
            
			os = new FileOutputStream(dest);

            // buffer size 1K
            byte[] buf = new byte[1024*128];

            int bytesRead;
            
            while ((bytesRead = is.read(buf)) > 0) {
            
            	os.write(buf, 0, bytesRead);
            
            }
	            
		}catch(Exception e) {
			
			e.printStackTrace();
			
			throw new RuntimeException(e.getLocalizedMessage());
			
		}finally {
            
        	if(!BaseTool.isNull(is))is.close();
            if(!BaseTool.isNull(os))os.close();
        }
		
    }

	
	/**
	 * Upload a file from local to a specific location on a remote host
	 * @param hid
	 * @param passwd
	 * @param localPath
	 * @param remoteLoc
	 * @return
	 */
	public String scp_upload(String hid, String passwd, String localPath, String remoteLoc, boolean removelocal) {
		
		String resp = null;
		
//		SSHSession session = new SSHSessionImpl();
		
		try {
			
			//get host ip, port, user name and password
			
//			String[] hostdetails = HostTool.getHostDetailsById(hid);
			
			//establish SSH session and generate a token for it
			
			session.login(hid, passwd, null, false);
			
			File localfile = new File(localPath);
			
			String filename = localfile.getName();
			
			String fileloc = remoteLoc + "/" + filename; //upload file to temporary folder
			
			log.info("upload " + localPath + " to " + remoteLoc);
			
			session.getSsh().newSCPFileTransfer().upload(localPath, remoteLoc);
			
			//remove the local temporal files
			
			if(removelocal) localfile.delete();
			
			session.getSsh().close();
			
			String file_url = null;
			
			resp = "{\"filename\": \"" + fileloc + "\", \"ret\": \"success\"}";
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
			throw new RuntimeException(e.getLocalizedMessage());
			
		}  finally {
			
			
			
		}
        		
		return resp;
		
	}
	
	/**
	 * Upload a file from local to the home directory of remote host
	 * @param hid
	 * @param passwd
	 * @param localPath
	 * @return
	 */
	public String scp_upload(String hid, String passwd, String localPath) {
		
		String resp = null;
		
		try {
			
			//establish SSH session and generate a token for it
			
			session.login(hid, passwd, null, false);
			
			File localfile = new File(localPath);
			
			String filename = localfile.getName();
			
			String fileloc = filename; //upload file to temporary folder
			
			log.info("upload " + localPath + " to " + fileloc);
			
			SCPFileTransfer transfer = session.getSsh().newSCPFileTransfer();
			
			transfer.upload(localPath, fileloc);
			
			//remove the local temporal files
			
			localfile.delete();
			
			session.getSsh().close();
			
			String file_url = null;
			
			resp = "{\"filename\": \"" + fileloc + "\", \"ret\": \"success\"}";
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
			throw new RuntimeException(e.getLocalizedMessage());
			
		}  finally {
			
			
			
		}
        		
		return resp;
		
	}
	
	public void close_browser(String token) {
		
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
	
	public String getFolderJSON(List<RemoteResourceInfo> list, String file_path) {
		
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
	
	
	
	public String continue_browser(String token, String file_path) {
		
		String resp = null;
		
		try {
			
			//establish SSH session and generate a token for it
			
			SSHSession session = token2session.get(token);
			
			SFTPClient ftpclient = token2ftpclient.get(token);
			
			List<RemoteResourceInfo> list = ftpclient.ls(file_path);
			
			resp = getFolderJSON(list, file_path);
	    	
		} catch (Exception e) {
			
			e.printStackTrace();
			
			resp = "{ \"ret\" : \"failure\", \"msg\": \""+e.getLocalizedMessage()+"\"}";
			
		}  finally {
		   
		}
		
		return resp;
		
	}
	
	public String open_sftp_browser(String hid, String password, String file_path, String sessionid) {
		
		String resp = null;
		
		try {
			
			//establish SSH session and generate a token for it
			
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
	
	public String scp_fileeditor(String filepath, String content, String sessionid) {
		
		String resp = null;
		
		try {
			
			Set<FilePermission> perms = token2ftpclient.get(sessionid).perms(filepath);
			
			int permmask = FilePermission.toMask(perms);
			
			boolean w = FilePermission.USR_W.isIn(permmask) || FilePermission.OTH_W.isIn(permmask) || FilePermission.GRP_W.isIn(permmask);
			
			if(w) {
				
				String filename = new File(filepath).getName();
				
				String local = bt.getFileTransferFolder() + "/" + filename + new RandomString(3).nextString();
				
				bt.writeString2File(content, local);
				
				log.info("Writing local file " + local + " into remote file : " + filepath);
				
				token2ftpclient.get(sessionid).put(local, filepath);
				
				new File(local).delete();
				
				resp = "{\"ret\": \"success\"}";
				
			}else {
				
				resp = "{\"ret\": \"failure\", \"reason\": \"you don't have write permission. Use chmod +x in command line to grant write/execute permission.\"}";
				
			}
			
			
			
		}catch(Exception e) {
			
			e.printStackTrace();
			
			resp = "{\"ret\": \"failure\", \"reason\": \"" + e.getLocalizedMessage() + "\"}";
			
		}

		return resp;
		
	}
	
	public String scp_download(String filepath, String sessionid) {
		
		String resp = null;
		
		try {
			
			String filename = new File(filepath).getName();
			
			
			
			String dest = bt.getFileTransferFolder() + "/" + filename;
			
			File destfile = new File(dest);
			
			if(destfile.exists()) {
				
				destfile.delete();
				
			}
			
			log.info(filepath + " " + dest);
			
			token2ftpclient.get(sessionid).get(filepath, dest);
			
			resp = "{\"ret\": \"success\", \"path\": \"download/" + upload_file_path + "/" + filename + "\"}";
			
		}catch(Exception e) {
			
			e.printStackTrace();
			
			resp = "{\"ret\": \"failure\", \"reason\": \"" + e.getLocalizedMessage() + "\"}";
			
		}

		return resp;
		
	}
	
	public void scp_download(String hid, String password, String file_path, String dest_path) {
		
		String resp = null;
		
		try {
			
			session.login(hid, password, null, false);
			
			log.info("download " + file_path + " to " + dest_path);
			
			session.getSsh().newSCPFileTransfer().download(file_path, dest_path);
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
			throw new RuntimeException(e.getLocalizedMessage());
			
		}  finally {
		   
			
			
		}
		
	}

	/**
	 * SCP download
	 * @param hid
	 * @param password
	 * @param file_path
	 * @return
	 */
	public String scp_download(String hid, String password, String file_path) {
		
		String filename = new RandomString(9).nextString();
		
		String fileloc = bt.getFileTransferFolder() + "/" + filename;
		
		scp_download(hid, password, file_path, fileloc);
		
		String resp = "{\"filename\": \"" + filename + "\", \"ret\": \"success\"}";
				
		return resp;
		
	}
	
}
