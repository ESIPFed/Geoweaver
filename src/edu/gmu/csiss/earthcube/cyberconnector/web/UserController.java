package edu.gmu.csiss.earthcube.cyberconnector.web;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import edu.gmu.csiss.earthcube.cyberconnector.search.*;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.context.request.WebRequest;

import edu.gmu.csiss.earthcube.cyberconnector.ncwms.ncWMSTool;
import edu.gmu.csiss.earthcube.cyberconnector.order.Order;
import edu.gmu.csiss.earthcube.cyberconnector.order.OrderTool;
import edu.gmu.csiss.earthcube.cyberconnector.products.DeleteProductTool;
import edu.gmu.csiss.earthcube.cyberconnector.products.LikeProductTool;
import edu.gmu.csiss.earthcube.cyberconnector.products.Product;
import edu.gmu.csiss.earthcube.cyberconnector.products.RetrieveProductTool;
import edu.gmu.csiss.earthcube.cyberconnector.services.QueryServiceTool;
import edu.gmu.csiss.earthcube.cyberconnector.services.RegisterServiceTool;
import edu.gmu.csiss.earthcube.cyberconnector.services.Service;
import edu.gmu.csiss.earthcube.cyberconnector.tools.LocalFileTool;
import edu.gmu.csiss.earthcube.cyberconnector.tools.PlaceOrderTool;
import edu.gmu.csiss.earthcube.cyberconnector.user.User;
import edu.gmu.csiss.earthcube.cyberconnector.user.UserTool;
import edu.gmu.csiss.earthcube.cyberconnector.utils.BaseTool;
import edu.gmu.csiss.earthcube.cyberconnector.utils.Message;
import edu.gmu.csiss.earthcube.cyberconnector.utils.RandomString;
import edu.gmu.csiss.earthcube.cyberconnector.utils.SysDir;


/**
*Class UserController.java
*@author Ziheng Sun
*@time Jan 26, 2017 6:02:12 PM
*Original aim is to support CyberConnector.
*/	
@Controller 
//@RequestMapping(value="/")     
@SessionAttributes({"sessionUser"})
public class UserController { 

    // Thread safe map
    static Map<String, User> users = Collections.synchronizedMap(new HashMap<String, User>()); 
    
    Logger logger = Logger.getLogger(this.getClass());
    
    @RequestMapping(value = "/regser", method = RequestMethod.GET)
    public String regser(ModelMap model, WebRequest request, SessionStatus status, HttpSession session){
    	
    	String name = (String)session.getAttribute("sessionUser");
    	
    	String resp = "regser";
    	
    	if(name == null){
    		
    		resp = "redirect:login";
    		
    	}else{
    		
//    		model.addAttribute("username", name);
    		
    	}
    	
    	return resp;
    	
    }
    
    @RequestMapping(value = "/servicesearch", method = RequestMethod.GET)
    public String servicesearch(ModelMap model, WebRequest request, SessionStatus status, HttpSession session){
    	
    	String name = (String)session.getAttribute("sessionUser");
    	
    	String resp = "servicesearch";
    	
    	//revise by Z. June 8 2017
    	//service search requires no log in
    	
//    	if(name == null){
//    		
//    		resp = "redirect:login";
//    		
//    	}else{
    		
    		List<Service> sers = QueryServiceTool.retrieveAll();
    		
    		model.addAttribute("services", sers);
    		
//    	}
    	
    	return resp;
    	
    }
    
