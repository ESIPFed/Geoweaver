package com.gw.web;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.gw.database.UserRepository;
import com.gw.jpa.GWUser;
import com.gw.ssh.RSAEncryptTool;
import com.gw.tools.UserTool;
import com.gw.utils.BaseTool;
import com.gw.utils.EmailMessage;
import com.gw.utils.EmailService;
import com.gw.utils.HttpUtil;
import com.gw.utils.RandomString;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
@Controller 
@RequestMapping(value="/user")  
public class UserController {

    Logger logger = Logger.getLogger(this.getClass());

    @Autowired
    BaseTool bt;

    @Autowired
    UserTool ut;
    
    @Autowired
    UserRepository userRepository;

    @Autowired
    EmailService et;


    @PostMapping("/profile")
    public @ResponseBody String profile(@Validated @RequestBody GWUser newUser) {
        
        String resp = "";

        try{

            Iterable<GWUser> users = userRepository.findAll();
            for (GWUser user : users) {
                if (user.getId().equals(newUser.getId())) {
                    
                    System.out.println("User  exists!");

                    resp = "{\"status\":\"success\", \"username\":\""+user.getUsername()+"\", \"email\": \""+user.getEmail()+"\" }";

                }
            }

            if(bt.isNull(resp)){

                resp = "{\"status\":\"failed\", \"message\":\"No account is associated with that email\"}";
            }

        }catch(Exception e){

            resp = "{\"status\":\"failed\", \"message\":\""+e.getLocalizedMessage()+"\"}";

        }
        
        return resp;
    }

