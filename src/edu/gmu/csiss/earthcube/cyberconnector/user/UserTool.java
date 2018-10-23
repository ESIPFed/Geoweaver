package edu.gmu.csiss.earthcube.cyberconnector.user;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import edu.gmu.csiss.earthcube.cyberconnector.database.DataBaseOperation;
import edu.gmu.csiss.earthcube.cyberconnector.order.Order;
import edu.gmu.csiss.earthcube.cyberconnector.products.Product;
import edu.gmu.csiss.earthcube.cyberconnector.services.Service;
import edu.gmu.csiss.earthcube.cyberconnector.utils.BaseTool;
import edu.gmu.csiss.earthcube.cyberconnector.utils.Message;

/**
*Class UserTool.java
*@author Ziheng Sun
*@time Jan 27, 2017 10:59:58 PM
*Original aim is to support CyberConnector.
*/
public class UserTool {
	
	static Map<String, Token> emailToToken = new HashMap();
	
	static Logger logger = Logger.getLogger(UserTool.class);
	
	public static boolean checkOwner(String uid, String pid){
		
		StringBuffer sql = new StringBuffer("select count(*) as total from products where userid='")
				
				.append(uid)
				
				.append("' and parent_abstract_model = '")
				
				.append(pid).append("'; ");
		
		ResultSet rs = DataBaseOperation.query(sql.toString());
		
		int totalnum = 0;
		
		try {
			
			if(rs.next())
			
				totalnum = rs.getInt("total");
			
		} catch (SQLException e) {
			
			e.printStackTrace();
			
			throw new RuntimeException("Fail to get the owner of the product.");
			
		}
		
		boolean yes = false;
		
		if(totalnum==1){
			
			yes = true;
			
		}
		
		return yes;
		
	}
	
	/**
	 * Get User ID By Name
	 * @param name
	 * @return
	 */
	public static String getUserIDByName(String name){
		
		String sql1 = "select uid from users where name = '" + name + "';";
		
		ResultSet rs = DataBaseOperation.query(sql1);
		
		String userid = null;
		
		try {
			
			if(rs.next()){
				
				userid = rs.getString("uid");
				
			}

			rs.close();
			
		} catch (SQLException e) {
			
			e.printStackTrace();
			
			throw new RuntimeException("Fail to get user id by name." + e.getLocalizedMessage());
			
		}finally{
			
			DataBaseOperation.closeConnection();
			
		}
		
		return userid;
		
	}
	/**
	 * 
	 * @param request
	 * @return
	 */
	public static String checkLogin(HttpServletRequest request){
		
		HttpSession session = request.getSession(true);
		
		String username = (String)session.getAttribute("sessionUser");
		
		if(username==null){
			
			throw new RuntimeException("You have to log in first.");
			
		}
		
		return username;
		
	}
	
