package edu.gmu.csiss.earthcube.cyberconnector.products;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import edu.gmu.csiss.earthcube.cyberconnector.database.DataBaseOperation;
import edu.gmu.csiss.earthcube.cyberconnector.tools.GetProductInputDetailsTool;

/**
*Class RetrieveProductTool.java
*@author Ziheng Sun
*@time Feb 8, 2017 12:25:06 PM
*Original aim is to support CyberConnector.
*/
public class RetrieveProductTool {
	
	/**
	 * Absorb values into product
	 * @param p
	 * @param pmap
	 * @return
	 */
	public static Product absorbParameterValuePairs(Product p, Map<String, String[]> pmap){
		
		for(int i=0, size=p.getInputlist().size(); i<size; i++){
			
			Input in = p.getInputlist().get(i);
			
			if(pmap.get(in.getKey())!=null){
				
				String val = pmap.get(in.getKey())[0];
				
				in.setValue(val);
				
			}
			
		}
		
		return p;
	}
	
	/**
	 * Retrieve product information by id
	 * @param pid
	 * @return
	 */
	public static Product retrieveProduct(String pid){
		
		Product p = new Product();
		
		String sql = "select * from products where identifier = '" + pid + "'; ";
		
		try{
			
			ResultSet rs = DataBaseOperation.query(sql);
			
			if(rs.next()){
				
				p.setName(rs.getString("name"));
				
				p.setDesc(rs.getString("description"));
				
				p.setAbbr(rs.getString("abbreviation"));
				
				p.setId(rs.getString("identifier"));
				
				p.setKeywords(rs.getString("keywords"));
				
				p.setBegintime(rs.getString("begintime"));
				
				p.setEndtime(rs.getString("endtime"));
				
				p.setWest(rs.getDouble("west"));
				
				p.setEast(rs.getDouble("east"));
				
				p.setSouth(rs.getDouble("south"));
				
				p.setNorth(rs.getDouble("north"));
				
				p.setSrs(rs.getString("srs"));
				
				p.setFormat(rs.getString("dataFormat"));
				
				p.setIfvirtual(rs.getString("ifvirtual"));
				
				p.setIsspatial(rs.getString("isspatial"));
				
				p.setParentmodel(rs.getString("parent_abstract_model"));
				
				p.setFormat(rs.getString("dataFormat"));
				
				p.setAccessurl(rs.getString("accessURL"));
				
				p.setLastupdate(rs.getString("lastUpdateDate"));
				
				p.setUserid(rs.getString("userid"));
				
				p.setOntology(rs.getString("ontology_reference"));
				
				p.setLikes(rs.getInt("likes"));
				
			}
			
			GetProductInputDetailsTool tool = new GetProductInputDetailsTool();
			
			String inputjson = tool.getInputDetailsJSON(pid);
			
			JSONParser parser = new JSONParser();
			
			JSONObject jsonarrayobj = (JSONObject) parser.parse(inputjson);
			
			JSONArray array = (JSONArray)jsonarrayobj.get("inputparametermap");
			
			List<Input> inputlist = new ArrayList();
			
			for(int i=0; i<array.size(); i++){
				
				JSONObject obj = (JSONObject)array.get(i);
				
				Input newinput = new Input();
				
				newinput.setDatatype((String)obj.get("datatype"));
				
				newinput.setFormat((String)obj.get("format"));
				
				newinput.setKey((String)obj.get("key"));
				
				newinput.setName((String)obj.get("name"));
				
				newinput.setEname(RetrieveProductTool.encode((String)obj.get("name")));
				
				inputlist.add(newinput);
				
			}
			
			p.setInputlist(inputlist);
			
		}catch(Exception e){
			
			e.printStackTrace();
			
			throw new RuntimeException("Error in querying products." + e.getLocalizedMessage());
			
		}finally{
			
			DataBaseOperation.closeConnection();
			
		}
		
		return p;
		
	}
	
	/**
	 * Encode
	 * @param name
	 * @return
	 */
	public static String encode(String name){
		
		name = name.replaceAll("\\s", "__s_");
		
		name = name.replaceAll("\\(", "__lb_");
		
		name = name.replaceAll("\\)", "__rb_");
		
		name = name.replaceAll("\\.", "__pe_");
		
		name = name.replaceAll("\\/", "__sl_");
		
		name = name.replaceAll("\\:", "__co_");
		
		name = name.replaceAll("\\,", "__com_");
		
		return name;
		
	}
	
	/**
	 * Decode
	 * @param ename
	 * @return
	 */
	public static String decode(String ename){
		
		return ename.replaceAll("__s_", " ")
				.replaceAll("__lb_", "(")
				.replaceAll("__rb_", ")")
				.replaceAll("__pe_", ".")
				.replaceAll("__sl_", "/")
				.replaceAll("__co_", ":")
				.replaceAll("__com_", ",");
		
	}
	
	public static void main(String[] args){
		
		String result = RetrieveProductTool.encode("End/Date (e.g. 2016-12-31)");
		
		System.out.println("Result : " + result);
		
		System.out.println("Decoded : " + RetrieveProductTool.decode(result));
		
	}

}
