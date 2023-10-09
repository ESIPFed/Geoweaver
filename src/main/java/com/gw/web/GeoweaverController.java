package com.gw.web;


import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.gw.database.WorkflowRepository;
import com.gw.jpa.GWProcess;
import com.gw.jpa.GWUser;
import com.gw.jpa.Host;
import com.gw.jpa.Workflow;
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
import com.gw.tools.EnvironmentTool;
import com.gw.utils.RandomString;
import com.gw.tools.UserTool;
import com.gw.tools.ExecutionTool;

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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.HttpServletRequest;
/**
 * Controller for SSH related activities, including all the handlers for Geoweaver.
 * This controller handles various web requests related to Geoweaver's functionality.
 */
@Controller 
@RequestMapping(value="/web")
public class GeoweaverController {

    // Logger for logging controller-related activities.
    Logger logger = LoggerFactory.getLogger(getClass());
    
    // Autowired tools and services required for various operations.
    @Autowired
    ProcessTool pt;
    
    @Autowired
    WorkflowTool wt;

    @Autowired
    WorkflowRepository workflowrepository;
    
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
    EnvironmentTool et;

    @Autowired
    ExecutionTool ext;
    
    @Autowired
    SSHSession sshSession;
    
    // Configuration property for upload file path.
    @Value("${geoweaver.upload_file_path}")
    String upload_file_path;

    @Autowired
    UserTool ut;
    
    // Session manager to manage SSH sessions.
    public static SessionManager sessionManager;
    
    // Static block to initialize the session manager.
    static {
        sessionManager = new SessionManager();
    }
    
    // Destructor method to close all SSH sessions when the application is destroyed.
    @PreDestroy
    public void destroy() {
        logger.debug("Callback triggered - @PreDestroy.");
        sessionManager.closeAll();
    }

    /**
     * Handles a POST request to delete all history records associated with a host.
     * @param model The ModelMap to store response data.
     * @param request The WebRequest containing request parameters.
     * @return A response string indicating the result of the operation.
     */
    @RequestMapping(value="/delAllHistory", method= RequestMethod.POST)
    public @ResponseBody String delAllHistory(ModelMap model, WebRequest request){
        String resp = null;
        try{
            String hostid = request.getParameter("id");
            resp = hist.deleteAllHistoryByHost(hostid);
        }catch(Exception e){
            throw new RuntimeException("Failed: " + e.getLocalizedMessage());
        }
        return resp;
    }

    /**
     * Handles a POST request to delete history records with no notes associated with a host.
     * @param model The ModelMap to store response data.
     * @param request The WebRequest containing request parameters.
     * @return A response string indicating the result of the operation.
     */
    @RequestMapping(value="/delNoNotesHistory", method = RequestMethod.POST)
    public @ResponseBody String delNoNotesHistory(ModelMap model, WebRequest request){
        String resp = null;
        try{
            String hostid = request.getParameter("id");
            resp = hist.deleteNoNotesHistoryByHost(hostid);
        }catch(Exception e){
            throw new RuntimeException("Failed: " + e.getLocalizedMessage());
        }
        return resp;
    }
    
    /**
     * Handles a POST request to delete a resource (host, process, workflow, history, or clear nodes and edges).
     * @param model The ModelMap to store response data.
     * @param request The WebRequest containing request parameters.
     * @return A response string indicating the result of the operation.
     */
    @RequestMapping(value = "/del", method = RequestMethod.POST)
    public @ResponseBody String del(ModelMap model, WebRequest request){
        String resp = null;
        try {
            String id = request.getParameter("id");
            String type = request.getParameter("type");
            switch (Objects.requireNonNull(type)) {
                case "host":
                    resp = ht.del(id);
                    break;
                case "process":
                    resp = pt.del(id);
                    break;
                case "workflow":
                    resp = wt.del(id);
                    break;
                case "history":
                    resp = hist.deleteById(id);
                    break;
                case "clear_nodes_edges":
                    assert id != null;
                    Optional<Workflow> optionalWorkflow = workflowrepository.findById(id);
                    if (optionalWorkflow.isPresent()) {
                        Workflow wf = optionalWorkflow.get();
                        wf.setEdges(Collections.emptyList().toString());
                        wf.setNodes(Collections.emptyList().toString());
                        workflowrepository.save(wf);
                    }
                    break;
            }
        } catch(Exception e) {
            throw new RuntimeException("Failed: " + e.getLocalizedMessage());
        }
        return resp;
    }
    
    /**
     * Handles a POST request to perform a search for a resource (host, process, or workflow).
     * @param model The ModelMap to store response data.
     * @param request The WebRequest containing request parameters.
     * @return A response string containing the search results.
     */
    @RequestMapping(value = "/search", method = RequestMethod.POST)
    public @ResponseBody String search(ModelMap model, WebRequest request){
        String resp = null;
        try {
            String type = request.getParameter("type");
            String keywords = request.getParameter("keywords");
            resp = st.search(keywords, type);
        } catch(Exception e) {
            throw new RuntimeException("Failed: " + e.getLocalizedMessage());
        }
        return resp;
    }
    
