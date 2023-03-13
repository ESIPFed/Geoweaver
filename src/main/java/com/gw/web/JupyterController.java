package com.gw.web;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
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
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.WebRequest;

import com.gw.jpa.Host;
import com.gw.tools.HistoryTool;
import com.gw.tools.HostTool;
import com.gw.utils.BaseTool;

@Controller
public class JupyterController {
	
	Logger logger = LoggerFactory.getLogger(getClass());
	
	private String scheme = "http";
	private String server = "localhost";
	private int port = 8888;
	
	@Autowired
	RestTemplate restTemplate;
	
	HttpHeaders headers = new HttpHeaders();
	
	@Autowired
	HistoryTool history_tool;
	
	@Autowired
	BaseTool bt;
	
	@Autowired
	HostTool ht;
	
	int TIMEOUT = 30000;
	
	public JupyterController(RestTemplateBuilder builder) {
		
	}
	
	@Bean(name = "restTemplate")
	@Scope("prototype")
    public RestTemplate getRestTemplate() {
		
		RestTemplate restTemplate1 = new RestTemplate();
		
		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
		requestFactory.setConnectTimeout(TIMEOUT);
		requestFactory.setReadTimeout(TIMEOUT);
		
		CloseableHttpClient httpClient = HttpClients.custom()
				
	            .setDefaultRequestConfig(RequestConfig.custom()
	            		.setCookieSpec(CookieSpecs.STANDARD)
						.setRedirectsEnabled(false)
	            		.build())
	            .build();
		
		restTemplate1.setRequestFactory(requestFactory);
		
		logger.debug("A new restTemplate is created");
		
        return restTemplate1;
    }
	
