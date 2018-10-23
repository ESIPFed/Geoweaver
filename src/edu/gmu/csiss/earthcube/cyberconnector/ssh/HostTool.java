package edu.gmu.csiss.earthcube.cyberconnector.ssh;


import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import edu.gmu.csiss.earthcube.cyberconnector.database.DataBaseOperation;
import edu.gmu.csiss.earthcube.cyberconnector.utils.RandomString;

public class HostTool {

	static Logger logger = Logger.getLogger(HostTool.class);
	
	public static String detail(String id) {
		
		String detail = null;
		
		String sql = "select * from hosts where id = '" + id + "';";
		
		ResultSet rsmd = DataBaseOperation.query(sql);

	    JSONObject obj = new JSONObject();
	      
		try {
			
			if(rsmd.next()) {
				
			      int numColumns = rsmd.getMetaData().getColumnCount();
			      
			      for (int i=1; i<numColumns+1; i++) {
			      
			    	String column_name = rsmd.getMetaData().getColumnName(i);

			        if(rsmd.getMetaData().getColumnType(i)==java.sql.Types.ARRAY){
			         obj.put(column_name, rsmd.getArray(column_name));
			        }
			        else if(rsmd.getMetaData().getColumnType(i)==java.sql.Types.BIGINT){
			         obj.put(column_name, rsmd.getInt(column_name));
			        }
			        else if(rsmd.getMetaData().getColumnType(i)==java.sql.Types.BOOLEAN){
			         obj.put(column_name, rsmd.getBoolean(column_name));
			        }
			        else if(rsmd.getMetaData().getColumnType(i)==java.sql.Types.BLOB){
			         obj.put(column_name, rsmd.getBlob(column_name));
			        }
			        else if(rsmd.getMetaData().getColumnType(i)==java.sql.Types.DOUBLE){
			         obj.put(column_name, rsmd.getDouble(column_name)); 
			        }
			        else if(rsmd.getMetaData().getColumnType(i)==java.sql.Types.FLOAT){
			         obj.put(column_name, rsmd.getFloat(column_name));
			        }
			        else if(rsmd.getMetaData().getColumnType(i)==java.sql.Types.INTEGER){
			         obj.put(column_name, rsmd.getInt(column_name));
			        }
			        else if(rsmd.getMetaData().getColumnType(i)==java.sql.Types.NVARCHAR){
			         obj.put(column_name, rsmd.getNString(column_name));
			        }
			        else if(rsmd.getMetaData().getColumnType(i)==java.sql.Types.VARCHAR){
			         obj.put(column_name, rsmd.getString(column_name));
			        }
			        else if(rsmd.getMetaData().getColumnType(i)==java.sql.Types.TINYINT){
			         obj.put(column_name, rsmd.getInt(column_name));
			        }
			        else if(rsmd.getMetaData().getColumnType(i)==java.sql.Types.SMALLINT){
			         obj.put(column_name, rsmd.getInt(column_name));
			        }
			        else if(rsmd.getMetaData().getColumnType(i)==java.sql.Types.DATE){
			         obj.put(column_name, rsmd.getDate(column_name));
			        }
			        else if(rsmd.getMetaData().getColumnType(i)==java.sql.Types.TIMESTAMP){
			        obj.put(column_name, rsmd.getTimestamp(column_name));   
			        }
			        else{
			         obj.put(column_name, rsmd.getObject(column_name));
			        }
			      }

			}
			
			detail = obj.toJSONString();
			
			logger.info(detail);
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}finally {
			
			DataBaseOperation.closeConnection();
			
		}

		
		return detail;
		
	}
	
	/**
	 * 
	 * @return
	 * @throws SQLException 
	 */
	public static String list(String owner) {
		
		StringBuffer json = new StringBuffer("[");
		try {
			
			String sql = "select id, name from hosts;";
			
			ResultSet rs = DataBaseOperation.query(sql);
			
			int num = 0;
			
			while(rs.next()) {
				
				String hostid = rs.getString("id");
				
				String hostname = rs.getString("name");
				
				if( num++ != 0) {
					
					json.append(",");
					
				}
				
				json.append("{\"id\":\"").append(hostid).append("\", \"name\": \"").append(hostname).append("\"}");
				
			}
			
			json.append("]");
			
		}catch(Exception e) {
			
			e.printStackTrace();
			
		}finally {
			
			DataBaseOperation.closeConnection();
			
		}
		
		return json.toString();
		
	}
	
	
	/**
	 * Add a new host
	 * @param hostname
	 * @param hostip
	 * @param hostport
	 * @param username
	 * @param owner
	 */
	public static String add(String hostname, String hostip, String hostport, String username, String owner) {
		
		String newhostid = new RandomString(6).nextString();
		
		StringBuffer sql = new StringBuffer("insert into hosts (id, name, ip, port, user, owner) values ('")
				
				.append(newhostid).append("', '")
				
				.append(hostname).append("', '")
				
				.append(hostip).append("', '")
				
				.append(hostport).append("', '")
				
				.append(username).append("', '")
				
				.append(owner).append("'); ");
		
//		logger.info(sql);
		
		DataBaseOperation.execute(sql.toString());
		
		return newhostid;
		
	}
	
	/**
	 * Remove a host from database
	 * @param hostid
	 */
	public static String del(String hostid) {
		
		StringBuffer sql = new StringBuffer("delete from hosts where id = '").append(hostid).append("';");
		
		DataBaseOperation.execute(sql.toString());
		
		return "done";
		
	}
	
	public static void main(String[] args) throws SQLException {
		
		
		System.out.println(HostTool.list(""));;
		
	}
	
}
