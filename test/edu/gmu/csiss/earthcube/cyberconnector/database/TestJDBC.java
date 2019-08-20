package edu.gmu.csiss.earthcube.cyberconnector.database;

import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.h2.tools.RunScript;
import org.junit.Test;


public class TestJDBC {

	public void test() throws Exception{
		
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
	
	
	
	@Test
	public void h2() {

	        //To start h2 server uncomment next two lines:
	        //String arguments[] = {"-tcpAllowOthers", "-tcpPort", "9092"};
	        //Server server = Server.createTcpServer(arguments).start();

		    // JDBC driver name and database URL
		    String JDBC_DRIVER = "org.h2.Driver";
		    String DB_URL = "jdbc:h2:D:\\data\\geoweaver\\geoweaver;MODE=MySQL";

		    //  Database credentials
		    String USER = "geoweaver";
		    String PASS = "geoweaver";

	        Connection conn = null;
	        Statement stmt = null;
	        try{
	            //STEP 2: Register JDBC driver
	            Class.forName(JDBC_DRIVER);

	            //STEP 3: Open a connection
	            System.out.println("Connecting to database...");
	            conn = DriverManager.getConnection(DB_URL, USER, PASS);
	            
//	            System.out.println("Initiate Geoweaver file..");
//	            RunScript.execute(conn, new FileReader("D:\\work\\GitHub\\Geoweaver\\docker\\db\\gw-h2.sql"));
	            
	            //STEP 4: Execute a query
//	            System.out.println("Creating statement...");
//	            stmt = conn.createStatement();
////	            String sql =  "CREATE TABLE   REGISTRATION " + 
////	                    "(id INTEGER not NULL, " + 
////	                    " first VARCHAR(255), " +  
////	                    " last VARCHAR(255), " +  
////	                    " age INTEGER, " +  
////	                    " PRIMARY KEY ( id ))";  
////                stmt.executeUpdate(sql);
////                System.out.println("Created table in given database..."); 
	            
//	            String sql = "select id, name, description from process_type;";
//	            ResultSet rs = stmt.executeQuery(sql);
//	            
//	            //STEP 5: Extract data from result set
//	            while(rs.next()){
//	                //Retrieve by column name
//	                String address  = rs.getString("name");
//	                String city  = rs.getString("id");
//	                
//	                //Display values
//	                System.out.println("name: " + address + ", id: " + city);
//	            }
	            
//	            String sql = "insert into abstract_model (identifier, name, namespace, process_connection, param_connection) values ('sfoznislf1zj7320gq4p', 'TestAnother', 'http://geoweaver.csiss.gmu.edu/workflow/TestAnother', ?, ? )";
//	            
//				PreparedStatement statement= conn.prepareStatement   (sql );
//				
//				statement.setString(1,"test process connection");
//				
//				statement.setString(2,"test param connection");
//				
//				statement.executeUpdate();
				
	            //remove ID column from abstract_model
	            
	            String sql  = "alter table abstract_model drop column ID;";
	            
	            stmt = conn.createStatement();
	            
	            stmt.execute(sql);
				
	            
	            //To start h2 web console uncomment next line:
	            //org.h2.tools.Server.startWebServer(conn);
	            //STEP 6: Clean-up environment
//	            rs.close();
//	            stmt.close();
	            conn.close();
	        }catch(SQLException se){
	            //Handle errors for JDBC
	            se.printStackTrace();
	        }catch(Exception e){
	            //Handle errors for Class.forName
	            e.printStackTrace();
	        }finally{
	            //finally block used to close resources
	            try{
	                if(stmt!=null)
	                    stmt.close();
	            }catch(SQLException se2){
	            }// nothing we can do
	            try{
	                if(conn!=null)
	                    conn.close();
	            }catch(SQLException se){
	                se.printStackTrace();
	            }//end finally try
	        }//end try
	        System.out.println("Goodbye!");


		
	}
	
	public void jdbc(){
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
