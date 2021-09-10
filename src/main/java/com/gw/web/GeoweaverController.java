package com.gw.web;


import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.gw.jpa.GWUser;
import com.gw.search.GWSearchTool;
import com.gw.ssh.RSAEncryptTool;
import com.gw.ssh.SSHSession;
import com.gw.tools.DashboardTool;
import com.gw.tools.FileTool;
import com.gw.tools.HistoryTool;
import com.gw.tools.HostTool;
import com.gw.tools.ProcessTool;
import com.gw.tools.SessionManager;
import com.gw.tools.WorkflowTool;
import com.gw.utils.BaseTool;
import com.gw.utils.RandomString;
import com.gw.tools.UserTool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.HttpServletRequest;
/**
 * 
 * Controller for SSH related activities, including all the handlers for Geoweaver.
 * 
 * @author Ziheng Sun
 * 
 * @date 5 Oct 2018
 * 
 */

@Controller 
@RequestMapping(value="/web")     
//@SessionAttributes({"SSHToken"})
//@RequestMapping("/Geoweaver/web")
public class GeoweaverController {

	Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	ProcessTool pt;
	
	@Autowired
	WorkflowTool wt;
	
	@Autowired
	HostTool ht;
	
	@Autowired
	BaseTool bt;
	
	@Autowired
	GWSearchTool st;
	
	@Autowired
	FileTool ft;
	
	@Autowired
	HistoryTool hist;

	@Autowired
	DashboardTool dbt;
	
	@Autowired
	SSHSession sshSession;
	
	@Value("${geoweaver.upload_file_path}")
	String upload_file_path;

	@Autowired
	UserTool ut;
	
	public static SessionManager sessionManager;
	
	static {
		
		sessionManager = new SessionManager();
		
	}
	
	@PreDestroy
    public void destroy() {
		
		logger.debug("Callback triggered - @PreDestroy.");
        
        sessionManager.closeAll();
        
    }

	@RequestMapping(value="/delAllHistory", method= RequestMethod.POST)
	public @ResponseBody String delAllHistory(ModelMap model, WebRequest request){

		String resp = null;

		try{

			String hostid = request.getParameter("id");

			resp = hist.deleteAllHistoryByHost(hostid);

		}catch(Exception e){

			throw new RuntimeException("failed " + e.getLocalizedMessage());

		}

		return resp;

	}

	@RequestMapping(value="/delNoNotesHistory", method = RequestMethod.POST)
	public @ResponseBody String delNoNotesHistory(ModelMap model, WebRequest request){

		String resp = null;

		try{

			String hostid = request.getParameter("id");

			resp = hist.deleteNoNotesHistoryByHost(hostid);

		}catch(Exception e){

			throw new RuntimeException("failed " + e.getLocalizedMessage());

		}

		return resp;

	}
	
	@RequestMapping(value = "/del", method = RequestMethod.POST)
    public @ResponseBody String del(ModelMap model, WebRequest request){
		
		String resp = null;
		
		try {
			
			String id = request.getParameter("id");
			
			String type = request.getParameter("type");
			
			if(type.equals("host")) {

				resp = ht.del(id);
				
			}else if(type.equals("process")) {
				
				resp = pt.del(id);
				
			}else if(type.equals("workflow")) {
				
				resp = wt.del(id);
				
			}else if(type.equals("history")) {
				
				resp = hist.deleteById(id);
				
			}
			
		}catch(Exception e) {
			
			throw new RuntimeException("failed " + e.getLocalizedMessage());
			
		}
		
		return resp;
		
	}
	
	@RequestMapping(value = "/search", method = RequestMethod.POST)
    public @ResponseBody String search(ModelMap model, WebRequest request){
		
		String resp = null;
		
		try {
			
			String type = request.getParameter("type");
					
			String keywords = request.getParameter("keywords");
			
			resp = st.search(keywords, type);
			
		}catch(Exception e) {
			
			throw new RuntimeException("failed " + e.getLocalizedMessage());
			
		}
		
		return resp;
		
	}
	
	@RequestMapping(value = "/dashboard", method = RequestMethod.POST)
    public @ResponseBody String dashboard(ModelMap model, WebRequest request){
		
		String resp = null;
		
		try {
			
			resp = dbt.getJSON();
			
		}catch(Exception e) {
			e.printStackTrace();
			throw new RuntimeException("failed " + e.getLocalizedMessage());
			
		}
		
		return resp;
		
	}

