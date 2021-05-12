package com.gw.web;

import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class GoogleEarthEngineController {
    
    @RequestMapping(value="/gee-proxy/{hostid}/**", 
		method = RequestMethod.GET,
		consumes = MediaType.ALL_VALUE,
		produces = MediaType.ALL_VALUE)
	public ResponseEntity proxyget( @PathVariable("hostid") String hostid, HttpMethod method, HttpServletRequest request) 
        throws URISyntaxException
	{
		// ResponseEntity resp = processGet_415(method, request, hostid);
		
	    // return resp;
        return null;
	    
	}

}
