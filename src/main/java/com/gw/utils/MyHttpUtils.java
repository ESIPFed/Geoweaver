package com.gw.utils;

/**
 * 
 * @author Administrator
 *
 * updated by Ziheng Sun on 4/22/2016
 *
 */
public class MyHttpUtils
{
	
	// public static Logger theLogger = Logger.getLogger(MyHttpUtils.class);
	
	// /**
	//  * 
	//  * @param querystr
	//  * @return
	//  */
	// public static NameValuePair[] turnStr2NVPs(String querystr) {
		
	// 	String[] ss = querystr.split("&");
		
	// 	NameValuePair[] nvps = new NameValuePair[ss.length];
		
	// 	for(int i=0;i<ss.length;i++) {
			
	// 		String[] kv = ss[i].split("=");
			
	// 		nvps[i] = new NameValuePair(kv[0], kv[1]);
			
	// 	}
		
	// 	return nvps;
		
	// }
	
// 	public static String doPost_Auth_URLEncode(String url, String postContent, String username, String password) {
		
// 		String resp = "";
// 	    try {
// 				HttpClient client = new HttpClient(); //or any method to get a client instance
// 				Credentials credentials = new UsernamePasswordCredentials(username, password);
// 				client.getState().setCredentials(AuthScope.ANY, credentials);
// 				PostMethod post = new PostMethod(url);
// //		        post.setRequestEntity(new StringRequestEntity(postContent));
// //		        post.addParameter("location", "sdfdsfds");
// //		        post.addParameter("id", "sdfds");
// //		        post.addParameters(arg0);
// 				post.addParameters(turnStr2NVPs(postContent));
// 		        int returnCode = client.executeMethod(post);
// 		        theLogger.debug("ReturnCode: " + returnCode);
// 		      //add by Ziheng Sun on 5/3/2016 - to judge if the URL is secured
// 				if(returnCode == 401){
// 					throw new RuntimeException("HTTP Code 401 Unauthorized visit. This URL is secured.");
// 				}
// 		        // execute method and handle any error responses.
// 		        BufferedReader br = null;
// 		        if(returnCode == HttpStatus.SC_NOT_IMPLEMENTED) {
// 				       System.err.println("The Post method is not implemented by this URI");
// 				       // still consume the response body
// 				       resp = post.getResponseBodyAsString();
// 			    } else {
// 				       br = new BufferedReader(new InputStreamReader(post.getResponseBodyAsStream()));
// 				       String readLine = null;
// 					   while(((readLine = br.readLine()) != null)) {
// 						      System.err.println(readLine);
// 						      resp += readLine + "\n";
// 					   }
					   
// 			    }
// 		} catch (Exception e) {
// 				// TODO Auto-generated catch block
// 			e.printStackTrace();
// 		}
// 	    theLogger.debug("Response: " + resp);
	    
// 		return resp;
// 	}
	