	@RequestMapping(value = "/detail", method = RequestMethod.POST)
    public @ResponseBody String detail(ModelMap model, WebRequest request){
		
		String resp = null;
		
		try {
			
			String type = request.getParameter("type");
					
			String id = request.getParameter("id");
			
			if(type.equals("host")) {

				resp = ht.detail(id);
				
			}else if(type.equals("process")) {
				
				resp = pt.detail(id);
				
			}else if(type.equals("workflow")) {
				
				resp = wt.detail(id);
				
			}
			
		}catch(Exception e) {
			
			throw new RuntimeException("failed " + e.getLocalizedMessage());
			
		}
		
		return resp;
		
	}
	
	@RequestMapping(value = "/key", method = RequestMethod.POST)
    public @ResponseBody String getpublickey(ModelMap model, WebRequest request, HttpSession session){
		
		String resp = null;
		
		try {
			
			resp = RSAEncryptTool.getPublicKey(session.getId());
			
		}catch(Exception e) {
			
			e.printStackTrace();
			
			throw new RuntimeException("failed " + e.getLocalizedMessage());
			
		}
		
		return resp;
		
	}

	/**
     * This is the password reset callback url
     * @param token
     * @param model
     * @return
     */
    @RequestMapping(value = "/reset_password", method = RequestMethod.GET)
    public String showResetPasswordForm(@Param(value = "token") String token, Model model) {

        if(!bt.isNull(token)){

            System.err.print(token);
            // User user = userService.getByResetPasswordToken(token);
            String userid = ut.token2userid.get(token);
            Date created_date = ut.token2date.get(token);
    
            if(!bt.isNull(userid)){
    
                long time_difference =  new Date().getTime() - created_date.getTime();
    
                //if the token is one hour old
                if(time_difference<60*60*1000){
    
                    GWUser user = ut.getUserById(userid);
    
                    model.addAttribute("token", token);
                    
                    if (user == null) {
                        // model.addAttribute("message", "Invalid Token");
                        return "Invalid Token";
                    }
    
                }
    
            }

        }else{

            model.addAttribute("error", "No Token. Invalid Link. ");
            
        }


         
        return "reset_password_form";
    }
	
	@RequestMapping(value = "/recent", method = RequestMethod.POST)
    public @ResponseBody String recent_history(ModelMap model, WebRequest request){
		
		String resp = null;
		
		try {
			
			String type = request.getParameter("type");
			
			int number = Integer.parseInt(request.getParameter("number"));
			
			if(type.equals("process")) {
				
				resp = pt.recent(number);
				
			}else if(type.equals("workflow")) {
				
				resp = wt.recent(number);
				
			}else if(type.equals("host")) {
				
				String hid = request.getParameter("hostid");
				
				resp = ht.recent(hid, number);
				
			}
			
		}catch(Exception e) {
			
			e.printStackTrace();
			
			throw new RuntimeException("failed " + e.getLocalizedMessage());
			
		}
		
		return resp;
		
	}
	