    @RequestMapping(value = "/checklog", method = RequestMethod.POST)
    public @ResponseBody String checklogin(ModelMap model, SessionStatus status, HttpSession session){
    	
    	String resp = null;
    	
    	String name = (String)session.getAttribute("sessionUser");
    	
    	if(name==null){
    		
    		resp = "{ \"login\": false }";
    		
    	}else{
    	
    		resp = "{ \"login\": true }";
    		
    	}
    	
    	return resp;
    	
    }
    
    
    @RequestMapping(value = "/updateservice", method = RequestMethod.POST)
    public @ResponseBody String updateService(ModelMap model, WebRequest request, SessionStatus status, HttpSession session){
    	
    	String name = (String)session.getAttribute("sessionUser");
    	
    	String resp = ""; //number of likes 
    	
    	logger.debug("Service update request received.");
    	
    	if(name == null){
    		
    		resp = "redirect:login";
    		
    	}else{
    		
    		String sid = request.getParameter("serviceid");
    		
    		logger.debug("service id is : " + sid);
    		
    		RegisterServiceTool tool  = new RegisterServiceTool();
    		
    		try{
    		
    			tool.updateWSDL(sid);

        		System.out.print("Server response is : " + resp);
        		
        		resp = "Done";
        		
    		}catch(Exception e){
    			
    			throw new RuntimeException( "Failed. " + e.getLocalizedMessage());
    			
    		}
    		
    		
    	}
    	
    	return resp;
    	
    }
    
    @RequestMapping(value = "/productlike", method = RequestMethod.POST)
    public @ResponseBody String productlike(ModelMap model, WebRequest request, SessionStatus status, HttpSession session){
    	
    	String name = (String)session.getAttribute("sessionUser");
    	
    	String resp = ""; //number of likes 
    	
    	logger.debug("Like request received.");
    	
    	if(name == null){
    		
    		resp = "redirect:login";
    		
    	}else{
    		
    		String productid = request.getParameter("pid");
    		
    		logger.debug("product id is : " + productid);
    		
    		int newlikes = LikeProductTool.like(productid);
    		
    		resp = "{ \"likes\" : \""+newlikes+"\" }";
    		
    		System.out.print("Server response is : " + resp);
    		
    	}
    	
    	return resp;
    	
    }
    
    
    @RequestMapping(value = "/productsearch", method = RequestMethod.GET)
    public String productsearchpage(ModelMap model, WebRequest request, SessionStatus status, HttpSession session){
    	
    	String name = (String)session.getAttribute("sessionUser");
    	
    	String resp = "productsearch";
    	
//    	if(name == null){
//    		
//    		resp = "redirect:login";
//    		
//    	}else{
    		
    		model.addAttribute("request", new SearchRequest());
    		
//    	}
    	
    	return resp;
    	
    }

    
    /**
     * User Logout Post Process
     * @param user
     * @param result
     * @return
     */
    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    public String logout( ModelMap model,  WebRequest request, SessionStatus status, HttpSession session) {
    	
        Message msg = UserTool.logout((String)session.getAttribute("sessionUser"));
        
        String resp = "message";
        
        if(msg.isIsdone()){
        	
        	//session.setAttribute("sessionUser", null);
        	
        	//session.removeAttribute("sessionUser");
        	
        	status.setComplete();
        	
            request.removeAttribute("sessionUser", WebRequest.SCOPE_SESSION);
        	
        	msg.setTitle("Logout Successful!");
        	
        	msg.setStrongmsg("your information is safe with us");
        	
        	msg.setDisplaymsg("You are now signed out. Thanks for using CyberConnector. ");
        	
        	model.addAttribute("message", msg);
        	
        	model.addAttribute("forwardURL", "index");
        	
        	model.addAttribute("forward", "redirect to main page");
        	
        }else{

        	msg.setTitle("Logout Failed");
        	
        	msg.setStrongmsg("your information is safe with us");
        	
        	msg.setDisplaymsg("Something goes wrong. Contact our webmaster (zsun@gmu.edu) ");
        	
        	model.addAttribute("message", msg);
        	
        	model.addAttribute("forwardURL", "index");
        	
        	model.addAttribute("forward", "redirect to main page");
        	
        }
        
        logger.debug("Current user: " + (String)session.getAttribute("sessionUser"));
        
        return resp;
    }
    
    /**
     * User Login Get Process
     * @param model
     * @return
     */
    @RequestMapping(value = "/login", method = RequestMethod.GET) 
    public String displayLogin(Model model, HttpSession session) { 
    	
    	String name = (String)session.getAttribute("sessionUser");
    	
    	String resp = null;
    	
    	if(name == null){
    		
    		model.addAttribute("user", new User());
    		
    		resp = "login";
    		
    	}else{
    		
    		resp = "redirect:user_profile";
    		
    	}
    	
         
        return resp; 
    }
   
    
    @RequestMapping(value = "/owner", method = RequestMethod.GET)
    public @ResponseBody String checkowner_get(ModelMap model, WebRequest request,   HttpSession session){
    	
    	String resp = null;
    	
    	resp = "{ \"ret\" : \"true\"}";
    		
    	return resp;
    	
    	
    }
    
