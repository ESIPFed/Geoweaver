package gw.web;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.WebRequest;

import gw.jpa.Host;
import gw.tools.HistoryTool;
import gw.tools.HostTool;
import gw.utils.BaseTool;

@Controller 
public class JupyterController {
	
	Logger logger = LoggerFactory.getLogger(getClass());
	
	private String scheme = "http";
//	private String server = "192.168.0.80";
	private String server = "localhost";
	private int port = 8888;
	
	RestTemplate restTemplate = new RestTemplate();
	
	HttpHeaders headers = new HttpHeaders();
	
	HistoryTool history_tool = new HistoryTool();
	
	/**
	 * Add URL Proxy
	 * @param resp
	 * @param hostid
	 * @return
	 */
	String addURLProxy(String resp, String hostid) {
		
		if(!BaseTool.isNull(resp))
			resp = resp
//				.replaceAll(scheme + "://" + server + ":" + port, replacement)
				.replace("\"/static/", "\"/Geoweaver/web/jupyter-proxy/"+hostid+"/static/")
				.replace("\"/custom/", "\"/Geoweaver/web/jupyter-proxy/"+hostid+"/custom/")
				.replace("\"/login?", "\"/Geoweaver/web/jupyter-proxy/"+hostid+"/login?")
				.replace("\"/tree", "\"/Geoweaver/web/jupyter-proxy/"+hostid+"/tree")
//				.replace("'contents': 'services/contents',", "'contents': 'Geoweaver/web/jupyter-proxy/services/contents',")
				.replace("/static/base/images/logo.png", "/Geoweaver/web/jupyter-proxy/"+hostid+"/static/base/images/logo.png")
				.replace("baseUrl: '/static/',", "baseUrl: '/Geoweaver/web/jupyter-proxy/"+hostid+"/static/',")
				.replace("url_path_join(this.base_url, 'api/config',", "url_path_join('/Geoweaver/web/jupyter-proxy/"+hostid+"/', 'api/config',")
				.replace("this.base_url,", "'/Geoweaver/web/jupyter-proxy/"+hostid+"/',")
				.replace("that.base_url,", "'/Geoweaver/web/jupyter-proxy/"+hostid+"/',")
				.replace("'/Geoweaver/web/jupyter-proxy/"+hostid+"/', \"api/kernels\"", "'/Geoweaver/jupyter-socket/"+hostid+"/', \"api/kernels\"")
				.replace("requirejs(['custom/custom'], function() {});", "requirejs(['Geoweaver/web/jupyter-proxy/"+hostid+"/custom/custom'], function() {});")
				.replace("src=\"/files/", "src=\"/Geoweaver/web/jupyter-proxy/"+hostid+"/files/")
				.replace("this.notebook.base_url,", "'/Geoweaver/web/jupyter-proxy/"+hostid+"/',")
//				.replace("\"nbextensions/\"", "\"Geoweaver/web/jupyter-proxy/nbextensions/\"")
//				.replace("this.base_url", "'/Geoweaver/web/jupyter-proxy/'")
//				.replace("static/base/images/logo.png", "Geoweaver/web/jupyter-proxy/static/base/images/logo.png")
//				.replace("static/services/contents", "Geoweaver/web/jupyter-proxy/static/services/contents")
//				.replace("favicon.ico", "/Geoweaver/web/jupyter-proxy/favicon.ico")
				;
		
		return resp;
		
	}
	
	/**
	 * Error Control
	 * @param message
	 * @param hostid
	 * @return
	 */
	private ResponseEntity errorControl(String message, String hostid) {
		
		logger.error(message);
		
		HttpHeaders headers = new HttpHeaders();
		
		headers.add("Content-Length", "0");
		
		ResponseEntity resp = null;
		
		if(message.indexOf("403 Forbidden")!=-1) {
			
			resp = new ResponseEntity<String>(
					message, 
		    		updateHeaderLength(headers, message, hostid), 
		    		HttpStatus.FORBIDDEN);
			
		}else if(message.indexOf("404 Not Found")!=-1) {
			
			resp = new ResponseEntity<String>(
					message, 
		    		updateHeaderLength(headers, message, hostid), 
		    		HttpStatus.NOT_FOUND);
			
		}else if(message.indexOf("400 Bad Request")!=-1) {
			
			resp = new ResponseEntity<String>(
					message, 
		    		updateHeaderLength(headers, message, hostid), 
		    		HttpStatus.BAD_REQUEST);
			
		}else {
			
			resp = new ResponseEntity<String>(
					message, 
		    		updateHeaderLength(headers, message, hostid), 
		    		HttpStatus.INTERNAL_SERVER_ERROR);
			
		}
		
		return resp;
		
	}
	