	/**
	 * Get history of process or workflow
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/log", method = RequestMethod.POST)
    public @ResponseBody String one_history(ModelMap model, WebRequest request){
		
		String resp = null;
		
		try {
			
			String type = request.getParameter("type");
			
			String hid = request.getParameter("id");
			
			if(type.equals("process")) {
				
				resp = pt.one_history(hid);
				
			}else if(type.equals("workflow")) {
				
				resp = wt.one_history(hid);
				
			}else if(type.equals("host")) {
				
				resp = ht.one_history(hid);
				
			}
			
		}catch(Exception e) {
			
			e.printStackTrace();
			
			throw new RuntimeException("failed " + e.getLocalizedMessage());
			
		}
		
		return resp;
		
	}
	
	@RequestMapping(value = "/stop", method = RequestMethod.POST)
    public @ResponseBody String stop(ModelMap model, WebRequest request){
		
		String resp = null;
		
		try {
			
			String type = request.getParameter("type");
			
			String id = request.getParameter("id");
			
			if(type.equals("process")) {
				
				resp = pt.stop( id);
				
			}else if(type.equals("workflow")) {
				
				resp = wt.stop(id);
				
			}
			
		}catch(Exception e) {
			
			e.printStackTrace();
			
			throw new RuntimeException("failed " + e.getLocalizedMessage());
			
		}
		
		return resp;
		
	}
	
	@RequestMapping(value = "/logs", method = RequestMethod.POST)
    public @ResponseBody String all_history(ModelMap model, WebRequest request){
		
		String resp = null;
		
		try {
			
			
			String type = request.getParameter("type");
			

			logger.debug("enter logs " + type);
			
			String id = request.getParameter("id");
			
			String isactive = request.getParameter("isactive");
			
			if(type.equals("process")) {
				
				if(bt.isNull(id)) {
					
					if("true".equals(isactive)) {
						
						resp = pt.all_active_process();
//						resp = pt.all_history(id);
						
					}else {
						
						//return all process running history
						
						//zero processes
						
					}
					
				}else {
					
					resp = pt.all_history(id);
					
				}
				
			}else if(type.equals("workflow")) {
				
				if(bt.isNull(id)) {
					
					if("true".equals(isactive)) {
				
						resp = wt.all_active_process();
						
					}
					
				}else {
				
					resp = wt.all_history(id);
					
				}
				
			}
			
		}catch(Exception e) {
			
			e.printStackTrace();
			
			throw new RuntimeException("failed " + e.getLocalizedMessage());
			
		}
		
		return resp;
		
	}
	
	@RequestMapping(value = "/env", method = RequestMethod.POST)
    public @ResponseBody String env(ModelMap model, WebRequest request){
		
		String resp = null;
		
		try {
			
			String hid = request.getParameter("hid");
			
			resp = ht.getEnvironments(hid);
			
		}catch(Exception e) {
			
			e.printStackTrace();
			
			throw new RuntimeException("failed " + e.getLocalizedMessage());
			
		}
		
		return resp;
		
	}
	
	@RequestMapping(value = "/list", method = RequestMethod.POST)
    public @ResponseBody String list(ModelMap model, WebRequest request, HttpSession session, HttpServletRequest httprequest){
		
		String resp = null;
		
		try {
			
			String type = request.getParameter("type");

			String ownerid = ut.getAuthUserId(session.getId(), ut.getClientIp(httprequest));
			
			if(type.equals("host")) {

				resp = ht.list(ownerid);
				
			}else if(type.equals("process")) {
				
				resp = pt.list(ownerid);
				
			}else if(type.equals("workflow")) {
				
				resp = wt.list(ownerid);
				
			}
			
		}catch(Exception e) {
			
			e.printStackTrace();
			
//			throw new RuntimeException("failed " + e.getLocalizedMessage());
			
			resp = "{\"ret\": \"failure\", \"reason\": \"Database Query Error.\"}";
			
		}
		
		return resp;
		
	}
	
	@RequestMapping(value = "/checkLiveSession", method = RequestMethod.POST)
    public @ResponseBody String checklivesession(ModelMap model, WebRequest request){
		
		String resp = null;
		
		try {
			
			String hid = request.getParameter("hostId");
			
			
			
			resp = "{\"exist\": false}";
			
		}catch(Exception e) {
			
			e.printStackTrace();
			
			throw new RuntimeException("failed " + e.getLocalizedMessage());
			
		}
		
		return resp;
		
	}
	
	@RequestMapping(value = "/closefilebrowser", method = RequestMethod.POST)
	public @ResponseBody String closefileBrowser(ModelMap model, WebRequest request, HttpSession session) {
		
		String resp = null;
		
		try {
			
			ft.close_browser(session.getId());
			
			resp = "{ \"ret\": \"success\"}";
			
		}catch(Exception e) {
			
			e.printStackTrace();
			
			throw new RuntimeException(e.getLocalizedMessage());
			
		}
		
		return resp;
		
	}
	
	@RequestMapping(value = "/openfilebrowser", method = RequestMethod.POST)
	public @ResponseBody String fileBrowser(ModelMap model, WebRequest request, HttpSession session) {
		
		String resp = null;
		
		try {
			
			String hid = request.getParameter("hid");
			
			String encrypted = request.getParameter("pswd");
			
			String init_path = request.getParameter("init_path");
			
			if(!bt.isNull(encrypted)) {
				
				String password = RSAEncryptTool.getPassword(encrypted, session.getId());
				
				resp = ft.open_sftp_browser(hid, password, init_path, session.getId());
				
			}else {
				
				resp = ft.continue_browser(session.getId(), init_path);
				
			}
			
		}catch(Exception e) {
			
			e.printStackTrace();
			
			throw new RuntimeException(e.getLocalizedMessage());
			
		}
		
		return resp;
		
	}
	
	@RequestMapping(value = "/retrievefile", method = RequestMethod.POST)
	public @ResponseBody String scpdownload(ModelMap model, WebRequest request, HttpSession session) {
		
		String resp = null;
		
		try {
			
			String filepath = request.getParameter("filepath");
			
			resp = ft.scp_download(filepath,session.getId());
			
		}catch(Exception e) {
			
			e.printStackTrace();
			
			throw new RuntimeException(e.getLocalizedMessage());
			
		}
		
		return resp;
		
	}


	
	@RequestMapping(value = "/download/{tempfolder}/{filename}", method = RequestMethod.GET)
	public ResponseEntity<Resource> fileGetter(ModelMap model, @PathVariable(value="tempfolder") String tempfolder, 
			@PathVariable(value="filename") String filename, WebRequest request, HttpSession session) {
		
		ResponseEntity<Resource> resp = null;
		
		try {
			
//			http://localhost:8070/download/temp/115336065-smiling-boy-on-a-toy-swing-on-a-playground-in-a-park-black-and-white-vector-illustration-in-a-cartoo.jpg
			
			if(tempfolder.equals(upload_file_path)) {
				
				HttpHeaders headers = new HttpHeaders(); 
				
				headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
		        
				headers.add("Pragma", "no-cache");
		        
		        headers.add("Expires", "0");
				
//				String filename = "";
				
				headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);
				
				File file = new File(bt.getFileTransferFolder() + filename);
				
				InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

			    resp = ResponseEntity.ok()
			            .headers(headers)
			            .contentLength(file.length())
			            .contentType(MediaType.APPLICATION_OCTET_STREAM)
			            .body(resource);
				
			}
			
			
		}catch(Exception e) {
			
			e.printStackTrace();
			
			throw new RuntimeException(e.getLocalizedMessage());
			
		}
		
		return resp;
		
	}
	
	
	
	@RequestMapping(value = "/updatefile", method = RequestMethod.POST)
	public @ResponseBody String fileEditor(ModelMap model, WebRequest request, HttpSession session) {
		
		String resp = null;
		
		try {
			
			String filepath = request.getParameter("filepath");
			
			String content = request.getParameter("content");
			
			resp = ft.scp_fileeditor(filepath, content, session.getId());
			
		}catch(Exception e) {
			
			e.printStackTrace();
			
			throw new RuntimeException(e.getLocalizedMessage());
			
		}
		
		return resp;
		
	}

	@RequestMapping(value = "/executeWorkflow", method = RequestMethod.POST)
    public @ResponseBody String executeWorkflow(ModelMap model, WebRequest request, HttpSession session){
		
		String resp = null;
		
		try {
			
			String id = request.getParameter("id");
			
			String mode = request.getParameter("mode");

			String token = request.getParameter("token");

			if(bt.isNull(token)){

				token = session.getId();

			}

			String history_id = bt.isNull(request.getParameter("history_id"))? 
				new RandomString(18).nextString(): request.getParameter("history_id");
			
			String[] hosts = request.getParameterValues("hosts[]");
			
			String[] encrypted_password = request.getParameterValues("passwords[]");
			
			String[] passwords = RSAEncryptTool.getPasswords(encrypted_password, session.getId());
			
			// resp = wt.execute(id, mode, hosts, passwords, session.getId());
			resp = wt.execute(history_id, id, mode, hosts, passwords, token);
			
		}catch(Exception e) {
			
			e.printStackTrace();
			
			throw new RuntimeException("failed " + e.getLocalizedMessage());
			
		}
		
		return resp;
		
	}
	
	/**
	 * Add local file as a new process
	 * @param model
	 * @param request
	 * @param session
	 * @return
	 */
	@RequestMapping(value = "/addLocalFile", method = RequestMethod.POST)
    public @ResponseBody String addLocalFile(ModelMap model, WebRequest request, HttpSession session){
		
		String resp = null;
		
		try {
			
			String filepath = request.getParameter("filepath");
			
			String hid = request.getParameter("hid"); 
			
			String type = request.getParameter("type");
			
			String content = request.getParameter("content");
			
			String name = request.getParameter("name");

			String ownerid = request.getParameter("ownerid");

			String confidential = request.getParameter("confidential");
			
			String pid = pt.add_database(name, type, content, filepath, hid, ownerid, confidential);
			
			resp = "{\"id\" : \"" + pid + "\", \"name\":\"" + name + "\", \"desc\" : \""+ type +"\" }";
			
		}catch(Exception e) {
			
			e.printStackTrace();
			
			throw new RuntimeException("failed " + e.getLocalizedMessage());
			
		}
		
		return resp;
		
	}
	