	/**
	 * Retrieve information from database
	 * @param name
	 * @return
	 */
	public static User retrieveInformation(String name){
		
		User u = new User();
		
		u.setName(name);
		
		String sql = "select uid, address, fullname, type, last_login_time, last_operate_time, status, email, phone, department, institute from users where name = '"+name+"'; ";
		
		logger.debug(sql);
		
		try{
			
			ResultSet rs = DataBaseOperation.query(sql);
			
			if(rs.next()){
				
				u.setId(rs.getString("uid"));
				
				u.setType(rs.getString("type"));
				
				u.setAddress(rs.getString("address"));
				
				u.setDepartment(rs.getString("department"));
				
				u.setInstitute(rs.getString("institute"));
				
				u.setEmail(rs.getString("email"));
				
				u.setFullname(rs.getString("fullname"));
				
				u.setLast_login_time(rs.getString("last_login_time"));
				
				u.setLast_operation_time(rs.getString("last_operate_time"));
				
				u.setPhone(rs.getString("phone"));
				
				u.setStatus(rs.getString("status"));
				
				try{
					
					//get orders of this user
					
					String sql2 = "select orderid, product, ordertime, status,parametermap  from orders where userid = '"+u.getId()+"' order by ordertime desc;";
					
					logger.error("SQL 2 : " + sql2);
				
					ResultSet rs2 = DataBaseOperation.query(sql2);
					
					List orders = new ArrayList();
					
					while(rs2.next()){
						
						Order o = new Order();
						
						o.setOrderid(rs2.getString("orderid"));
						
						o.setProduct(rs2.getString("product"));
						
						o.setBegintime(rs2.getString("ordertime"));
						
						o.setStatus(rs2.getString("status"));
						
						String pmap = rs2.getString("parametermap");
						
						if(pmap!=null&&pmap.startsWith("termination,")){
							
							String[] ps = pmap.split(";");
							
							String[] ts = ps[0].split(",");
							
							String[] cs = ps[1].split(",");
							
							if(cs.length==2){
								if(BaseTool.isNull(cs[1].trim())){
									o.setCron(null);
								}else
									o.setCron(cs[1].trim());
							}
							
							if(ts.length==2){
								
								if(BaseTool.isNull(cs[1].trim())){
								
									o.setTermination(null);
								
								}else{
								
									o.setTermination(ts[1].trim());
								
								}
							
							}
							
						}
						
						orders.add(o);
						
					}
					
					DataBaseOperation.closeConnection();
					
					u.setOrders(orders);
					
					//get resources (service and VDP) of this user
					
					List resources = new ArrayList();
					
					String sql3 = "select id, name, registerdate, accessURL from service where userid = '"+u.getId()+"' order by registerdate desc; ";
					
					logger.debug("SQL 3 : " + sql3);
					
					ResultSet rs3 = DataBaseOperation.query(sql3);
					
					List services = new ArrayList();
					
					while(rs3.next()){
						
						Service s = new Service();
						
						s.setId(rs3.getString("id"));
						
						s.setName(rs3.getString("name"));
						
						s.setRegisterdate(rs3.getString("registerdate"));
						
						s.setAccessurl(rs3.getString("accessURL"));
						
						services.add(s);
						
					}
					
					DataBaseOperation.closeConnection();
					
					u.setServices(services);
					
					List products = new ArrayList();
					
					String sql4 = "select identifier, name, begintime from products where userid = '"+u.getId()+"'; ";
					
					logger.debug("SQL4 :" + sql4);
					
					ResultSet rs4 = DataBaseOperation.query(sql4);
					
					while(rs4.next()){
						
						Product p = new Product();
						
						p.setId(rs4.getString("identifier"));
						
						p.setName(rs4.getString("name"));
						
						p.setBegintime(rs4.getString("begintime"));
						
						products.add(p);
						
					}
					
					DataBaseOperation.closeConnection();
					
					u.setProducts(products);
					
				}catch(Exception e){
					
					e.printStackTrace();
					
				}
				
			}else{
				
				throw new RuntimeException("Unable to find user " + name);
				
			}
			
		}catch(Exception e){
			
			e.printStackTrace();
			
		}finally{
			DataBaseOperation.closeConnection();
		}
		
		return u;
		
	}
	
	/**
	 * Logout
	 * @param user
	 * @return
	 */
	public static Message logout(String username){
		
		Message msg = null;
		
		BaseTool tool = new BaseTool();
		
		String sql = "UPDATE users SET last_operate_time='"+tool.getCurrentMySQLDatetime()+"', status = 'inactive' WHERE name = '"+username+"';";
		
		logger.debug(sql);
		
		try {
			
			DataBaseOperation.execute(sql);
		
			msg = new Message("database", "user_logout", "success", true);
			
		}catch(Exception e){
			
			e.printStackTrace();
			
			msg = new Message("database", "user_logout", e.getLocalizedMessage(), false);
			
		}
		
		return msg;
		
	}
	
	public static void activate(User user){
		
		BaseTool tool = new BaseTool();
		
		String sql = "UPDATE users SET last_operate_time='"+tool.getCurrentMySQLDatetime()+"', status = 'active' WHERE name = '"+user.getName()+"';";
		
		logger.debug(sql);
		
		try {
			
			DataBaseOperation.execute(sql);
			
		}catch(Exception e){
			
			e.printStackTrace();
			
		}
		
	}
	
