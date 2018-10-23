package edu.gmu.csiss.earthcube.cyberconnector.web;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 
 * This is the controller of SpringMVC for CyberConnector
 * 
 * @author Z.S.
 * @date 20170126
 * 
 */
@Controller
public class PortalController {

	Logger logger = Logger.getLogger(this.getClass());
	
    @RequestMapping("/")
    public String index() {
        return "index";
    }

    @RequestMapping("{name}")
    public String view(@PathVariable String name, HttpSession session) {
    	
    	logger.debug("Current user: " + (String)session.getAttribute("sessionUser"));
    	
    	logger.debug("Current sessionId: " + (String)session.getId());
    	
        return name;
    }
    
    
}