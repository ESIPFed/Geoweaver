package edu.gmu.csiss.earthcube.cyberconnector.tools;

import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import edu.gmu.csiss.earthcube.cyberconnector.database.DataBaseBroker;

/**
 *Class FetchProductListTool.java
 *@author Ziheng Sun
 *@time Oct 30, 2015 4:57:08 PM
 *Original aim is to support CyberConnector.
 */
public class FetchProductListTool {
	
	public String getProductListJSON(){
		List<String[]> productlist = DataBaseBroker.fetchProductList();
		JSONArray arr = new JSONArray();
		for(int i=0;i<productlist.size();i++){
			JSONObject obj = new JSONObject();
			String[] aproduct = productlist.get(i);
			obj.put("id", aproduct[0]);
			obj.put("name", aproduct[1]);
			arr.add(obj);
		}
		return arr.toJSONString();
	}
	
}
