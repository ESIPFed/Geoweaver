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
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.protocol.HTTP;
import org.glassfish.grizzly.http.HttpHeader;
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

import javassist.bytecode.stackmap.BasicBlock.Catch;

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
	RestTemplate GErestTemplate;
	
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
	

	public String getRealTargetURL(String referurl) {

		String targeturl = referurl;

		try{
			targeturl = URLDecoder.decode(referurl, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return targeturl;
	}

    /**
     * modify returned url for service being proxied.
     * @param resp
     * @param hostID
     * @return
     */
    String addURLProxy(String resp, String hostID) {
        if (!bt.isNull(resp))
            resp = resp.
                replace("\"/images/GoogleEarthEngine_v1.png", "\"/Geoweaver/GoogleEarth-proxy/"+hostID+"/images/GoogleEarthEngine_v1.png")
                .replace("\"/javascript/polyfills/webcomponentsjs/webcomponents-loader.js", "\"/Geoweaver/GoogleEarth-proxy/"+hostID+"/javascript/polyfills/webcomponentsjs/webcomponents-loader.js")
                .replace("\"/javascript/polyfills/web-animations-next-lite.min.js", "\"/Geoweaver/GoogleEarth-proxy/"+hostID+"/javascript/polyfills/web-animations-next-lite.min.js")
                .replace("\"/javascript/playground.js", "\"/Geoweaver/GoogleEarth-proxy/"+hostID+"/javascript/playground.js")
                .replace("\"/css/playground.css", "\"/Geoweaver/GoogleEarth-proxy/"+hostID+"/css/playground.css")
                .replace("\"/javascript/autotrack.js", "\"/Geoweaver/GoogleEarth-proxy/"+hostID+"/javascript/autotrack.js")
                .replace("\"/javascript/escodegen.js", "\"/Geoweaver/GoogleEarth-proxy/"+hostID+"/javascript/escodegen.js")
                .replace("\"/ace/ace.js", "\"/Geoweaver/GoogleEarth-proxy/"+hostID+"/ace/ace.js")
                .replace("\"/javascript/acorn.js", "\"/Geoweaver/GoogleEarth-proxy/"+hostID+"/javascript/acorn.js")
                .replace("\"/javascript/stan.js", "\"/Geoweaver/GoogleEarth-proxy/"+hostID+"/javascript/stan.js")
                .replace("\"/images/vertical_handle.png", "\"/Geoweaver/GoogleEarth-proxy/"+hostID+"/images/vertical_handle.png")
                .replace("\"/images/bigicon.png", "\"/Geoweaver/GoogleEarth-proxy/"+hostID+"/images/bigicon.png")
                .replace("\"/images/info.png", "\"/Geoweaver/GoogleEarth-proxy/"+hostID+"/images/info.png")
                .replace("\"/images/loading.gif", "\"/Geoweaver/GoogleEarth-proxy/"+hostID+"/images/loading.gif")
                .replace("\"/images/locked.svg", "\"/Geoweaver/GoogleEarth-proxy/"+hostID+"/images/locked.svg")
                .replace("\"/images/zippy-tab.svg", "\"/Geoweaver/GoogleEarth-proxy/"+hostID+"/images/zippy-tab.svg")
                .replace("\"/images/handle.png", "\"/Geoweaver/GoogleEarth-proxy/"+hostID+"/images/handle.png")
                .replace("\"/favicon.png", "\"/Geoweaver/GoogleEarth-proxy/"+hostID+"/favicon.png")
                .replace("\"/preferences/set", "\"/Geoweaver/GoogleEarth-proxy/"+hostID+"/preferences/set")
                .replace("\"/namespace/user", "\"/Geoweaver/GoogleEarth-proxy/"+hostID+"/namespace/user")
                .replace("\"/cloud/projects", "\"/Geoweaver/GoogleEarth-proxy/"+hostID+"/cloud/projects")
                .replace("\"/versions/script_manager", "\"/Geoweaver/GoogleEarth-proxy/"+hostID+"/versions/script_manager")
                .replace("\"/repo/list?only_default=true", "\"/Geoweaver/GoogleEarth-proxy/"+hostID+"/repo/list?only_default=true")
                .replace("\"/scripts/load", "\"/Geoweaver/GoogleEarth-proxy/"+hostID+"/scripts/load")
				.replace("\"/docs/get", "\"/Geoweaver/GoogleEarth-proxy/"+hostID+"/docs/get")
				.replace("\"/auth/refresh", "\"/Geoweaver/GoogleEarth-proxy/"+hostID+"/auth/refresh")

				// gStatic Domain (www.gstatic.com)
				.replace("https://gstatic.com", "/Geoweaver/GoogleEarth-proxy/"+hostID + "/?gee_proxy_url=https://gstatic.com")
				// .replace("https://www.gstatic.com", "/Geoweaver/GoogleEarth-proxy/"+hostID + "/?gee_proxy_url=https://www.gstatic.com")

				// content-earthengine Domain (https://content-earthengine.googleapis.com)
				.replace("https://www.content-earthengine.googleapis.com", "/Geoweaver/GoogleEarth-proxy/"+hostID + "/?gee_proxy_url=https://www.content-earthengine.googleapis.com")
				
				// developers.google.com, some of the links are for information popup only, just in case
				.replace("https://www.developers.google.com", "/Geoweaver/GoogleEarth-proxy/"+hostID + "/?gee_proxy_url=https://www.developers.google.com")
				
				// GoogleAPIs domain for user auth (www.googleapis.com)
				.replace("https://www.googleapis.com", "/Geoweaver/GoogleEarth-proxy/"+hostID + "/?gee_proxy_url=https://www.googleapis.com")

				// Maps domain (www.maps.googleapis.com)
				.replace("https://www.maps.googleapis.com", "/Geoweaver/GoogleEarth-proxy/"+hostID + "/?gee_proxy_url=https://maps.googleapis.com")
				
				// Maps Resources domain (www.maps.gstatic.com)
				// .replace("https://www.maps.gstatic.com", "/Geoweaver/GoogleEarth-proxy/"+hostID + "/?gee_proxy_url=https://www.maps.gstatic.com")

				// .replace("https://code.earthengine.google.com", "/Geoweaver/GoogleEarth-proxy/"+hostID + "/?gee_proxy_url=https://code.earthengine.google.com")

				// .replace("this.aa.send", "console.log(this.aa), console.log('find content-earthengine - ' + a), this.aa.send")
				// .replace("this.aa.open(b,String(a)", "this.aa.open(b,'/Geoweaver/GoogleEarth-proxy/"+hostID + "/?gee_proxy_url='+ String(a)") 
				.replace("this.aa.open(b,String(a)", "this.aa.open(b,String(a).startsWith('https://content-earthengine')? '/Geoweaver/GoogleEarth-proxy/"+hostID + "/?gee_proxy_url='+String(a): (String(a).startsWith('/repo')?'/Geoweaver/GoogleEarth-proxy/"+hostID + "'+String(a):String(a)) ")
				.replace("this.aa.open(b, String(a)", "this.aa.open(b,String(a).startsWith('https://content-earthengine')? '/Geoweaver/GoogleEarth-proxy/"+hostID + "/?gee_proxy_url='+String(a): (String(a).startsWith('/repo')?'/Geoweaver/GoogleEarth-proxy/"+hostID + "'+String(a):String(a)) ")

				//this should only escape the content-earthengine url, the second parameter could be something else which should not be proxied. More code is required here. 

				// earthengine.googleapis.com Domain (https://earthengine.googleapis.com)
				.replace("https://www.earthengine.googleapis.com", "/Geoweaver/GoogleEarth-proxy/"+hostID + "/?gee_proxy_url=https://www.earthengine.googleapis.com");

        return resp;
    }


    /**
     * Error Control
     * @param message
     * @param hostID
     * @return
     */
    private ResponseEntity errorControl(String message, String hostID) {

        HttpHeaders headers = new HttpHeaders();

        headers.add("Content-Length", "0");

        ResponseEntity resp = null;

        if(message.indexOf("403 Forbidden")!=-1) {
            
            resp = new ResponseEntity<String>(message,
                    updateHeader(headers, message, hostID),
                    HttpStatus.FORBIDDEN);

        }else if(message.indexOf("404 Not Found")!=-1) {

            resp = new ResponseEntity<String>(message,
                    updateHeader(headers, message, hostID),
                    HttpStatus.NOT_FOUND);
        } else if (message.indexOf("400 Bad Request")!=-1) {

            resp = new ResponseEntity<String>(message,
                    updateHeader(headers, message, hostID),
                    HttpStatus.BAD_REQUEST);
        }else {

            resp = new ResponseEntity<String>(message,
                    updateHeader(headers, message, hostID),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return resp;
	}
	
	/**
	 * Get actual url request from GEE 
	 * @param requesturi
	 * @return
	 */
	private String getRealRequestURL(String requesturi) {
		
		
		String realurl = requesturi.substring(requesturi.indexOf("GoogleEarth-proxy") + 24); // http://localhost:8070/Geoweaver/GoogleEarth-proxy/ikpyln/

		if (realurl.contains(".map")) {
			realurl = "";
		}
		return realurl;
	}
		

	private String getHeaderProperty(HttpHeaders headers, String key) {


		String contentType = null;

		List<String> cts = headers.get(key);

		if (!bt.isNull(cts)) {
			contentType = cts.get(0);
		}

		return contentType;

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
	 * Update Header Referers
	 * @param oldheaders
	 * @param h
	 * @param realurl
	 * the suffix after the host ip and port
	 * @param querystr
	 * the query string after question mark ?
	 * @return
	 */
	private HttpHeaders updateRequestHeader(HttpHeaders oldheaders, Host h, String realurl, String querystr) {
		
		HttpHeaders newheaders = oldheaders;
		// logger.debug("OLD HEADERS for /userInfo: "+oldheaders.toString());
		try {
            // logger.debug("Old Headers [updateRequestHeader]: "+oldheaders.toString());
			// String[] ss = bt.parseGoogleEarthURL(h.getUrl());
			
            // URI uri = new URI(ss[0], null, ss[1], Integer.parseInt(ss[2]), realurl, querystr, null);
            // URI uri = new URI("https", null, "code.earthengine.google.com", 443, realurl, querystr, null);
			
			// String hosturl = ss[0] + "://" + ss[1] + ":" + ss[2];
			
			newheaders =  HttpHeaders.writableHttpHeaders(oldheaders);
			// logger.debug("New Headers [updateRequestHeader]: "+newheaders.toString());
			logger.debug("{{{Real URL}}} : "+realurl);

			if (realurl.contains("oauth2/v3/userinfo")) {
				newheaders.set("Host", "www.googleapis.com");
			}else {
				newheaders.set("Host", "code.earthengine.google.com");
			}
			// newheaders.set("Authorization", "Bearer ya29.a0ARrdaM--b1xUZoFH43HDyskOSMzCmwuWWK-eE6IPu-IoJJtoXcIYoee4weLuISp_STBFUtyYpAXbHPd2X4c93v2Ul5aEYuY93FvqRxnMv3u9TYqJ2moKwP0X77Rbt-V9aojqaxRsmm1uwLbBToLYtZjv2Tg3aoEdjH_GwXY");
			
			// newheaders.set("origin", hosturl);
			
			newheaders.set("target_url", "https://code.earthengine.google.com" + realurl);
			// newheaders.set("Sec-Fetch-Site", "cross-site");
			// newheaders.set("referer", "https://www.google.com/");


			// newheaders.set("Cookie", "1P_JAR=2021-08-05-12; NID=220=u-LNrAEPFdeVsyrsuNGH0vrMJ3evUk4slFvTlkocypTzT91hRtopv46iZRpMlHcZCQZKYHOvBsE14Zr4MFMvQHOZYTCwM4Di4V75jbOW4wpblD7T2x6foy0mk4iPtZ8N8YAP8yxNdccj1fuHjGZUY7bs14qiDEajPtdNj_5bHYYvKKpUdhOnP-gKN_3ltFNWYM7xiz7syIwSkr9POusMykTmAIHX; ANID=AHWqTUmyDLiPVRBJu2uzjfg53HwdKlMzJyUKA78SFMsPVc5xhGvmznup-DkrB7ly; SID=AQhGIWBfoujDsbSy4_9oOunWFqQ7EuWKKf0k0wOxi4Ywc14pM5lbwRABPhYyf97rjEu_PQ.; __Secure-1PSID=AQhGIWBfoujDsbSy4_9oOunWFqQ7EuWKKf0k0wOxi4Ywc14pNoN0Y-J4WbYq4-0NfLkhDw.; __Secure-3PSID=AQhGIWBfoujDsbSy4_9oOunWFqQ7EuWKKf0k0wOxi4Ywc14p2TsQpztYwrUoyDtmWBOygw.; HSID=AVCbW41BibNQwwfpw; SSID=AeVfYx3QAMXuH7u8m; APISID=kM8fFOubCVp2wktp/A_1AQLK1Lw9OfqLag; SAPISID=rwlezJ0aGcpTLyBw/A4q4W1uy-a62RaEki; __Secure-1PAPISID=rwlezJ0aGcpTLyBw/A4q4W1uy-a62RaEki; __Secure-3PAPISID=rwlezJ0aGcpTLyBw/A4q4W1uy-a62RaEki; SIDCC=AJi4QfFZ52seUH9wZwqciRVHmOF7HTjCIzBINyb9SdHkGMCt2kDzCanVu21iW-1XQAHaJhQg9w; __Secure-3PSIDCC=AJi4QfHU0Uq-zpB51tNkPzL_d0aSIcF9JVt50sc_PFwjpDIQ2iR1PUZXnESASB0VmehJ6VOMhg; SACSID=~AJKiYcH5eS4ox9v2GZNDXKqNpP5N32RNiI6_DozJSJ1cS8-rUcS0QFLh8PJBEwXtyxBl5YVZbtytOL06aKPJ9jL5zSCALiYLqJPr-7Y4Ywg_Q539EBJs9j_kqdlsJi3HLovBkBVLkjz5q0Q1sKGmD7CmRQ98q9owAtfUTWyRB4cGPkIm0gfTOi-hhJQ7JJhyCqtoH-vuCCa8StcS9q7M-CpezzG4qBZwrDfKCibpbVja12q-UwTQs8v-VN4-Gf3U9qVNL4NibaTva0dqwc7J8nVBnS8YTpRmmjM2j96OBQsXbLFMhJm9tG4m8FqtaNoSbwVwclXQh1BB");
			// newheaders.set("Cookie", "NID=222=IYiayD1FmZUeOeqBOzh2sB2jm-62QqTuzzJSGb2xJzYSs6r2p-4O7cw3n1cJ1RkoQ01np0yyLLbYIrG3QMzM094eJxOcLrQnZViH_1J5O647Kqat4KuaoHrOaXhnVN3H_m33BimjpNp3r7aLjgkt4uLwLp8Sp0j4_cD_rtfRFLivUOFPoR1Ht54V8DCrTeBdp_AV1S4; SID=BghxcHyB2amqONMILi7zeAsWXY0iRFunToXIP9oy5_89jm_ySAkNbTl3NQllb-XBzpS2fA.; __Secure-1PSID=BghxcHyB2amqONMILi7zeAsWXY0iRFunToXIP9oy5_89jm_y1fQRgJCVB1Mo6EONqhdrlg.; __Secure-3PSID=BghxcHyB2amqONMILi7zeAsWXY0iRFunToXIP9oy5_89jm_y4EJ-n43IGJnBzAPb0tx9gw.; HSID=AJVF1x2_Hp5TfgteS; SSID=A_muf920ZHqtoCfEk; AP…gQZtL8F3R_skuSXaww5uSINlkKQm8bf5zOxSqjXUAzKGs7nRtzPhEzWs; __Secure-3PSIDCC=AJi4QfHY-0iTHe7jz4VXMovftDj-ZT4q1nOhm33LxdiHWqfD77zJbV8mRRNPi4cN8_khZDQv; SACSID=~AJKiYcGqZM8Ik2_V6I-OTf2E87PN2W07gJTexJyuKl0eOGkZKYlT1Cno2o4tPi1QAk9Z2gF0CW0rK_w-9Vae8TI00so6tlFOVi95wpnBFs7LF7O-gK7pJmBiuw4Dbwam7L6XppbGgBYxt8Qi1R2QnIdrgVgoKv4ODIT3WQXx7Vs1in7IbqcyGWfQRXHK1ptN8sXGYAWM-Znd9B_XEDOLeplF1Hx06fF7XFxzx5aOiHVteMZnwPBUnzrNtX_9m45QVYr6mavgPuIJFE6bDNvALecfaVoiyUaWMYMBAA_A1wRVkOhgJnaMNjKogF7tGB4yx0gbfk39q16Ecy-Nia8yb0QQTw62R_QX4A");
			// newheaders.set("Cookie", oldheaders.get("Set-Cookie").tostring());
			// 1P_JAR=2021-08-12-01; NID=221=gs77G5RrU9YasubvYfDG192a42VeO4Zm5xchyiPnAzJV2AcT-IqF8zVSQbj2xIbZ4N_E3pTPSEm65wY2z98jFUSSiCQ0ecSjK63ZvyKi1lxrSoDqRhMxX1EIaRC7NLjz646kXQkW_KSvLdf1fjZzHSZ9l0n2tI2nccdE6_RUlpvZLhhlWAHZRpYu6g4Ous1f1x6adKxDnmezKlztDzrR3Qc12dGSHVtLEmPH; ANID=AHWqTUm85ZxHX2mrE-c6EKGzx7hKbsJzMFQlQLZhthM4k5F6hpt3NI1AlOFViSGW; SID=AAhxcHhNH_TKdXEYs9qCXRc1JfiEw9pwFtKeq5eaEBXjZ8F1q7mc-TTH9klo_A7BtVU7LA.; __Secure-1PSID=AAhxcHhNH_TKdXEYs9qCXRc1JfiEw9pwFtKeq5eaEBXjZ8F1_qZB4LRJn6B4uwAyGd5drQ.; __Secure-3PSID=AAhxcHhNH_TKdXEYs9qCXRc1JfiEw9pwFtKeq5eaEBXjZ8F1r8Hz4RUgKwlDxjnC_iQN0A.; HSID=AYMx7e7laiBhGMDVE; SSID=Ak-l61fPxGhwTz84Q; APISID=
			// newheaders.set("Upgrade-Insecure-Requests", "1");

			// newheaders.set("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:89.0) Gecko/20100101 Firefox/89.0");

			// newheaders.set("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");

			// newheaders.set("Accept-Language", "en-US,en;q=0.5");

			// newheaders.set("Accept-Encoding", "gzip, deflate, br");

			// newheaders.set("Alt-Used", "code.earthengine.google.com");

			// newheaders.set("Connection", "keep-alive");

		} catch (Exception e) {
			
			e.printStackTrace();
			
		}

		return newheaders;
		
	}

	/**
	 * Update response header
	 * @param oldheaders
	 * @param h
	 * @param realurl
	 * @param querystr
	 * @return
	 */
	private HttpHeaders updateResponseHeader(HttpHeaders oldheaders, byte[] body, String hostid) {
		// logger.debug("Old Headers: "+oldheaders.toString());
		HttpHeaders newheaders = oldheaders;
		
		try {
		
			// String[] ss = bt.parseGoogleEarthURL(h.getUrl());
			
            // URI uri = new URI(ss[0], null, ss[1], Integer.parseInt(ss[2]), realurl, querystr, null);
            // URI uri = new URI("https", null, "code.earthengine.google.com", 443, realurl, querystr, null);
			
			// String hosturl = ss[0] + "://" + ss[1] + ":" + ss[2];
			
			newheaders =  HttpHeaders.writableHttpHeaders(oldheaders);
			
			newheaders.set("Host", "code.earthengine.google.com");
			// newheaders.set("Cookie", newheaders.get("Set-Cookie").toString());
            // logger.debug("New Headers [updateResponseHeader]: "+newheaders.get("Set-Cookie"));
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}
		
		return newheaders;
		
	}


    private HttpHeaders getHeaders(HttpHeaders headers, HttpMethod method, HttpServletRequest request, String hostid) throws NumberFormatException, URISyntaxException {
		
		HttpHeaders newheaders = headers;

        try {

			String realurl =  this.getRealRequestURL(request.getRequestURI());
			
			Host h = ht.getHostById(hostid);
			
            newheaders = this.updateRequestHeader(headers, h, realurl, request.getQueryString());

			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return newheaders;
		
	}

	HttpHeaders updateHeader(HttpHeaders oldHeaders, int bodylength, String hostid) {
		
		HttpHeaders newHeaders = new HttpHeaders();

		// logger.debug("{[{Old Headers}]}: " + oldHeaders);

		oldHeaders.forEach((key, value) -> {

			try {

				String lowkey = key.toLowerCase();

				if(lowkey.equals("location")) {

					logger.debug("Location Header: " + lowkey);
					newHeaders.set(lowkey, "/Geoweaver/GoogleEarth-proxy/" + hostid + value.get(0));
				
				}else if(lowkey.equals("transfer-encoding") && value.get(0).equals("chunky")){
					
					logger.info("Skiping header property of transfer-encoding chunk");

				}else if (lowkey.equals("content-length")) {
					
					newHeaders.set(lowkey, String.valueOf(bodylength));

				} else if(lowkey.equals("Set-Cookie")){
					logger.debug("IT Does have Set-Cookie 🍪");
					List newValues = new ArrayList();

					for (String singleval : value) {

						String newsingleval = singleval.replace("Path=", "Path="+"/Geoweaver/GoogleEarth-proxy/" + hostid);

						newValues.add(newsingleval);

						if (singleval.contains("-oauth-state-") && singleval.contains("=\"\";")){
							logger.debug("[[[Cookie contains Oauth]]]:");
						}
					}

					newHeaders.addAll(lowkey, newValues);
				
				}else {
					newHeaders.addAll(lowkey, value);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

		});
		// logger.debug("!!!New Headers!!!: " + newHeaders);

		return newHeaders;
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

		if(!bt.isNull(returnbody))
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

		if(!bt.isNull(returnbody))
			try{
				bodylength = returnbody.getBytes("UTF-8").length;

			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

		return updateHeader(oldheaders, bodylength, hostid);
		
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
	private ResponseEntity processGET(RequestEntity reqentity, HttpMethod method, HttpServletRequest request, String hostid) throws URISyntaxException
	{
				
		ResponseEntity resp = null;
		
		try {
			
			logger.debug("==============");
			
			// logger.debug("This is a GET request...");
			
			logger.debug("Request Google 🌍 URI: " + reqentity.getUrl().toString());
						
			
			HttpHeaders newheaders = getHeaders(reqentity.getHeaders(), method, request, hostid);
			
			HttpEntity newentity = new HttpEntity(bt.isNull(reqentity.getBody())?"":reqentity.getBody(), newheaders);
			
			String targeturl = "";

			if (bt.isNull(request.getParameter("gee_proxy_url"))) {
			
				targeturl = getRealTargetURL(newheaders.get("target_url").get(0)); //using referer as the target url is not right
			
			} else {

				targeturl = request.getParameter("gee_proxy_url");

				if (targeturl.contains("https://www.googleapis.com/oauth2/v3/userinfo")) {
					
					newheaders.set("Host", "www.googleapis.com");

				} else if (targeturl.contains("https://maps.googleapis.com")){
					
					newheaders.set("Host", "maps.googleapis.com");
					newheaders.set("Referer", "https://code.earthengine.google.com/");

				} else {
					newheaders.set("Host", "content-earthengine.googleapis.com");
					newheaders.set("Referer", "https://code.earthengine.google.com/");
				}
			}
			logger.debug("NEW HEADERS for /userInfo: "+newheaders.toString());

			logger.debug("[Target URL GoogleEarth]: "+targeturl);
			// logger.info("New target url: " + targeturl);
			

			// ResponseEntity<byte[]> responseEntity = restTemplate.exchange("https://code.earthengine.google.com/", method, newentity, byte[].class);

            // URI uri = new URI("https", null, "https://code.earthengine.google.com/", 443, request.getRequestURI(), request.getQueryString(), null);

			// logger.debug(newentity.toString());



            ResponseEntity<byte[]> responseEntity =
                GErestTemplate.exchange(targeturl, HttpMethod.GET, newentity, byte[].class);
		
				
			String contenttype = getHeaderProperty(responseEntity.getHeaders(), "Content-Type");


                
				// logger.debug("TEST!!!!: " + headers.toString());
				// logger.debug(headers.toString());
				// logger.debug(newheaders.toString());
				
			byte[] newbody = null;

			if(targeturl.endsWith("playground.js")){

				logger.debug("checkpoint");

			}
			
			if(!bt.isNull(responseEntity.getBody()) && !targeturl.contains(".png") && !targeturl.contains(".woff")
			 && !(!bt.isNull(contenttype) && (contenttype.contains("image") || contenttype.contains("font"))) ){
				 
				 newbody =  addURLProxy(new String(responseEntity.getBody()), hostid).getBytes();

				 

			}else {
				newbody = responseEntity.getBody();
			}
			
				
			// HttpHeaders headers = updateResponseHeader(responseEntity.getHeaders(), newbody, hostid);
			HttpHeaders headers = updateHeader(responseEntity.getHeaders(), newbody, hostid);


			resp = new ResponseEntity<byte[]>(
					newbody, 
					headers, 
					responseEntity.getStatusCode());

			
		    
		    
		}catch (HttpStatusCodeException ex) {
		    
			ex.printStackTrace();
		    // http status code e.g. `404 NOT_FOUND`
		    logger.error(ex.getStatusCode().toString());
		    
		    // get response body
		    System.out.println(ex.getResponseBodyAsString());
		    
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


// 	@RequestMapping(value="/GoogleEarth-proxy/{hostid}/scripts/load", 
//     method = RequestMethod.GET,
//     consumes = MediaType.ALL_VALUE,
//     produces = MediaType.ALL_VALUE)
// public ResponseEntity proxyroot_get_scripts(HttpMethod method, @PathVariable("hostid") String hostid, RequestEntity reqentity, HttpServletRequest request) throws URISyntaxException
// {
//     logger.info("/SCRIPTS/LOAD: "+reqentity.toString());
//     logger.info("/SCRIPTS/LOAD: "+method.toString());
//     logger.info("/SCRIPTS/LOAD: "+request.toString());
// 	logger.info("/SCRIPTS/LOAD: "+hostid.toString());

//     ResponseEntity resp = processGET(reqentity, method, request, hostid);
// 	logger.debug("/SCRIPTS/LOAD response status code: " + resp.getStatusCode());

    
    
//     return resp;
    
// }


    @RequestMapping(value="/GoogleEarth-proxy/{hostid}/**", 
    method = RequestMethod.GET,
    consumes = MediaType.ALL_VALUE,
    produces = MediaType.ALL_VALUE)
public ResponseEntity proxyroot_get(HttpMethod method, @PathVariable("hostid") String hostid, RequestEntity reqentity, HttpServletRequest request) throws URISyntaxException
{
    // logger.info(reqentity.toString());
    // logger.info(method.toString());
    // logger.info(request.toString());
	// logger.info(hostid.toString());

    ResponseEntity resp = processGET(reqentity, method, request, hostid);
	logger.debug("response status code: " + resp.getStatusCode());

    


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

	// @RequestMapping(value="/GoogleEarth-proxy/{hostid}/oauth2/v3/userinfo", 
	// method = RequestMethod.OPTIONS,
	// consumes = MediaType.ALL_VALUE,
	// produces = MediaType.ALL_VALUE)
	// public ResponseEntity proxyoptions( @PathVariable("hostid") String hostid, HttpMethod method, HttpServletRequest request) throws URISyntaxException
	// {
	// ResponseEntity resp = processOptions(method, request, hostid);

	// return resp;

	// }

    @RequestMapping(value="/GoogleEarth-proxy/{hostid}/preferences/set", 
    method = RequestMethod.POST,
    consumes = MediaType.ALL_VALUE,
    produces = MediaType.ALL_VALUE)
    public ResponseEntity proxypost_415( @PathVariable("hostid") String hostid, HttpMethod method, HttpServletRequest request) throws URISyntaxException
    {
     ResponseEntity resp = processPost_415(method, request, hostid);

     return resp;

    }

	@RequestMapping(value="/GoogleEarth-proxy/{hostid}/**", 
	method = RequestMethod.POST,
	consumes = MediaType.ALL_VALUE,
	produces = MediaType.ALL_VALUE)
	public ResponseEntity proxypost( RequestEntity reqentity, @PathVariable("hostid") String hostid, HttpMethod method, HttpServletRequest request) throws URISyntaxException
	{
		ResponseEntity resp = processPost(reqentity, method, request, hostid);

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
			logger.debug("!!!POST! [NewHeaders]: " + newheaders);
			
			HttpEntity newentity = new HttpEntity(reqentity.getBody(), newheaders);

			String target_url = getRealTargetURL(newheaders.get("target_url").get(0));
			
		    ResponseEntity<String> responseEntity = GErestTemplate.exchange(target_url, method, newentity, String.class);
		    
//		    if(realurl.indexOf("auth")!=-1)
//		    
//		    	logger.info("Response Body: " + responseEntity.getBody());
		    
			String newbody = addURLProxy(responseEntity.getBody(), hostid);

			HttpHeaders newrespheaders = updateHeader(responseEntity.getHeaders(), newbody, hostid);

			logger.debug("POST! [NewResponseHeaders]: " + newrespheaders);

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


	private ResponseEntity processPost_415(HttpMethod method, HttpServletRequest request, String hostid) throws URISyntaxException
    {
        
//      return processUtil(reqentity, method, request, hostid);
        
        ResponseEntity resp = null;
        
        try {
            
            logger.debug("==============");
            
            logger.debug("Request URI: " + request.getRequestURI());
            
            HttpHeaders newheaders = getHeaders(this.getHeaderByRequest(request), method, request, hostid);
            
            HttpEntity newentity = new HttpEntity(bt.getBody(request), newheaders);

            String target_url = getRealTargetURL(newheaders.get("target_url").get(0));
            
            ResponseEntity<String> responseEntity = GErestTemplate.exchange(target_url, method, newentity, String.class);

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




}