	@RequestMapping(value = "/stopProcess", method = RequestMethod.POST)
    public @ResponseBody String stopProcess(ModelMap model, WebRequest request, HttpSession session){
		
		String resp = null;
		
		try {
			
			String pid = request.getParameter("processId");
			
			String hisid = request.getParameter("historyId");
			
//			String encrypted_password = request.getParameter("pswd");
//			
//			String bin = request.getParameter("env[bin]");
//			
//			String pyenv = request.getParameter("env[pyenv]");
//			
//			String basedir = request.getParameter("env[basedir]");
			
//			String password = RSAEncryptTool.getPassword(encrypted_password, session.getId());
			
			resp = pt.stop(hisid);
			
		}catch(Exception e) {
			
			e.printStackTrace();
			
			throw new RuntimeException("failed " + e.getLocalizedMessage());
			
		}
		
		return resp;
		
	}
	
	@RequestMapping(value = "/executeProcess", method = RequestMethod.POST)
    public @ResponseBody String executeProcess(ModelMap model, WebRequest request, HttpSession session){
		
		String resp = null;
		
		try {
			
			String pid = request.getParameter("processId");
			
			String hid = request.getParameter("hostId");
			
			String encrypted_password = request.getParameter("pswd");
			
			String bin = request.getParameter("env[bin]");
			
			String pyenv = request.getParameter("env[pyenv]");
			
			String basedir = request.getParameter("env[basedir]");
			
			String password = RSAEncryptTool.getPassword(encrypted_password, session.getId());

			String history_id = bt.isNull(request.getParameter("history_id"))?new RandomString(12).nextString(): request.getParameter("history_id");

			resp = pt.execute(history_id, pid, hid, password, session.getId(), false, bin, pyenv, basedir);
			
		}catch(Exception e) {
			
			e.printStackTrace();
			
			throw new RuntimeException("failed " + e.getLocalizedMessage());
			
		}
		
		return resp;
		
	}
	