	/**
	 * Get real request uri
	 * @param requesturi
	 * @return
	 */
	private String getRealRequestURL(String requesturi) {
		
		String realurl =  requesturi.substring(requesturi.indexOf("jupyter-proxy") + 20);// /Geoweaver/web/jupyter-proxy/test
		
		return realurl;
		
	}
	
	/**
	 * Update Header Referers
	 * @param oldheaders
	 * @param h
	 * @param realurl
	 * @param querystr
	 * @return
	 */
	private HttpHeaders updateHeaderReferer(HttpHeaders oldheaders, Host h, String realurl, String querystr) {
		
		HttpHeaders newheaders = oldheaders;
		
		try {
		
			String[] ss = h.parseJupyterURL();
			
			URI uri = new URI(ss[0], null, ss[1], Integer.parseInt(ss[2]), realurl, querystr, null);
			
			String hosturl = ss[0] + "://" + ss[1] + ":" + ss[2];
			
			newheaders =  HttpHeaders.writableHttpHeaders(oldheaders);
			
			newheaders.set("host", ss[1] + ":" + ss[2]);
			
			newheaders.set("origin", hosturl);
			
			newheaders.set("referer", uri.toString());
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}
		
		return newheaders;
		
	}
	
	/**
	 * General Processing Function
	 * @param entity
	 * @param method
	 * @param request
	 * @param hostid
	 * @return
	 */
	private ResponseEntity processUtil(HttpEntity entity, HttpMethod method, HttpServletRequest request, String hostid) {
		
		ResponseEntity resp = null;
		
		try {
			
			logger.info("==============");
			
			logger.info("Ensuring Unicode to UTF");
			
			logger.info("Request URI: " + request.getRequestURI());
			
			logger.info("Request Headers: " + entity.getHeaders());
			
			logger.info("Query String: " + request.getQueryString());
			
//			String realurl =  this.getRealRequestURL(request.getRequestURI());
//			
//			Host h = HostTool.getHostById(hostid);
//			
//			logger.info("HTTP Method: " + method.toString());
//			
//			restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
//			
//			HttpHeaders newheaders = this.updateHeaderReferer(entity.getHeaders(), h, realurl, request.getQueryString());
			
			HttpHeaders newheaders = getHeaders(entity.getHeaders(), method, request, hostid);
			
			HttpEntity newentity = new HttpEntity(entity.getBody(), newheaders);
			
			logger.info("URL: " + newheaders.get("referer").get(0));
			
		    ResponseEntity<String> responseEntity = restTemplate.exchange(newheaders.get("referer").get(0), method, newentity, String.class);
		    
		    String newbody = addURLProxy(responseEntity.getBody(), hostid);
	    	
		    resp = new ResponseEntity<String>(
		    		newbody,  
		    		updateHeaderLength(responseEntity.getHeaders(), newbody, hostid), 
		    		responseEntity.getStatusCode());
		    
//		    if(method.equals(HttpMethod.GET)) {
//		    	
//		    	String newbody = addURLProxy(responseEntity.getBody(), hostid);
//		    	
//			    resp = new ResponseEntity<String>(
//			    		newbody,  
//			    		updateHeaderLength(responseEntity.getHeaders(), newbody, hostid), 
//			    		responseEntity.getStatusCode());
//		    	
//		    }else {
//		    	
//		    	resp = new ResponseEntity(
//			    		addURLProxy(responseEntity.getBody(), hostid), 
//			    		responseEntity.getHeaders(), 
//			    		responseEntity.getStatusCode());
//		    	
//		    }
		    
		}catch(Exception e) {
			
			e.printStackTrace();
			
			resp = errorControl(e.getLocalizedMessage(), hostid);
			
		}
		
	    return resp;
		
	}
	
	
	private HttpHeaders getHeaders(HttpHeaders headers, HttpMethod method, HttpServletRequest request, String hostid) throws NumberFormatException, URISyntaxException {
		
		String realurl =  this.getRealRequestURL(request.getRequestURI());
		
		Host h = HostTool.getHostById(hostid);
		
		String[] ss = h.parseJupyterURL();
		
		URI uri = new URI(ss[0], null, ss[1], Integer.parseInt(ss[2]), realurl, request.getQueryString(), null);
		
		logger.info("URL: " + uri.toString());
		
		logger.info("HTTP Method: " + method.toString());
		
		HttpHeaders newheaders = this.updateHeaderReferer(headers, h, realurl, request.getQueryString());
		
		return newheaders;
		
	}
	
	
	
