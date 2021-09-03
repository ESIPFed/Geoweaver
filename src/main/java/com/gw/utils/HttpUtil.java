package com.gw.utils;

import javax.servlet.http.HttpServletRequest;

public class HttpUtil {
    
    /**
	 * GETS HOSTNAME:PORT
	 * @param HttpServletRequest
	 * @return String
	 * @throws Exception
	 */
	public static String getSiteURL(HttpServletRequest request) {
		String result = "";
		try {
			String siteURL = request.getHeader("referer");
            result = siteURL.replace(request.getServletPath(), ""); 
            siteURL = siteURL.replaceAll("\\s","");

            if(!siteURL.endsWith("/")) {
                    siteURL += "/";
            }
		}catch(Exception e){
			
			e.printStackTrace();
			
			result = e.getLocalizedMessage();
			
		}
		return result;

    }
}
