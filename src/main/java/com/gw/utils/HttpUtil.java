package com.gw.utils;

import javax.servlet.http.HttpServletRequest;

public class HttpUtil {

  /**
   * GETS HOSTNAME:PORT
   *
   * @param HttpServletRequest
   * @return String
   * @throws Exception
   */
  public static String getSiteURL(HttpServletRequest request) {
    String result = "";
    try {
      String siteURL = request.getHeader("referer");
      if (siteURL == null) {
        return "NullPointerException";
      }
      result = siteURL.replace(request.getServletPath(), "");
      result = result.replaceAll("\\s", "");

      if (!result.endsWith("/")) {
        result += "/";
      }
    } catch (Exception e) {

      e.printStackTrace();

      result = e.getLocalizedMessage();
    }
    return result;
  }
}