	/**
	 * Process Patch
	 * @param entity
	 * @param method
	 * @param request
	 * @param hostid
	 * @return
	 */
	private ResponseEntity processPatch(HttpEntity entity, HttpMethod method, HttpServletRequest request, String hostid) {
		
//		return processUtil(entity, method, request, hostid);
		
		ResponseEntity resp = null;
		
		try {
			
			logger.info("==============");
			
			logger.info("Request URI: " + request.getRequestURI());
			
			logger.info("Request Headers: " + entity.getHeaders());
			
			logger.info("Query String: " + request.getQueryString());
			
//			String realurl =  this.getRealRequestURL(request.getRequestURI());
//			
//			Host h = HostTool.getHostById(hostid);
//			
//			logger.info("HTTP Method: " + method.toString());
//			
//			restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
//			
//			HttpHeaders newheaders = this.updateHeaderReferer(entity.getHeaders(), h, realurl, request.getQueryString());
			
			HttpHeaders newheaders = getHeaders(entity.getHeaders(), method, request, hostid);
			
			HttpEntity newentity = new HttpEntity(entity.getBody(), newheaders);
			
			logger.info("URL: " + newheaders.get("referer").get(0));
			
		    ResponseEntity<String> responseEntity = restTemplate.exchange(newheaders.get("referer").get(0), method, newentity, String.class);
		    
		    resp = new ResponseEntity(
		    		addURLProxy(responseEntity.getBody(), hostid), 
		    		responseEntity.getHeaders(), 
		    		responseEntity.getStatusCode());
		    
		}catch(Exception e) {
			
			e.printStackTrace();
			
			resp = errorControl(e.getLocalizedMessage(), hostid);
			
		}
		
	    return resp;
		
	}
	
	/**
	 * Process PUT request
	 * @param entity
	 * @param method
	 * @param request
	 * @param hostid
	 * @return
	 */
	private ResponseEntity processPut(HttpEntity entity, HttpMethod method, HttpServletRequest request, String hostid) {
		
		ResponseEntity resp = null;
		
		try {
			
			logger.info("==============");
			
			logger.info("Request URI: " + request.getRequestURI());
			
			logger.info("Query String: " + request.getQueryString());
			
//			String body = IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8);
			
			HttpHeaders newheaders = getHeaders(entity.getHeaders(), method, request, hostid);
			
			HttpEntity newentity = new HttpEntity(entity.getBody(), newheaders);
			
			//only save the content when the request content is jupyter notebook
			if("notebook".equals(((Map)entity.getBody()).get("type"))) {
				
				String jsonString = new JSONObject((Map)entity.getBody()).toString();
				
				history_tool.saveJupyterCheckpoints(hostid, jsonString, newheaders);
				
			}
			
		    ResponseEntity<String> responseEntity = restTemplate.exchange(newheaders.get("referer").get(0), method, newentity, String.class);
		    
		    resp = new ResponseEntity(
		    		addURLProxy(responseEntity.getBody(), hostid), 
		    		responseEntity.getHeaders(), 
		    		responseEntity.getStatusCode());
		    
		}catch(Exception e) {
			
			e.printStackTrace();
			
			resp = errorControl(e.getLocalizedMessage(), hostid);
			
		}
		
	    return resp;
		
	}
	
	/**
	 * Process DELETE request
	 * @param headers
	 * @param method
	 * @param request
	 * @param hostid
	 * @return
	 */
	private ResponseEntity processDelete(HttpEntity entity, HttpMethod method, HttpServletRequest request, String hostid) {
		
//		return processUtil(entity, method, request, hostid);
		
		ResponseEntity resp = null;
		
		try {
			
			logger.info("==============");
			
			logger.info("Request URI: " + request.getRequestURI());
			
			logger.info("Query String: " + request.getQueryString());
			
//			String realurl =  this.getRealRequestURL(request.getRequestURI());
//			
//			Host h = HostTool.getHostById(hostid);
//			
//			String[] ss = h.parseJupyterURL();
//			
//			URI uri = new URI(ss[0], null, ss[1], Integer.parseInt(ss[2]), realurl, request.getQueryString(), null);
//			
//			logger.info("URL: " + uri.toString());
//			
//			logger.info("HTTP Method: " + method.toString());
//			
////			HttpEntity entity = new HttpEntity(headers);
//			
//			HttpHeaders newheaders = this.updateHeaderReferer(entity.getHeaders(), h, realurl, request.getQueryString());
			
			HttpHeaders newheaders = getHeaders(entity.getHeaders(), method, request, hostid);
			
			HttpEntity newentity = new HttpEntity(entity.getBody(), newheaders);
			
		    ResponseEntity<String> responseEntity = restTemplate.exchange(newheaders.get("referer").get(0), method, newentity, String.class);
		    
		    resp = new ResponseEntity(
		    		addURLProxy(responseEntity.getBody(), hostid), 
		    		responseEntity.getHeaders(), 
		    		responseEntity.getStatusCode());
		    
		}catch(Exception e) {
			
			e.printStackTrace();
			
			resp = errorControl(e.getLocalizedMessage(), hostid);
			
		}
		
	    return resp;
		
	}
	