    /**
     * Check if the user is the owner of a product
     * Z. 7/13/2017
     * @param model
     * @param request
     * @param session
     * @return
     */
    @RequestMapping(value = "/owner", method = RequestMethod.POST)
    public @ResponseBody String checkowner(ModelMap model, WebRequest request,   HttpSession session){
    	
    	String resp = null;
    	
    	String userid = request.getParameter("uid");
    	
    	String modelid = request.getParameter("mid");
    	
    	boolean yes = UserTool.checkOwner(userid, modelid);
    	
    	resp = "{ \"ret\" : \""+yes+"\"}";
    		
    	return resp;
    	
    	
    }
    
    @RequestMapping(value = "/auth", method = RequestMethod.POST)
    public @ResponseBody String authenticate( ModelMap model, WebRequest request,   HttpSession session) {
    	
    	String resp = null;
    	
    	String username = request.getParameter("name");
    	
    	String password = request.getParameter("password");
    	
    	User user = new User();
    	
    	user.setName(username);
    	
    	user.setPassword(password);
    	
    	Message msg = UserTool.login(user);
    	
    	if(msg.isIsdone()){
    		
    		String uid = UserTool.getUserIDByName(username);
    		
    		resp = "{ \"ret\" : \"true\", \"uid\":\""+uid+"\" }";
    		
    	}else{
    		
    		resp = "{ \"ret\" : \"false\" }";
    		
    	}
    	
    	return resp;
    	
    }
    	
    
    
    /**
     * User Login Post Process
     * @param user
     * @param result
     * @return
     */
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String login(    @ModelAttribute("user") User user, BindingResult result, ModelMap model, HttpSession session) {
    	
        logger.debug("Display Name on the Profile Page "+ user.getName()+ "\n" );
        
        Message msg = UserTool.login(user);
        
        String resp = null;
        
        if(msg.isIsdone()){
        	
        	session.setAttribute("sessionUser", user.getName());
        	
        	logger.debug("SessionId is: " + session.getId());
        	
        	user = UserTool.retrieveInformation(user.getName());
        	
        	model.addAttribute("user", user);
        	
        	resp = "redirect:user_profile";
        	
        }else{
        	
        	msg.setTitle("Fail to login");
			
			msg.setStrongmsg("Reason: " + msg.getInformation());
        	
        	model.addAttribute("forwardURL", "login");
        	
        	model.addAttribute("forward", "redirect to login page");
        	
        	model.addAttribute("message", msg);
        	
        	resp = "message";
        	
        }
        
        logger.debug("Response page is: " + resp);
        
        return resp;
    }
    
    @RequestMapping(value = "/user_edit", method = RequestMethod.POST) 
    public String edit(@ModelAttribute("user") User user, BindingResult result, Model model, HttpSession session) {
    	
    	String name = (String)session.getAttribute("sessionUser");
    	
    	String resp = "message";
    	
    	if(name == null){
    		
    		resp = "redirect:login";
    		
    	}else{
    		
    		user.setName(name);
    		
    		Message msg = UserTool.updateExistingUser(user);
    		
    		if(msg.isIsdone()){
    			
    			msg.setTitle("Updated!!");
            	
            	msg.setStrongmsg("Your new information has been recorded. Old information has been erased.");
            	
            	model.addAttribute("forwardURL", "index");
            	
            	model.addAttribute("forward", "redirect to main page");
            	
    		}else{
    			
    			msg.setTitle("Oops!!!Fail to update your information.");
    			
    			msg.setStrongmsg("Very sorry about that. The reason seems to be " + msg.getInformation());
            	
            	model.addAttribute("forwardURL", "user_edit");
            	
            	model.addAttribute("forward", "redirect to edit page");
    			
    		}
    		
    		model.addAttribute("message", msg);
        	
    	}
         
        return resp; 
    }
    