	/**
	 * Decode the url if it has spaces or other special characters
	 * @param referurl
	 * @return
	 */
	public String getRealTargetURL(String referurl) {
		
		String targeturl = referurl;
		try {
			targeturl = URLDecoder.decode(referurl,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		return targeturl;
		
	}
	
	/**
	 * Add URL Proxy, this function should be the difference among jupyter notebook, jupyterhub, and jupyterlab
	 * @param resp
	 * @param hostid
	 * @return
	 */
	String addURLProxy(String resp, String hostid) {
		
		if(!BaseTool.isNull(resp))
			resp = resp
				.replace("\"/static/", "\"/Geoweaver/jupyter-proxy/"+hostid+"/static/")
				.replace("\"/custom/custom.css\"", "\"/Geoweaver/jupyter-proxy/"+hostid+"/custom/custom.css\"")
				.replace("\"/login", "\"/Geoweaver/jupyter-proxy/"+hostid+"/login")
				.replace("\"/tree", "\"/Geoweaver/jupyter-proxy/"+hostid+"/tree")
				.replace("/static/base/images/logo.png", "/Geoweaver/jupyter-proxy/"+hostid+"/static/base/images/logo.png")
				.replace("baseUrl: '/static/',", "baseUrl: '/Geoweaver/jupyter-proxy/"+hostid+"/static/',")
				.replace("data-base-url=\"/\"", "data-base-url=\"/Geoweaver/jupyter-proxy/"+hostid+"/\"")
				.replace("this.base_url, \"api/kernels\"", "this.base_url.replace(\"jupyter-proxy\", \"jupyter-socket\"), \"api/kernels\"")
				.replace("that.base_url, \"api/kernels\"", "that.base_url.replace(\"jupyter-proxy\", \"jupyter-socket\"), \"api/kernels\"")
				.replace("src=\"/files/", "src=\"/Geoweaver/jupyter-proxy/"+hostid+"/files/")
				.replace("nbextensions : '/nbextensions'", "nbextensions : '../nbextensions'")
				.replace("custom : '/custom',", "custom : '../custom',")
				.replace("kernelspecs : '/kernelspecs',", "kernelspecs : '../kernelspecs',")
				
				//for jupyterhub
				.replace("\"/hub", "\"/Geoweaver/jupyter-proxy/"+hostid+"/hub")
				.replace("baseUrl: '/hub/static/js'", "baseUrl: '/Geoweaver/jupyter-proxy/"+hostid+"/hub/static/js'")
				.replace("href=\"/user", "href=\"/Geoweaver/jupyter-proxy/"+hostid+"/user")
				.replace("src=\"/user", "src=\"/Geoweaver/jupyter-proxy/"+hostid+"/user")
				.replace("src=\"/hub", "src=\"/Geoweaver/jupyter-proxy/"+hostid+"/hub")
				.replace("href='/hub", "href='/Geoweaver/jupyter-proxy/"+hostid+"/hub")
				.replace("baseUrl: '/user", "baseUrl: '/Geoweaver/jupyter-proxy/"+hostid+"/user")
				.replace("'/user/", "'/Geoweaver/jupyter-proxy/"+hostid+"/user/")
				.replace("data-base-url=\"/user/", "data-base-url=\"/Geoweaver/jupyter-proxy/"+hostid+"/user/")
				
				//for jupyterlab
				.replace("\"baseUrl\": \"/\"", "\"baseUrl\": \"/Geoweaver/jupyter-proxy/"+hostid+"/\"")

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
		
		HttpHeaders headers = new HttpHeaders();
		
		headers.add("Content-Length", "0");
		
		ResponseEntity resp = null;
		
		if(message.indexOf("403 Forbidden")!=-1) {
			
			resp = new ResponseEntity<String>(
					message, 
		    		updateHeader(headers, message, hostid), 
		    		HttpStatus.FORBIDDEN);
			
		}else if(message.indexOf("404 Not Found")!=-1) {
			
			resp = new ResponseEntity<String>(
					message, 
		    		updateHeader(headers, message, hostid), 
		    		HttpStatus.NOT_FOUND);
			
		}else if(message.indexOf("400 Bad Request")!=-1) {
			
			resp = new ResponseEntity<String>(
					message, 
		    		updateHeader(headers, message, hostid), 
		    		HttpStatus.BAD_REQUEST);
			
		}else {
			
			resp = new ResponseEntity<String>(
					message, 
		    		updateHeader(headers, message, hostid), 
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
		
		String realurl =  requesturi.substring(requesturi.indexOf("jupyter-proxy") + 20); // http://localhost:8070/Geoweaver/jupyter-proxy/bf0vd7/
		
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
		
			String[] ss = bt.parseJupyterURL(h.getUrl());
			
			URI uri = new URI(ss[0], null, ss[1], Integer.parseInt(ss[2]), realurl, querystr, null);
			
			String hosturl = ss[0] + "://" + ss[1] + ":" + ss[2];
			
			newheaders =  HttpHeaders.writableHttpHeaders(oldheaders);
			
			newheaders.set("host", ss[1] + ":" + ss[2]);
			
			newheaders.set("origin", hosturl);
			
			newheaders.set("target_url", URLDecoder.decode(uri.toString(), "utf-8"));

			newheaders.set("referer", URLDecoder.decode(uri.toString(), "utf-8"));
			
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
			
			logger.debug("==============");
			
//			logger.debug("Ensuring Unicode to UTF");
			
			logger.debug("Request URI: " + request.getRequestURI());
			 
			HttpHeaders newheaders = getHeaders(entity.getHeaders(), method, request, hostid); 
			
			HttpEntity newentity = new HttpEntity(entity.getBody(), newheaders);
			
//			logger.debug("URL: " + newheaders.get("referer").get(0));

			String target_url = getRealTargetURL(newheaders.get("target_url").get(0));
			
		    ResponseEntity<String> responseEntity = restTemplate.exchange(target_url, method, newentity, String.class);
		    
		    String newbody = addURLProxy(responseEntity.getBody(), hostid);
	    	
		    resp = new ResponseEntity<String>(
		    		newbody,  
		    		updateHeader(responseEntity.getHeaders(), newbody, hostid), 
		    		responseEntity.getStatusCode());

		    
		}catch (HttpStatusCodeException ex) {
		    
		    String newbody = addURLProxy(ex.getResponseBodyAsString(), hostid);
		    
		    resp = errorControl(newbody, hostid);
		    
		}catch(Exception e) {
			
			e.printStackTrace();
			
			resp = errorControl(e.getLocalizedMessage(), hostid);
			
		}
		
	    return resp;
		
	}
	
	/**
	 * Get content type from headers
	 * @param headers
	 * @return
	 */
	private String getHeaderProperty(HttpHeaders headers, String key) {
		
		String contenttype = null;
    	
		List<String> cts = headers.get(key);
    	
    	if(!BaseTool.isNull(cts))
    		contenttype = cts.get(0);
    	
//    	logger.debug(key + " : " + contenttype);
    	
    	return contenttype;
		
	}
	
	
	private HttpHeaders getHeaders(HttpHeaders headers, HttpMethod method, HttpServletRequest request, String hostid) throws NumberFormatException, URISyntaxException {
		
		HttpHeaders newheaders = headers;
		
		try {

			String realurl =  this.getRealRequestURL(request.getRequestURI());
			
			Host h = ht.getHostById(hostid);
			
			newheaders = this.updateHeaderReferer(headers, h, realurl, request.getQueryString());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
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
	private ResponseEntity processPatch( HttpMethod method, HttpServletRequest request, String hostid) {
		
//		return processUtil(entity, method, request, hostid);
		
		ResponseEntity resp = null;
		
		try {
			
			logger.debug("==============");
			
			logger.debug("Request URI: " + request.getRequestURI());
			
//			logger.debug("Request Headers: " + entity.getHeaders());
			
//			logger.info("Query String: " + request.getQueryString());
			
//			String realurl =  this.getRealRequestURL(request.getRequestURI());
//			
//			Host h = HostTool.getHostById(hostid);
//			
//			logger.info("HTTP Method: " + method.toString());
//			
//			restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
//			
//			HttpHeaders newheaders = this.updateHeaderReferer(entity.getHeaders(), h, realurl, request.getQueryString());
			
			HttpHeaders newheaders = getHeaders(this.getHeaderByRequest(request), method, request, hostid);
			
			HttpEntity newentity = new HttpEntity(bt.getBody(request), newheaders);
			
//			logger.debug("URL: " + newheaders.get("referer").get(0));
			
		    ResponseEntity<String> responseEntity = restTemplate.exchange(getRealTargetURL(newheaders.get("target_url").get(0)), method, newentity, String.class);
		    
		    resp = new ResponseEntity(
		    		addURLProxy(responseEntity.getBody(), hostid), 
		    		responseEntity.getHeaders(), 
		    		responseEntity.getStatusCode());
		    
		}catch (HttpStatusCodeException ex) {
		    
		    // http status code e.g. `404 NOT_FOUND`
//		    logger.error(ex.getStatusCode().toString());
		    
		    // get response body
//		    System.out.println(ex.getResponseBodyAsString());
		    
		    // get http headers
//		    HttpHeaders headers = ex.getResponseHeaders();
//		    System.out.println(headers.get("Content-Type"));
//		    System.out.println(headers.get("Server"));
		    
		    String newbody = addURLProxy(ex.getResponseBodyAsString(), hostid);
		    
		    resp = errorControl(newbody, hostid);
		    
		}catch(Exception e) {
			
			e.printStackTrace();
			
			resp = errorControl(e.getLocalizedMessage(), hostid);
			
		}
		
	    return resp;
		
	}

	/**
	 * Get headers by request
	 * @param request
	 * @return
	 */
	private HttpHeaders getHeaderByRequest(HttpServletRequest request){

		HttpHeaders header = new HttpHeaders();

		Enumeration<String> hearderNames = request.getHeaderNames();

		while(hearderNames.hasMoreElements())
		{
			String headerName = hearderNames.nextElement();

			header.add(headerName, request.getHeader(headerName));

		}

		return header;

	}

	/**
	 * Process PUT request
	 * @param entity
	 * @param method
	 * @param request
	 * @param hostid
	 * @return
	 */
	private ResponseEntity processPut(HttpMethod method, HttpServletRequest request, String hostid) {
		
		ResponseEntity resp = null;
		
		try {
			
			logger.debug("==============");

			logger.debug("PUT request without httpentity...");
			
			logger.debug("Request URI: " + request.getRequestURI());
			
//			logger.debug("Query String: " + request.getQueryString());
			
//			String body = IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8);
			
			HttpHeaders newheaders = getHeaders(this.getHeaderByRequest(request), method, request, hostid);

			String body = bt.getBody(request);

			//only save the content when the request content is jupyter notebook
			// logger.debug("PUT request received, body: " + body);

			if(body.contains("\"type\":\"notebook\"")){
				
				history_tool.saveJupyterCheckpoints(hostid, body, newheaders);
				
			}
			
			HttpEntity newentity = new HttpEntity(body, newheaders);
			
		    ResponseEntity<String> responseEntity = restTemplate.exchange(
				getRealTargetURL(newheaders.get("referer").get(0)), 
				method, newentity, String.class);
		    
		    resp = new ResponseEntity(
				addURLProxy(responseEntity.getBody(), hostid), 
				responseEntity.getHeaders(), 
				responseEntity.getStatusCode());
		    
		}catch (HttpStatusCodeException ex) {
		    
		    // http status code e.g. `404 NOT_FOUND`
		    logger.error(ex.getStatusCode().toString());
		    
		    // get response body
//		    System.out.println(ex.getResponseBodyAsString());
		    
		    // get http headers
//		    HttpHeaders headers = ex.getResponseHeaders();
//		    System.out.println(headers.get("Content-Type"));
//		    System.out.println(headers.get("Server"));
		    
		    String newbody = addURLProxy(ex.getResponseBodyAsString(), hostid);
		    
		    resp = errorControl(newbody, hostid);
		    
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
			
			logger.debug("==============");

			logger.debug("PUT request...");
			
			logger.debug("Request URI: " + request.getRequestURI());
			
//			logger.debug("Query String: " + request.getQueryString());
			
//			String body = IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8);
			
			HttpHeaders newheaders = getHeaders(entity.getHeaders(), method, request, hostid);
			
			HttpEntity newentity = new HttpEntity(entity.getBody(), newheaders);
			
			//only save the content when the request content is jupyter notebook
			if("notebook".equals(((Map)entity.getBody()).get("type"))) {
				
				String jsonString = new JSONObject((Map)entity.getBody()).toString();
				
				history_tool.saveJupyterCheckpoints(hostid, jsonString, newheaders);
				
			}
			
		    ResponseEntity<String> responseEntity = restTemplate.exchange(getRealTargetURL(newheaders.get("referer").get(0)), method, newentity, String.class);
		    
		    resp = new ResponseEntity(
		    		addURLProxy(responseEntity.getBody(), hostid), 
		    		responseEntity.getHeaders(), 
		    		responseEntity.getStatusCode());
		    
		}catch (HttpStatusCodeException ex) {
		    
		    // http status code e.g. `404 NOT_FOUND`
		    logger.error(ex.getStatusCode().toString());
		    
		    // get response body
//		    System.out.println(ex.getResponseBodyAsString());
		    
		    // get http headers
//		    HttpHeaders headers = ex.getResponseHeaders();
//		    System.out.println(headers.get("Content-Type"));
//		    System.out.println(headers.get("Server"));
		    
		    String newbody = addURLProxy(ex.getResponseBodyAsString(), hostid);
		    
		    resp = errorControl(newbody, hostid);
		    
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
			
			logger.debug("==============");
			
			logger.debug("Request URI: " + request.getRequestURI());
			
//			logger.debug("Query String: " + request.getQueryString());
			
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

			String target_url = getRealTargetURL(newheaders.get("target_url").get(0));
			
		    ResponseEntity<String> responseEntity = restTemplate.exchange(target_url, method, newentity, String.class);
		    
		    resp = new ResponseEntity(
		    		addURLProxy(responseEntity.getBody(), hostid), 
		    		responseEntity.getHeaders(), 
		    		responseEntity.getStatusCode());
		    
		}catch (HttpStatusCodeException ex) {
		    
		    // http status code e.g. `404 NOT_FOUND`
//		    logger.error(ex.getStatusCode().toString());
		    
		    // get response body
//		    System.out.println(ex.getResponseBodyAsString());
		    
		    // get http headers
//		    HttpHeaders headers = ex.getResponseHeaders();
//		    System.out.println(headers.get("Content-Type"));
//		    System.out.println(headers.get("Server"));
		    
		    String newbody = addURLProxy(ex.getResponseBodyAsString(), hostid);
		    
		    resp = errorControl(newbody, hostid);
		    
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
	private ResponseEntity processPost_415(HttpMethod method, HttpServletRequest request, String hostid) throws URISyntaxException
	{
		
//		return processUtil(reqentity, method, request, hostid);
		
		ResponseEntity resp = null;
		
		try {
			
			logger.debug("==============");
			
			logger.debug("Request URI: " + request.getRequestURI());
			
			HttpHeaders newheaders = getHeaders(this.getHeaderByRequest(request), method, request, hostid);
			
			HttpEntity newentity = new HttpEntity(bt.getBody(request), newheaders);

			String target_url = getRealTargetURL(newheaders.get("target_url").get(0));
			
		    ResponseEntity<String> responseEntity = restTemplate.exchange(target_url, method, newentity, String.class);

			String newbody = addURLProxy(responseEntity.getBody(), hostid);

			HttpHeaders newrespheaders = updateHeader(responseEntity.getHeaders(), newbody, hostid);

		    resp = new ResponseEntity(
					newbody, 
					newrespheaders, 
		    		responseEntity.getStatusCode());
		    
		}catch (HttpStatusCodeException ex) {
		    
		    String newbody = addURLProxy(ex.getResponseBodyAsString(), hostid);
		    
		    resp = errorControl(newbody, hostid);
		    
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
			
			logger.debug("==============");
			
			logger.debug("Request URI: " + request.getRequestURI());
			
//			logger.info("Query String: " + request.getQueryString());
			
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

			String target_url = getRealTargetURL(newheaders.get("target_url").get(0));
			
		    ResponseEntity<String> responseEntity = restTemplate.exchange(target_url, method, newentity, String.class);
		    
//		    if(realurl.indexOf("auth")!=-1)
//		    
//		    	logger.info("Response Body: " + responseEntity.getBody());
		    
			String newbody = addURLProxy(responseEntity.getBody(), hostid);

			HttpHeaders newrespheaders = updateHeader(responseEntity.getHeaders(), newbody, hostid);

		    resp = new ResponseEntity(
					newbody, 
					newrespheaders, 
		    		responseEntity.getStatusCode());
		    
		}catch (HttpStatusCodeException ex) {
		    
		    // http status code e.g. `404 NOT_FOUND`
//		    logger.error(ex.getStatusCode().toString());
		    
		    // get response body
//		    System.out.println(ex.getResponseBodyAsString());
		    
		    // get http headers
//		    HttpHeaders headers = ex.getResponseHeaders();
//		    System.out.println(headers.get("Content-Type"));
//		    System.out.println(headers.get("Server"));
		    
		    String newbody = addURLProxy(ex.getResponseBodyAsString(), hostid);
		    
		    resp = errorControl(newbody, hostid);
		    
		}catch(Exception e) {
			
			e.printStackTrace();
			
			resp = errorControl(e.getLocalizedMessage(), hostid);
			
		}
		
	    return resp;
	    
	}

	private <T> ResponseEntity<T> processGet_415(HttpMethod method, HttpServletRequest request, String hostid) throws URISyntaxException
	{
		
		ResponseEntity resp = null;
		
		try {
			
			logger.debug("==============");
			
			// logger.debug("This is a GET request...");
			
			boolean ishub = false;
			
			if(request.getRequestURI().contains("user")) ishub = true;
			
			if(request.getRequestURI().contains("api/kernels")){

				logger.info("URI: " + request.getRequestURI());

				logger.info("Old Request HTTP Headers: " + this.getHeaderByRequest(request));

			}
			
			
			HttpHeaders newheaders = getHeaders(this.getHeaderByRequest(request), method, request, hostid);
			
			HttpEntity newentity = new HttpEntity(bt.getBody(request), newheaders);
			
			String targeturl = getRealTargetURL(newheaders.get("target_url").get(0)); //using referer as the target url is not right
			// String targeturl = getRealTargetURL(reqentity.getUrl().toString());
			
			// logger.info("New target url: " + targeturl);
			
			// if(ishub)logger.info("New Request HTTP Headers: " + newheaders.toString());
			
//			String sec_fetch_type = getHeaderProperty(reqentity.getHeaders(), "Sec-Fetch-Dest");
			
//			logger.debug(URLDecoder.decode(newheaders.get("referer").get(0),"UTF-8"));
			
//			((SimpleClientHttpRequestFactory)restTemplate.getRequestFactory()).setConnectTimeout(TIMEOUT);

			ResponseEntity<byte[]> responseEntity = restTemplate.exchange(targeturl, method, newentity, byte[].class);

			String contenttype = getHeaderProperty(responseEntity.getHeaders(), "Content-Type");

			byte[] newbody = null;

			if(!BaseTool.isNull(responseEntity.getBody()) && !targeturl.contains(".png") && !targeturl.contains(".woff")
			 && !(!BaseTool.isNull(contenttype) && (contenttype.contains("image") || contenttype.contains("font"))) ){

				newbody =  addURLProxy(new String(responseEntity.getBody()), hostid).getBytes();

			}else{

				newbody = responseEntity.getBody();

			}

			// if(ishub) logger.debug("Old Response Header: " + responseEntity.getHeaders().toString());
			
			HttpHeaders headers = updateHeader(responseEntity.getHeaders(), newbody, hostid);
			
			// if(ishub) logger.debug("New Response Header: " + headers.toString());

			resp = new ResponseEntity<byte[]>(
					newbody, 
					headers, 
					responseEntity.getStatusCode());
		    
		}catch (HttpStatusCodeException ex) {
		    
		    // http status code e.g. `404 NOT_FOUND`
//		    logger.error(ex.getStatusCode().toString());
		    
		    // get response body
//		    System.out.println(ex.getResponseBodyAsString());
		    
		    // get http headers
//		    HttpHeaders headers = ex.getResponseHeaders();
//		    System.out.println(headers.get("Content-Type"));
//		    System.out.println(headers.get("Server"));
		    
		    String newbody = addURLProxy(ex.getResponseBodyAsString(), hostid);
		    
		    resp = errorControl(newbody, hostid);
		    
		}catch(Exception e) {
			
			e.printStackTrace();
			
			resp = errorControl(e.getLocalizedMessage(), hostid);
			
		}
		
	    return resp;
	    
	}
	
	/**
	 * Process GET Request
	 * @param <T>
	 * @param headers
	 * @param method
	 * @param request
	 * @param hostid
	 * @return
	 * @throws URISyntaxException
	 */
	private <T> ResponseEntity<T> processGET(RequestEntity reqentity, HttpMethod method, HttpServletRequest request, String hostid) throws URISyntaxException
	{
		
//		return processUtil(reqentity, method, request, hostid);
		
		ResponseEntity resp = null;
		
		try {
			
			logger.debug("==============");
			
			// logger.debug("This is a GET request...");
			
			logger.debug("Request URI: " + reqentity.getUrl().toString());
			
			boolean ishub = false;
			
			if(request.getRequestURI().contains("user")) ishub = true;
			
			// if(ishub)logger.info("Old Request HTTP Headers: " + reqentity.getHeaders().toString());
			
			HttpHeaders newheaders = getHeaders(reqentity.getHeaders(), method, request, hostid);
			
			HttpEntity newentity = new HttpEntity(reqentity.getBody(), newheaders);
			
			String targeturl = getRealTargetURL(newheaders.get("target_url").get(0)); //using referer as the target url is not right
			// String targeturl = getRealTargetURL(reqentity.getUrl().toString());
			
			// logger.info("New target url: " + targeturl);
			
			// if(ishub)logger.info("New Request HTTP Headers: " + newheaders.toString());
			
//			String sec_fetch_type = getHeaderProperty(reqentity.getHeaders(), "Sec-Fetch-Dest");
			
//			logger.debug(URLDecoder.decode(newheaders.get("referer").get(0),"UTF-8"));
			
//			((SimpleClientHttpRequestFactory)restTemplate.getRequestFactory()).setConnectTimeout(TIMEOUT);

			ResponseEntity<byte[]> responseEntity = restTemplate.exchange(targeturl, method, newentity, byte[].class);

			String contenttype = getHeaderProperty(responseEntity.getHeaders(), "Content-Type");

			byte[] newbody = null;

			if(!BaseTool.isNull(responseEntity.getBody()) && !targeturl.contains(".png") && !targeturl.contains(".woff")
			 && !(!BaseTool.isNull(contenttype) && (contenttype.contains("image") || contenttype.contains("font"))) ){

				newbody =  addURLProxy(new String(responseEntity.getBody()), hostid).getBytes();

			}else{

				newbody = responseEntity.getBody();

			}

			// if(ishub) logger.debug("Old Response Header: " + responseEntity.getHeaders().toString());
			
			HttpHeaders headers = updateHeader(responseEntity.getHeaders(), newbody, hostid);
			
			// if(ishub) logger.debug("New Response Header: " + headers.toString());

			resp = new ResponseEntity<byte[]>(
					newbody, 
					headers, 
					responseEntity.getStatusCode());

			
// 			if(targeturl.contains(".png") || targeturl.contains(".woff")) {
				
// 				ResponseEntity<byte[]> responseEntity = restTemplate.exchange(targeturl, method, newentity, byte[].class);
// //				
// //				String newbody = new String(responseEntity.getBody());
// //				
// //				HttpHeaders headers = updateHeader(responseEntity.getHeaders(), newbody, hostid);
// //			    
// //				resp = new ResponseEntity<byte[]>(
// //						responseEntity.getBody(), 
// //			    		headers, 
// //			    		responseEntity.getStatusCode());;
				
// 				resp = responseEntity;
				
// 			}else {
				
// 				ResponseEntity<String> responseEntity = restTemplate.exchange(targeturl, method, newentity, String.class);
			    
		    	
// 		    	String newbody = responseEntity.getBody();
		    	
// 		    	String contenttype = getHeaderProperty(responseEntity.getHeaders(), "Content-Type");
		    	
// 		    	// if(BaseTool.isNull(newbody)|| ( !BaseTool.isNull(contenttype) && (contenttype.contains("image")
// 				// 	|| contenttype.contains("font")) )) {
// 				if( !BaseTool.isNull(contenttype) && (contenttype.contains("image") || contenttype.contains("font")) ) {
// //			    	HttpHeaders headers = updateHeader(responseEntity.getHeaders(), newbody, hostid);
// //			    	
// //			    	resp = new ResponseEntity<byte[]>(
// //							BaseTool.isNull(newbody)?null:newbody.getBytes("UTF-8"), 
// //				    		headers, 
// //				    		responseEntity.getStatusCode());;
// 		    		// find a way not send the same request twice just because the type is not byte for image/font files
// 		    		resp = restTemplate.exchange(targeturl, method, newentity, byte[].class);
		    		
// //		    		resp = responseEntity;
		    		
// 		    	}else {
		    		
// 			    	if(BaseTool.isNull(responseEntity.getBody())){
// 						resp = new ResponseEntity<byte[]>(
// 								null,
// 								headers, 
// 								responseEntity.getStatusCode());
// 					}else{
						
// 						newbody = addURLProxy(responseEntity.getBody(), hostid);

// 						HttpHeaders headers = updateHeader(responseEntity.getHeaders(), newbody, hostid);

// 						resp = new ResponseEntity<byte[]>(
// 								newbody.getBytes("UTF-8"), 
// 								headers, 
// 								responseEntity.getStatusCode());
// 					}

	    		
// 		    	}
// //		    	
	    		
				
// 			}
			
		    
		    
		}catch (HttpStatusCodeException ex) {
		    
		    // http status code e.g. `404 NOT_FOUND`
//		    logger.error(ex.getStatusCode().toString());
		    
		    // get response body
//		    System.out.println(ex.getResponseBodyAsString());
		    
		    // get http headers
//		    HttpHeaders headers = ex.getResponseHeaders();
//		    System.out.println(headers.get("Content-Type"));
//		    System.out.println(headers.get("Server"));
		    
		    String newbody = addURLProxy(ex.getResponseBodyAsString(), hostid);
		    
		    resp = errorControl(newbody, hostid);
		    
		}catch(Exception e) {
			
			e.printStackTrace();
			
			resp = errorControl(e.getLocalizedMessage(), hostid);
			
		}
		
	    return resp;
	    
	}

	/**
	 * 
	 * @param oldheaders
	 * @param bodylength
	 * @param hostid
	 * @return
	 */
	HttpHeaders updateHeader(HttpHeaders oldheaders, int bodylength, String hostid) {
		
		HttpHeaders newheaders = new HttpHeaders();
		
		oldheaders.forEach((key, value) -> {
	    	
			try {

				String lowkey = key.toLowerCase();

		    	if(lowkey.equals("location")) {
		    		
		    		newheaders.set(lowkey, "/Geoweaver/jupyter-proxy/" + hostid + value.get(0));
		    		
		    	}else if(lowkey.equals("transfer-encoding") && value.get(0).equals("chunked")){

					logger.info("skip the header property of transfer encoding and value is chunked");

				// }else if(lowkey.equals("cache-control")){

				// 	logger.info("remove cache control");

				}else if (lowkey.equals("content-length")){
		    		
//		    		logger.debug("Old Content Length: " + value);
		    		
					newheaders.set(lowkey, String.valueOf(bodylength));
					
				}else if(lowkey.equals("set-cookie")){

					List newvalues = new ArrayList();

					for(String singleval : value){
						
						String newsingleval = singleval.replace("Path=", "Path="+"/Geoweaver/jupyter-proxy/" + hostid );
						String newsingleval_socket = singleval.replace("Path=", "Path="+"/Geoweaver/jupyter-socket/" + hostid );

						newvalues.add(newsingleval);
						newvalues.add(newsingleval_socket);

						// jupyterhub-user-zsun-oauth-state-uypLrTlm=""; expires=Mon, 16 Mar 2020 03:08:07 GMT; Path=/Geoweaver/jupyter-proxy/urlwti/user/zsun/
						
						if(singleval.contains("-oauth-state-") && singleval.contains("=\"\";")){
							
							int first_break = singleval.indexOf("-oauth-state-") + 12 ;
							int second_break = singleval.indexOf("=\"\"");
							singleval = singleval.substring(0, first_break) + singleval.substring(second_break);
							
							newsingleval = singleval.replace("Path=", "Path="+"/Geoweaver/jupyter-proxy/" + hostid );
							newsingleval_socket = singleval.replace("Path=", "Path="+"/Geoweaver/jupyter-socket/" + hostid );

							newvalues.add(newsingleval);
							newvalues.add(newsingleval_socket);

						}
						
						// newvalues.add(singleval.replace("Path=", "Path="+"/Geoweaver/jupyter-socket/" + hostid ));

					}

					newheaders.addAll(lowkey, newvalues);
			
		    	}else {
		    		
		    		// newheaders.set(lowkey, value.get(0));
					newheaders.addAll(lowkey, value);
		    		
		    	}
	    	
			} catch (Exception e) {
				e.printStackTrace();
			}
	    });
		
		
		return newheaders;
		
	}

	/**
	 * Update Header Length and Origin and location
	 * @param oldheaders
	 * @param returnbody
	 * @param hostid
	 * @return
	 */
	HttpHeaders updateHeader(HttpHeaders oldheaders, byte[] returnbody, String hostid) {
		
		int bodylength = 0;

		if(!BaseTool.isNull(returnbody))
			bodylength = returnbody.length;

		return updateHeader(oldheaders, bodylength, hostid);
		
	}
	
	/**
	 * Update Header Length and Origin and location
	 * @deprecated
	 * @param oldheaders
	 * @param returnbody
	 * @param hostid
	 * @return
	 */
	HttpHeaders updateHeader(HttpHeaders oldheaders, String returnbody, String hostid) {
		
		int bodylength = 0;
		if(!BaseTool.isNull(returnbody))
			try {
				bodylength = returnbody.getBytes("UTF-8").length;
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		return updateHeader(oldheaders, bodylength, hostid);
		
	}
	
	@RequestMapping(value="/jupyter-proxy/{hostid}/hub/login", method = RequestMethod.POST)
	public ResponseEntity jupyterhub_login( HttpMethod method, @PathVariable("hostid") String hostid, 
			@RequestHeader HttpHeaders httpheaders, HttpServletRequest request) throws URISyntaxException
	{
//		ResponseEntity resp = processPost(reqentity, method, request);
		
		ResponseEntity resp = null;
		
		
//		resp = processUtil(reqentity, method, request, hostid);
		
		try {
			
//			URI uri = new URI("https", null, server, port, request.getRequestURI(), request.getQueryString(), null);
			
			logger.debug("==============");
			
			logger.debug("Login attempt starts...");
			
			logger.debug("Request URI: " + request.getRequestURI());
			
			logger.info("HTTP Method: " + method.toString());
			
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

			HttpEntity requestentity = new HttpEntity(reqstr.toString(), newheaders);

			String target_url = getRealTargetURL(newheaders.get("target_url").get(0));
			
		    ResponseEntity<String> responseEntity = restTemplate.exchange(target_url, method, requestentity, String.class);
		    
			HttpHeaders respheaders = updateHeader(responseEntity.getHeaders(), responseEntity.getBody(), hostid);

		    resp = new ResponseEntity(
		    		responseEntity.getBody(), 
		    		respheaders, 
		    		responseEntity.getStatusCode());
		    
			
		}catch (HttpStatusCodeException ex) {
		    
		    String newbody = addURLProxy(ex.getResponseBodyAsString(), hostid);
		    
		    resp = errorControl(newbody, hostid);
		    
		}catch(Exception e) {
			
			e.printStackTrace();
			
			resp = errorControl(e.getLocalizedMessage(), hostid);
			
		}
		
	    return resp;
	    
	}

	@RequestMapping(value="/jupyter-proxy/{hostid}/lab/login", method = RequestMethod.POST)
	public ResponseEntity jupyterlab_login( HttpMethod method, @PathVariable("hostid") String hostid, 
			@RequestHeader HttpHeaders httpheaders, HttpServletRequest request) throws URISyntaxException
	{
//		ResponseEntity resp = processPost(reqentity, method, request);
		
		ResponseEntity resp = null;
		
		
//		resp = processUtil(reqentity, method, request, hostid);
		
		try {
			
//			URI uri = new URI("https", null, server, port, request.getRequestURI(), request.getQueryString(), null);
			
			logger.debug("==============");
			
			logger.debug("Login attempt starts...");
			
			logger.debug("Request URI: " + request.getRequestURI());
			
			logger.info("HTTP Method: " + method.toString());
			
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

			HttpEntity requestentity = new HttpEntity(reqstr.toString(), newheaders);

			String target_url = getRealTargetURL(newheaders.get("target_url").get(0));
			
		    ResponseEntity<String> responseEntity = restTemplate.exchange(target_url, method, requestentity, String.class);
		    
			HttpHeaders respheaders = updateHeader(responseEntity.getHeaders(), responseEntity.getBody(), hostid);

		    resp = new ResponseEntity(
		    		responseEntity.getBody(), 
		    		respheaders, 
		    		responseEntity.getStatusCode());
		    
			
		}catch (HttpStatusCodeException ex) {
		    
		    String newbody = addURLProxy(ex.getResponseBodyAsString(), hostid);
		    
		    resp = errorControl(newbody, hostid);
		    
		}catch(Exception e) {
			
			e.printStackTrace();
			
			resp = errorControl(e.getLocalizedMessage(), hostid);
			
		}
		
	    return resp;
	    
	}
	
	/**
	 * Login Jupyter Notebook
	 * @param method
	 * @param hostid
	 * @param httpheaders
	 * @param request
	 * @return
	 * @throws URISyntaxException
	 */
	@RequestMapping(value="/jupyter-proxy/{hostid}/login", method = RequestMethod.POST)
	public ResponseEntity jupyter_login( HttpMethod method, @PathVariable("hostid") String hostid, 
			@RequestHeader HttpHeaders httpheaders, HttpServletRequest request) throws URISyntaxException
	{
//		ResponseEntity resp = processPost(reqentity, method, request);
		
		ResponseEntity resp = null;
		
		
//		resp = processUtil(reqentity, method, request, hostid);
		
		try {
			
//			URI uri = new URI("https", null, server, port, request.getRequestURI(), request.getQueryString(), null);
			
			logger.debug("==============");
			
			logger.debug("Login attempt starts...");
			
			logger.debug("Request URI: " + request.getRequestURI());
			
			logger.info("Query String: " + request.getQueryString());
			
			logger.info("Original Request String: " + request.getParameterMap());
			
			logger.info("Old Headers: " + httpheaders);
			
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
			logger.info("HTTP Method: " + method.toString());
			
			HttpHeaders newheaders = getHeaders(httpheaders, method, request, hostid);
			
//			MultiValueMap<String, String> map= new LinkedMultiValueMap<String, String>();
			
			Iterator hmIterator = request.getParameterMap().entrySet().iterator(); 
			  
	        // Iterate through the hashmap 
			
			StringBuffer reqstr = new StringBuffer();
	  
	        while (hmIterator.hasNext()) { 
	            
	        	Map.Entry mapElement = (Map.Entry)hmIterator.next(); 
	            
	        	String key = (String)mapElement.getKey();
	        	
	        	String value = (((String[])(mapElement.getValue()))[0]);
	        	
	        	if(!BaseTool.isNull(reqstr.toString())) {
	            	
	            	reqstr.append("&");
	            	
	            }

	        	// if(key.equals("_xsrf")) {
	        		
	        	// 	newheaders.set("cookie", "_xsrf="+value);
	        		
	        	// 	logger.info("Cookie XSRF: " + value);
	        		
	        	// }
	        	
	        	reqstr.append(key).append("=").append(value);
	            
	        }
	        
//			HttpHeaders newheaders = this.updateHeaderReferer(httpheaders, h, realurl, request.getQueryString());
			
			HttpEntity requestentity = new HttpEntity(reqstr.toString(), newheaders);
			
			logger.info("Body: " + requestentity.getBody());
			
			logger.info("New Headers: " + requestentity.getHeaders());
			
			String target_url = getRealTargetURL(newheaders.get("target_url").get(0));
			
		    ResponseEntity<String> responseEntity = restTemplate.exchange(target_url, method, requestentity, String.class);
		    
		    HttpHeaders respheaders = responseEntity.getHeaders();
		    
		    if(responseEntity.getStatusCode()==HttpStatus.FOUND) {
		    	
		    	HttpHeaders newresponseheaders = new HttpHeaders();
		    	
			    respheaders.forEach((key, value) -> {
			    	
			    	if(key.toLowerCase().equals("location")) {
			    		
			    		newresponseheaders.set(key, "/Geoweaver/jupyter-proxy/" + hostid + value.get(0));
			    		
			    	}else {
			    		
			    		newresponseheaders.addAll(key, value);
			    		
			    	}
			    	
			    });
			    
			    respheaders = newresponseheaders;
			    
			    logger.info(respheaders.toString());
		    	
		    }else if(responseEntity.getStatusCode()==HttpStatus.UNAUTHORIZED) {
		    	
		    	logger.error("Login Unauthorized");
		    	
		    }
		    
		    resp = new ResponseEntity(
		    		responseEntity.getBody(), 
		    		respheaders, 
		    		responseEntity.getStatusCode());
		    
			
		}catch (HttpStatusCodeException ex) {
		    
		    String newbody = addURLProxy(ex.getResponseBodyAsString(), hostid);
		    
		    resp = errorControl(newbody, hostid);
		    
		}catch(Exception e) {
			
			e.printStackTrace();
			
			resp = errorControl(e.getLocalizedMessage(), hostid);
			
		}
		
	    return resp;
	    
	}
	
	@RequestMapping(value="/jupyter-proxy/{hostid}/**", method = RequestMethod.DELETE,
		consumes = MediaType.ALL_VALUE,
		produces = MediaType.ALL_VALUE)
	public ResponseEntity proxydelete( RequestEntity reqentity, @PathVariable("hostid") String hostid, HttpMethod method, HttpServletRequest request) throws URISyntaxException
	{
		ResponseEntity resp = processDelete(reqentity, method, request, hostid);
		
	    return resp;
	    
	}
	
	@RequestMapping(value="/jupyter-proxy/{hostid}/**", 
		method = RequestMethod.PATCH,
		consumes = MediaType.ALL_VALUE,
		produces = MediaType.ALL_VALUE)
	public ResponseEntity proxypatch( @PathVariable("hostid") String hostid, HttpMethod method, HttpServletRequest request) throws URISyntaxException
	{
		ResponseEntity resp = processPatch(method, request, hostid);
		
	    return resp;
	    
	}

	
	@RequestMapping(value="/jupyter-proxy/{hostid}/**", 
	// 	method = RequestMethod.PUT,
	// 	consumes = MediaType.ALL_VALUE,
	// 	produces = MediaType.ALL_VALUE)
	// public ResponseEntity proxyput( RequestEntity reqentity, @PathVariable("hostid") String hostid, HttpMethod method, HttpServletRequest request) throws URISyntaxException
	// {
	// 	ResponseEntity resp = processPut(reqentity, method, request, hostid);
		
	//     return resp;
	    
	// }
	// @RequestMapping(value="/jupyter-proxy/{hostid}/**/lab/api/workspaces/**",
		method = RequestMethod.PUT,
		consumes = MediaType.ALL_VALUE,
		produces = MediaType.ALL_VALUE)
	public ResponseEntity proxyput_415( @PathVariable("hostid") String hostid, HttpMethod method,  HttpServletRequest request) throws URISyntaxException
	{

		logger.info("Proxy Put 415 ");

		ResponseEntity resp = processPut(method, request, hostid);
		
	    return resp;
	    
	}
	
	
	

	@RequestMapping(value="/jupyter-proxy/{hostid}/**/api/sessions/**", 
		method = RequestMethod.POST,
		consumes = MediaType.ALL_VALUE,
		produces = MediaType.ALL_VALUE)
	public ResponseEntity proxypost( @PathVariable("hostid") String hostid, HttpMethod method, HttpServletRequest request) throws URISyntaxException
	{
		ResponseEntity resp = processPost_415(method, request, hostid);
		
	    return resp;
	    
	}

	@RequestMapping(value="/jupyter-proxy/{hostid}/**", 
		method = RequestMethod.POST,
		consumes = MediaType.ALL_VALUE,
		produces = MediaType.ALL_VALUE)
	public ResponseEntity proxypost( RequestEntity reqentity, @PathVariable("hostid") String hostid, HttpMethod method, HttpServletRequest request) throws URISyntaxException
	{
		ResponseEntity resp = processPost(reqentity, method, request, hostid);
		
	    return resp;
	    
	}
	
	@RequestMapping(value="/jupyter-proxy/{hostid}/**", 
	// 	method = RequestMethod.GET,
	// 	consumes = MediaType.ALL_VALUE,
	// 	produces = MediaType.ALL_VALUE)
	// public ResponseEntity proxyget(RequestEntity reqentity, HttpMethod method, @PathVariable("hostid") String hostid, HttpServletRequest request) throws URISyntaxException
	// {
	// 	ResponseEntity resp = processGET( reqentity, method, request, hostid);
		
	//     return resp;
	    
	// }

	// @RequestMapping(value="/jupyter-proxy/{hostid}/**/api/sessions/**", 
		method = RequestMethod.GET,
		consumes = MediaType.ALL_VALUE,
		produces = MediaType.ALL_VALUE)
	public ResponseEntity proxyget( @PathVariable("hostid") String hostid, HttpMethod method, HttpServletRequest request) throws URISyntaxException
	{
		ResponseEntity resp = processGet_415(method, request, hostid);
		
	    return resp;
	    
	}
	
	@RequestMapping(value="/jupyter-proxy/{hostid}", 
		method = RequestMethod.GET,
		consumes = MediaType.ALL_VALUE,
		produces = MediaType.ALL_VALUE)
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
	
	@RequestMapping(value = "/jupyter-http", 
		method = RequestMethod.GET,
		consumes = MediaType.ALL_VALUE,
		produces = MediaType.ALL_VALUE)
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
	
	@RequestMapping(value = "/jupyter-https", 
		method = RequestMethod.POST,
		consumes = MediaType.ALL_VALUE,
		produces = MediaType.ALL_VALUE)
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
	
}
