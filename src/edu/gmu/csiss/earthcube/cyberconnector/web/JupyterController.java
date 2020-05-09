package edu.gmu.csiss.earthcube.cyberconnector.web;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.WebRequest;

import edu.gmu.csiss.earthcube.cyberconnector.utils.BaseTool;

@Controller 
public class JupyterController {
	
	Logger logger = LoggerFactory.getLogger(getClass());
	
	private String scheme = "http";
//	private String server = "192.168.0.80";
	private String server = "localhost";
	private int port = 8888;
	
	RestTemplate restTemplate = new RestTemplate();
	
	HttpHeaders headers = new HttpHeaders();
	
	String addURLProxy(String resp) {
		
		if(!BaseTool.isNull(resp))
			resp = resp
//				.replaceAll(scheme + "://" + server + ":" + port, replacement)
				.replace("\"/static/", "\"/Geoweaver/web/jupyter-proxy/static/")
				.replace("\"/custom/", "\"/Geoweaver/web/jupyter-proxy/custom/")
				.replace("\"/login?", "\"/Geoweaver/web/jupyter-proxy/login?")
				.replace("\"/tree", "\"/Geoweaver/web/jupyter-proxy/tree")
//				.replace("'contents': 'services/contents',", "'contents': 'Geoweaver/web/jupyter-proxy/services/contents',")
				.replace("/static/base/images/logo.png", "/Geoweaver/web/jupyter-proxy/static/base/images/logo.png")
				.replace("baseUrl: '/static/',", "baseUrl: '/Geoweaver/web/jupyter-proxy/static/',")
				.replace("url_path_join(this.base_url, 'api/config',", "url_path_join('/Geoweaver/web/jupyter-proxy/', 'api/config',")
				.replace("this.base_url,", "'/Geoweaver/web/jupyter-proxy/',")
				.replace("that.base_url,", "'/Geoweaver/web/jupyter-proxy/',")
				.replace("requirejs(['custom/custom'], function() {});", "requirejs(['Geoweaver/web/jupyter-proxy/custom/custom'], function() {});")
//				.replace("this.base_url", "'/Geoweaver/web/jupyter-proxy/'")
//				.replace("static/base/images/logo.png", "Geoweaver/web/jupyter-proxy/static/base/images/logo.png")
//				.replace("static/services/contents", "Geoweaver/web/jupyter-proxy/static/services/contents")
//				.replace("favicon.ico", "/Geoweaver/web/jupyter-proxy/favicon.ico")
				;
		
		return resp;
	}
	
	private ResponseEntity processPost(RequestEntity reqentity, HttpMethod method, HttpServletRequest request) throws URISyntaxException
	{
		ResponseEntity resp = null;
		
		try {
			
			logger.info("==============");
			
			logger.info("Request URI: " + request.getRequestURI());
			
			logger.info("Query String: " + request.getQueryString());
			
			String realurl =  request.getRequestURI().substring(request.getRequestURI().indexOf("jupyter-proxy") + 13);// /Geoweaver/web/jupyter-proxy/test
			
			URI uri = new URI(scheme, null, server, port, realurl, request.getQueryString(), null);
			
			logger.info("URL: " + uri.toString());
			
			logger.info("HTTP Method: " + method.toString());
			
		    ResponseEntity<String> responseEntity = restTemplate.exchange(uri, method, reqentity, String.class);
		    
//		    if(realurl.indexOf("auth")!=-1)
//		    
//		    	logger.info("Response Body: " + responseEntity.getBody());
		    
		    resp = new ResponseEntity(
		    		addURLProxy(responseEntity.getBody()), 
		    		responseEntity.getHeaders(), 
		    		responseEntity.getStatusCode());
		    
		}catch(Exception e) {
			
			e.printStackTrace();
			
		}
		
	    return resp;
	    
	}
	
