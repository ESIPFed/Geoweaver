package edu.gmu.csiss.earthcube.cyberconnector.tools;

import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import edu.gmu.csiss.earthcube.cyberconnector.database.DataBaseBroker;

/**
 *Class QueryOrderStatusTool.java
 *@author Ziheng Sun
 *@time Aug 10, 2015 12:11:27 PM
 *Original aim is to support iGFDS.
 */
public class QueryOrderStatusTool {

	/**
	 * Query status by order id
	 * @param orderid
	 * @return
	 * Status
	 */
	public String queryStatusByOrderId(String orderid){
		String status  = DataBaseBroker.queryAnOrderStatus(orderid);
		return status;
	}
	
	/**
	 * Query the list of product
	 * @return
	 * A json string of the product list
	 */
	public String queryVirtualProductList(){
		List productlist = DataBaseBroker.getListofVirtualProducts();
		//encode the information into a json object 
		String jsonstr = JSONValue.toJSONString(productlist);
		System.out.println(jsonstr);
		return jsonstr;
	}
	
}
