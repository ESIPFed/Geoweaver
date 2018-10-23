package edu.gmu.csiss.earthcube.cyberconnector.tools;

import edu.gmu.csiss.earthcube.cyberconnector.utils.BaseTool;

/**
 *Class DataR.java
 *@author Ziheng Sun
 *@time Dec 8, 2015 5:03:08 PM
 *Original aim is to support CyberConnector.
 */
public class DataR {
	/**
	 * 
	 * @param txtfile
	 * @param seperator
	 * @return
	 */
	public static String turnTxt2CSV(String txtfile){
		String cont = BaseTool.readStringFromFile(txtfile);
		String[] lines = cont.split("\n");
		StringBuffer newcont = new StringBuffer();
		for(int i=0;i<lines.length;i++){
			String line = lines[i].trim();
			newcont.append(line.replaceAll("\\s+", ";")).append("\n");
		}
		return newcont.toString();
	}
	/**
	 * Turn its to csv
	 */
	public static void turnITStoCSV(){
		String filename = "GOM14740_its.dat";
		String newcont = turnTxt2CSV(filename);
		String newfilename = "GOM14740_its_2.csv";
		BaseTool.writeString2File(newcont.toString(), newfilename);
		System.out.println("ITS is converted to CSV.");
	}
	/**
	 * Turn CRM sounding to CSV
	 */
	public static void turnCRMSoundingToCSV(){
		String filename = "fields_10_1_0.txt";
		String newcont = turnTxt2CSV(filename);
//		System.out.println(newcont);
		String newfile = "fields_10_1_0.csv";
//		BaseTool.writeString2File("", newfile);
		BaseTool.writeString2File(newcont.trim(), newfile);
		System.out.println("The basic field is turned to CSV.");
	}
	
	public static void extractTheFirstTenLinesFromBasicFields(){
		String filename = "fields.lsan_v1";
		String cont = BaseTool.readStringFromFile(filename);
		String[] lines = cont.split("\n");
		for(int i=0;i<10;i++){
			//combine the first 41 lines and remove the first line
			StringBuffer newcont = new StringBuffer();
			for(int j=i*41+1;j<(i+1)*41;j++){
				newcont.append(lines[j]).append("\n");
			}
			String newfilename = filename+"."+i+".txt";
			BaseTool.writeString2File(newcont.toString(), newfilename);
			BaseTool.writeString2File(turnTxt2CSV(newfilename), newfilename);
		}
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		turnITStoCSV();
//		turnCRMSoundingToCSV();
		extractTheFirstTenLinesFromBasicFields();
	}

}