	@RequestMapping(value = "/edit", method = RequestMethod.POST)
    public @ResponseBody String edit(ModelMap model, WebRequest request){
		
		String resp = null;
		
		try {
			
			String type = request.getParameter("type");
			
			if(type.equals("host")) {
				
				String hostid = request.getParameter("hostid");
				
				checkID(hostid);
				
				String hostname = request.getParameter("hostname");
				
				String hostip = request.getParameter("hostip");
				
				String hostport = request.getParameter("hostport");
				
				String username = request.getParameter("username");
				
				String hosttype = request.getParameter("hosttype");
				
				String url = request.getParameter("url");
				
//				String owner = request.getParameter("owner");
				
				ht.update(hostid, hostname, hostip, hostport, username, hosttype, null, url);
				
				resp = "{ \"hostid\" : \"" + hostid + "\", \"hostname\" : \""+ hostname + "\" }";
				
			}else if(type.equals("process")) {
				
				String lang = request.getParameter("lang");
				
				String name = request.getParameter("name");
				
				String desc = request.getParameter("desc");
				
				String id = request.getParameter("id");
				
				checkID(id);
				
				String code = null;
				
				if(lang.equals("shell")) {
					
					code = request.getParameter("code");
					
				}else if(lang.equals("builtin")) {
					
					String operation = request.getParameter("code[operation]");
					
					code = "{ \"operation\" : \"" + operation + "\", \"params\":[";
					
					List params = new ArrayList();
					
					int i=0;
					
					while(request.getParameter("code[params]["+i+"][name]")!=null) {
						
						if(i!=0) {
							
							code += ", ";
							
						}
						
						code += "{ \"name\": \"" + request.getParameter("code[params]["+i+"][name]") + "\", \"value\": \"" + request.getParameter("code[params]["+i+"][value]") + "\" }";
						
						i++;
						
					}
					
					code += "] }";
					
				}else if(lang.equals("jupyter")) {
					
					code = request.getParameter("code");
					
				}else {
					
					code = request.getParameter("code");
					
				}
				
				pt.update(id, name, lang, code, desc);
				
				resp = "{\"id\" : \"" + id + "\"}";
				
			}else if(type.equals("workflow")) {
				
				String wid = request.getParameter("id");
				
				checkID(wid);
				
				String nodes = request.getParameter("nodes");
				
				String edges = request.getParameter("edges");
				
				wt.update(wid, nodes, edges);
				
				resp = "{\"id\" : \"" + wid + "\"}";
				
			}else if(type.equals("history")){

				String hisid = request.getParameter("id");

				String notes = request.getParameter("notes");

				hist.updateNotes(hisid, notes);

				resp = "{\"id\" : \"" + hisid + "\"}";

			}
			
		}catch(Exception e) {
			
			e.printStackTrace();
			
			throw new RuntimeException("failed " + e.getLocalizedMessage());
			
		}
		
		return resp;
		
	}
	