	/**
	 * 
	 * add by Ziheng Sun on 4/26/2016
	 * @param url
	 * @param postContent
	 * @param username
	 * @param password
	 * @return
	 */
// 	public static String doPost_BasicAuth(String url, String postContent, String username, String password){
// 		String resp = "";
// 	    try {
// 				HttpClient client = new HttpClient(); //or any method to get a client instance
// 				Credentials credentials = new UsernamePasswordCredentials(username, password);
// 				client.getState().setCredentials(AuthScope.ANY, credentials);
// 				PostMethod post = new PostMethod(url);
// 		        post.setRequestEntity(new StringRequestEntity(postContent));
// 		        int returnCode = client.executeMethod(post);
// 		        theLogger.debug("ReturnCode: " + returnCode);
// 		      //add by Ziheng Sun on 5/3/2016 - to judge if the URL is secured
// 				if(returnCode == 401){
// 					throw new RuntimeException("HTTP Code 401 Unauthorized visit. This URL is secured.");
// 				}
// 		        // execute method and handle any error responses.
// 		        BufferedReader br = null;
// 		        if(returnCode == HttpStatus.SC_NOT_IMPLEMENTED) {
// 				       System.err.println("The Post method is not implemented by this URI");
// 				       // still consume the response body
// 				       resp = post.getResponseBodyAsString();
// 			    } else {
// 				       br = new BufferedReader(new InputStreamReader(post.getResponseBodyAsStream()));
// 				       String readLine = null;
// 					   while(((readLine = br.readLine()) != null)) {
// 						      System.err.println(readLine);
// 						      resp += readLine + "\n";
// 					   }
					   
// 			    }
// 		} catch (Exception e) {
// 				// TODO Auto-generated catch block
// 			e.printStackTrace();
// 		}
// 	    theLogger.debug("Response: " + resp);
// //		try {
// ////            URL url = new URL ("http://ip:port/login");
// //			URL u = new URL(url);
// ////            String encoding = Base64Encoder.encode ((username+ ":" + password).getBytes());
// //			byte[] encodedBytes = Base64.encodeBase64("Test".getBytes());
// //			String encoding = new String(encodedBytes);
// //			theLogger.info("encodedBytes " + encoding);
// //
// //            HttpURLConnection connection = (HttpURLConnection) u.openConnection();
// //            connection.setRequestMethod("POST");
// //            connection.setDoOutput(true);
// //            String userpass = username + ":" + password;
// //            String basicAuth = "Basic " + new String(new Base64().encode(userpass.getBytes()));
// //            connection.setRequestProperty ("Authorization", basicAuth);
// //            InputStream content = (InputStream)connection.getInputStream();
// //            BufferedReader in   = new BufferedReader (new InputStreamReader (content));
// //            String line;
// //            while ((line = in.readLine()) != null) {
// //                theLogger.info(line);
// //            }
// //        } catch(Exception e) {
// //            e.printStackTrace();
// //        }
// 		return resp;
// 	}
	/**
	 * 
	 * @param url
	 * @param postContent
	 * @return
	 * @throws Exception
	 */
// 	public static String doPost2(String url, String postContent) throws Exception {
// 		URL u = new URL(url);

// 		// Open the connection and prepare to POST
// 		URLConnection uc = u.openConnection();
// 		HttpURLConnection huc = (HttpURLConnection)uc;
// //		Fri Sep 02 23:37:24 EDT 2016:DEBUG:>> "Content-Type: text/xml;charset=UTF-8[\r][\n]"

// 		huc.setRequestProperty("Content-Type", "text/xml;charset=UTF-8");
// 		huc.setDoOutput(true);
// 		huc.setDoInput(true);
// 		huc.setAllowUserInteraction(false);
		
// 		DataOutputStream dstream = new DataOutputStream(huc.getOutputStream());
		
// 		// POST it
// 		dstream.writeBytes(postContent);
// 		dstream.close();

// 		//add by Ziheng Sun on 5/3/2016 - to judge if the URL is secured
// 		int code = huc.getResponseCode();
// 		if(code == 401){
// 			throw new RuntimeException("HTTP Code 401 Unauthorized visit. This URL is secured.");
// 		}
		
// 		// Read Response
// 		InputStream in = huc.getInputStream();

// 		BufferedReader r = new BufferedReader(new InputStreamReader(in));
// 		StringBuffer buf = new StringBuffer();
// 		String line;
// 		while ((line = r.readLine())!=null)
// 			buf.append(line);

// 		in.close();
		

// 		return buf.toString();
// 	}
	/**
	 * 
	 * doPost
	 * 
	 * created by Z.S. on 20160926
	 * 
	 * @param url
	 * @param postContent
	 * @param contenttype
	 * @return
	 * @throws Exception
	 */
// 	public static String doPost(String url, String postContent, String contenttype) throws Exception {
		
// 		URL u = new URL(url);
		
// 		// Open the connection and prepare to POST
		
// 		URLConnection uc = u.openConnection();
		
// 		HttpURLConnection huc = (HttpURLConnection)uc;
// //		Fri Sep 02 23:37:24 EDT 2016:DEBUG:>> "Content-Type: text/xml;charset=UTF-8[\r][\n]"

// 		huc.setRequestProperty("Content-Type", contenttype);
		
// 		huc.setDoOutput(true);
		
// 		huc.setDoInput(true);
		
// 		huc.setAllowUserInteraction(false);
		
// 		DataOutputStream dstream = new DataOutputStream(huc.getOutputStream());
		
// 		// POST it
// 		dstream.writeBytes(postContent);
		
// 		dstream.close();

// 		//add by Ziheng Sun on 5/3/2016 - to judge if the URL is secured
// 		int code = huc.getResponseCode();
		
// 		if(code == 401){
		
// 			throw new RuntimeException("HTTP Code 401 Unauthorized visit. This URL is secured.");
		
// 		}
		
// 		// Read Response
// 		InputStream in = huc.getInputStream();

// 		BufferedReader r = new BufferedReader(new InputStreamReader(in));
		
// 		StringBuffer buf = new StringBuffer();
		
// 		String line;
		
// 		while ((line = r.readLine())!=null)
			
// 			buf.append(line);

// 		in.close();
		
// 		return buf.toString();
		
// 	}
	
	
	
	
	/**
	 * HTTP Post
	 * @param url
	 * @param postContent
	 * @return
	 * @throws Exception
	 */
	// public static String doPost(String url, String postContent) throws Exception {
	// 	URL u = new URL(url);