    /**
     * User Registration Get Process
     * @param model
     * @return
     */
    @RequestMapping(value = "/user_edit", method = RequestMethod.GET) 
    public String edit(Model model, HttpSession session) {
    	
    	String name = (String)session.getAttribute("sessionUser");
    	
    	String resp = "user_edit";
    	
    	if(name == null){
    		
    		resp = "redirect:login";
    		
    	}else{
    	
    		User u = UserTool.retrieveInformation(name);
    		
    		model.addAttribute("user", u);
    		
    	}
         
        return resp; 
    }
    
    /**
     * User Registration Get Process
     * @param model
     * @return
     */
    @RequestMapping(value = "/user_register", method = RequestMethod.GET) 
    public String register(Model model) { 
        model.addAttribute("user", new User());
        
        Message msg = new Message("", "", "", false);
        model.addAttribute("message", msg);
        
        return "user_register"; 
    }
    /**
     * User Registration Post Process
     * @param user
     * @param result
     * @return
     */
    @RequestMapping(value = "/user_register", method = RequestMethod.POST)
    public String register(    @ModelAttribute("user") User user, BindingResult result, Model model) {
    	
        logger.debug(" Name entered "+ user.getName()+ "\n" );
        
        logger.debug(" Password entered " + user.getPassword() + "\n");
        
        Message msg = UserTool.registerNewUser(user);
        
        String resp = null;

        if(msg.isIsdone()){
        	
        	resp = "user_profile";
        	
        }
        else  {
        	
        	model.addAttribute("message", msg);
        	
        	resp = "user_register";
        	
        }
        
        return resp;
    }
    
    @RequestMapping(value = "/vieworder", method = RequestMethod.POST)
    public @ResponseBody String  vieworder(Model model, WebRequest request, SessionStatus status, HttpSession session){
    	
    	String orderdetailsjson = null;
    	
    	String name = (String)session.getAttribute("sessionUser");
    	
    	if(name == null){
    		
    		orderdetailsjson = "{'error': 'You have to login first.'}";
    		
    	}else{
    	
    		String orderid = request.getParameter("orderid");
    		
    		try{
    			
    			orderdetailsjson = OrderTool.getOrderById(orderid).toJSON();
    			
    			logger.debug(orderdetailsjson);
    			    			
    		}catch(Exception e){
    			
//    			e.printStackTrace();
    			logger.error(e.getLocalizedMessage());
    			
    			orderdetailsjson = "{'error' : '"+e.getLocalizedMessage()+"'}";
    			
    		}
    	
    	}
    	
    	return orderdetailsjson;
    }
    
    @RequestMapping(value = "/deleteproduct", method = RequestMethod.POST)
    public String  deleteproduct(Model model, WebRequest request, SessionStatus status, HttpSession session){
    	
    	String name = (String)session.getAttribute("sessionUser");
    	
    	String resp = "message";
    	
    	if(name == null){
    		
    		resp = "redirect:login";
    		
    	}else{
    	
    		String productid = request.getParameter("productid");
    		
    		try{
    			
    			//OrderTool.deleteOrder(orderid);
    			DeleteProductTool.delete(productid);
    			
    			model.addAttribute("show", "tab3");
    			
    			resp = "redirect:user_profile";
    			
    		}catch(Exception e){
    			
    			Message msg = new Message("deleteorder", "browser", "failed", false);
    			
    			msg.setTitle("Delete Service Failed");
            	
            	msg.setStrongmsg("Oops!! We are unable to delete this service at this moment.");
            	
            	msg.setDisplaymsg("Something goes wrong. "+e.getLocalizedMessage()+"Contact our webmaster (zsun@gmu.edu) ");
            	
            	model.addAttribute("message", msg);
            	
            	model.addAttribute("forwardURL", "index");
            	
            	model.addAttribute("forward", "redirect to main page");
    			
    		}
    	
    	}
    	
    	return resp;
    }
    