	public static Message login(User user){
		
		Message msg = null;
		
		String sql = "select count(*) as total from users where name = '" + user.getName() + "' and pswd = '" + user.getPassword() + "';";
		
		logger.debug(sql);
		
		ResultSet rs = DataBaseOperation.query(sql);
		
		try {
			
			int num = -1;
			
			if(rs.next()){
				
				num = rs.getInt("total");
				
			}	
			
			logger.debug("The number of users is: " + num);
			
			if(num==1){
				
				activate(user);
				
				msg = new Message("database", "user_login", "success", true);
				
			}else{
				
				msg = new Message("database", "user_login", "The user doesn't exist or the password is incorrect.", false);
				
			}
			
		} catch (SQLException e) {
			
			msg = new Message("database", "user_login", e.getLocalizedMessage(), false );
			
			e.printStackTrace();
			
		}finally{
			DataBaseOperation.closeConnection();
		}
		
		return msg;
		
	}
	/**
	 * Update the information of existing user
	 * @param user
	 * @return
	 */
	public static Message updateExistingUser(User user){
		
		Message msg = new Message("database", "user_edit", "success", true);
		
		//email, full name, department, institute, address
		
		BaseTool tool = new BaseTool();
		
		String sql = "update users set last_operate_time='"+tool.getCurrentMySQLDatetime()
				
				+"', address='" + user.getAddress() + "', fullname='" + user.getFullname() 
				
				+ "', email='" + user.getEmail() + "', department = '" + user.getDepartment() + "', institute = '" + user.getInstitute() 
				
				+ "' where name = '" + user.getName() + "' ; ";
		
		logger.debug(sql);
		
		try{
			
			DataBaseOperation.update(sql);
			
		}catch(Exception e){
			
			e.printStackTrace();
			
			msg = new Message("database", "user_edit", "Error" + e.getLocalizedMessage(), false);
			
		}
		
		return msg;
		
	}
	
	public static Message registerNewUser(User user){
		
		BaseTool tool = new BaseTool();
		
		Message msg = null;
		
		Boolean newUser = false;
		
		Boolean newEmail = false;
				
		String newUserValidate = "select count(*) as total from users where name = '" + user.getName() + "';";
		
		String newEmailValidate = "select count(*) as total from users where email = '" + user.getEmail() + "';";
		
		ResultSet rsUser = DataBaseOperation.query(newUserValidate);
		
		ResultSet rsEmail = DataBaseOperation.query(newEmailValidate);
		
		try {
			
			int numUser = -1;
			int numEmail = -1;
			
			if(rsUser.next()){				
				numUser = rsUser.getInt("total");				
			}				
			logger.debug("The number of users is: " + numUser);
			
			if(numUser>0){				
				newUser = false;				
			}else{				
				newUser = true;				
			}
			
			if(rsEmail.next()){				
				numEmail = rsEmail.getInt("total");				
			}				
			logger.debug("The number of email is: " + numEmail);
			
			if(numEmail>0){				
				newEmail = false;				
			}else{				
				newEmail = true;				
			}
						
		} catch (SQLException e) {
			
			msg = new Message("database", "user_login", e.getLocalizedMessage(), false );
			
			e.printStackTrace();
			
		}finally{
			
			DataBaseOperation.closeConnection();
		
		}
		
		if (newUser == false) {
			
			msg = new Message("database", "user_register", "The username has been occupied, please select a new username.", false);
			
			return msg;
		}
		else if (newEmail == false) {
			msg = new Message("database", "user_register", "The email has been occupied, please select a new email.", false);
			
			return msg;
		}
		
		else { 
			
			String sql = "INSERT INTO users (name,  pswd, address, fullname, sex, type,  last_login_time,  last_operate_time,  status,  token, email,  phone, department,  institute,  last_ip) VALUES ( '"
					+ user.getName() +"','" + user.getPassword() + "','" + user.getAddress() + "','" + user.getFullname() + "','" + user.getSex() + "','" + user.getType() + "','" + tool.getCurrentMySQLDatetime() + "','" 
					+ tool.getCurrentMySQLDatetime() + "','" + user.getStatus() + "','" + user.getToken() + "','" 
					+ user.getEmail() + "','" + user.getPhone() + "','" +user.getDepartment() + "','" + user.getInstitute() + "','"
					+ user.getLast_ip() +"' );";
			
			logger.debug(sql);
						
			try{
			
				DataBaseOperation.execute(sql);
				
				msg = new Message("database", "user_register", "success", true);
				
			}catch(Exception e){
				
				logger.error(e.getLocalizedMessage());
				
				msg = new Message("database", "user_register", e.getLocalizedMessage(), false);
				
			}finally{
				
				//no need to close connection if the operation is execute - Z.S. on 03/20/2017
				//DataBaseOperation.closeConnection();
				
			}
			
			return msg;
		}

		
	}
	