	@SuppressWarnings("unchecked")
	private ResponseEntity processGET(HttpHeaders headers, HttpMethod method, HttpServletRequest request) throws URISyntaxException
	{
		ResponseEntity resp = null;
		
		try {
			
			logger.info("==============");
			
			logger.info("This is a GET request...");
			
			logger.info("Request URI: " + request.getRequestURI());
			
			logger.info("Query String: " + request.getQueryString());
			
			String realurl =  request.getRequestURI().substring(request.getRequestURI().indexOf("jupyter-proxy") + 13);// /Geoweaver/web/jupyter-proxy/test
			
			URI uri = new URI(scheme, null, server, port, realurl, request.getQueryString(), null);
			
			logger.info("URL: " + uri.toString());
			
			logger.info("HTTP Method: " + method.toString());
			
			logger.info("HTTP Headers: " + headers.toString());
			
			HttpEntity entity = new HttpEntity(headers);
			
		    ResponseEntity<String> responseEntity = restTemplate.exchange(uri, method, entity, String.class);
		    
		    if(realurl.equals("/tree"))
		    
		    	logger.info("Response Body: " + responseEntity.getBody());
	    	
	    	logger.info("Response Header: " + responseEntity.getHeaders());
	    	
	    	String newbody = addURLProxy(responseEntity.getBody());
	    	
		    resp = new ResponseEntity<String>(
		    		newbody, 
		    		updateHeaderLength(responseEntity.getHeaders(), newbody), 
		    		responseEntity.getStatusCode());
		    
		}catch(Exception e) {
			
			e.printStackTrace();
			
		}
		
	    return resp;
	    
	}
	
	HttpHeaders updateHeaderLength(HttpHeaders oldheaders, String returnbody) {
		
		
		HttpHeaders newheaders = new HttpHeaders();
    	
	    
		oldheaders.forEach((key, value) -> {
	    	
	    	if(key.toLowerCase().equals("location")) {
	    		
	    		newheaders.set(key, "/Geoweaver/web/jupyter-proxy" + value.get(0));
	    		
	    	}else if (key.toLowerCase().equals("content-length")){
	    		
	    		newheaders.set(key, String.valueOf(returnbody.length()));
	    		
	    	}else {
	    		
	    		newheaders.set(key, value.get(0));
	    		
	    	}
	    	
	    });
		
		return newheaders;
		
	}
	