    @RequestMapping(value = "/deleteservice", method = RequestMethod.POST)
    public String  deleteservice(Model model, WebRequest request, SessionStatus status, HttpSession session){
    	
    	String name = (String)session.getAttribute("sessionUser");
    	
    	String resp = "message";
    	
    	if(name == null){
    		
    		resp = "redirect:login";
    		
    	}else{
    	
    		String serviceid = request.getParameter("serviceid");
    		
    		try{
    			
    			//OrderTool.deleteOrder(orderid);
    			RegisterServiceTool.deleteService(serviceid);
    			
    			model.addAttribute("show", "tab3");
    			
    			resp = "redirect:user_profile";
    			
    		}catch(Exception e){
    			
    			Message msg = new Message("deleteorder", "browser", "failed", false);
    			
    			msg.setTitle("Delete Service Failed");
            	
            	msg.setStrongmsg("Oops!! We are unable to delete this service at this moment.");
            	
            	msg.setDisplaymsg("Something goes wrong. "+e.getLocalizedMessage()+"Contact our webmaster (zsun@gmu.edu) ");
            	
            	model.addAttribute("message", msg);
            	
            	model.addAttribute("forwardURL", "index");
            	
            	model.addAttribute("forward", "redirect to main page");
    			
    		}
    	
    	}
    	
    	return resp;
    }
    
    @RequestMapping(value = "/deleteorder", method = RequestMethod.POST)
    public String  deleteorder(Model model, WebRequest request, SessionStatus status, HttpSession session){
    	
    	String name = (String)session.getAttribute("sessionUser");
    	
    	String resp = "message";
    	
    	if(name == null){
    		
    		resp = "redirect:login";
    		
    	}else{
    	
    		String orderid = request.getParameter("orderid");
    		
    		try{
    			
    			OrderTool.deleteOrder(orderid);
    			
    			model.addAttribute("show", "tab2");
    			
    			resp = "redirect:user_profile";
    			
    		}catch(Exception e){
    			
    			Message msg = new Message("deleteorder", "browser", "failed", false);
    			
    			msg.setTitle("Delete Order Failed");
            	
            	msg.setStrongmsg("Oops!! We are unable to delete this order at this moment.");
            	
            	msg.setDisplaymsg("Something goes wrong. "+e.getLocalizedMessage()+"Contact our webmaster (zsun@gmu.edu) ");
            	
            	model.addAttribute("message", msg);
            	
            	model.addAttribute("forwardURL", "index");
            	
            	model.addAttribute("forward", "redirect to main page");
    			
    		}
    	
    	}
    	
    	return resp;
    }
    
    @RequestMapping(value = "/reorder", method = RequestMethod.POST) 
    public @ResponseBody String reorder(Model model, WebRequest request, SessionStatus status, HttpSession session) { 
    	
    	String name = (String)session.getAttribute("sessionUser");
    	
    	String resp =  null;
    	
    	if(name == null){
    		
    		resp = "{\"error\":\"you need log in first.\"}";
    		
    	}else{
    		
    		logger.info("receive the reorder request");
    		
    		String oid = request.getParameter("oid");
    		
    		Order o = OrderTool.getOrderById(oid);
    		
    		Map parametermap = o.getParametermap();

    		parametermap.put("email", o.getMail());
    		
    		parametermap.put("userid", o.getUserid());
    		
    		PlaceOrderTool t = new PlaceOrderTool();
    		
			String orderid = t.placeOrder(parametermap);
			
			resp = "{\"oid\": \""+orderid+"\"}";
    		
    	}
    	
    	return resp;
    	
    }
    