	/**
	 * Process POST Request
	 * @param reqentity
	 * @param method
	 * @param request
	 * @param hostid
	 * @return
	 * @throws URISyntaxException
	 */
	private ResponseEntity processPost(RequestEntity reqentity, HttpMethod method, HttpServletRequest request, String hostid) throws URISyntaxException
	{
		
//		return processUtil(reqentity, method, request, hostid);
		
		ResponseEntity resp = null;
		
		try {
			
			logger.info("==============");
			
			logger.info("Request URI: " + request.getRequestURI());
			
			logger.info("Query String: " + request.getQueryString());
			
//			String realurl =  this.getRealRequestURL(request.getRequestURI());
//			
//			Host h = HostTool.getHostById(hostid);
//			
//			String[] ss = h.parseJupyterURL();
//			
//			URI uri = new URI(ss[0], null, ss[1], Integer.parseInt(ss[2]), realurl, request.getQueryString(), null);
//			
//			logger.info("URL: " + uri.toString());
//			
//			logger.info("HTTP Method: " + method.toString());
//			
//			HttpHeaders newheaders = this.updateHeaderReferer(reqentity.getHeaders(), h, realurl, request.getQueryString());
			
			HttpHeaders newheaders = getHeaders(reqentity.getHeaders(), method, request, hostid);
			
			HttpEntity newentity = new HttpEntity(reqentity.getBody(), newheaders);
			
		    ResponseEntity<String> responseEntity = restTemplate.exchange(newheaders.get("referer").get(0), method, newentity, String.class);
		    
//		    if(realurl.indexOf("auth")!=-1)
//		    
//		    	logger.info("Response Body: " + responseEntity.getBody());
		    
		    resp = new ResponseEntity(
		    		addURLProxy(responseEntity.getBody(), hostid), 
		    		responseEntity.getHeaders(), 
		    		responseEntity.getStatusCode());
		    
		}catch(Exception e) {
			
			e.printStackTrace();
			
			resp = errorControl(e.getLocalizedMessage(), hostid);
			
		}
		
	    return resp;
	    
	}
	
	/**
	 * Process GET Request
	 * @param headers
	 * @param method
	 * @param request
	 * @param hostid
	 * @return
	 * @throws URISyntaxException
	 */
	private ResponseEntity processGET(RequestEntity reqentity, HttpMethod method, HttpServletRequest request, String hostid) throws URISyntaxException
	{
		
//		return processUtil(reqentity, method, request, hostid);
		
		ResponseEntity resp = null;
		
		try {
			
			logger.info("==============");
			
			logger.info("This is a GET request...");
			
			logger.info("Request URI: " + request.getRequestURI());
			
			logger.info("Query String: " + request.getQueryString());
			
//			String realurl =  this.getRealRequestURL(request.getRequestURI());
//			
//			Host h = HostTool.getHostById(hostid);
//			
//			String[] ss = h.parseJupyterURL();
//			
//			URI uri = new URI(ss[0], null, ss[1], Integer.parseInt(ss[2]), realurl, request.getQueryString(), null);
//			
//			logger.info("URL: " + uri.toString());
//			
//			logger.info("HTTP Method: " + method.toString());
//			
//			logger.info("HTTP Headers: " + headers.toString());
//			
////			HttpEntity entity = new HttpEntity(headers);
//			
//			HttpHeaders newheaders = this.updateHeaderReferer(reqentity.getHeaders(), h, realurl, request.getQueryString());
			
			HttpHeaders newheaders = getHeaders(reqentity.getHeaders(), method, request, hostid);
			
			HttpEntity newentity = new HttpEntity(reqentity.getBody(), newheaders);
			
		    ResponseEntity<String> responseEntity = restTemplate.exchange(newheaders.get("referer").get(0), method, newentity, String.class);
		    
//		    if(realurl.equals("/tree"))
		    
//		    	logger.info("Response Body: " + responseEntity.getBody());
	    	
	    	logger.info("Response Header: " + responseEntity.getHeaders());
	    	
	    	logger.info("Response HTTP Code: " + responseEntity.getStatusCode());
	    	
	    	String newbody = addURLProxy(responseEntity.getBody(), hostid);
	    	
		    resp = new ResponseEntity<String>(
		    		newbody, 
		    		updateHeaderLength(responseEntity.getHeaders(), newbody, hostid), 
		    		responseEntity.getStatusCode());
		    
		}catch(Exception e) {
			
			e.printStackTrace();
			
			resp = errorControl(e.getLocalizedMessage(), hostid);
			
		}
		
	    return resp;
	    
	}
	
