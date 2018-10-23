package edu.gmu.csiss.earthcube.cyberconnector.products;

import edu.gmu.csiss.earthcube.cyberconnector.database.DataBaseOperation;
import edu.gmu.csiss.earthcube.cyberconnector.utils.Message;

/**
*Class DeleteProductTool.java
*@author Ziheng Sun
*@time Feb 20, 2017 11:38:36 AM
*Original aim is to support CyberConnector.
*/
public class DeleteProductTool {
	
	public static void delete(String productid){
		
		System.out.println("Begin to delete product :" + productid);
		
		//note: the product is delete. The abstract model is permanently reserved and has to be manually cleared.
		
		String sql  = "delete from products where identifier = '"+productid+"';";
		
		DataBaseOperation.execute(sql);
		
	}
	
	public static void deleteBothProductAModel(String productid){
		
		System.out.println("Begin to delete product and model :" + productid);
		
		String sql = "delete from products, abstract_model where abstract_model.identifier = products.parent_abstract_model and products.identifier = '" + productid + "';";
		
		DataBaseOperation.execute(sql);
		
	}
	
}