	// 	// Open the connection and prepare to POST
	// 	URLConnection uc = u.openConnection();
	// 	HttpURLConnection huc = (HttpURLConnection)uc;
	// 	huc.setDoOutput(true);
	// 	huc.setDoInput(true);
	// 	huc.setAllowUserInteraction(false);
		
	// 	DataOutputStream dstream = new DataOutputStream(huc.getOutputStream());
		
	// 	// POST it
	// 	dstream.writeBytes(postContent);
	// 	dstream.close();

	// 	//add by Ziheng Sun on 5/3/2016 - to judge if the URL is secured
	// 	int code = huc.getResponseCode();
	// 	if(code == 401){
	// 		throw new RuntimeException("HTTP Code 401 Unauthorized visit. This URL is secured.");
	// 	}
		
	// 	// Read Response
	// 	InputStream in = huc.getInputStream();

	// 	BufferedReader r = new BufferedReader(new InputStreamReader(in));
	// 	StringBuffer buf = new StringBuffer();
	// 	String line;
	// 	while ((line = r.readLine())!=null)
	// 		buf.append(line);

	// 	in.close();
		

	// 	return buf.toString();
	// }
	
	/**
	 * HTTP GET
	 * @param url
	 * @return
	 * @throws Exception
	 */
	// public static String doGet(String url) throws Exception
	// {
		
	// 	//url = java.net.URLEncoder.encode(url,"UTF-8");
		
	// 	theLogger.debug("Encoded URL: " + url);
		
	// 	URL u = new URL(url);

	// 	// Open the connection and prepare to POST
	// 	URLConnection uc = u.openConnection();
	// 	HttpURLConnection huc = (HttpURLConnection)uc;
	// 	huc.setDoOutput(false);
	// 	huc.setDoInput(true);
	// 	huc.setAllowUserInteraction(false);
		
	// 	//add by Ziheng Sun on 5/3/2016 - to judge if the URL is secured
	// 	int code = huc.getResponseCode();
	// 	if(code == 401){
	// 		throw new RuntimeException("HTTP Code 401 Unauthorized visit. This URL is secured.");
	// 	}

	// 	// Read Response
	// 	InputStream in = huc.getInputStream();

	// 	BufferedReader r = new BufferedReader(new InputStreamReader(in));
	// 	StringBuffer buf = new StringBuffer();
	// 	String line;
	// 	while ((line = r.readLine())!=null)
	// 		buf.append(line);

	// 	in.close();

	// 	return buf.toString();
	// }
	/**
	 * HTTP GET with cookies
	 * @param url
	 * @param cookie_str
	 * @return
	 * @throws Exception
	 */
	// public static String doGetWithCookies(String url, String cookie_str) throws Exception
	// {
	// 	URL u = new URL(url);

	// 	// Open the connection and prepare to POST
	// 	URLConnection uc = u.openConnection();
	// 	HttpURLConnection huc = (HttpURLConnection)uc;
	// 	huc.setDoOutput(false);
	// 	huc.setDoInput(true);
	// 	huc.setAllowUserInteraction(false);
	// 	huc.setRequestProperty("Cookie", cookie_str);

	// 	//add by Ziheng Sun on 5/3/2016 - to judge if the URL is secured
	// 	int code = huc.getResponseCode();
	// 	if(code == 401){
	// 		throw new RuntimeException("HTTP Code 401 Unauthorized visit. This URL is secured.");
	// 	}
		
	// 	// Read Response
	// 	InputStream in = huc.getInputStream();

	// 	BufferedReader r = new BufferedReader(new InputStreamReader(in));
	// 	StringBuffer buf = new StringBuffer();
	// 	String line;
	// 	while ((line = r.readLine())!=null)
	// 		buf.append(line);

	// 	in.close();

	// 	return buf.toString();
	// }