	HttpHeaders updateHeaderLength(HttpHeaders oldheaders, String returnbody, String hostid) {
		
		HttpHeaders newheaders = new HttpHeaders();
    	
		oldheaders.forEach((key, value) -> {
	    	
	    	if(key.toLowerCase().equals("location")) {
	    		
	    		newheaders.set(key, "/Geoweaver/web/jupyter-proxy/" + hostid + value.get(0));
	    		
	    	}else if (key.toLowerCase().equals("content-length")){
	    		
	    		newheaders.set(key, String.valueOf(returnbody.length()));
	    		
	    	}else {
	    		
	    		newheaders.set(key, value.get(0));
	    		
	    	}
	    	
	    });
		
		return newheaders;
		
	}
	
	@RequestMapping(value="/jupyter-proxy/{hostid}/login", method = RequestMethod.POST)
	public ResponseEntity jupyter_login( HttpMethod method, @PathVariable("hostid") String hostid, 
			@RequestHeader HttpHeaders httpheaders, HttpServletRequest request) throws URISyntaxException
	{
//		ResponseEntity resp = processPost(reqentity, method, request);
		
		ResponseEntity resp = null;
		
		
//		resp = processUtil(reqentity, method, request, hostid);
		
		try {
			
//			URI uri = new URI("https", null, server, port, request.getRequestURI(), request.getQueryString(), null);
			
			logger.info("==============");
			
			logger.info("Login attempt starts...");
			
			logger.info("Request URI: " + request.getRequestURI());
			
			logger.info("Query String: " + request.getQueryString());
			
			logger.info("Original Request String: " + request.getParameterMap());
			
//			String realurl =  this.getRealRequestURL(request.getRequestURI());
//			
//			Host h = HostTool.getHostById(hostid);
//			
//			String[] ss = h.parseJupyterURL();
//			
//			int current_port = Integer.parseInt(ss[2]);
//			
//			URI uri = new URI(ss[0], null, ss[1], current_port, realurl, request.getQueryString(), null);
//			
//			logger.info("URL: " + uri.toString());
//			
//			logger.info("HTTP Method: " + method.toString());
			
			HttpHeaders newheaders = getHeaders(httpheaders, method, request, hostid);
			
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

			
//			HttpHeaders newheaders = this.updateHeaderReferer(httpheaders, h, realurl, request.getQueryString());
			
			HttpEntity requestentity = new HttpEntity(reqstr.toString(), newheaders);
			
			logger.info("Body: " + requestentity.getBody());
			
			logger.info("Headers: " + requestentity.getHeaders());
			
		    ResponseEntity<String> responseEntity = restTemplate.exchange(newheaders.get("referer").get(0), method, requestentity, String.class);
		    
		    HttpHeaders respheaders = responseEntity.getHeaders();
		    
		    if(responseEntity.getStatusCode()==HttpStatus.FOUND) {
		    	
//		    	MultiValueMap<String, String> headers =new LinkedMultiValueMap<String, String>();
		    	
		    	HttpHeaders newresponseheaders = new HttpHeaders();
		    	
		    	logger.info("Redirection: " + newresponseheaders);
			    
			    logger.info("Response: " + responseEntity.getBody());
			    
//			    responseEntity = restTemplate.exchange(uri, method, requestentity, String.class);
			    
//			    responseEntity.getHeaders().compute("Location", (k, v) -> {v.clear(); v.add("/Geoweaver/web/jupyter-proxy/tree?");});
			    
//			    responseEntity.getHeaders().set("Location", "/Geoweaver/web/jupyter-proxy/tree?");
			    
//			    respheaders.set("Location", "/Geoweaver/web/jupyter-proxy/tree?");
			    
//			    respheaders.setLocation(new URI("/Geoweaver/web/jupyter-proxy/tree?"));
			    
//			    respheaders.add("Test", "Test Value");
			    
			    respheaders.forEach((key, value) -> {
			    	
			    	if(key.toLowerCase().equals("location")) {
			    		
			    		newresponseheaders.set(key, "/Geoweaver/web/jupyter-proxy/" + hostid + value.get(0));
			    		
			    	}else {
			    		
			    		newresponseheaders.set(key, value.get(0));
			    		
			    	}
			    	
			    });
			    
			    respheaders = newresponseheaders;
			    
//			    Set ent = respheaders.entrySet();
			    
			    logger.info(respheaders.toString());
		    	
		    }else if(responseEntity.getStatusCode()==HttpStatus.UNAUTHORIZED) {
		    	
		    	logger.error("Login Unauthorized");
		    	
		    }
		    
//		    resp = new ResponseEntity(null, respheaders, resp.getStatusCode());
		    
		    resp = new ResponseEntity(
		    		responseEntity.getBody(), 
		    		respheaders, 
		    		responseEntity.getStatusCode());
		    
			
		}catch(Exception e) {
			
			e.printStackTrace();
			
			resp = errorControl(e.getLocalizedMessage(), hostid);
			
		}
		
	    return resp;
	    
	}
	
