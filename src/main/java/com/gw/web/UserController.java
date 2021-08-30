package com.gw.web;

import javax.servlet.http.HttpSession;

import com.gw.database.UserRepository;
import com.gw.jpa.GWUser;
import com.gw.ssh.RSAEncryptTool;
import com.gw.utils.BaseTool;
import com.gw.utils.RandomString;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
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
    UserRepository userRepository;

    @PostMapping("/profile")
    public @ResponseBody String profile(@Validated @RequestBody GWUser newUser) {
        
        String resp = "";

        try{

            Iterable<GWUser> users = userRepository.findAll();
            for (GWUser user : users) {
                if (user.getId().equals(newUser.getId())) {
                    
                    System.out.println("User  exists!");
                    //send out password reset email

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

    @PostMapping("/forgetpassword")
    public @ResponseBody String resetpassword(@Validated @RequestBody GWUser newUser) {
        
        String resp = "";

        try{

            Iterable<GWUser> users = userRepository.findAll();
            for (GWUser user : users) {
                if (user.getEmail().equals(newUser.getEmail())) {
                    System.out.println("User  exists!");
                    //send out password reset email

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
    public @ResponseBody String loginUser(@Validated @RequestBody GWUser user, HttpSession session) {

        String resp = "";

        try{
            
            resp = "{\"status\":\"failed\", \"message\":\"not found\"}";

            Iterable<GWUser> users = userRepository.findAll();
            for (GWUser other : users) {
                if (other.getUsername().equals(user.getUsername()) || other.getEmail().equals(user.getUsername())) {
                    logger.info("Found username match");

                    //decrypt the password into plain text
                    String password = RSAEncryptTool.getPassword(user.getPassword(), session.getId());

                    String new512str = bt.get_SHA_512_SecurePassword(password, other.getId());
                    
                    if(other.getPassword().equals(new512str)){

                        other.setLoggedIn(true);
                        userRepository.save(other);
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


    @PostMapping("/logout")
    public @ResponseBody String logUserOut(@Validated @RequestBody GWUser user) {

        String resp = "";

        try{
            
            resp = "{\"status\":\"failed\", \"message\":\"not found\"}";

            Iterable<GWUser> users = userRepository.findAll();
            for (GWUser other : users) {
                if (other.getUsername().equals(user.getUsername()) || other.getEmail().equals(user.getUsername()) 
                || other.getId().equals(user.getId())) {
                    
                        other.setLoggedIn(false);
                        userRepository.save(other);
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