	/**
	 * Login request only returns headers without body
	 * @param request_headers
	 * @param body
	 * @return
	 */
// 	public static org.springframework.http.ResponseEntity<String> login_jupyterhub(String target_url, org.springframework.http.HttpEntity requestentity, String hostid){

// 		org.springframework.http.ResponseEntity resp = null;

// 		try{

// 			java.net.http.HttpClient client =  java.net.http.HttpClient.newHttpClient();

// 			// Host: geoweaver2.eastus.cloudapp.azure.com:8000
// 			// User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:72.0) Gecko/20100101 Firefox/72.0
// 			// Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8
// 			// Accept-Language: en-US,en;q=0.5
// 			// Accept-Encoding: gzip, deflate
// 			// Content-Type: application/x-www-form-urlencoded
// 			// Content-Length: 34
// 			// Origin: http://geoweaver2.eastus.cloudapp.azure.com:8000
// 			// DNT: 1
// 			// Connection: keep-alive
// 			// Referer: http://geoweaver2.eastus.cloudapp.azure.com:8000/hub/login
// 			// Cookie: _xsrf=2|c0891671|6a2f9feb3275ae40b57960c55db32f51|1615354056
// 			// Upgrade-Insecure-Requests: 1
// 			// create a request
// 			System.out.println("Testing JupyterHub Request..");

// 			// ObjectMapper objectMapper = new ObjectMapper();
// 			// String requestBody = objectMapper.writeValueAsString(values);
// 			org.springframework.http.HttpHeaders headers = requestentity.getHeaders();
			
// 			Map<String, String> header_vm = headers.toSingleValueMap();
			
// 			java.net.http.HttpRequest.Builder builder = java.net.http.HttpRequest.newBuilder(URI.create(target_url));

// 			for (java.util.Map.Entry mapElement : header_vm.entrySet()) { 

// 				String key = (String)mapElement.getKey(); 
	  
// 				String value = (String)mapElement.getValue(); 
	  
// 				System.out.println(key + " : " + value); 

// 				String lowkey = key.toLowerCase();

// 				if(lowkey.equals("host") || lowkey.equals("connection") || lowkey.equals("content-length")){

// 					continue;

// 				}

// 				builder = builder.header(key, value);

// 			} 

// 			java.net.http.HttpRequest request = builder
// 				// .header("Cache-Control", "max-age=0")
// 				// .header("Upgrade-Insecure-Requests", "1")
// 				// .header("Content-Type", "application/x-www-form-urlencoded")
// 				// .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.82 Safari/537.36")
// 				// .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
// 				// .header("Sec-Fetch-Site", "same-origin")
// 				// .header("Sec-Fetch-Mode", "navigate")
// 				// .header("Sec-Fetch-User", "?1")
// 				// .header("Sec-Fetch-Dest", "document")
// 				// .header("Cookie", "_xsrf=2|c0891671|6a2f9feb3275ae40b57960c55db32f51|1615354056")
// 				// .header("Upgrade-Insecure-Requests", "1")
// 				.version(java.net.http.HttpClient.Version.HTTP_1_1)
// 				.POST(java.net.http.HttpRequest.BodyPublishers.ofString(String.valueOf(requestentity.getBody())))
// 				.build();

// 			System.out.println(request.headers().map());

// 			java.net.http.HttpResponse response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
			
// 			java.net.http.HttpHeaders respheaders = response.headers();

// 			respheaders.map().forEach((k, v) -> System.out.println(k + ":" + v));

// 			// print status code
// 			System.out.println(response.statusCode());

// 			// print response body
// 			System.out.println(response.body());
	
// 			// org.springframework.http.HttpHeaders respheaders = ;

		    
// //		    	MultiValueMap<String, String> headers =new LinkedMultiValueMap<String, String>();
		    	
// 			org.springframework.http.HttpHeaders newresponseheaders = new org.springframework.http.HttpHeaders();
			
// //		    	logger.info("Redirection: " + newresponseheaders);

// 			System.out.println("Response: " + response.body());
			
// //			    responseEntity = restTemplate.exchange(uri, method, requestentity, String.class);
			
// //			    responseEntity.getHeaders().compute("Location", (k, v) -> {v.clear(); v.add("/Geoweaver/web/jupyter-proxy/tree?");});
			
// //			    responseEntity.getHeaders().set("Location", "/Geoweaver/web/jupyter-proxy/tree?");
			
// //			    respheaders.set("Location", "/Geoweaver/web/jupyter-proxy/tree?");
			
// //			    respheaders.setLocation(new URI("/Geoweaver/web/jupyter-proxy/tree?"));
			
// //			    respheaders.add("Test", "Test Value");
			
// 			respheaders.map().forEach((key, value) -> {
				
// 				if(key.toLowerCase().equals("location")) {
					
// 					newresponseheaders.set(key, "/Geoweaver/jupyter-proxy/" + hostid + value.get(0));
					
// 				}else {
					
// 					newresponseheaders.set(key, value.get(0));
					
// 				}
				
// 			});
			
// 		    resp = new org.springframework.http.ResponseEntity(
// 		    		response.body(), 
// 		    		newresponseheaders, 
// 		    		response.statusCode());
	

// 		}catch(Exception e){

// 			e.printStackTrace();

// 		}

// 		return resp;

// 	}

// 	public static void testJupyterHubLogin(){

// 		try{

// 			java.net.http.HttpClient client =  java.net.http.HttpClient.newHttpClient();
// 			// Host: geoweaver2.eastus.cloudapp.azure.com:8000
// 			// User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:72.0) Gecko/20100101 Firefox/72.0
// 			// Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8
// 			// Accept-Language: en-US,en;q=0.5
// 			// Accept-Encoding: gzip, deflate
// 			// Content-Type: application/x-www-form-urlencoded
// 			// Content-Length: 34
// 			// Origin: http://geoweaver2.eastus.cloudapp.azure.com:8000
// 			// DNT: 1
// 			// Connection: keep-alive
// 			// Referer: http://geoweaver2.eastus.cloudapp.azure.com:8000/hub/login
// 			// Cookie: _xsrf=2|c0891671|6a2f9feb3275ae40b57960c55db32f51|1615354056
// 			// Upgrade-Insecure-Requests: 1
// 			// create a request
// 			System.out.println("Testing JupyterHub Request..");

// 			java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder(URI.create("http://geoweaver2.eastus.cloudapp.azure.com:8000/hub/login?next="))
// 				// .headers(headers)
// 				.header("Cache-Control", "max-age=1")
// 				.header("Upgrade-Insecure-Requests", "1")
// 				.header("Content-Type", "application/x-www-form-urlencoded")
// 				.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.82 Safari/537.36")
// 				.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
// 				.header("Sec-Fetch-Site", "same-origin")
// 				.header("Sec-Fetch-Mode", "navigate")
// 				.header("Sec-Fetch-User", "?1")
// 				.header("Sec-Fetch-Dest", "document")
// 				.header("Cookie", "_xsrf=2|c0891671|6a2f9feb3275ae40b57960c55db32f51|1615354056")
// 				.header("Upgrade-Insecure-Requests", "1")
// 				.version(java.net.http.HttpClient.Version.HTTP_1_1)
// 				.POST(java.net.http.HttpRequest.BodyPublishers.ofString("username=%%%%%%%%%%%%%&password=###########################"))
// 				.build();

// 			System.out.println(request.headers().map());
	
// 			// use the client to send the request
// 			java.net.http.HttpResponse response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
	

// 			java.net.http.HttpHeaders respheaders = response.headers();

// 			respheaders.map().forEach((k, v) -> System.out.println(k + ":" + v));

// 			// print status code
// 			System.out.println(response.statusCode());

// 			// print response body
// 			System.out.println(response.body());
	
	

// 		}catch(Exception e){

// 			e.printStackTrace();

// 		}

// 	}
	
	
// 	public static void main(String[] args){
		
// //		String url = "http://test.webdav.org/auth-basic/";
// //		String postContent = "test";
// //		String username = "user1";
// //		String password = "user1";
// //		theLogger.info("Request: " + postContent);
// //		String resp = MyHttpUtils.doPost_BasicAuth(url, postContent, username, password);
// //		theLogger.info("Response: " + resp);
		
// 		try {
// 			// String resp = MyHttpUtils.doGet("http://ows.dev.52north.org:8080/wps/WebProcessingService?request=GetCapabilities&service=WPS&version=2.0.0");
			
// 			// theLogger.debug("Response:" + resp);
			
// 			MyHttpUtils.testJupyterHubLogin();

// 		} catch (Exception e) {
// 			// TODO Auto-generated catch block
// 			e.printStackTrace();
// 		}
// 	}
	
}