	@RequestMapping(value="/jupyter-proxy/login", method = RequestMethod.POST)
	public ResponseEntity jupyter_login( HttpMethod method, @RequestHeader HttpHeaders httpheaders, HttpServletRequest request) throws URISyntaxException
	{
//		ResponseEntity resp = processPost(reqentity, method, request);
		
		ResponseEntity resp = null;
		
		try {
			
//			URI uri = new URI("https", null, server, port, request.getRequestURI(), request.getQueryString(), null);
			
			logger.info("==============");
			
			logger.info("Request URI: " + request.getRequestURI());
			
			logger.info("Query String: " + request.getQueryString());
			
			logger.info("Original Request String: " + request.getParameterMap());
			
			String realurl =  request.getRequestURI().substring(request.getRequestURI().indexOf("jupyter-proxy") + 13);// /Geoweaver/web/jupyter-proxy/test
			
//			logger.info("Real URL : " + realurl);
			
//			realurl = "https://" + server + ":" + port + "/" + realurl;
//			
//			if(!BaseTool.isNull(request.getQueryString())) {
//				
//				realurl += "?" + request.getQueryString();
//				
//			}
			
			URI uri = new URI(scheme, null, server, port, realurl, request.getQueryString(), null);
			
//			URI uri = new URI(realurl);
			
			logger.info("URL: " + uri.toString());
			
			logger.info("HTTP Method: " + method.toString());
			
//			headers.clear();
//				
//			headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
			
			MultiValueMap<String, String> map= new LinkedMultiValueMap<String, String>();
			
			Iterator hmIterator = request.getParameterMap().entrySet().iterator(); 
			  
	        // Iterate through the hashmap 
			
			StringBuffer reqstr = new StringBuffer();
	  
	        while (hmIterator.hasNext()) { 
	            
	        	Map.Entry mapElement = (Map.Entry)hmIterator.next(); 
	            
	            map.add((String)mapElement.getKey(), ((String[])(mapElement.getValue()))[0]);
	            
	            if(!BaseTool.isNull(reqstr.toString())) {
	            	
	            	reqstr.append("&");
	            	
	            }
	            
	            reqstr.append((String)mapElement.getKey()).append("=").append(((String[])(mapElement.getValue()))[0]);
	            
	        }

			HttpEntity requestentity = new HttpEntity(reqstr.toString(), httpheaders);
			
			
			logger.info("Body: " + requestentity.getBody());
			
			logger.info("Headers: " + requestentity.getHeaders());
			
		    ResponseEntity<String> responseEntity = restTemplate.exchange(uri, method, requestentity, String.class);
		    
		    HttpHeaders respheaders = responseEntity.getHeaders();
		    
		    if(responseEntity.getStatusCode()==HttpStatus.FOUND) {
		    	
//		    	MultiValueMap<String, String> headers =new LinkedMultiValueMap<String, String>();
		    	
		    	HttpHeaders newheaders = new HttpHeaders();
		    	
		    	logger.info("Redirection: " + respheaders);
			    
			    logger.info("Response: " + responseEntity.getBody());
			    
//			    responseEntity = restTemplate.exchange(uri, method, requestentity, String.class);
			    
//			    responseEntity.getHeaders().compute("Location", (k, v) -> {v.clear(); v.add("/Geoweaver/web/jupyter-proxy/tree?");});
			    
//			    responseEntity.getHeaders().set("Location", "/Geoweaver/web/jupyter-proxy/tree?");
			    
//			    respheaders.set("Location", "/Geoweaver/web/jupyter-proxy/tree?");
			    
//			    respheaders.setLocation(new URI("/Geoweaver/web/jupyter-proxy/tree?"));
			    
//			    respheaders.add("Test", "Test Value");
			    
			    respheaders.forEach((key, value) -> {
			    	
			    	if(key.toLowerCase().equals("location")) {
			    		
			    		newheaders.set(key, "/Geoweaver/web/jupyter-proxy" + value.get(0));
			    		
			    	}else {
			    		
			    		newheaders.set(key, value.get(0));
			    		
			    	}
			    	
			    });
			    
			    respheaders = newheaders;
			    
//			    Set ent = respheaders.entrySet();
			    
			    logger.info(respheaders.toString());
		    	
		    }else if(responseEntity.getStatusCode()==HttpStatus.UNAUTHORIZED) {
		    	
		    	
		    	
		    }
		    
		    resp = new ResponseEntity(
		    		null, 
		    		respheaders, 
		    		responseEntity.getStatusCode());
		    
			
		}catch(Exception e) {
			
			e.printStackTrace();
			
		}
		
	    return resp;
	    
	}
	
	@RequestMapping(value="/jupyter-proxy/**", method = RequestMethod.POST)
	public ResponseEntity proxypost( RequestEntity reqentity, HttpMethod method, HttpServletRequest request) throws URISyntaxException
	{
		ResponseEntity resp = processPost(reqentity, method, request);
		
//		try {
//			
////			URI uri = new URI("https", null, server, port, request.getRequestURI(), request.getQueryString(), null);
//			
//			logger.info("Request URI: " + request.getRequestURI());
//			
//			logger.info("Query String: " + request.getQueryString());
//			
//			String realurl =  request.getRequestURI().substring(request.getRequestURI().indexOf("jupyter-proxy") + 13);// /Geoweaver/web/jupyter-proxy/test
//			
////			logger.info("Real URL : " + realurl);
//			
////			realurl = "https://" + server + ":" + port + "/" + realurl;
////			
////			if(!BaseTool.isNull(request.getQueryString())) {
////				
////				realurl += "?" + request.getQueryString();
////				
////			}
//			
//			URI uri = new URI(scheme, null, server, port, realurl, request.getQueryString(), null);
//			
////			URI uri = new URI(realurl);
//			
//			logger.info("URL: " + uri.toString());
//			
//			logger.info("HTTP Method: " + method.toString());
//			
//
//			if(realurl.indexOf("login")!=-1) {
//				
//				headers.clear();
//				
//				headers.add("", "");
//				
//			}
//			
//			
//			HttpEntity<String> bodyentity = BaseTool.isNull(body)?null:new HttpEntity<String>(body);
//			
//			logger.info("Body: " + bodyentity.getBody());
//			
//			logger.info("Headers: " + bodyentity.getHeaders());
//			
//		    ResponseEntity<String> responseEntity = restTemplate.exchange(uri, method, bodyentity, String.class);
//		    
//		    resp = replaceURLProxyHeader(responseEntity.getBody());
//		    
//			
//		}catch(Exception e) {
//			
//			e.printStackTrace();
//			
//		}
		
	    return resp;
	    
	}
	