    /**
     * This is the password reset callback url
     * @param token
     * @param model
     * @return
     */
    @GetMapping("/reset_password")
    public String showResetPasswordForm(@Param(value = "token") String token, Model model) {
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
                    model.addAttribute("message", "Invalid Token");
                    return "message";
                }

            }

        }

         
        return "reset_password_form";
    }
    
    /**
     * The post request comes from the password reset callback url
     * @param request
     * @param model
     * @return
     */
    @PostMapping("/reset_password")
    public @ResponseBody String processResetPassword(HttpServletRequest request, Model model) {
        String token = request.getParameter("token");
        String password = request.getParameter("password");
         
        String userid = ut.token2userid.get(token);

        String resp = "{\"status\": \"failed\"}";

        if(!bt.isNull(userid)){

            GWUser user = ut.getUserById(userid);

            Date created_date = ut.token2date.get(token);

            long time_difference =  new Date().getTime() - created_date.getTime();

            //if the token is one hour old
            if(time_difference<60*60*1000){

                // userService.updatePassword(user, password);


            
            }
                

        }
        
         
        return "index";
    }

    @PostMapping("/forgetpassword")
    public @ResponseBody String resetpassword(@Validated @RequestBody GWUser newUser, HttpServletRequest request) {
        
        String resp = "";

        try{

            Iterable<GWUser> users = userRepository.findAll();
            for (GWUser user : users) {
                if (user.getEmail().equals(newUser.getEmail())) {
                    HttpUtil httpUtil = new HttpUtil();
    	            String siteUrl = httpUtil.getSiteURL(request);
                    System.out.println("User  exists!");
                    //send out password reset email
                    et.send_resetpassword(user, siteUrl);
                    resp = "{\"status\":\"success\", \"message\":\"a password reset email has been sent\"}";
                }
            }

            if(bt.isNull(resp)){

                resp = "{\"status\":\"failed\", \"message\":\"No account is associated with that email\"}";
            }

        }catch(Exception e){

            resp = "{\"status\":\"failed\", \"message\":\""+e.getLocalizedMessage()+"\"}";

        }
        
        return resp;
    }

    @PostMapping("/register")
    public @ResponseBody String registerUser(@Validated @RequestBody GWUser newUser, HttpSession session) {
        
        String resp = "";

        try{

            Iterable<GWUser> users = userRepository.findAll();
            System.out.println("New user: " + newUser.toString());
            for (GWUser user : users) {
                System.out.println("Registered user: " + newUser.toString());
                if (user.getEmail().equals(newUser.getEmail())) {
                    System.out.println("User Already exists!");
                    resp = "{\"status\":\"failed\", \"message\":\"the email address has already been registered\"}";
                }
            }

            if(bt.isNull(resp)){

                //validate the email
                if(bt.validate(newUser.getEmail())){

                    newUser.setId(new RandomString(10).nextString());
                    String o = RSAEncryptTool.getPassword(newUser.getPassword(), session.getId());
                    String new512str = bt.get_SHA_512_SecurePassword(o, newUser.getId());
                    newUser.setPassword(new512str);
                    userRepository.save(newUser);

                    resp = "{\"status\":\"success\", \"message\":\"You are registered!\"}";
                
                }else{

                    resp = "{\"status\":\"failed\", \"message\":\"Invalid Email\"}";

                }

                
            }

        }catch(Exception e){

            resp = "{\"status\":\"failed\", \"message\":\""+e.getLocalizedMessage()+"\"}";

        }
        
        return resp;
    }


    @PostMapping("/login")
    public @ResponseBody String loginUser(@Validated @RequestBody GWUser user, HttpSession session, HttpServletRequest request) {

        String resp = "";

        try{
            
            resp = "{\"status\":\"failed\", \"message\":\"not found\"}";

            //decrypt the password into plain text
            String password = RSAEncryptTool.getPassword(user.getPassword(), session.getId());

            Iterable<GWUser> users = userRepository.findAll();
            for (GWUser other : users) {
                if (other.getUsername().equals(user.getUsername()) || other.getEmail().equals(user.getUsername())) {
                    logger.info("Found username match");

                    String new512str = bt.get_SHA_512_SecurePassword(password, other.getId());
                    
                    if(other.getPassword().equals(new512str)){

                        other.setLoggedIn(true);
                        userRepository.save(other);

                        String ipaddress = ut.getClientIp(request);

                        ut.bindSessionUser(session.getId(), other.getId(), ipaddress);

                        resp = "{\"status\":\"success\", \"username\":\""+other.getUsername()+"\", \"id\":\""+other.getId()+"\", \"message\":\"You are logged in!\"}";
                        break;
                    
                    }
                    
                }
            }

        }catch(Exception e){
        
            e.printStackTrace();
        
            resp = "{\"status\":\"failed\", \"message\":\""+e.getLocalizedMessage()+"\"}";

        }

        return resp;
    }

    
    @PostMapping("/logbackin")
    public @ResponseBody String logbackonafterrefresh(HttpSession session, HttpServletRequest request) {

        String resp = "";

        try{
            
            resp = "{\"status\":\"FALSE\", \"message\":\"no active session\"}";

            if(ut.isAuth(session.getId(), ut.getClientIp(request))){

                String id = ut.getAuthUserId(session.getId(), ut.getClientIp(request));

                GWUser u = ut.getUserById(id);

                resp = "{\"status\":\"TRUE\", \"id\":\""+id+"\", \"name\": \""+u.getUsername()+"\"}";

            }

        }catch(Exception e){
        
            e.printStackTrace();
        
            resp = "{\"status\":\"FALSE\", \"message\":\""+e.getLocalizedMessage()+"\"}";

        }

        return resp;

    }

    @PostMapping("/logout")
    public @ResponseBody String logUserOut(@Validated @RequestBody GWUser user, HttpSession session) {

        String resp = "";

        try{
            
            resp = "{\"status\":\"failed\", \"message\":\"not found\"}";

            Iterable<GWUser> users = userRepository.findAll();
            for (GWUser other : users) {
                if (other.getUsername().equals(user.getUsername()) || other.getEmail().equals(user.getUsername()) 
                || other.getId().equals(user.getId())) {
                    
                        other.setLoggedIn(false);
                        userRepository.save(other);
                        ut.removeSessionById(session.getId());
                        resp = "{\"status\":\"success\", \"message\":\"You are logged out!\"}";
                        break;
                    
                }
            }

        }catch(Exception e){
        
            e.printStackTrace();
        
            resp = "{\"status\":\"failed\", \"message\":\""+e.getLocalizedMessage()+"\"}";

        }

        return resp;

    }
    // @DeleteMapping("/users/all")
    // public UserStatus deleteUsers() {
    //     userRepository.deleteAll();
    //     return UserStatus.SUCCESS;
    // }
}