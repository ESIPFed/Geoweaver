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
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
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
import com.gw.utils.LoggingRequestInterceptor;

/**
 * 
 * Controller for Google Earth related activities.
 * 
 * @author Ahmed Alnaim
 * 
 * @date 31 Jul 2021
 * 
 */

@Controller
public class GoogleEarthController {
    
    Logger logger = LoggerFactory.getLogger(getClass());

    private String scheme = "https";

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
    

    public GoogleEarthController(RestTemplateBuilder builder) {

    }

    @Bean(name = "GErestTemplate")
    @Scope("prototype")
    public RestTemplate getRestTemplate() {

        RestTemplate GErestTemplate = new RestTemplate();

        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();

        requestFactory.setConnectTimeout(TIMEOUT);
        requestFactory.setReadTimeout(TIMEOUT);

        CloseableHttpClient httpClient = HttpClients.custom()
        
            .setDefaultRequestConfig(RequestConfig.custom()
                .setCookieSpec(CookieSpecs.STANDARD)
                    .setRedirectsEnabled(false)
                    .build())
            .build();

        requestFactory.setHttpClient(httpClient);
        GErestTemplate.setRequestFactory(requestFactory);

        logger.info("A new Google Earth restTemplate is created");

        return GErestTemplate;
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
		
			String[] ss = bt.parseGoogleEarthURL(h.getUrl());
			
            // URI uri = new URI(ss[0], null, ss[1], Integer.parseInt(ss[2]), realurl, querystr, null);
            URI uri = new URI("https", null, "https://code.earthengine.google.com/", 443, realurl, querystr, null);
			
			String hosturl = ss[0] + "://" + ss[1] + ":" + ss[2];
			
			newheaders =  HttpHeaders.writableHttpHeaders(oldheaders);
			
			// newheaders.set("host", ss[1] + ":" + ss[2]);
			
			// newheaders.set("origin", hosturl);
			
			// newheaders.set("target_url", URLDecoder.decode(uri.toString(), "utf-8"));

			newheaders.set("referer", URLDecoder.decode(uri.toString(), "utf-8"));
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}
		
		return newheaders;
		
	}


    private HttpHeaders getHeaders(HttpHeaders headers, HttpMethod method, HttpServletRequest request, String hostid) throws NumberFormatException, URISyntaxException {
		
		HttpHeaders newheaders = headers;
		
		try {

			// String realurl =  this.getRealRequestURL(request.getRequestURI());
			
			// Host h = ht.getHostById(hostid);
			
            // newheaders = this.updateHeaderReferer(headers, h, realurl, request.getQueryString());

			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return newheaders;
		
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
				
		ResponseEntity resp = null;
		
		try {
			
			logger.debug("==============");
			
			// logger.debug("This is a GET request...");
			
			logger.debug("Request Google 🌍 URI: " + reqentity.getUrl().toString());
						
			
			HttpHeaders newheaders = getHeaders(reqentity.getHeaders(), method, request, hostid);
			
			HttpEntity newentity = new HttpEntity(reqentity.getBody(), newheaders);
			
			// String targeturl = getRealTargetURL(newheaders.get("target_url").get(0)); //using referer as the target url is not right
			
			// logger.info("New target url: " + targeturl);
			

			// ResponseEntity<byte[]> responseEntity = restTemplate.exchange("https://code.earthengine.google.com/", method, newentity, byte[].class);

            // URI uri = new URI("https", null, "https://code.earthengine.google.com/", 443, request.getRequestURI(), request.getQueryString(), null);

            ResponseEntity<ResponseEntity> responseEntity =
                restTemplate.exchange("https://code.earthengine.google.com/", HttpMethod.GET, newentity, ResponseEntity.class);
        
            return responseEntity.getBody();
			// String contenttype = getHeaderProperty(responseEntity.getHeaders(), "Content-Type");

			// byte[] newbody = null;

			// if(!bt.isNull(responseEntity.getBody()) && !targeturl.contains(".png") && !targeturl.contains(".woff")
			//  && !(!bt.isNull(contenttype) && (contenttype.contains("image") || contenttype.contains("font"))) ){

				// newbody =  addURLProxy(new String(responseEntity.getBody()), hostid).getBytes();

			// }else{

				// newbody = responseEntity.getBody();

			// }

			// if(ishub) logger.debug("Old Response Header: " + responseEntity.getHeaders().toString());
			
			// HttpHeaders headers = updateHeader(responseEntity.getHeaders(), newbody, hostid);
			
			// if(ishub) logger.debug("New Response Header: " + headers.toString());

			// resp = new ResponseEntity<byte[]>(
			// 		newbody, 
			// 		headers, 
			// 		responseEntity.getStatusCode());

			
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
		    	
// 		    	// if(bt.isNull(newbody)|| ( !bt.isNull(contenttype) && (contenttype.contains("image")
// 				// 	|| contenttype.contains("font")) )) {
// 				if( !bt.isNull(contenttype) && (contenttype.contains("image") || contenttype.contains("font")) ) {
// //			    	HttpHeaders headers = updateHeader(responseEntity.getHeaders(), newbody, hostid);
// //			    	
// //			    	resp = new ResponseEntity<byte[]>(
// //							bt.isNull(newbody)?null:newbody.getBytes("UTF-8"), 
// //				    		headers, 
// //				    		responseEntity.getStatusCode());;
// 		    		// find a way not send the same request twice just because the type is not byte for image/font files
// 		    		resp = restTemplate.exchange(targeturl, method, newentity, byte[].class);
		    		
// //		    		resp = responseEntity;
		    		
// 		    	}else {
		    		
// 			    	if(bt.isNull(responseEntity.getBody())){
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
		    
		    // String newbody = addURLProxy(ex.getResponseBodyAsString(), hostid);
		    
		    // resp = errorControl(newbody, hostid);
		    
		}catch(Exception e) {
			
			e.printStackTrace();
			
			// resp = errorControl(e.getLocalizedMessage(), hostid);
			
		}
		
	    return resp;
	    
	}




    @RequestMapping(value="/GoogleEarth-proxy/{hostid}", 
    method = RequestMethod.GET,
    consumes = MediaType.ALL_VALUE,
    produces = MediaType.ALL_VALUE)
public ResponseEntity proxyroot_get(HttpMethod method, @PathVariable("hostid") String hostid, RequestEntity reqentity, HttpServletRequest request) throws URISyntaxException
{
    logger.info(reqentity.toString());
    logger.info(method.toString());
    logger.info(request.toString());
    logger.info(hostid.toString());
    ResponseEntity resp = processGET(reqentity, method, request, hostid);
    // URI yahoo = new URI("https://code.earthengine.google.com/");
    // HttpHeaders httpHeaders = new HttpHeaders();
    // httpHeaders.setLocation(yahoo);
    // return new ResponseEntity<>(httpHeaders, HttpStatus.SEE_OTHER);
    


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





}
