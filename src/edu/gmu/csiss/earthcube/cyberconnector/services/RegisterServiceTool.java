package edu.gmu.csiss.earthcube.cyberconnector.services;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import edu.gmu.csiss.earthcube.cyberconnector.database.DataBaseOperation;
import edu.gmu.csiss.earthcube.cyberconnector.user.UserTool;
import edu.gmu.csiss.earthcube.cyberconnector.utils.BaseTool;
import edu.gmu.csiss.earthcube.cyberconnector.utils.Message;
import edu.gmu.csiss.earthcube.cyberconnector.utils.SysDir;

/**
 *Class RegisterServiceTool.java
 *@author Ziheng Sun; Chen Zhang
 *@time Aug 19, 2015 11:32:18 AM
 *Original aim is to support CyberConnector.
 */
public class RegisterServiceTool {
	
	static Logger logger = Logger.getLogger(RegisterServiceTool.class);
	
	public static void updateWSDL(String serviceid){
		
		System.out.println("Begin to update service : " + serviceid);
		
		System.out.println("Judge if this service is SOAP or WPS.");
		
		String sql = "select serviceType,wsdlURL from service where id = '" + serviceid + "';";
		
		ResultSet rs = DataBaseOperation.query(sql);
		
		try {
			
			if(rs.next()){
				
				String type = rs.getString("serviceType");
				
				if("SOAPWSDL".equals(type)){

					String wsdlurl = rs.getString("wsdlURL");
					
					sendUpdateWSDLRequest(wsdlurl);
					
				}else if("WPS".equals(type)){
					
					throw new UnsupportedOperationException("WPS is not supported yet.");
					
				}
				
			}else{
				
				System.out.println("The service doesn't exist. No need to delete.");
				
			}
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
			throw new RuntimeException("Fail to update the service." + e.getLocalizedMessage());
			
		}finally{
			
			DataBaseOperation.closeConnection();
			
		}
	}
	
	public static void sendUpdateWSDLRequest(String wsdl){
		
		String req = "$SERVICEUPDATE$"+wsdl;
		
//		String req = "$SERVICEREGISTRATION$"+wsdl+"$"+username;
		
		logger.info("Registration Address" + SysDir.registrationaddress);
		
		String resp = BaseTool.POST(req, SysDir.registrationaddress);
		
		if(resp.indexOf("Sorry")==0){
		
			//failed
			throw new RuntimeException(resp);
		
		}else{
			
			//done
			
			System.out.println("Update is done. "+ resp);
			
			
		}
		
	}
	
	/**
	 * Register a wsdl to the database
	 * @param wsdl
	 */
	public void registerWSDL(String wsdl, String username){
		String req = "$SERVICEREGISTRATION$"+wsdl;
//		String req = "$SERVICEREGISTRATION$"+wsdl+"$"+username;
		logger.info("Registration Address" + SysDir.registrationaddress);
		String resp = BaseTool.POST(req, SysDir.registrationaddress);
		if(resp.indexOf("Sorry")==0){
			//failed
			throw new RuntimeException(resp);
		}else{
			//done
			System.out.println("Registration is done. "+ resp);
			//belong this service to its owner
			
			try {

				String userid = UserTool.getUserIDByName(username);

				String sql2 = "update service set userid='"+userid+"' where wsdlURL = '" + wsdl + "';";
				
				DataBaseOperation.update(sql2);
				
			} catch (Exception e) {
				
				e.printStackTrace();
				
				throw new RuntimeException("Fail to belong the service record to its owner." + e.getLocalizedMessage());
				
			}
			
		}
	}
	/**
	 * Ziheng 2/20/2017
	 * @param serviceid
	 * @return
	 */
	public static Message deleteService(String serviceid){
		
		System.out.println("Begin to delete service : " + serviceid);
		
		System.out.println("Judge if this service is SOAP or WPS.");
		
		String sql = "select serviceType,wsdlURL from service where id = '" + serviceid + "';";
		
		ResultSet rs = DataBaseOperation.query(sql);
		
		Message msg = null;
		
		try {
			
			if(rs.next()){
				
				String type = rs.getString("serviceType");
				
				if("SOAPWSDL".equals(type)){

					String wsdlurl = rs.getString("wsdlURL");
					
					unregisterWSDL(wsdlurl);
					
					msg = new Message("workflowcore", "deleteservice", "success", true);
					
				}else if("WPS".equals(type)){
					
					unregisterWPS(serviceid);
					
				}
				
			}else{
				
				System.out.println("The service doesn't exist. No need to delete.");
				
			}
			
		} catch (SQLException e) {
			
			e.printStackTrace();
			
		}finally{
			
			DataBaseOperation.closeConnection();
			
		}
		
		return null;
	}
	/**
	 * Waiting to be implemented
	 * @param uid
	 * @return
	 */
	public static Message unregisterWPS(String uid){
		
		return null;
		
	}
	
	/**
	 * Chen Zhang
	 * @param s
	 */
	public static Message registerWPS(Service s, String uid){
		
		Message msg = null;
		
		String sql = "INSERT INTO service (id, home, name, description, status, majorVersion, minorVersion, keywords, serviceType, accessurl, wsdlurl, userid) VALUES ( '"
				+ s.getId() +"','" + s.getHome() +"','" + s.getName() +"','" + s.getDesc() +"','" + s.getStatus()+"','" + s.getMajorversion() +"','" + s.getMinorversion() +"','" + s.getKeywords() +"','" + s.getServicetype() +"','" + s.getAccessurl() +"','" + s.getWsdlurl() +"','" + Integer.parseInt(uid) +"' );";
		
		System.out.println("SQL query: "+sql);
				
		try{
			
			DataBaseOperation.execute(sql);
			
			msg = new Message("database", "newservice", "success", true);
						
		}catch(Exception e){
			
			msg = new Message("database", "newservice", e.getLocalizedMessage(), false);
			
		}finally{
			
			DataBaseOperation.closeConnection();
			
		}
		
		return msg;
		
	}


	
	public static void unregisterWSDL(String wsdl){
		String req = "$SERVICEUNREGISTRATION$"+wsdl;
		String resp = BaseTool.POST(req, SysDir.registrationaddress);
		if(resp.indexOf("Sorry")==0){
			//failed
			throw new RuntimeException(resp);
		}else{
			//done
			System.out.println("The service record has been deleted from registry. "+ resp);
		}
	}
	
}
