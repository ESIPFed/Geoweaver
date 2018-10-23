package edu.gmu.csiss.earthcube.cyberconnector.tools;

import org.apache.log4j.Logger;

import edu.gmu.csiss.earthcube.cyberconnector.utils.BaseTool;
import edu.gmu.csiss.earthcube.cyberconnector.utils.SysDir;

/**
 *Class GetProductInputDetailsTool.java
 *@author Ziheng Sun
 *@time Oct 30, 2015 5:55:17 PM
 *Original aim is to support CyberConnector.
 */
public class GetProductInputDetailsTool {
	
	Logger logger = Logger.getLogger(GetProductInputDetailsTool.class);
	
	public String getInputDetailsJSON(String productid){
		logger.debug("Trying to fetch the input details...");
		String details = BaseTool.POST("$GETPRODUCTINPUTDETAILS$"+productid, SysDir.executionservletaddress);
		logger.debug(details);
		return details;
	}
	
}