    /**
     * Place order
     * @param order
     * @param result
     * @param model
     * @param request
     * @param status
     * @param session
     * @return
     */
    @RequestMapping(value = "/placeorder", method = RequestMethod.POST) 
    public String placeorder(Model model, WebRequest request, SessionStatus status, HttpSession session) { 
    	
    	String name = (String)session.getAttribute("sessionUser");
    	
    	String resp = "message";
    	
    	if(name == null){
    		
    		resp = "redirect:login";
    		
    	}else{
    		
    		try{
    			
    			Map<String, String[]> pmap = request.getParameterMap();
    			
        		Map<String, String> parametermap = new HashMap();
    			
        		Iterator it = pmap.entrySet().iterator();
    		    
        		while (it.hasNext()) {
    		    
        			Map.Entry pair = (Map.Entry)it.next();
    		        
        			logger.debug(pair.getKey() + " = " + ((String[])pair.getValue())[0]);
    		        
        			parametermap.put((String)pair.getKey(), ((String[])pair.getValue())[0]);
    		    
        		}
        		
        		//get email by username
        		
        		User u = UserTool.retrieveInformation(name);
        		
        		parametermap.put("email", u.getEmail());
        		
        		parametermap.put("userid", u.getId());
        		
        		PlaceOrderTool t = new PlaceOrderTool();
        		
    			String orderid = t.placeOrder(parametermap);
        		
        		Message msg = new Message("placeorder", "browser", "success", true);
        		
        		msg.setTitle("We have received your order. The processing starts right now..");
            	
            	msg.setStrongmsg("Congratulations! Your order id is " + orderid + ". A notification e-mail has already been sent to your account.");
            	
            	msg.setDisplaymsg("Your request has been heard and we will take care of it.");
            	
            	model.addAttribute("message", msg);
            	
            	model.addAttribute("forwardURL", "user_profile");
            	
            	model.addAttribute("forward", "redirect to your profile");
        		
    		}catch(Exception e){
    			
    			e.printStackTrace();
    			
    			Message msg = new Message("placeorder", "browser", "failure", false);
        		
        		msg.setTitle("Something Happens");
            	
            	msg.setStrongmsg("Sorry, the order is unsuccessful. Please return to the order page and place the order again or contact the webmaster.");
            	
            	msg.setDisplaymsg("Your request has not been submitted yet.");
            	
            	model.addAttribute("message", msg);
            	
            	model.addAttribute("forwardURL", "history.go(-2)");
            	
            	model.addAttribute("forward", "redirect to order page");
    			
    		}
    		
    		
    	}
    	
        return resp; 
    }
    
    /**
     * Order a product
     * @param model
     * @return
     */
    @RequestMapping(value = "/productorder", method = RequestMethod.GET) 
    public String orderproduct(@RequestParam("pid") String productid, Model model,  WebRequest request, SessionStatus status, HttpSession session) { 
    	
    	String name = (String)session.getAttribute("sessionUser");
    	
    	String resp = "productorder";
    	
    	if(name == null){
    		
    		resp = "redirect:login";
    		
    	}else{
    		
    		Product p = RetrieveProductTool.retrieveProduct(productid);
    		
    		//if there are parameter value pairs, absorb the valid values into the product object
    		RetrieveProductTool.absorbParameterValuePairs(p, request.getParameterMap());
    		
    		model.addAttribute("product", p); 
    		
            model.addAttribute("order", new Order()); 
    		
    	}
    	
        return resp; 
    }
    
    /**
     * Forget Password Get Process
     * @param model
     * @return
     */
    @RequestMapping(value = "/user_forget", method = RequestMethod.GET) 
    public String forget(Model model) { 
        model.addAttribute("user", new User()); 
        return "user_forget"; 
    }
    
    
    
