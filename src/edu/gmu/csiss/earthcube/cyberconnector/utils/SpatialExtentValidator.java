package edu.gmu.csiss.earthcube.cyberconnector.utils;
/**
 *Class SpatialExtentValidator.java
 *@author Ziheng Sun
 *@time Sep 2, 2015 11:28:12 AM
 *Original aim is to support CyberConnector.
 */
public class SpatialExtentValidator {
	/**
	 * Validate the input spatial extent
	 * @param north
	 * @param south
	 * @param west
	 * @param east
	 * @param proj
	 * @return
	 * If valid, return true; Else, return false.
	 */
	public static boolean validate(String north, String south, String west, String east, String proj, String productcategory){
		boolean valid = false;
		try{
			double s = Double.parseDouble(south);
			double w = Double.parseDouble(west);
			double e = Double.parseDouble(east);
			double n = Double.parseDouble(north);
			if(proj.equals("EPSG:4326")){
				//check if the latitude is out of range
				if(s>90||s<-90||n>90||n<-90){
					
				}
				//check if the longitude is out of range
				else if(w>180||w<-180||e>180||e<-180){
					
				}
				//check if the south is smaller than the north
				else if(s>=n){
					
				}
				//check if the west is smaller than the east
				else if(w>=e){
					
				}
				//check if the spatial extent is bigger than 1degree*1degree
				else if((e-w)>1||(n-s)>1){
					throw new RuntimeException("The spatial extent is larger than 1degree*1degree.");
				}else {
					valid = true;
				}
				
			}
		}catch(Exception e){
			e.printStackTrace();
			throw new RuntimeException("The input spatial coordinates or projection are incorrect."+e.getLocalizedMessage());
		}
		
		return valid;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		-77.78732299804688,37.37015718405753,-78.84201049804688,38.479394673276445
		System.out.println(SpatialExtentValidator.validate("38.479394673276445", "37.37015718405753", "-78.84201049804688", "-77.78732299804688", "ESPG:4326", "16 days 250m global customizable VCI"));;
	}

}
