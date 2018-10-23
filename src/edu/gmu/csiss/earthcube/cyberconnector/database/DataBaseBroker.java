package edu.gmu.csiss.earthcube.cyberconnector.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.gmu.csiss.earthcube.cyberconnector.utils.BaseTool;

/**
 *Class DataBaseBroker.java
 *@author ziheng
 *@time Aug 10, 2015 11:48:29 AM
 *Original aim is to support CyberConnector.
 */
public class DataBaseBroker {
	
	static Logger logger = Logger.getLogger(DataBaseBroker.class);

	public static List getListofVirtualProducts(){
//		SELECT  `identifier`,  `abbreviation`,  LEFT(`desc`, 256),  LEFT(`keywords`, 256),  `name`,  `east`,  `south`,  `west`,  `north`,  `srs`,  `begintime`,  `endtime`,  `ifvirtual`,  `parent_abstract_model`,  `dataFormat`,  LEFT(`accessURL`, 256),  LEFT(`ontology_reference`, 256),  `lastUpdateDate` FROM `cyberconnector`.`products` LIMIT 1000;
		StringBuffer sql = new StringBuffer("select identifier, name from cyberconnector.products;");
		ResultSet rs = DataBaseOperation.query(sql.toString());
		List productlist = new ArrayList();
		try {
			while(rs.next()){
				Map m = new HashMap();
//				String[] idname = new String[2];
				m.put("id", rs.getString("identifier")) ;
				m.put("name", rs.getString("name"));
				productlist.add(m);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return productlist;
	}
	
	public static void addNewDataSet(String oid, String name,  String producturl){
		StringBuffer sql = new StringBuffer("insert into datasets (identifier, name, dataURL) values ('");
		sql.append(oid).append("','");
		sql.append(name).append("','");
		sql.append(producturl).append("');");
		logger.info(sql.toString());
		DataBaseOperation.execute(sql.toString());
	}
	/**
	 * 
	 * @param parametermap
	 * @return
	 */
	public static String turnMap2String(Map<String, String> parametermap){
		Iterator it = parametermap.keySet().iterator();
		StringBuffer str = new StringBuffer();
		int label = -1;
		while(it.hasNext()){
			String key = (String)it.next();
			String value = (String)parametermap.get(key);
			if(label!=-1){
				str.append(";");
			
			}
			str.append(key).append(",").append(value);
			label++;
		}
		return str.toString();
	}
	/**
	 * add new order
	 * @param oid
	 * @param productid
	 * @param parametermap
	 */
	public static void addNewOrder(String oid, String productid, Map<String, String> parametermap){
		StringBuffer sql = new StringBuffer("insert into cyberconnector.orders (orderid, product, ordertime, updatetime,  email, status, message, parametermap, begintime, userid) values ('");
		BaseTool tool = new BaseTool();
		String curtime = tool.getCurrentMySQLDatetime();
		sql.append(oid).append("','").append(productid).append("','").append(curtime).append("','").append(tool.getCurrentMySQLDatetime()).append("','");
		sql.append((String)parametermap.get("email"))
				.append("','Ready', 'A new order is placed.','")
				.append(turnMap2String(parametermap))
				.append("','")
				.append(curtime)
				.append("','")
				.append((String)parametermap.get("userid")).append("');");
		logger.info(sql.toString());
		DataBaseOperation.execute(sql.toString());
	}
	/**
	 * Add a new order to the database
	 * @param oid
	 * @param category
	 * @param east
	 * @param south
	 * @param west
	 * @param north
	 * @param proj
	 * @param begintime
	 * @param endtime
	 * @param mail
	 */
	public static void addNewOrder(String oid, String category, String east, String south, String west, String north, String proj, String begintime, String endtime, String mail){
		StringBuffer sql = new StringBuffer("insert into cyberconnector.orders (orderid, product, ordertime, updatetime, project,  east, south, west, north, email, begintime, endtime, status, message) values ('");
		BaseTool tool = new BaseTool();
		sql.append(oid).append("','").append(category).append("','").append(tool.getCurrentMySQLDatetime()).append("','").append(tool.getCurrentMySQLDatetime()).append("','").append(proj).append("','").append(east).append("','");
		sql.append(south).append("','").append(west).append("','").append(north).append("','").append(mail).append("','");
		sql.append(begintime).append("','").append(endtime).append("','Ready', 'A new order is placed.');");
		logger.info(sql.toString());
		DataBaseOperation.execute(sql.toString());
	}
	/**
	 * Update the status of an order
	 * @param oid
	 * Order id
	 * @param status
	 * Order new status
	 */
	public static void updateAnOrderStatus(String oid, String status){
		BaseTool tool = new BaseTool();
		String curtime = tool.getCurrentMySQLDatetime();
		StringBuffer sql = new StringBuffer("update cyberconnector.orders set status = '").append(status).append("', updatetime = '").append(curtime).append("', endtime = '").append(curtime).append("' where orderid = '").append(oid).append("';");
		DataBaseOperation.update(sql.toString());
	}
	/**
	 * Update an order's status and message
	 * @param oid
	 * @param status
	 * @param message
	 */
	public static void updateAnOrderStatus(String oid, String status,String message){
		BaseTool tool = new BaseTool();
		message = tool.escape(message);
		String curtime = tool.getCurrentMySQLDatetime();
		StringBuffer sql = new StringBuffer("update cyberconnector.orders set status = '").append(status).append("', updatetime = '").append(curtime).append("', endtime = '").append(curtime).append("', message = '").append(message).append(" ' where orderid = '").append(oid).append("';");
		logger.info(sql.toString());
		DataBaseOperation.update(sql.toString());
	}
	/**
	 * Query the status of an order
	 * @param orderid
	 * The id of an order
	 * @return
	 * The status of an order
	 */
	public static String queryAnOrderStatus(String orderid){
		StringBuffer sql = new StringBuffer("select status from cyberconnector.orders where orderid = '").append(orderid).append("';");
		ResultSet rs = DataBaseOperation.query(sql.toString());
		String status = null;
		try {
			if(rs.next()){
				status = rs.getString("status");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("Fail to query the status of an order whose id is "+orderid+". Message: "+e.getLocalizedMessage());
		}finally{
			DataBaseOperation.closeConnection();
		}
		return status;
	}
	/**
     * fetch product list from the database
     * add by Ziheng Sun on 10/30/2015
     * @return
     */
    public static synchronized List fetchProductList(){
   	 StringBuffer sql = new StringBuffer();
   	 sql.append("select identifier, name from cyberconnector.products;");
   	 ResultSet rs = DataBaseOperation.query(sql.toString());
   	 List productlist = new ArrayList();
   	 try{
   		 while(rs.next()){
   			 String id = rs.getString("identifier");
   			 String name = rs.getString("name");
       		 String[] aproduct = {id, name};
       		 productlist.add(aproduct);
       	 }
   	 }catch(Exception e){
   		 throw new RuntimeException(e.getLocalizedMessage());
   	 }finally{
   		 DataBaseOperation.closeConnection();
   	 }
   	 return productlist;
    }
	/**
	 * Update the message of an order
	 * @param orderid
	 * Order id
	 * @param message
	 * Message content
	 */
	public static void updateOrderMessage(String orderid, String message){
		StringBuffer sql = new StringBuffer("update cyberconnector.orders set message = '").append(message).append("' where orderid = '").append(orderid).append("';");
		DataBaseOperation.update(sql.toString());
	}
	/**
	 * Register a new product into the database
	 * @param id
	 * @param abbr
	 * @param desc
	 * @param keywords
	 * @param name
	 * @param east
	 * @param south
	 * @param west
	 * @param north
	 * @param srs
	 * @param begintime
	 * @param endtime
	 * @param ifvirtual
	 * @param parent_abstract_model
	 * @param dataformat
	 * @param accessURL
	 * @param ontologyreference
	 * @param lastUpdateDate
	 */
	public static void registerAProduct(String id, String abbr, String desc, String keywords, String name, String east, String south, String west, String north, String srs, String begintime, String endtime, boolean ifvirtual, String parent_abstract_model, String dataformat, String accessURL, String ontologyreference, String lastUpdateDate){
		StringBuffer sql = new StringBuffer("insert into cyberconnector.products (identifier, abbreviation, description, keywords, name, east, south, west, north, srs, begintime, endtime, ifvirtual, parent_abstract_model, dataFormat, accessURL, ontology_reference, lastUpdateDate ) values ('").append(id).append("', '").append(abbr).append("','");
		sql.append(desc).append("','").append(keywords).append("','");
		sql.append(name).append("',").append(east).append(",");
		sql.append(south).append(",").append(west).append(",");
		sql.append(north).append(",'").append(srs).append("','");
		sql.append(begintime).append("','").append(endtime).append("',");
		sql.append(ifvirtual).append(",'").append(parent_abstract_model).append("','");
		sql.append(dataformat).append("','").append(accessURL).append("','");
		sql.append(ontologyreference).append("','").append(lastUpdateDate).append("');");
		logger.info(sql.toString());
		DataBaseOperation.execute(sql.toString());
	}
	/**
	 * Query abstract model id by product name
	 * @param product
	 * Product name
	 * @return
	 * Corresponding abstract model
	 */
	public static String queryAbstractModelIdByProductName(String product) {
		StringBuffer sql = new StringBuffer("select abstract_model.identifier from cyberconnector.abstract_model, cyberconnector.products where products.name = '").append(product).append("' and products.parent_abstract_model = abstract_model.identifier;");
		logger.info(sql.toString());
		ResultSet rs = DataBaseOperation.query(sql.toString());
		String amid = null;
		try {
			if(rs.next()){
				amid = rs.getString("identifier");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("Cannot find the corresponding abstract model of the specified product.");
		}finally{
			DataBaseOperation.closeConnection();
		}
		return amid;
	}
	/**
	 * Get product name by id
	 * @param productid
	 * @return
	 */
	public static String getProductNameById(String productid) {
		StringBuffer sql = new StringBuffer("select name from cyberconnector.products where identifier = '").append(productid).append("';");
		logger.info(sql.toString());
		ResultSet rs = DataBaseOperation.query(sql.toString());
		String name = null;
		try {
			if(rs.next()){
				name = rs.getString("name");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("SQLException:"+e.getLocalizedMessage());
		}finally{
			DataBaseOperation.closeConnection();
		}
		return name;
	}
	
	/**
	 * Query abstrac model xml by model id
	 * @param abstractmodelid
	 * Id of abstract model
	 * @return
	 * Model XML string array: string[0] is process connection description XML; string[1] is parameter connection description XML.
	 */
	public static String[] queryAbstracModelXMLById(String abstractmodelid) {
		StringBuffer sql = new StringBuffer("select process_connection, param_connection from abstract_model where identifier = '").append(abstractmodelid).append("';");
		logger.info(sql.toString());
		ResultSet rs = DataBaseOperation.query(sql.toString());
		String[] array = new String[2];
		try {
			if(rs.next()){
				String process_connection = rs.getString("process_connection");
				String param_connection = rs.getString("param_connection");
				array[0] = process_connection;
				array[1] = param_connection;
			}else{
				throw new RuntimeException("No such an abstract model with the inputted identifier.");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			DataBaseOperation.closeConnection();
			throw new RuntimeException("Exception happens when querying abstract model XML by model id. Message: " + e.getLocalizedMessage());
		}finally{
			DataBaseOperation.closeConnection();
		}
		return array;
	}
	/**
	 * get parameter map
	 * @param oid
	 * @return
	 */
	public static String getParametermap(String oid) {
		StringBuffer sql = new StringBuffer("select parametermap from cyberconnector.orders where orderid = '").append(oid).append("';");
		logger.info(sql.toString());
		ResultSet rs = DataBaseOperation.query(sql.toString());
		String pmap = null;
		try {
			if(rs.next()){
				pmap = rs.getString("parametermap");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			DataBaseOperation.closeConnection();
			throw new RuntimeException("Fail to get parameter map."+e.getLocalizedMessage());
		}finally{
			DataBaseOperation.closeConnection();
		}
		return pmap;
	}

}
