package edu.gmu.csiss.earthcube.cyberconnector.products;

import java.sql.ResultSet;
import java.sql.SQLException;

import edu.gmu.csiss.earthcube.cyberconnector.database.DataBaseOperation;

/**
*Class LikeProductTool.java
*@author Ziheng Sun
*@time Feb 17, 2017 2:28:38 PM
*Original aim is to support CyberConnector.
*/
public class LikeProductTool {

	public static int like(String productid){
		
		int likes = -1;
		
		String sql = "select likes from products where identifier = '" + productid + "'; ";
		
		System.out.println(sql);
		
		ResultSet rs = DataBaseOperation.query(sql);
		
		try {
			
			if(rs.next()){
		
				likes = rs.getInt("likes");
				
				likes++;
				
				System.out.println("New likes is : " + likes);
				
				//update the database
				
				String sql2 = "update products set likes=" + likes + " where identifier = '" +productid + "';";
				
				System.out.println(sql2);
				
				DataBaseOperation.update(sql2);
				
			}
		
		} catch (SQLException e) {
		
			e.printStackTrace();
		
		}finally{
			
			DataBaseOperation.closeConnection();
			
		}
		
		return likes;
		
	}
	
}
