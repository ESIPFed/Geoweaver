package edu.gmu.csiss.earthcube.cyberconnector.products;
/**
*Class Input.java
*@author Ziheng Sun
*@time Feb 8, 2017 11:03:44 PM
*Original aim is to support CyberConnector.
*/
public class Input {

	String datatype, format, key, name, ename, value;
	
	public Input(){
		
	}
	
	public Input(String datatype, String format, String key, String name, String value){
		
		this.datatype = datatype;
		
		this.format = format;
		
		this.key = key;
		
		this.name = name;
		
		this.value = value;
		
	}
	
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getEname() {
		return ename;
	}

	public void setEname(String ename) {
		this.ename = ename;
	}

	public String getDatatype() {
		return datatype;
	}

	public void setDatatype(String datatype) {
		this.datatype = datatype;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}
