/**
 * Path utilities for Geoweaver
 * 
 * Provides functions to handle context paths correctly when Geoweaver is deployed
 * behind a proxy with parent paths.
 */

GW.path = {
  /**
   * Gets the base path for Geoweaver, handling cases where it's deployed behind a proxy
   * with parent paths.
   * 
   * @returns {string} The base path including the trailing slash
   */
  getBasePath: function() {
    // Get the context path from the current location
    var contextPath = window.location.pathname;
    // Extract the base path (everything up to and including /Geoweaver/)
    var basePath = "/";
    
    // Handle complex proxy paths like https://ondemand.orc.gmu.edu/rnode/hop044.orc.gmu.edu/27289/Geoweaver/
    if (contextPath.includes("/Geoweaver/")) {
      // Keep the entire path up to and including /Geoweaver/
      basePath = contextPath.substring(0, contextPath.indexOf("/Geoweaver/") + "/Geoweaver/".length);
    } else if (contextPath.endsWith("/Geoweaver")) {
      // If the path ends with /Geoweaver (no trailing slash)
      basePath = contextPath + "/";
    } else if (contextPath.startsWith("/Geoweaver")) {
      basePath = "/Geoweaver/";
    }
    
    console.log("Using base path: " + basePath);
    return basePath;
  },
  
  /**
   * Gets the base URL for API calls, handling cases where Geoweaver is deployed behind a proxy
   * with parent paths.
   * 
   * @returns {string} The base URL for API calls
   */
  getApiBasePath: function() {
    return this.getBasePath();
  },
  
  /**
   * Gets the WebSocket prefix URL, handling cases where Geoweaver is deployed behind a proxy
   * with parent paths.
   * 
   * @returns {string} The WebSocket prefix URL
   */
  getWsPrefix: function() {
    var basePath = this.getBasePath();
    var s = (window.location.protocol === "https:" ? "wss://" : "ws://") + 
            window.location.host + 
            basePath;
    
    return s;
  }
};