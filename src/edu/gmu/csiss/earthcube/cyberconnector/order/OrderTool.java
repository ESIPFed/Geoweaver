package edu.gmu.csiss.earthcube.cyberconnector.order;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.gmu.csiss.earthcube.cyberconnector.database.DataBaseOperation;
import edu.gmu.csiss.earthcube.cyberconnector.tools.PlaceOrderTool;
import edu.gmu.csiss.earthcube.cyberconnector.utils.BaseTool;

/**
*Class OrderTool.java
*@author Ziheng Sun
*@time Feb 7, 2017 9:43:27 PM
*Original aim is to support CyberConnector.
*/
public class OrderTool {
	
	static Logger logger  = Logger.getLogger(OrderTool.class);
	
	public static boolean deleteOrder(String orderid){
		
		String sql = "delete from orders where orderid = '" + orderid + "'; ";
		
		boolean status =  DataBaseOperation.execute(sql);
		
		PlaceOrderTool.deleteSchedule(orderid);
		
		return status;
		
	}
	
	public static Order getOrderById(String orderid){
		
		Order o = new Order();
		
		try {
			
			//get order details
			
			String sql  = "select * from orders where orderid = '" + orderid + "';";
			
			ResultSet rs = DataBaseOperation.query(sql);
			
			if(rs.next()){
				
				o.setOrderid(rs.getString("orderid"));
				
				o.setProduct(rs.getString("product"));
				
				o.setMail(rs.getString("email"));
				
				o.setOrdertime(rs.getString("ordertime"));
				
				o.setUpdatetime(rs.getString("updatetime"));
				
				o.setBegintime(rs.getString("begintime"));
				
				o.setEndtime(rs.getString("endtime"));
				
				o.setEast(rs.getString("east"));
				
				o.setNorth(rs.getString("north"));
				
				o.setWest(rs.getString("west"));
				
				o.setSouth(rs.getString("south"));
				
				o.setProj(rs.getString("project"));
				
				o.setStatus(rs.getString("status"));
				
				o.setUserid(rs.getString("userid"));
				
				String pmap = rs.getString("parametermap");
				
				if(pmap != null){
					
					String[] ps = pmap.split(";");
					
					Map<String, String> tempmap = new HashMap();
					
					for(int i=0; i<ps.length; i++){
						
						String[] kv = ps[i].split(",");
						
						if("email".equals(kv[0])||"userid".equals(kv[0])){
							
							continue;
							
						}
						
						if(kv.length == 2 && !BaseTool.isNull(kv[1].trim())){
							
							tempmap.put(kv[0], kv[1]);
							
						}else{
							
							tempmap.put(kv[0], null);
							
						}
						
					}
					
					o.setParametermap(tempmap);
					
				}
				
			}
			
			DataBaseOperation.closeConnection();
			
			String sql2 = "select dataURL from datasets where identifier = '" + orderid + "';";
			
			rs = DataBaseOperation.query(sql2);
			
			StringBuffer resulturls = new StringBuffer(); //there might be multiple URLs since the order may be executed more than one time 
			
			int num = 0;
			
			while(rs.next()){
				
				if(num!=0){
					
					resulturls.append("<br/>");
					
				}
				
				String dataurl = rs.getString("dataURL");
				
				if(dataurl.toLowerCase().startsWith("http")){
				
					resulturls.append("<a href=\\\"").append(dataurl).append("\\\" >Result").append(num).append("</a>");
					
				}else{
					
					resulturls.append(dataurl);
					
					break; //the error messages usually are the same. Just display once. - Z.S. on 5/24/2017
					
				}
				
				num++;
				
			}
			
			if(!BaseTool.isNull(resulturls.toString())){
			
				o.setResult(resulturls.toString());
				
			}else{
				
				o.setResult(null);
				
			}
			
		} catch (SQLException e) {
			
			e.printStackTrace();
			
			throw new RuntimeException("Fail to get order details from database. " + e.getLocalizedMessage());
			
		}finally{
			
			DataBaseOperation.closeConnection();
			
		}
		
		return o;
		
	}
	
}