	@RequestMapping(value = "/file/{file_name}", method = RequestMethod.GET)
	public void getFile(@PathVariable("file_name") String fileName, HttpServletResponse response) {
		
	    try {
	    
	    	// get your file as InputStream
	    	String fileloc = bt.getFileTransferFolder() + "/" + fileName;
	      
	    	File my_file = new File(fileloc);
	      
	    	InputStream is = new FileInputStream(my_file);
	      
	    	// copy it to response's OutputStream
	      
	    	org.apache.commons.io.IOUtils.copy(is, response.getOutputStream());
	      
	    	response.flushBuffer();
	    	
	    } catch (Exception ex) {
	      
		    	logger.error("Error writing file to output stream. Filename was " + fileName + " - " + ex);
		    	
		    	throw new RuntimeException("IOError writing file to output stream");
	    	
	    }

	}
	
	/**
	 * upload file to remote host
	 * @param model
	 * @param request
	 * @param session
	 * @return
	 */
	@RequestMapping(value = "/upload", method = RequestMethod.POST)
    public @ResponseBody String upload(ModelMap model, WebRequest request, HttpSession session){
		
		String resp = null;
		
		try {
			
			String rel_filepath = request.getParameter("filepath");
			
			String rel_url = "download/"+upload_file_path+"/";
			
			String filename = rel_filepath.substring(rel_url.length());
			
			String filepath = bt.getFileTransferFolder() + "/" + filename;
			
			String hid = request.getParameter("hid");
			
			String encrypted = request.getParameter("encrypted");
			
			String password = RSAEncryptTool.getPassword(encrypted, session.getId());
			
			resp = ft.scp_upload(hid, password, filepath);
			
		}catch(Exception e) {
			
			e.printStackTrace();
			
			throw new RuntimeException("failed " + e.getLocalizedMessage());
			
		}
		
		return resp;
		
	}
	

	
	@RequestMapping(value = "/retrieve", method = RequestMethod.POST)
    public @ResponseBody String retrieve(ModelMap model, WebRequest request, HttpSession session){
		
		//retrieve file from remote to geoweaver
		
		String resp = null;
		
		try {
			
			String hid = request.getParameter("hostid");
			
			String encrypted = request.getParameter("pswd");
			
			String password = RSAEncryptTool.getPassword(encrypted, session.getId());
			
			String filepath = request.getParameter("filepath");
			
			resp = ft.scp_download(hid, password, filepath);
			
		}catch(Exception e) {
			
			e.printStackTrace();
			
			throw new RuntimeException("failed " + e.getLocalizedMessage());
			
		}
		
		return resp;
		
	}
	