	@RequestMapping(value="/jupyter-proxy/{hostid}/**", method = RequestMethod.DELETE)
	public ResponseEntity proxydelete( RequestEntity reqentity, @PathVariable("hostid") String hostid, HttpMethod method, HttpServletRequest request) throws URISyntaxException
	{
		ResponseEntity resp = processDelete(reqentity, method, request, hostid);
		
	    return resp;
	    
	}
	
	@RequestMapping(value="/jupyter-proxy/{hostid}/**", method = RequestMethod.PATCH)
	public ResponseEntity proxypatch( RequestEntity reqentity, @PathVariable("hostid") String hostid, HttpMethod method, HttpServletRequest request) throws URISyntaxException
	{
		ResponseEntity resp = processPatch(reqentity, method, request, hostid);
		
	    return resp;
	    
	}
	
	@RequestMapping(value="/jupyter-proxy/{hostid}/**", method = RequestMethod.PUT)
	public ResponseEntity proxyput( RequestEntity reqentity, @PathVariable("hostid") String hostid, HttpMethod method, HttpServletRequest request) throws URISyntaxException
	{
		ResponseEntity resp = processPut(reqentity, method, request, hostid);
		
	    return resp;
	    
	}
	
	@RequestMapping(value="/jupyter-proxy/{hostid}/**", method = RequestMethod.POST,
			consumes = MediaType.ALL_VALUE,
			produces = MediaType.ALL_VALUE)
	public ResponseEntity proxypost( RequestEntity reqentity, @PathVariable("hostid") String hostid, HttpMethod method, HttpServletRequest request) throws URISyntaxException
	{
		ResponseEntity resp = processPost(reqentity, method, request, hostid);
		
	    return resp;
	    
	}
	
	@RequestMapping(value="/jupyter-proxy/{hostid}/**", method = RequestMethod.GET)
	public ResponseEntity proxyget(RequestEntity reqentity, HttpMethod method, @PathVariable("hostid") String hostid, HttpServletRequest request) throws URISyntaxException
	{
		ResponseEntity resp = processGET( reqentity, method, request, hostid);
		
	    return resp;
	    
	}
	
	@RequestMapping(value="/jupyter-proxy/{hostid}", method = RequestMethod.GET)
	public ResponseEntity proxyroot_get(HttpMethod method, @PathVariable("hostid") String hostid, RequestEntity reqentity, HttpServletRequest request) throws URISyntaxException
	{
		ResponseEntity resp = processGET(reqentity, method, request, hostid);
		
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
	
	@RequestMapping(value = "/jupyter-websocket/{hostid}", method = RequestMethod.POST)
	public @ResponseBody String jupyter_websocket(ModelMap model, @PathVariable("hostid") String hostid, WebRequest request){
		
		String resp = null;
		
		try {
			
			
			
		}catch(Exception e) {
			
			throw new RuntimeException("failed " + e.getLocalizedMessage());
			
		}
		
		return resp;
		
	}

}
