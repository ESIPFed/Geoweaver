package edu.gmu.csiss.earthcube.cyberconnector.products;

import edu.gmu.csiss.earthcube.cyberconnector.database.DataBaseBroker;
import edu.gmu.csiss.earthcube.cyberconnector.utils.RandomString;

/**
 *Class RegisterProductTool.java
 *@author Ziheng Sun
 *@time Aug 12, 2015 11:19:35 AM
 *Original aim is to support CyberConnector.
 */
public class RegisterProductTool {
	/**
	 * Register a new product category
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
	public void registerAProduct(String abbr, String desc, String keywords, String name, String east, String south, String west, String north, String srs, String begintime, String endtime, boolean ifvirtual, String parent_abstract_model, String dataformat, String accessURL, String ontologyreference, String lastUpdateDate){
		//generate a id for the product
		String id = new RandomString(30).nextString();
		//register the metadata into the database
		DataBaseBroker.registerAProduct(id, abbr, desc, keywords, name, east, south, west, north, srs, begintime, endtime, ifvirtual, parent_abstract_model, dataformat, accessURL, ontologyreference, lastUpdateDate);
	}
	
	public static void main(String[] args){
		RegisterProductTool tool = new RegisterProductTool();
//		tool.registerAProduct("customized global VCI", "vegetation condition index", "vci", "16 days 250m global customizable VCI", "180", "-90", "-180", "90", "EPSG:4326", "2001-01-01", "2015-08-12", true, "urn:uuid:71da4a70-75d4-1031-b64f-598681aea6d6", "GeoTiff", null, null, "2015-08-12");
//		tool.registerAProduct("fvcom river input mexico bay", "river boundary condition data", "river boundary condition data", "river boundary condition data", "180", "-90", "-180", "90", null, "2001-01-01", "2015-08-12", true, "urn:uuid:71da4a70-75d4-1031-b64f-598681aea6d6", "GeoTiff", null, null, "2015-10-16");
//		tool.registerAProduct("fvcom temperature and salinity input", "temperature and salinity condition data", "temperature and salinity condition data", "temperature and salinity condition data", "180", "-90", "-180", "90", null, "2010-01-01", "2015-08-12", true, "urn:uuid:71da4a70-75d4-1031-b64f-598681aea6d6", "Netcdf", null, null, "2015-10-17");
		
		tool.registerAProduct("fvcom wind forcing input", "wind forcing  condition data", "wind forcing condition data", "wind forcing condition data", "180", "-90", "-180", "90", null, "2010-01-01", "2015-08-12", true, "urn:uuid:71da4a70-75d4-1031-b64f-598681aea6d6", "netcdf", null, null, "2015-10-17");
	}
}