    /**
     * Forget Password Post Process
     * @param user
     * @param result
     * @return
     */
    @RequestMapping(value = "/user_forget", method = RequestMethod.POST)
    public String forget(    @ModelAttribute("user") User user, BindingResult result, Model model) {
        logger.debug(" Name entered "+ user.getEmail()+ "\n" );
        
        Message msg = null;
     
        if (UserTool.checkUserExist(user)) {
        	
        	UserTool.passwordResetEmail(user);
        	
    		msg = new Message("user_forget", "browser", "success", true);
    		
    		msg.setTitle("A password reset email has been sent to the given e-mail. Please check your email account.");
        	
        	msg.setStrongmsg("If this request is not submitted in your intention, please ignore the email and contact the administrator.");
        	
        	msg.setDisplaymsg("Your request has been heard and we will take care of it.");
        	
        	model.addAttribute("message", msg);
        	
        	model.addAttribute("forwardURL", "login");
        	
        	model.addAttribute("forward", "redirect to your profile");
        }
        else {
        	
    		msg = new Message("user_forget", "browser", "failed", false);
    		
    		msg.setTitle("The given email address cannot be found.");
        	
        	msg.setStrongmsg("Please enter your registered email.");
        	
        	msg.setDisplaymsg("Fail to complete the request, please try again.");
        	
        	model.addAttribute("message", msg);
        	
        	model.addAttribute("forwardURL", "login");
        	
        	model.addAttribute("forward", "redirect to your profile");
        	
        }
        
        return "message";
    }
    
    
    
    
    /**
     * User Profile
     * @param model
     * @return
     */
    @RequestMapping(value = "/user_profile", method = RequestMethod.GET) 
    public String userprofile(Model model, WebRequest request, HttpSession session) { 
        
    	//get user information by session user name
    	
    	String username = (String)session.getAttribute("sessionUser");
    	
    	String resp = "";
    	
    	if(username == null){
    		
    		resp = "redirect:login";
    		
    	}else{
    		
    		logger.debug("Get User Profile : \n SessionId is: " + session.getId());
        	
        	User u = UserTool.retrieveInformation(username);
        	
        	model.addAttribute("user", u);
        	
        	String tabtag = request.getParameter("show");
        	
        	if(tabtag != null){
        		
            	model.addAttribute("fronttab", tabtag);
            	
        	}
        	
    		resp = "user_profile";
        	
    	}
    	
        return resp; 
        
    }
    /**
     * User Profile
     * @param user   
     * @param result
     * @return
     */
    @RequestMapping(value = "/user_profile", method = RequestMethod.POST)
    public String userprofile(    @ModelAttribute("user") User user, BindingResult result, ModelMap model) {
    	
    	user = UserTool.retrieveInformation(user.getName());
    	
    	logger.debug("User Profile Post : User Information is retrieved.");
    	
    	model.addAttribute("user", user); 
        
    	return "user_profile";
    }


       
    /**
     * New Service
     * @param model
     * @return
     */
    @RequestMapping(value = "/newservice", method = RequestMethod.GET) 
    public String newservice(Model model, HttpSession session) { 
        
        //get user information by session user name
        
        String username = (String)session.getAttribute("sessionUser");
        logger.debug("New Service: Username: " + username);
        
        String resp = "";
        
        if(username == null){
            
        	logger.debug("New Service: Redirect to login");
        			
            resp = "redirect:login";
            
        }else{
            
            logger.debug("New Service: Get User Profile : \n SessionId is: " + session.getId());
            
            Service s = new Service();
            
            model.addAttribute("service", s); 
            
            resp = "newservice";            
        }
        
        return resp; 
        
    }
    /**
     * New Service
     * @param service   
     * @param result
     * @return
     */
    @RequestMapping(value = "/newservice", method = RequestMethod.POST)
    public String newservice(@ModelAttribute("service") Service service, @ModelAttribute("user") User user, HttpSession session, BindingResult result, ModelMap model) {
        
    	String username = (String)session.getAttribute("sessionUser");
    	User u = UserTool.retrieveInformation(username);    	
    	
        logger.debug("New Service: Service Name entered "+ service.getName()+ " userID:" + u.getId() + "\n" );
                
    	Message msg = RegisterServiceTool.registerWPS(service, u.getId());
        
        model.addAttribute("service", service); 
        
        String resp = null;

        if(msg.isIsdone()){        	
        	resp = "user_profile";        	
        }else{        	
        	resp = "newservice";
        }
        
        return resp;
    }
    
    /**
     * User Registration Get Process
     * @param model
     * @return
     */
    @RequestMapping(value = "/user_password", method = RequestMethod.GET) 
    public String password(Model model, HttpSession session) {
    	
    	String name = (String)session.getAttribute("sessionUser");
    	
    	String resp = "user_password";
    	
    	if(name == null){
    		
    		resp = "redirect:login";
    		
    	}else{
    	
    		User u = UserTool.retrieveInformation(name);
    		
    		model.addAttribute("user", u);
    		
    	}
         
        return resp; 
    }
    
