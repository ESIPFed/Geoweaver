package edu.gmu.csiss.earthcube.cyberconnector.services;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import edu.gmu.csiss.earthcube.cyberconnector.database.DataBaseOperation;

/**
*Class QueryServiceTool.java
*@author Ziheng Sun
*@time Feb 22, 2017 10:52:54 AM
*Original aim is to support CyberConnector.
*/
public class QueryServiceTool {
	
	
	public static List<Service> retrieveAll(){
		
		List<Service> sers = new ArrayList();
		
		try {
			
			String sql = "select id, service.name as servicename, serviceType, wsdlURL, users.name as username  from service,users where service.userid = users.uid ;";
			
			System.out.println(sql);
			
			ResultSet rs = DataBaseOperation.query(sql);
			
			while(rs.next()){
				
				Service s = new Service();
				
				s.setId(rs.getString("id"));
				
				s.setName(rs.getString("servicename"));
				
				s.setServicetype(rs.getString("serviceType"));
				
				s.setUsername(rs.getString("username"));
				
				s.setWsdlurl(rs.getString("wsdlURL"));
				
				sers.add(s);
				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return sers;
		
	}

}