	public static boolean checkUserExist(User user){
		
		String sql = "SELECT email from users where email = '"+ user.getEmail() + "';";
		
		ResultSet rs = DataBaseOperation.query(sql);
		
		boolean userExist = false;
		
		try {
			
			if (rs.next()) {
			
				userExist = true;
			
			}
		
		} catch (SQLException e) {
			
			//e.printStackTrace();
			
			logger.error(e.getLocalizedMessage());
			
		}
		
		DataBaseOperation.closeConnection();
		
		return userExist;
	}
	
	public static void passwordResetEmail(User user) {
		
		Token t = new Token();
		t.setToken(UUID.randomUUID().toString());
		
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		
		t.setExpireDate(timestamp.getTime()+5*60*1000);
		
		String emailContent = "Please click the following link to reset password of your CyberConnector account. To ensure your account safety, do NOT share or let other people get this email. The link will be expired in 5 minutes.";
		emailContent += "\nhttp://localhost:8081/CyberConnector/web/user_setpassword?token=" + t.getToken();
		
		BaseTool tool = new BaseTool();
		
		tool.notifyUserByEmail(user.getEmail(), emailContent);
		
		emailToToken.put(user.getEmail(), t);
	}
	
	
	public static boolean validateToken(String token) {
		
		boolean validate = false;
		
		boolean exist = false;
		
		Token tok = null;
		
		String email = null;
		
	    Iterator it = emailToToken.entrySet().iterator();
	    
	    while (it.hasNext()) {
	    	
	        Map.Entry pair = (Map.Entry)it.next();
	        
	        Token t = (Token) pair.getValue();
	        
	        if (t.getToken().equals(token)) {
	        	
	        	exist = true;
	        	
	        	tok = t;
	        	
	        	email = (String) pair.getKey();
	        	
	        	break;
	        }
	    
	    }

	    if (exist) {
	    	
	    	Timestamp timestamp = new Timestamp(System.currentTimeMillis());
	    	
	    	if ((timestamp.getTime()-tok.getExpireDate())<5*60*1000) {
	    	
	    		validate = true;
	    	
	    	}
	    			
	    }
	   
	    
	    return validate;
		
	}
	
	
	public static User loginByToken(String token){		
		
		Iterator it = emailToToken.entrySet().iterator();
		
		String email = null;
	    
	    while (it.hasNext()) {
	    	
	        Map.Entry pair = (Map.Entry)it.next();
	        
	        Token t = (Token) pair.getValue();
	        
	        if (t.getToken().equals(token)) {
	        		        	
	        	email = (String) pair.getKey();
	        	
	        	break;
	        }
	    
	    }
	    		
		Message msg = null;
		
		String sql = "select name from users where email = '" + email + "';";
		
		logger.debug(sql);
		
		ResultSet rs = DataBaseOperation.query(sql);
		
		User user = null;
		
		try {
			
			String name = null;
			
			if(rs.next()){
				
				name = rs.getString("name");
				
				user = retrieveInformation(name);
			}
			
			
		} catch (SQLException e) {
			
			msg = new Message("database", "user_login", e.getLocalizedMessage(), false );
			
			//e.printStackTrace();
			logger.error(e.getLocalizedMessage());
			
		}finally{
			DataBaseOperation.closeConnection();
		}
		
		return user;
		
	}
	
	
	
	
	/**
	 * Update the information of existing user
	 * @param user
	 * @return
	 */
	public static Message resetPassword(User user){
		
		Message msg = new Message("database", "user_edit", "success", true);
						
		String sql = "update users set pswd='"+ user.getPassword() + "' where name = '" + user.getName() + "' ; ";
		
		logger.debug(sql);
		
		try{
			
			DataBaseOperation.update(sql);
			
		}catch(Exception e){
			
			//e.printStackTrace();
			logger.error(e.getLocalizedMessage());
			
			msg = new Message("database", "user_setpassword", "Error" + e.getLocalizedMessage(), false);
			
		}
		
		return msg;
		
	}
}
