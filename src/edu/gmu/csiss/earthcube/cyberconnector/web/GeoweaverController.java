package edu.gmu.csiss.earthcube.cyberconnector.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.context.request.WebRequest;

import edu.gmu.csiss.earthcube.cyberconnector.ssh.FileTool;
import edu.gmu.csiss.earthcube.cyberconnector.ssh.HostTool;
import edu.gmu.csiss.earthcube.cyberconnector.ssh.ProcessTool;
import edu.gmu.csiss.earthcube.cyberconnector.ssh.RSAEncryptTool;
import edu.gmu.csiss.earthcube.cyberconnector.ssh.SSHSession;
import edu.gmu.csiss.earthcube.cyberconnector.ssh.SSHSessionImpl;
import edu.gmu.csiss.earthcube.cyberconnector.ssh.SSHSessionManager;
import edu.gmu.csiss.earthcube.cyberconnector.ssh.WorkflowTool;
import edu.gmu.csiss.earthcube.cyberconnector.utils.RandomString;
import edu.gmu.csiss.earthcube.cyberconnector.utils.SysDir;

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
//@RequestMapping(value="/")     
//@SessionAttributes({"SSHToken"})
public class GeoweaverController {

	Logger logger = LoggerFactory.getLogger(getClass());
	
	public static SSHSessionManager sshSessionManager;
	
	static {
		
		sshSessionManager = new SSHSessionManager();
		
	}
	
	@PreDestroy
    public void destroy() {
		
        System.out.println(
          "Callback triggered - @PreDestroy.");
        
        sshSessionManager.closeAll();
        
    }
	
