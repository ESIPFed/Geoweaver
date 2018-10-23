package edu.gmu.csiss.earthcube.cyberconnector.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class TestJDBC {

	public static void main(String[] args)throws Exception{
		ResultSet rs = DataBaseOperation.query("select * from datasets");
		String name = null;
		int id;
		while(rs.next()) {  
			name = rs.getString("identifier");
			id = rs.getInt("tid");
			name = new String(name.getBytes("ISO-8859-1"),"GB2312");
			System.out.println(id + "\t" + name);  
		}  
		rs.close();
                                            DataBaseOperation.closeConnection();
	}
	
	public static void jdbc(){
		String driver = "com.mysql.jdbc.Driver";
		String url = "jdbc:mysql://127.0.0.1:3306/eng";
		String user = "root";
		String password = "geoeng";

		try {
			Class.forName(driver);
			Connection conn = DriverManager.getConnection(url, user, password);
			if(!conn.isClosed())
				System.out.println("Succeeded connecting to the Database!");
			Statement statement = conn.createStatement();
	
			String sql = "select * from users";
			ResultSet rs = statement.executeQuery(sql);
			String name = null;
			int id;
			while(rs.next()) {  
				name = rs.getString("Username");
				id = rs.getInt("Id");
				name = new String(name.getBytes("ISO-8859-1"),"GB2312");
				System.out.println(id + "\t" + name);  
			}  
			rs.close();  
			conn.close();   
			
		} catch(ClassNotFoundException e) {   
			System.out.println("Sorry,can`t find the Driver!");   
			e.printStackTrace();   
		} catch(SQLException e) {   
			e.printStackTrace();   
		} catch(Exception e) {   
			e.printStackTrace();   
		}  
	}
}