	@RequestMapping(value="/jupyter-proxy/**", method = RequestMethod.GET)
	public ResponseEntity proxyget(HttpMethod method, @RequestHeader HttpHeaders headers, HttpServletRequest request) throws URISyntaxException
	{
		ResponseEntity resp = processGET( headers, method, request);
		
	    return resp;
	    
	}
	
	@RequestMapping(value="/jupyter-proxy", method = RequestMethod.GET)
	public ResponseEntity proxyroot_get(HttpMethod method, @RequestHeader HttpHeaders headers, HttpServletRequest request) throws URISyntaxException
	{
		ResponseEntity resp = processGET(headers, method, request);
		
//		try {
//			
//			URI uri = new URI(scheme, null, server, port, null, request.getQueryString(), null);
//			
//			logger.info("URL: " + uri.toString());
//			
//		    ResponseEntity<String> responseEntity = restTemplate.exchange(uri, method, null, String.class);
//		    
//		    resp = replaceURLProxyHeader(responseEntity.getBody());
//			
//		}catch(Exception e) {
//			
//			e.printStackTrace();
//			
//		}
		
	    return resp;
	    
	}
	
	@RequestMapping(value = "/jupyter-http", method = RequestMethod.GET)
    public @ResponseBody String jupyter_http(ModelMap model, WebRequest request){
		
		String resp = null;
		
		try {
			
			String targeturl = request.getParameter("url");
			
			HttpClient client = new HttpClient(new MultiThreadedHttpConnectionManager());
			
			client.getHttpConnectionManager().getParams().setConnectionTimeout(30000);
			
			GetMethod get = new GetMethod(targeturl);
            
			get.setFollowRedirects(true);
			
            int iGetResultCode = client.executeMethod(get);
			
			resp = get.getResponseBodyAsString();
            
            
			
		}catch(Exception e) {
			
			throw new RuntimeException("failed " + e.getLocalizedMessage());
			
		}
		
		return resp;
		
	}
	
	@RequestMapping(value = "/jupyter-https", method = RequestMethod.POST)
    public @ResponseBody String jupyter_https(ModelMap model, WebRequest request){
		
		String resp = null;
		
		try {
			
			String targeturl = request.getParameter("url");
			
			HttpClient client = new HttpClient(new MultiThreadedHttpConnectionManager());
			
			client.getHttpConnectionManager().getParams().setConnectionTimeout(30000);
			
			GetMethod get = new GetMethod(targeturl);
            
			get.setFollowRedirects(true);
			
            int iGetResultCode = client.executeMethod(get);
			
			resp = get.getResponseBodyAsString();
            
            
			
		}catch(Exception e) {
			
			throw new RuntimeException("failed " + e.getLocalizedMessage());
			
		}
		
		return resp;
		
	}
	
	@RequestMapping(value = "/jupyter-websocket", method = RequestMethod.POST)
	public @ResponseBody String jupyter_websocket(ModelMap model, WebRequest request){
		
		String resp = null;
		
		try {
			
			
		}catch(Exception e) {
			
			throw new RuntimeException("failed " + e.getLocalizedMessage());
			
		}
		
		return resp;
		
	}

}