    /**
     * Handles a POST request to retrieve dashboard information.
     * @param model The ModelMap to store response data.
     * @param request The WebRequest containing request parameters.
     * @return A response string containing the dashboard information.
     */
    @RequestMapping(value = "/dashboard", method = RequestMethod.POST)
    public @ResponseBody String dashboard(ModelMap model, WebRequest request){
        String resp = null;
        try {
            resp = dbt.getJSON();
        } catch(Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed: " + e.getLocalizedMessage());
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

        if(!BaseTool.isNull(token)){

            System.err.print(token);
            // User user = userService.getByResetPasswordToken(token);
            String userid = ut.token2userid.get(token);
            Date created_date = ut.token2date.get(token);
    
            if(!BaseTool.isNull(userid)){
    
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
			
			int number = Integer.parseInt(Objects.requireNonNull(request.getParameter("number")));

			switch (Objects.requireNonNull(type)) {
				case "process":

					resp = pt.recent(number);

					break;
				case "workflow":

					resp = wt.recent(number);

					break;
				case "host":

					String hid = request.getParameter("hostid");

					resp = ht.recent(hid, number);

					break;
			}
			
		}catch(Exception e) {
			
			e.printStackTrace();
			
			throw new RuntimeException("failed " + e.getLocalizedMessage());
			
		}
		
		return resp;
		
	}

	@RequestMapping(value = "/downloadworkflow", method = RequestMethod.POST)
    public @ResponseBody String downloadworkflow(ModelMap model, WebRequest request){
		
		String resp = null;
		
		try {
			
			String option = request.getParameter("option");
			
			String wid = request.getParameter("id");
			
			resp = wt.download(wid, option);
			
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

	@RequestMapping(value= "/skip_workflow_process", method = RequestMethod.POST)
	public @ResponseBody String skip_process(ModelMap model, WebRequest request){

		String resp = null;
		
		try {
			
			String workflow_id = request.getParameter("workflowid");
			
			String workflow_process_id = request.getParameter("processid");

			String if_skipped = request.getParameter("skip");

			checkID(workflow_id);

			checkID(workflow_process_id);

			wt.skip_process(workflow_id, workflow_process_id, if_skipped);

			resp = String.format("{\"id\": \"%s\", \"message\": \"%s\"}", workflow_id, "success");

		}catch(Exception e) {
			
			e.printStackTrace();
			
			throw new RuntimeException("failed " + e.getLocalizedMessage());
			
		}
		
		return resp;

	}

	@RequestMapping(value= "/check_workflow_process_skipped", method = RequestMethod.POST)
	public @ResponseBody String check_skip_process(ModelMap model, WebRequest request){

		String resp = null;
		
		try {
			
			String workflow_id = request.getParameter("workflowid");
			
			String workflow_process_id = request.getParameter("processid");

			checkID(workflow_id);

			checkID(workflow_process_id);

			resp = String.format("{ \"if_skipped\": %s}", wt.check_process_skipped(workflow_id, workflow_process_id));

		}catch(Exception e) {
			
			e.printStackTrace();
			
			throw new RuntimeException("failed " + e.getLocalizedMessage());
			
		}
		
		return resp;

	}

	@RequestMapping(value = "/workflow_process_log", method = RequestMethod.POST)
    public @ResponseBody String workflow_process_log(ModelMap model, WebRequest request){
		
		String resp = null;
		
		try {
			
			String workflowhistoryid = request.getParameter("workflowhistoryid");
			
			String processid = request.getParameter("processid");
			
			resp = hist.getWorkflowProcessHistory(workflowhistoryid, processid);

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
				
				resp = pt.stop(id);
				
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
				
				if(BaseTool.isNull(id)) {
					
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
				
				if(BaseTool.isNull(id)) {
					
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
			
			resp = et.getEnvironments(hid);
			
		}catch(Exception e) {
			
			e.printStackTrace();
			
			throw new RuntimeException("failed " + e.getLocalizedMessage());
			
		}
		
		return resp;
		
	}

	/**
	 * Handles HTTP POST requests to list hosts with associated environments.
	 *
	 * @param model       The model to add attributes to.
	 * @param request     The web request.
	 * @param session     The user's HTTP session.
	 * @param httprequest The HTTP request.
	 * @return A JSON response containing a list of hosts with environments or an error message.
	 */
	@RequestMapping(value = "/listhostwithenvironments", method = RequestMethod.POST)
	public @ResponseBody String listhostwithenvironments(ModelMap model, WebRequest request, HttpSession session, HttpServletRequest httprequest) {
		String resp = null;
		try {
			// Retrieve the authenticated user's ownerid based on the session and HTTP request IP.
			String ownerid = ut.getAuthUserId(session.getId(), ut.getClientIp(httprequest));
			// Retrieve a list of hosts with environments associated with the authenticated user.
			resp = ht.list(ownerid);
		} catch (Exception e) {
			e.printStackTrace();
			// Handle database query errors and return an appropriate JSON response.
			resp = "{\"ret\": \"failure\", \"reason\": \"Database Query Error.\"}";
		}
		return resp;
	}

	/**
	 * Handles HTTP POST requests to list resources (hosts, processes, workflows) of a specified type.
	 *
	 * @param model       The model to add attributes to.
	 * @param request     The web request.
	 * @param session     The user's HTTP session.
	 * @param httprequest The HTTP request.
	 * @return A JSON response containing a list of resources of the specified type or an error message.
	 */
	@RequestMapping(value = "/list", method = RequestMethod.POST)
	public @ResponseBody String list(ModelMap model, WebRequest request, HttpSession session, HttpServletRequest httprequest) {
		String resp = null;
		try {
			// Retrieve the type of resource to list (e.g., host, process, workflow).
			String type = request.getParameter("type");
			// Retrieve the authenticated user's ownerid based on the session and HTTP request IP.
			String ownerid = ut.getAuthUserId(session.getId(), ut.getClientIp(httprequest));
			// Depending on the resource type, retrieve a list of resources owned by the authenticated user.
			if (type.equals("host")) {
				resp = ht.list(ownerid);
			} else if (type.equals("process")) {
				resp = pt.list(ownerid);
			} else if (type.equals("workflow")) {
				resp = wt.list(ownerid);
			}
		} catch (Exception e) {
			e.printStackTrace();
			// Handle database query errors and return an appropriate JSON response.
			resp = "{\"ret\": \"failure\", \"reason\": \"Database Query Error.\"}";
		}
		return resp;
	}

	/**
	 * Handles HTTP POST requests to check the existence of a live session associated with a host.
	 *
	 * @param model   The model to add attributes to.
	 * @param request The web request.
	 * @return A JSON response indicating whether a live session with the specified hostId exists.
	 */
	@RequestMapping(value = "/checkLiveSession", method = RequestMethod.POST)
	public @ResponseBody String checklivesession(ModelMap model, WebRequest request) {
		String resp = null;
		try {
			// Retrieve the hostId parameter from the request.
			String hid = request.getParameter("hostId");
			// Check if a live session with the specified hostId exists and set the response accordingly.
			resp = "{\"exist\": false}";
		} catch (Exception e) {
			e.printStackTrace();
			// Handle exceptions and throw a runtime exception with an error message.
			throw new RuntimeException("failed " + e.getLocalizedMessage());
		}
		return resp;
	}

	
	/**
	 * Handles HTTP POST requests to close the file browser associated with a user session.
	 *
	 * @param model   The model to add attributes to.
	 * @param request The web request.
	 * @param session The user's HTTP session.
	 * @return A JSON response indicating a successful closure of the file browser.
	 */
	@RequestMapping(value = "/closefilebrowser", method = RequestMethod.POST)
	public @ResponseBody String closefileBrowser(ModelMap model, WebRequest request, HttpSession session) {
		String resp = null;
		try {
			// Close the file browser associated with the user's session.
			ft.close_browser(session.getId());
			resp = "{ \"ret\": \"success\"}";
		} catch (Exception e) {
			e.printStackTrace();
			// Handle exceptions and throw a runtime exception with an error message.
			throw new RuntimeException(e.getLocalizedMessage());
		}
		return resp;
	}

	/**
	 * Handles HTTP POST requests to open the file browser, either initializing a new session or continuing an existing one.
	 *
	 * @param model     The model to add attributes to.
	 * @param request   The web request.
	 * @param session   The user's HTTP session.
	 * @return A JSON response indicating the result of opening the file browser.
	 */
	@RequestMapping(value = "/openfilebrowser", method = RequestMethod.POST)
	public @ResponseBody String fileBrowser(ModelMap model, WebRequest request, HttpSession session) {
		String resp = null;
		try {
			// Retrieve the parameters from the request.
			String hid = request.getParameter("hid");
			String encrypted = request.getParameter("pswd");
			String init_path = request.getParameter("init_path");
			
			if (!BaseTool.isNull(encrypted)) {
				// If a password is encrypted, decrypt it and open the SFTP browser.
				String password = RSAEncryptTool.getPassword(encrypted, session.getId());
				resp = ft.open_sftp_browser(hid, password, init_path, session.getId());
			} else {
				// If no encrypted password is provided, continue an existing browser session.
				resp = ft.continue_browser(session.getId(), init_path);
			}
		} catch (Exception e) {
			e.printStackTrace();
			// Handle exceptions and throw a runtime exception with an error message.
			throw new RuntimeException(e.getLocalizedMessage());
		}
		return resp;
	}

	/**
	 * Handles HTTP POST requests to retrieve and download a file using SCP (Secure Copy Protocol).
	 *
	 * @param model   The model to add attributes to.
	 * @param request The web request.
	 * @param session The user's HTTP session.
	 * @return A JSON response indicating the result of the file retrieval and download.
	 */
	@RequestMapping(value = "/retrievefile", method = RequestMethod.POST)
	public @ResponseBody String scpdownload(ModelMap model, WebRequest request, HttpSession session) {
		String resp = null;
		try {
			// Retrieve the filepath parameter from the request.
			String filepath = request.getParameter("filepath");
			// Retrieve and download the file using SCP, associated with the user's session.
			resp = ft.scp_download(filepath, session.getId());
		} catch (Exception e) {
			e.printStackTrace();
			// Handle exceptions and throw a runtime exception with an error message.
			throw new RuntimeException(e.getLocalizedMessage());
		}
		return resp;
	}

	/**
	 * Handles HTTP GET requests to download a file.
	 *
	 * @param model     The model to add attributes to.
	 * @param tempfolder The temporary folder where the file is stored.
	 * @param filename  The name of the file to be downloaded.
	 * @param request   The web request.
	 * @param session   The user's HTTP session.
	 * @return A ResponseEntity containing the requested file for download.
	 */
	@RequestMapping(value = "/download/{tempfolder}/{filename}", method = RequestMethod.GET)
	public ResponseEntity<Resource> fileGetter(ModelMap model, @PathVariable(value="tempfolder") String tempfolder, 
			@PathVariable(value="filename") String filename, WebRequest request, HttpSession session) {
		ResponseEntity<Resource> resp = null;
		try {
			if (tempfolder.equals(upload_file_path)) {
				HttpHeaders headers = new HttpHeaders();
				headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
				headers.add("Pragma", "no-cache");
				headers.add("Expires", "0");
				headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);
				File file = new File(bt.getFileTransferFolder() + filename);
				InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
				resp = ResponseEntity.ok()
						.headers(headers)
						.contentLength(file.length())
						.contentType(MediaType.APPLICATION_OCTET_STREAM)
						.body(resource);
			}
		} catch (Exception e) {
			e.printStackTrace();
			// Handle exceptions and throw a runtime exception with an error message.
			throw new RuntimeException(e.getLocalizedMessage());
		}
		return resp;
	}

	/**
	 * Handles HTTP POST requests to edit a file.
	 *
	 * @param model   The model to add attributes to.
	 * @param request The web request.
	 * @param session The user's HTTP session.
	 * @return A JSON response indicating the result of the file editing operation.
	 */
	@RequestMapping(value = "/updatefile", method = RequestMethod.POST)
	public @ResponseBody String fileEditor(ModelMap model, WebRequest request, HttpSession session) {
		String resp = null;
		try {
			String filepath = request.getParameter("filepath");
			String content = request.getParameter("content");
			resp = ft.scp_fileeditor(filepath, content, session.getId());
		} catch (Exception e) {
			e.printStackTrace();
			// Handle exceptions and throw a runtime exception with an error message.
			throw new RuntimeException(e.getLocalizedMessage());
		}
		return resp;
	}

	/**
	 * Handles HTTP POST requests to read a Python environment on a host.
	 *
	 * @param model   The model to add attributes to.
	 * @param request The web request.
	 * @param session The user's HTTP session.
	 * @return A JSON response containing information about the Python environment on the host.
	 */
	@RequestMapping(value="/readEnvironment", method = RequestMethod.POST)
	public @ResponseBody String readPythonEnvironment(ModelMap model, WebRequest request, HttpSession session){
		String resp = null;
		try{
			String hid = request.getParameter("hostid");
			String password = request.getParameter("pswd");
			password = RSAEncryptTool.getPassword(password, session.getId());
			resp = ext.readEnvironment(hid, password);
		} catch (Exception e){
			e.printStackTrace();
			// Handle exceptions and create a JSON response with an error message.
			resp = bt.getErrorReturn(e.getLocalizedMessage());
		}
		return resp;
	}

	/**
	 * Handles HTTP POST requests to execute a workflow.
	 *
	 * @param model   The model to add attributes to.
	 * @param request The web request.
	 * @param session The user's HTTP session.
	 * @return A JSON response indicating the result of the workflow execution.
	 */
	@RequestMapping(value = "/executeWorkflow", method = RequestMethod.POST)
	public @ResponseBody String executeWorkflow(ModelMap model, WebRequest request, HttpSession session){
		String resp = null;
		try {
			String id = request.getParameter("id");
			String mode = request.getParameter("mode");
			String token = request.getParameter("token");
			String history_id = BaseTool.isNull(request.getParameter("history_id")) ? 
					new RandomString(18).nextString() : request.getParameter("history_id");
			String[] hosts = request.getParameterValues("hosts[]");
			String[] encrypted_password = request.getParameterValues("passwords[]");
			String[] environments = request.getParameterValues("envs[]");
			String[] passwords = RSAEncryptTool.getPasswords(encrypted_password, session.getId());
			resp = wt.execute(history_id, id, mode, hosts, passwords, environments, token);
		} catch (Exception e) {
			e.printStackTrace();
			// Handle exceptions and throw a runtime exception with an error message.
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
	
	/**
	 * Handles HTTP POST requests to execute a process.
	 *
	 * @param model    The model to add attributes to.
	 * @param request  The web request.
	 * @param session  The user's HTTP session.
	 * @return A JSON response indicating the result of the process execution.
	 */
	@RequestMapping(value = "/executeProcess", method = RequestMethod.POST)
	public @ResponseBody String executeProcess(ModelMap model, WebRequest request, HttpSession session){
		String resp = null;
		try {
			String pid = request.getParameter("processId");
			String hid = request.getParameter("hostId");
			String encrypted_password = request.getParameter("pswd");
			String token = request.getParameter("token");
			String bin = request.getParameter("env[bin]");
			String pyenv = request.getParameter("env[pyenv]");
			String basedir = request.getParameter("env[basedir]");
			String password = RSAEncryptTool.getPassword(encrypted_password, session.getId());
			String history_id = BaseTool.isNull(request.getParameter("history_id")) ? 
					new RandomString(12).nextString() : request.getParameter("history_id");
			resp = ext.executeProcess(history_id, pid, hid, password, token, false, bin, pyenv, basedir);
		} catch (Exception e) {
			e.printStackTrace();
			// Handle exceptions and throw a runtime exception with an error message.
			throw new RuntimeException("failed " + e.getLocalizedMessage());
		}
		return resp;
	}

	/**
	 * Handles HTTP POST requests to preload a workflow.
	 *
	 * @param model    The model to add attributes to.
	 * @param request  The web request.
	 * @return A JSON response indicating the precheck result for the workflow.
	 */
	@RequestMapping(value = "/preload/workflow", method = RequestMethod.POST)
	public @ResponseBody String preloadworkflow(ModelMap model,  WebRequest request){
		String resp = null;
		try {
			String filename = request.getParameter("filename");
			resp = wt.precheck(filename);
		} catch (Exception e) {
			e.printStackTrace();
			// Handle exceptions and throw a runtime exception with an error message.
			throw new RuntimeException("failed " + e.getLocalizedMessage());
		}
		return resp;
	}

	/**
	 * Handles HTTP POST requests to load a workflow.
	 *
	 * @param model    The model to add attributes to.
	 * @param request  The web request.
	 * @return A JSON response indicating the result of loading the workflow.
	 */
	@RequestMapping(value = "/load/workflow", method = RequestMethod.POST)
	public @ResponseBody String loadworkflow(ModelMap model, WebRequest request){
		String resp = null;
		try {
			String wid = request.getParameter("id");
			String filename = request.getParameter("filename");
			resp = wt.saveWorkflowFromFolder(wid, filename);
		} catch (Exception e) {
			e.printStackTrace();
			// Handle exceptions and throw a runtime exception with an error message.
			throw new RuntimeException("failed " + e.getLocalizedMessage());
		}
		return resp;
	}

	/**
	 * Handles HTTP POST requests to edit a process.
	 *
	 * @param model    The model to add attributes to.
	 * @param up       The updated process data.
	 * @param request  The web request.
	 * @return A JSON response indicating the result of editing the process.
	 */
	@RequestMapping(value = "/edit/process", method = RequestMethod.POST)
	public @ResponseBody String editprocess(ModelMap model, @RequestBody GWProcess up, WebRequest request){
		String resp = null;
		try {
			checkID(up.getId());
			pt.save(up);
			resp = "{\"id\" : \"" + up.getId() + "\"}";
		} catch (Exception e) {
			e.printStackTrace();
			// Handle exceptions and throw a runtime exception with an error message.
			throw new RuntimeException("failed " + e.getLocalizedMessage());
		}
		return resp;
	}

	/**
	 * Handles HTTP POST requests to edit a workflow.
	 *
	 * @param model    The model to add attributes to.
	 * @param w        The updated workflow data.
	 * @param request  The web request.
	 * @return A JSON response indicating the result of editing the workflow.
	 */
	@RequestMapping(value = "/edit/workflow", method = RequestMethod.POST)
	public @ResponseBody String editworkflow(ModelMap model, @RequestBody Workflow w, WebRequest request){
		String resp = null;
		try {
			checkID(w.getId());
			wt.save(w);
			resp = "{\"id\" : \"" + w.getId() + "\"}";
		} catch (Exception e) {
			e.printStackTrace();
			// Handle exceptions and throw a runtime exception with an error message.
			throw new RuntimeException("failed " + e.getLocalizedMessage());
		}
		return resp;
	}

	/**
	 * Handles HTTP POST requests to edit a host or history.
	 *
	 * @param model    The model to add attributes to.
	 * @param request  The web request.
	 * @return A JSON response indicating the result of editing the host or history.
	 */
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
				String confidential = request.getParameter("confidential");
				String url = request.getParameter("url");
				ht.update(hostid, hostname, hostip, hostport, username, hosttype, null, url, confidential);
				resp = "{ \"hostid\" : \"" + hostid + "\", \"hostname\" : \""+ hostname + "\" }";
			} else if(type.equals("history")) {
				String hisid = request.getParameter("id");
				String notes = request.getParameter("notes");
				hist.updateNotes(hisid, notes);
				resp = "{\"id\" : \"" + hisid + "\"}";
			}
		} catch (Exception e) {
			e.printStackTrace();
			// Handle exceptions and throw a runtime exception with an error message.
			throw new RuntimeException("failed " + e.getLocalizedMessage());
		}
		return resp;
	}

	/**
	 * Handles HTTP GET requests to retrieve a file.
	 *
	 * @param fileName  The name of the file to retrieve.
	 * @param response  The HTTP response object.
	 */
	@RequestMapping(value = "/file/{file_name}", method = RequestMethod.GET)
	public void getFile(@PathVariable("file_name") String fileName, HttpServletResponse response) {
		try {
			// Get your file as InputStream
			String fileloc = bt.getFileTransferFolder() + "/" + fileName;
			File my_file = new File(fileloc);
			InputStream is = new FileInputStream(my_file);

			// Copy it to the response's OutputStream
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
	
	/**
	 * Handles HTTP POST requests to retrieve a file from a remote host and save it in Geoweaver.
	 *
	 * @param model    The model to add attributes to.
	 * @param request  The web request.
	 * @param session  The HttpSession for user session management.
	 * @return A JSON response indicating the result of retrieving and saving the file.
	 */
	@RequestMapping(value = "/retrieve", method = RequestMethod.POST)
	public @ResponseBody String retrieve(ModelMap model, WebRequest request, HttpSession session){
		// Retrieve file from remote to Geoweaver
		String resp = null;
		try {
			String hid = request.getParameter("hostid");
			String encrypted = request.getParameter("pswd");
			String password = RSAEncryptTool.getPassword(encrypted, session.getId());
			String filepath = request.getParameter("filepath");
			resp = ft.scp_download(hid, password, filepath);
		} catch (Exception e) {
			e.printStackTrace();
			// Handle exceptions and throw a runtime exception with an error message.
			throw new RuntimeException("failed " + e.getLocalizedMessage());
		}
		return resp;
	}

	/**
	 * Handles HTTP POST requests to add a new process.
	 *
	 * @param model    The model to add attributes to.
	 * @param np       The new GWProcess data.
	 * @param request  The web request.
	 * @return A JSON response indicating the result of adding the process.
	 */
	@RequestMapping(value = "/add/process", method = RequestMethod.POST)
	public @ResponseBody String addProcess(ModelMap model, @RequestBody GWProcess np, WebRequest request){
		String resp = null;
		try {
			String ownerid = BaseTool.isNull(np.getOwner()) ? "111111" : np.getOwner();
			np.setOwner(ownerid);
			String newid = new RandomString(6).nextString();
			np.setId(newid);
			np.setCode(np.getCode());
			pt.save(np);
			resp = "{\"id\" : \"" + newid + "\", \"name\":\"" + np.getName() + "\", \"lang\": \"" + np.getDescription() + "\"}";
		} catch (Exception e) {
			e.printStackTrace();
			// Handle exceptions and throw a runtime exception with an error message.
			throw new RuntimeException("failed " + e.getLocalizedMessage());
		}
		return resp;
	}

	/**
	 * Handles HTTP POST requests to add a new host.
	 *
	 * @param model    The model to add attributes to.
	 * @param h        The Host data.
	 * @param request  The web request.
	 * @return A JSON response indicating the result of adding the host.
	 */
	@RequestMapping(value = "/add/host", method = RequestMethod.POST)
	public @ResponseBody String addHost(ModelMap model, Host h, WebRequest request){
		String resp = null;
		try {
			String ownerid = BaseTool.isNull(h.getOwner()) ? "111111" : h.getOwner();
			h.setOwner(ownerid);
			String newhostid = new RandomString(6).nextString();
			h.setId(newhostid);
			ht.save(h);
			resp = "{ \"id\" : \"" + h.getId() + "\", \"name\" : \"" + h.getName() + "\", \"type\": \"" + h.getType() + "\" }";
		} catch (Exception e) {
			e.printStackTrace();
			// Handle exceptions and throw a runtime exception with an error message.
			throw new RuntimeException("failed " + e.getLocalizedMessage());
		}
		return resp;
	}

	/**
	 * Handles HTTP POST requests to add a new workflow.
	 *
	 * @param model    The model to add attributes to.
	 * @param w        The Workflow data.
	 * @param request  The web request.
	 * @return A JSON response indicating the result of adding the workflow.
	 */
	@RequestMapping(value = "/add/workflow", method = RequestMethod.POST)
	public @ResponseBody String addWorkflow(ModelMap model, @RequestBody Workflow w, WebRequest request){
		String resp = null;
		try {
			String ownerid = BaseTool.isNull(w.getOwner()) ? "111111" : w.getOwner();
			w.setOwner(ownerid);
			String newwid = new RandomString(20).nextString();
			w.setId(newwid);
			wt.save(w);
			resp = "{\"id\" : \"" + newwid + "\", \"name\":\"" + w.getName() + "\"}";
		} catch (Exception e) {
			e.printStackTrace();
			// Handle exceptions and throw a runtime exception with an error message.
			throw new RuntimeException("failed " + e.getLocalizedMessage());
		}
		return resp;
	}

	/**
	 * Handles HTTP POST requests to add various types of entities (host, process, workflow).
	 *
	 * @param model    The model to add attributes to.
	 * @param request  The web request.
	 * @return A JSON response indicating the result of adding the entity.
	 */
	@RequestMapping(value = "/add", method = RequestMethod.POST)
	public @ResponseBody String add(ModelMap model, WebRequest request){
		String resp = null;
		try {
			String type = request.getParameter("type");
			if (type.equals("host")) {
				String hostname = request.getParameter("hostname");
				String hostip = request.getParameter("hostip");
				String hostport = request.getParameter("hostport");
				String username = request.getParameter("username");
				String hosttype = request.getParameter("hosttype");
				String url = request.getParameter("url");
				String confidential = request.getParameter("confidential");
				String ownerid = BaseTool.isNull(request.getParameter("ownerid")) ? "111111" : request.getParameter("ownerid");
				String hostid = ht.add(hostname, hostip, hostport, username, url, hosttype, ownerid, confidential);
				resp = "{ \"id\" : \"" + hostid + "\", \"name\" : \"" + hostname + "\", \"type\": \"" + hosttype + "\" }";
			} else if (type.equals("process")) {
				String lang = request.getParameter("lang");
				String name = request.getParameter("name");
				String desc = request.getParameter("desc");
				String ownerid = BaseTool.isNull(request.getParameter("ownerid")) ? "111111" : request.getParameter("ownerid");
				String confidential = request.getParameter("confidential");
				String code = null;
				if (lang.equals("shell")) {
					code = request.getParameter("code");
				} else if (lang.equals("builtin")) {
					String operation = request.getParameter("code[operation]");
					code = "{ \"operation\" : \"" + operation + "\", \"params\":[";
					List params = new ArrayList();
					int i = 0;
					while (request.getParameter("code[params][" + i + "][name]") != null) {
						if (i != 0) {
							code += ", ";
						}
						code += "{ \"name\": \"" + request.getParameter("code[params][" + i + "][name]") + "\", \"value\": \"" + request.getParameter("code[params][" + i + "][value]") + "\" }";
						i++;
					}
					code += "] }";
				} else if (lang.equals("jupyter")) {
					code = request.getParameter("code");
				} else {
					code = request.getParameter("code");
				}
				String pid = pt.add(name, lang, code, desc, ownerid, confidential);
				resp = "{\"id\" : \"" + pid + "\", \"name\":\"" + name + "\", \"lang\": \"" + lang + "\"}";
			} else if (type.equals("workflow")) {
				String name = request.getParameter("name");
				String nodes = request.getParameter("nodes");
				String edges = request.getParameter("edges");
				String ownerid = BaseTool.isNull(request.getParameter("ownerid")) ? "111111" : request.getParameter("ownerid");
				String wid = wt.add(name, nodes, edges, ownerid);
				resp = "{\"id\" : \"" + wid + "\", \"name\":\"" + name + "\"}";
			}
		} catch (Exception e) {
			e.printStackTrace();
			// Handle exceptions and throw a runtime exception with an error message.
			throw new RuntimeException("failed " + e.getLocalizedMessage());
		}
		return resp;
	}

	/**
	 * Handles HTTP GET requests to display an SSH terminal.
	 *
	 * @param model    The model to add attributes to.
	 * @param request  The web request.
	 * @param status   The session status.
	 * @param session  The HttpSession for user session management.
	 * @return The view to display the SSH terminal.
	 */
	@RequestMapping(value = "/geoweaver-ssh", method = RequestMethod.GET)
	public String sshterminal(ModelMap model, WebRequest request, SessionStatus status, HttpSession session){
		String token = request.getParameter("token");
		logger.debug("token : " + token);
		String resp = "redirect:geoweaver-ssh-login";
		// Validate the token
		if (token != null) {
			SSHSession ss = sessionManager.sshSessionByToken.get(token);
			if (ss != null) {
				model.addAttribute("host", ss.getHost());
				model.addAttribute("username", ss.getUsername());
				model.addAttribute("port", ss.getPort());
				model.addAttribute("token", ss.getToken());
				resp = "geoweaver-ssh";
			}
		}
		return resp;
	}
	
	/**
	 * Handles HTTP POST requests to close an SSH session inbox.
	 *
	 * @param model   The model to add attributes to.
	 * @param request The web request.
	 * @param session The HttpSession for user session management.
	 * @return A JSON response indicating the result of closing the SSH session inbox.
	 */
	@RequestMapping(value = "/geoweaver-ssh-logout-inbox", method = RequestMethod.POST)
	public @ResponseBody String ssh_close_inbox(Model model, WebRequest request, HttpSession session){
		String resp = "";
		try {
			String token = request.getParameter("token");
			if (token != null) {
				SSHSession s = sessionManager.sshSessionByToken.get(token);
				if (s != null) {
					s.logout();
					sessionManager.sshSessionByToken.remove(token);
				}
			}
			resp = "done";
		} catch (Exception e) {
			e.printStackTrace();
			// Handle exceptions and throw a runtime exception.
			throw new RuntimeException();
		}
		return resp;
	}

	/**
	 * Handles HTTP POST requests to authenticate an SSH session inbox.
	 *
	 * @param model   The model to add attributes to.
	 * @param request The web request.
	 * @param session The HttpSession for user session management.
	 * @return A JSON response containing the authentication token for the SSH session inbox.
	 */
	@RequestMapping(value = "/geoweaver-ssh-login-inbox", method = RequestMethod.POST)
	public @ResponseBody String ssh_auth_inbox(Model model, WebRequest request, HttpSession session){
		String resp = "";
		try {
			String host = request.getParameter("host");
			String port = request.getParameter("port");
			String username = request.getParameter("username");
			String encrypted = request.getParameter("password");
			String password = RSAEncryptTool.getPassword(encrypted, session.getId());
			String token = session.getId(); // Use session id as token
			boolean success = sshSession.login(host, port, username, password, token, true);
			logger.debug("SSH login: " + username + "=" + success);
			logger.debug("Adding SSH session for " + username);
			sessionManager.sshSessionByToken.put(token, sshSession); // Token is session id
			resp = "{\"token\": \"" + token + "\"}";
		} catch (Exception e) {
			e.printStackTrace();
			// Handle exceptions and throw a runtime exception with an error message.
			throw new RuntimeException(e.getLocalizedMessage());
		}
		return resp;
	}

	/**
	 * Handles HTTP POST requests to authenticate an SSH session.
	 *
	 * @param model   The model to add attributes to.
	 * @param request The web request.
	 * @param session The HttpSession for user session management.
	 * @return A redirection response to the SSH terminal view.
	 */
	@RequestMapping(value = "/geoweaver-ssh-login", method = RequestMethod.POST)
	public String ssh_auth(Model model, WebRequest request, HttpSession session){
		String resp = "redirect:geoweaver-ssh";
		try {
			String host = request.getParameter("host");
			String port = request.getParameter("port");
			String username = request.getParameter("username");
			String password = request.getParameter("password");
			String token = null;
			if (BaseTool.isNull(sessionManager.sshSessionByToken.get(host + "-" + username))) {
				token = new RandomString(16).nextString();
				boolean success = sshSession.login(host, port, username, password, token, true);
				logger.debug("SSH login: " + username + "=" + success);
				logger.debug("Adding SSH session for " + username);
				sessionManager.sshSessionByToken.put(token, sshSession);
			}
			model.addAttribute("host", host);
			model.addAttribute("username", username);
			model.addAttribute("port", port);
			model.addAttribute("token", token);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}
    
    /**
	 * Handles HTTP GET requests to display the SSH login page.
	 *
	 * @param model   The model to add attributes to.
	 * @param request The web request.
	 * @param session The HttpSession for user session management.
	 * @return The name of the view to render (SSH login page).
	 */
	@RequestMapping(value = "/geoweaver-ssh-login", method = RequestMethod.GET)
	public String ssh_login(Model model, WebRequest request, HttpSession session){
		String resp = "geoweaver-ssh-login";
		return resp;
	}

	/**
	 * Handles HTTP requests for error pages.
	 *
	 * @return The name of the view to render (error page).
	 */
	@RequestMapping("/error")
	public String handleError() {
		// Perform error-related tasks such as logging
		return "error";
	}

	/**
	 * Checks if an ID is null or empty and throws a runtime exception if it is.
	 *
	 * @param id The ID to check.
	 * @throws RuntimeException if the ID is null or empty.
	 */
	void checkID(String id) {
		if (BaseTool.isNull(id))
			throw new RuntimeException("No ID found");
	}

}
