package edu.gmu.csiss.earthcube.cyberconnector.web;

import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestBody;
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
	private String server = "192.168.0.80";
	private int port = 8888;
	
	RestTemplate restTemplate = new RestTemplate();
	
	HttpHeaders headers = new HttpHeaders();
	
	String replaceURLProxyHeader(String resp) {
		
		
		resp = resp
//				.replaceAll(scheme + "://" + server + ":" + port, replacement)
				.replaceAll("/static/", "/Geoweaver/web/jupyter-proxy/static/")
				.replaceAll("/custom/", "/Geoweaver/web/jupyter-proxy/custom/")
				.replaceAll("/login", "/Geoweaver/web/jupyter-proxy/login")
				.replaceAll("/tree", "/Geoweaver/web/jupyter-proxy/tree")
				.replaceAll("favicon.ico", "/Geoweaver/web/jupyter-proxy/favicon.ico")
				;
		
		return resp;
	}
	
	@RequestMapping(value="/jupyter-proxy/**", method = RequestMethod.POST)
	public @ResponseBody String proxypost(@RequestBody String body, HttpMethod method, HttpServletRequest request) throws URISyntaxException
	{
		String resp = null;
		
		try {
			
//			URI uri = new URI("https", null, server, port, request.getRequestURI(), request.getQueryString(), null);
			
			logger.info("Request URI: " + request.getRequestURI());
			
			logger.info("Query String: " + request.getQueryString());
			
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
			

			if(realurl.indexOf("login")!=-1) {
				
				headers.clear();
				
				headers.add("", "");
				
			}
			
			
			HttpEntity<String> bodyentity = BaseTool.isNull(body)?null:new HttpEntity<String>(body);
			
			logger.info("Body: " + bodyentity.getBody());
			
			logger.info("Headers: " + bodyentity.getHeaders());
			
		    ResponseEntity<String> responseEntity = restTemplate.exchange(uri, method, bodyentity, String.class);
		    
		    resp = replaceURLProxyHeader(responseEntity.getBody());
		    
			
		}catch(Exception e) {
			
			e.printStackTrace();
			
		}
		
	    return resp;
	    
	}
	
	@RequestMapping(value="/jupyter-proxy/**", method = RequestMethod.GET)
	public @ResponseBody String proxyget(HttpMethod method, HttpServletRequest request) throws URISyntaxException
	{
		String resp = null;
		
		try {
			
			logger.info("Request URI: " + request.getRequestURI());
			
			logger.info("Query String: " + request.getQueryString());
			
			String realurl =  request.getRequestURI().substring(request.getRequestURI().indexOf("jupyter-proxy") + 13);// /Geoweaver/web/jupyter-proxy/test
			
//			logger.info("Real URL : " + realurl);
//			
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
			
		    ResponseEntity<String> responseEntity = restTemplate.exchange(uri, method, null, String.class);
		    
		    resp = replaceURLProxyHeader(responseEntity.getBody());
			
		}catch(Exception e) {
			
			e.printStackTrace();
			
		}
		
	    return resp;
	    
	}
	
	@RequestMapping(value="/jupyter-proxy", method = RequestMethod.GET)
	public @ResponseBody String proxyroot_get(HttpMethod method, HttpServletRequest request) throws URISyntaxException
	{
		String resp = null;
		
		try {
			
			URI uri = new URI(scheme, null, server, port, null, request.getQueryString(), null);
			
			logger.info("URL: " + uri.toString());
			
		    ResponseEntity<String> responseEntity = restTemplate.exchange(uri, method, null, String.class);
		    
		    resp = replaceURLProxyHeader(responseEntity.getBody());
			
		}catch(Exception e) {
			
			e.printStackTrace();
			
		}
		
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