	@RequestMapping(value = "/add", method = RequestMethod.POST)
    public @ResponseBody String add(ModelMap model, WebRequest request){
		
		String resp = null;
		
		try {
			
			String type = request.getParameter("type");
			
			if(type.equals("host")) {
				
				String hostname = request.getParameter("hostname");
				
				String hostip = request.getParameter("hostip");
				
				String hostport = request.getParameter("hostport");
				
				String username = request.getParameter("username");
				
				String hosttype = request.getParameter("hosttype");
				
				String url = request.getParameter("url");

				String confidential = request.getParameter("confidential");

				String ownerid = bt.isNull(request.getParameter("ownerid"))?"111111":request.getParameter("ownerid");
				
				String hostid = ht.add(hostname, hostip, hostport,  username, url, hosttype, ownerid, confidential);
				
				resp = "{ \"id\" : \"" + hostid + "\", \"name\" : \""+ hostname + "\", \"type\": \""+hosttype+"\" }";
				
			}else if(type.equals("process")) {
				
				String lang = request.getParameter("lang");

				String name = request.getParameter("name");
				
				String desc = request.getParameter("desc");

				String ownerid = bt.isNull(request.getParameter("ownerid"))?"111111":request.getParameter("ownerid");

				String confidential = request.getParameter("confidential");
				
				String code = null;
				
				if(lang.equals("shell")) {
					
					code = request.getParameter("code");
					
				}else if(lang.equals("builtin")) {
					
					String operation = request.getParameter("code[operation]");
					
					code = "{ \"operation\" : \"" + operation + "\", \"params\":[";
					
					List params = new ArrayList();
					
					int i=0;
					
					while(request.getParameter("code[params]["+i+"][name]")!=null) {
						
						if(i!=0) {
							
							code += ", ";
							
						}
						
						code += "{ \"name\": \"" + request.getParameter("code[params]["+i+"][name]") + "\", \"value\": \"" + request.getParameter("code[params]["+i+"][value]") + "\" }";
						
						i++;
						
					}
					
					code += "] }";
					
				}else if(lang.equals("jupyter")) {
					
					code = request.getParameter("code");
					
				}else {
					
					code = request.getParameter("code");
					
				}
				
				String pid = pt.add(name, lang, code, desc, ownerid, confidential);
				
				resp = "{\"id\" : \"" + pid + "\", \"name\":\"" + name + "\", \"lang\": \""+lang+"\"}";
				
			}else if(type.equals("workflow")) {
				
				String name = request.getParameter("name");
				
				String nodes = request.getParameter("nodes");
				
				String edges = request.getParameter("edges");
				
				String ownerid = bt.isNull(request.getParameter("ownerid"))?"111111":request.getParameter("ownerid");

				String wid = wt.add(name, nodes, edges, ownerid);
				
				resp = "{\"id\" : \"" + wid + "\", \"name\":\"" + name + "\"}";
				
			}
			
		}catch(Exception e) {
			
			e.printStackTrace();
			
			throw new RuntimeException("failed " + e.getLocalizedMessage());
			
		}
		
		return resp;
		
	}
	
	@RequestMapping(value = "/geoweaver-ssh", method = RequestMethod.GET)
    public String sshterminal(ModelMap model, WebRequest request, SessionStatus status, HttpSession session){
    	
    	String token = request.getParameter("token");
    	
    	logger.debug("token : " + token);
    	
    	String resp = "redirect:geoweaver-ssh-login";
    	
    	//here should validate the token
    	if(token != null){
    		
//    		model.addAttribute("username", name);
    		
    		SSHSession ss = sessionManager.sshSessionByToken.get(token);
    		
    		if(ss!=null) {
    			
    			model.addAttribute("host", ss.getHost());
                
                model.addAttribute("username", ss.getUsername());
                
                model.addAttribute("port", ss.getPort());
                
                model.addAttribute("token", ss.getToken());
    			
    			resp = "geoweaver-ssh";
    			
    		}
    		
    	}
    	
    	return resp;
    	
    }
	
	@RequestMapping(value = "/geoweaver-ssh-logout-inbox", method = RequestMethod.POST)
    public @ResponseBody String ssh_close_inbox(Model model, WebRequest request, HttpSession session){
    	
    	String resp = "";
    	
    	try {
    		
        	String token = request.getParameter("token");
        	
        	if(token != null) {

            	SSHSession s =  sessionManager.sshSessionByToken.get(token);
            	
            	if(s != null) {
            		
            		s.logout();
            		
            		sessionManager.sshSessionByToken.remove(token);
            		
            	}
        		
        	}
        	
            resp = "done";
            
    	}catch(Exception e) {
    		
    		e.printStackTrace();
    		
    		throw new RuntimeException();
    		
    	}
    	
    	return resp;
    	
    }
	