	@RequestMapping(value = "/del", method = RequestMethod.POST)
    public @ResponseBody String del(ModelMap model, WebRequest request){
		
		String resp = null;
		
		try {
			
			String id = request.getParameter("id");
			
			String type = request.getParameter("type");
			
			if(type.equals("host")) {

				resp = HostTool.del(id);
				
			}else if(type.equals("process")) {
				
				resp = ProcessTool.del(id);
				
			}else if(type.equals("workflow")) {
				
				resp = WorkflowTool.del(id);
				
			}
			
		}catch(Exception e) {
			
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

				resp = HostTool.detail(id);
				
			}else if(type.equals("process")) {
				
				resp = ProcessTool.detail(id);
				
			}else if(type.equals("workflow")) {
				
				resp = WorkflowTool.detail(id);
				
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
	
	@RequestMapping(value = "/log", method = RequestMethod.POST)
    public @ResponseBody String one_history(ModelMap model, WebRequest request){
		
		String resp = null;
		
		try {
			
			String type = request.getParameter("type");
			
			String hid = request.getParameter("id");
			
			if(type.equals("process")) {
				
				resp = ProcessTool.one_history(hid);
				
			}else if(type.equals("workflow")) {
				
				resp = WorkflowTool.one_history(hid);
				
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
			
			String id = request.getParameter("id");
			
			if(type.equals("process")) {
				
				resp = ProcessTool.all_history(id);
				
			}else if(type.equals("workflow")) {
				
				resp = WorkflowTool.all_history(id);
				
			}
			
		}catch(Exception e) {
			
			e.printStackTrace();
			
			throw new RuntimeException("failed " + e.getLocalizedMessage());
			
		}
		
		return resp;
		
	}
	
	@RequestMapping(value = "/list", method = RequestMethod.POST)
    public @ResponseBody String list(ModelMap model, WebRequest request){
		
		String resp = null;
		
		try {
			
			String type = request.getParameter("type");
			
			if(type.equals("host")) {

				resp = HostTool.list("");
				
			}else if(type.equals("process")) {
				
				resp = ProcessTool.list("");
				
			}else if(type.equals("workflow")) {
				
				resp = WorkflowTool.list("");
				
			}
			
		}catch(Exception e) {
			
			e.printStackTrace();
			
			throw new RuntimeException("failed " + e.getLocalizedMessage());
			
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

	@RequestMapping(value = "/executeWorkflow", method = RequestMethod.POST)
    public @ResponseBody String executeWorkflow(ModelMap model, WebRequest request, HttpSession session){
		
		String resp = null;
		
		try {
			
			String id = request.getParameter("id");
			
			String mode = request.getParameter("mode");
			
			String[] hosts = request.getParameterValues("hosts[]");
			
			String[] encrypted_password = request.getParameterValues("passwords[]");
			
			String[] passwords = RSAEncryptTool.getPasswords(encrypted_password, session.getId());
			
			resp = WorkflowTool.execute(id, mode, hosts, passwords, null);
			
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
			
			String password = RSAEncryptTool.getPassword(encrypted_password, session.getId());
			
			resp = ProcessTool.execute(pid, hid, password, null);
			
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
				
//				String hostname = request.getParameter("hostname");
//				
//				String hostip = request.getParameter("hostip");
//				
//				String hostport = request.getParameter("hostport");
//				
//				String username = request.getParameter("username");
//				
//				String hostid = HostTool.update(hostname, hostip, hostport, username, null);
//				
//				resp = "{ \"hostid\" : \"" + hostid + "\", \"hostname\" : \""+ hostname + "\" }";
				
			}else if(type.equals("process")) {
				
				String lang = request.getParameter("lang");
				
				String code = request.getParameter("code");
				
				String name = request.getParameter("name");
				
				String desc = request.getParameter("desc");
				
				String id = request.getParameter("id");
				
				ProcessTool.update(id, name, lang, code, desc);
				
				resp = "{\"id\" : \"" + id + "\"}";
				
			}else if(type.equals("workflow")) {
				
				String wid = request.getParameter("id");
				
				String nodes = request.getParameter("nodes");
				
				String edges = request.getParameter("edges");
				
				WorkflowTool.update(wid, nodes, edges);
				
				resp = "{\"id\" : \"" + wid + "\"}";
				
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
	    	String fileloc = SysDir.geoweaver_file_path + fileName;
	      
	    	File my_file = new File(fileloc);
	      
	    	InputStream is = new FileInputStream(my_file);
	      
	    	// copy it to response's OutputStream
	      
	    	org.apache.commons.io.IOUtils.copy(is, response.getOutputStream());
	      
	    	response.flushBuffer();
	    	
	    } catch (Exception ex) {
	      
	    	logger.info("Error writing file to output stream. Filename was '{}'", fileName, ex);
	    	
	    	throw new RuntimeException("IOError writing file to output stream");
	    	
	    }

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
			
			resp = FileTool.scp_download(hid, password, filepath);
			
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
				
				String hostid = HostTool.add(hostname, hostip, hostport, username, null);
				
				resp = "{ \"id\" : \"" + hostid + "\", \"name\" : \""+ hostname + "\" }";
				
			}else if(type.equals("process")) {
				
				String lang = request.getParameter("lang");
				
				String code = request.getParameter("code");
				
				String name = request.getParameter("name");
				
				String desc = request.getParameter("desc");
				
				String pid = ProcessTool.add(name, lang, code, desc);
				
				resp = "{\"id\" : \"" + pid + "\", \"name\":\"" + name + "\"}";
				
			}else if(type.equals("workflow")) {
				
				String name = request.getParameter("name");
				
				String nodes = request.getParameter("nodes");
				
				String edges = request.getParameter("edges");
				
				String wid = WorkflowTool.add(name, nodes, edges);
				
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
    	
    	logger.info("token : {}", token);
    	
    	String resp = "redirect:geoweaver-ssh-login";
    	
    	//here should validate the token
    	if(token != null){
    		
//    		model.addAttribute("username", name);
    		
    		SSHSession ss = sshSessionManager.sessionsByToken.get(token);
    		
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

            	SSHSession s =  sshSessionManager.sessionsByToken.get(token);
            	
            	if(s != null) {
            		
            		s.logout();
            		
            		sshSessionManager.sessionsByToken.remove(token);
            		
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
        	
        	String password = request.getParameter("password");
        	
        	String token = request.getParameter("token");
        	
        	if(token!=null && sshSessionManager.sessionsByToken.get(token)!=null) {
        		
        		token = sshSessionManager.sessionsByToken.get(token).getToken();
        		
        	}else {
        		
        		token = new RandomString(16).nextString();
            	
            	SSHSession sshSession = new SSHSessionImpl();
            	
            	boolean success = sshSession.login(host, port, username, password, token, false);
            	
            	logger.info("SSH login: {}={}", username, success);
                        
                logger.info("adding SSH session for {}", username);
                
//                sshSessionManager.sessionsByUsername.put(host+"-"+username, sshSession);
                
                sshSessionManager.sessionsByToken.put(token, sshSession);
        		
        	}
        	
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
    		
    		throw new RuntimeException();
    		
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
        	
        	if(sshSessionManager.sessionsByToken.get(host+"-"+username)!=null) {
        		
        		token = sshSessionManager.sessionsByToken.get(host+"-"+username).getToken();
        		
        	}else {
        		
        		token = new RandomString(16).nextString();
            	
            	SSHSession sshSession = new SSHSessionImpl();
            	
            	boolean success = sshSession.login(host, port, username, password, token, false);
            	
            	logger.info("SSH login: {}={}", username, success);
                        
                logger.info("adding SSH session for {}", username);
                
//                sshSessionManager.sessionsByUsername.put(host+"-"+username, sshSession);
                
                sshSessionManager.sessionsByToken.put(token, sshSession);
        		
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

    public static void main(String[] args) {
    	
    	sshSessionManager.closeAll();
    	
    }
	
    
}
