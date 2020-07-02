package gw.ncwms;

import gw.utils.BaseTool;
import gw.utils.MyHttpUtils;
import gw.utils.SysDir;

/**
 * not used in Geoweaver
 * @author jensensun
 *
 */
@Deprecated
public class ncWMSTool {
	
	public static void addDataset(String querystr) {
		
		String resp = MyHttpUtils.doPost_Auth_URLEncode(SysDir.ncWMSURL+"/"+SysDir.ncUsername+"/addDataset", querystr, SysDir.ncUsername, SysDir.ncPassword);
		
		System.out.println("The response is : " + resp);
		
		if(resp.indexOf("is being added")==-1) {
			
			throw new RuntimeException("Fail to add data into ncWMS. " + resp);
			
		}
		
	}
	
	public static void removeDataset(String querystr) {
		
		String resp = MyHttpUtils.doPost_BasicAuth(SysDir.ncWMSURL, querystr, SysDir.ncUsername, SysDir.ncPassword);
		
		if(resp.indexOf("has been removed")==-1) {
			
			throw new RuntimeException("Fail to remove data from ncWMS. " + resp);
			
		}
		
	}
	
	public static void main(String[] args) {
		
//		ncWMSTool.addDataset("id=hmgrid");
		
//		ncWMSTool.addDataset("id=hmgrid&location=D:/work/TESTDATA/earthcube/archv.2009_092_00_3z.nc");
		
		String location = "http://localhost:8080/CyberConnector/uploadFile/archv.2009_099_00_3z.nc";
		
		if(location.startsWith(SysDir.PREFIXURL)) {
			
			location = BaseTool.getCyberConnectorRootPath() + "/" + location.replaceAll(SysDir.PREFIXURL+"/CyberConnector/","");
			
			System.out.println("the new location is : " + location);
			
		}
		
	}
	
}