	@RequestMapping(value = "/geoweaver-ssh-login-inbox", method = RequestMethod.POST)
    public @ResponseBody String ssh_auth_inbox(Model model, WebRequest request, HttpSession session){
    	
    	String resp = "";
    	
    	try {
    		
    		String host = request.getParameter("host");
        	
        	String port = request.getParameter("port");
        	
        	String username = request.getParameter("username");
        	
        	String encrypted = request.getParameter("password");
        	
        	String password = RSAEncryptTool.getPassword(encrypted, session.getId());
        	
//        	String token = request.getParameter("token");
        	
        	String token = session.getId(); //use session id as token
        	
//        	if(token!=null && sessionManager.sshSessionByToken.get(token)!=null) {
//        		
////        		token = sessionManager.sshSessionByToken.get(token).getToken();
//        		
//        	}else {
        		
//        		token = new RandomString(16).nextString(); //token must be assigned by server in case somebody else hijacks it
            	
//            	SSHSession sshSession = new SSHSessionImpl();
            	
            	boolean success = sshSession.login(host, port, username, password, token, true);
            	
            	logger.debug("SSH login: " + username + "=" + success);
                        
                logger.debug("adding SSH session for " + username);
                
//                sessionManager.sessionsByUsername.put(host+"-"+username, sshSession);
                
                sessionManager.sshSessionByToken.put(token, sshSession); //token is session id
        		
//        	}
        	
//            model.addAttribute("host", host);
//            
//            model.addAttribute("username", username);
//            
//            model.addAttribute("port", port);
//            
//            model.addAttribute("token", token);
            
            resp = "{\"token\": \""+token+"\"}";
            
    	}catch(Exception e) {
    		
    		e.printStackTrace();
    		
    		throw new RuntimeException(e.getLocalizedMessage());
    		
    	}
    	
    	return resp;
    	
    }
	
	@RequestMapping(value = "/geoweaver-ssh-login", method = RequestMethod.POST)
    public String ssh_auth(Model model, WebRequest request, HttpSession session){
    	
    	String resp = "redirect:geoweaver-ssh";
    	
    	try {
    		
    		String host = request.getParameter("host");
        	
        	String port = request.getParameter("port");
        	
        	String username = request.getParameter("username");
        	
        	String password = request.getParameter("password");
        	
        	String token = null;
        	
        	if(sessionManager.sshSessionByToken.get(host+"-"+username)!=null) {
        		
//        		token = sessionManager.sshSessionByToken.get(host+"-"+username).getToken();
        		
        	}else {
        		
        		token = new RandomString(16).nextString();
            	
//            	SSHSession sshSession = new SSHSessionImpl();
            	
            	boolean success = sshSession.login(host, port, username, password, token, true);
            	
            	logger.debug("SSH login: "+username+"=" + success);
                        
                logger.debug("adding SSH session for " + username);
                
//                sessionManager.sessionsByUsername.put(host+"-"+username, sshSession);
                
                sessionManager.sshSessionByToken.put(token, sshSession);
        		
        	}
        	
            model.addAttribute("host", host);
            
            model.addAttribute("username", username);
            
            model.addAttribute("port", port);
            
            model.addAttribute("token", token);
            
    	}catch(Exception e) {
    		
    		e.printStackTrace();
    		
    	}
    	
    	return resp;
    	
    }
    
    @RequestMapping(value = "/geoweaver-ssh-login", method = RequestMethod.GET)
    public String ssh_login(Model model, WebRequest request, HttpSession session){
    	
    	String resp = "geoweaver-ssh-login";
    	
//    	String error = request.getParameter("error");
//        String message = request.getParameter("message");
//        String logout = request.getParameter("logout");
        
//        ModelAndView model  = new ModelAndView("login");
//
//        if (message != null) {
//            model.addObject("message", message);
//        }
//
//        if (logout != null) {
//            model.addObject("message", "Logout successful");
//        }
//
//        if (error != null) {
//	        log.error(error);
//            model.addObject("error", "Login was unsuccessful");
//		}
//		
//        return model;
    	
    	return resp;
    	
    }

	@RequestMapping("/error")
    public String handleError() {
        //do something like logging
        return "error";
    }
    
    void checkID(String id) {
    	
    	if(bt.isNull(id))
    		
    		throw new RuntimeException("No ID found");
    	
    }



    public static void main(String[] args) {
    	
    	sessionManager.closeAll();
    	
    }
	
    
}