    @RequestMapping(value = "/user_password", method = RequestMethod.POST) 
    public String password(@ModelAttribute("user") User user, BindingResult result, Model model, HttpSession session) {
    	
    	String name = (String)session.getAttribute("sessionUser");
    	
    	String resp = "message";
    	
    	if(name == null){
    		
    		resp = "redirect:login";
    		
    	}else{
    		
    		user.setName(name);
    		
    		Message msg = UserTool.updateExistingUser(user);
    		
    		if(msg.isIsdone()){
    			
    			msg.setTitle("Updated!!");
            	
            	msg.setStrongmsg("Your new information has been recorded. Old information has been erased.");
            	
            	model.addAttribute("forwardURL", "index");
            	
            	model.addAttribute("forward", "redirect to main page");
            	
    		}else{
    			
    			msg.setTitle("Oops!!!Fail to update your information.");
    			
    			msg.setStrongmsg("Very sorry about that. The reason seems to be " + msg.getInformation());
            	
            	model.addAttribute("forwardURL", "user_edit");
            	
            	model.addAttribute("forward", "redirect to edit page");
    			
    		}
    		
    		model.addAttribute("message", msg);
        	
    	}
         
        return resp; 
    }
    
    /**
     * User Registration Get Process
     * @param model
     * @return
     */
    @RequestMapping(value = "/user_setpassword", method = RequestMethod.GET) 
    public String setpassword(Model model, WebRequest request, HttpSession session) {
    	
    	String token = (String)request.getParameter("token");
    	
    	String resp = "user_setpassword";
    	
    	if (UserTool.validateToken(token)) {
    		
    		resp = "user_setpassword";
    		
    		User u = UserTool.loginByToken(token);
    		
    		model.addAttribute("user", u);
    		
//    		session.setAttribute("sessionUser", u.getName());   
//    		
//    		logger.debug("User email: "+ u.getEmail() + "\n" );   
    	}
    	else {
    		
    		Message msg = new Message("user_forget", "browser", "fail", false);
    		
			msg.setTitle("Invalid token");
        	
        	msg.setStrongmsg("The token might be invalid or expired.");
        	
        	model.addAttribute("forwardURL", "login");
        	
        	model.addAttribute("forward", "redirect to main page");
        	
        	model.addAttribute("message", msg);
        	
    		resp = "message";
    	}
    	
        return resp; 
    }
    
    
    
    @RequestMapping(value = "/user_setpassword", method = RequestMethod.POST) 
    public String setpassword(@ModelAttribute("user") User user, BindingResult result, Model model, HttpSession session) {
    	
//    	String name = (String)session.getAttribute("sessionUser");
    	    	
    	logger.debug("sesion user: "+ user.getName() + "\n" );    	
    	   	
    	Message msg = UserTool.resetPassword(user);
    	
    			
		msg.setTitle("Password has been reset.");
    	
    	msg.setStrongmsg("Please login using the new password.");
    	
    	model.addAttribute("forwardURL", "login");
    	
    	model.addAttribute("forward", "redirect to main page");
    	
    	model.addAttribute("message", msg);
    	
    	String resp = "message";
    	
        return resp; 
    }
    
    
    /*************************************************************************************/
    
    @RequestMapping(value="/users", method=RequestMethod.GET) 
    public List<User> getUserList() { 

        List<User> r = new ArrayList<User>(users.values()); 

        return r;
        
    } 

    @RequestMapping(value="/users", method=RequestMethod.POST) 
    public String postUser(@ModelAttribute User user) { 

        users.put(user.getId(), user);
        
        return "success";
        
    } 

    @RequestMapping(value="/users/{id}", method=RequestMethod.GET) 
    public User getUser(@PathVariable Long id) { 
    	
        return users.get(id);
        
    } 

    @RequestMapping(value="/users/{id}", method=RequestMethod.PUT) 
    public String putUser(@PathVariable String id, @ModelAttribute User user) { 
    	
        User u = users.get(id); 
        
        u.setName(user.getName()); 
        
        users.put(id, u); 
        
        return "success";
        
    } 

    @RequestMapping(value="/users/{id}", method=RequestMethod.DELETE) 
    public String deleteUser(@PathVariable Long id) { 
    	
        users.remove(id); 
        
        return "success"; 
    
    } 
    
    /*************************************************************************************/




}
