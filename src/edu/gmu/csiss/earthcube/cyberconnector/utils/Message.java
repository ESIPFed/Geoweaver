package edu.gmu.csiss.earthcube.cyberconnector.utils;
/**
*Class Message.java
*@author Ziheng Sun
*@time Jan 27, 2017 11:08:53 PM
*Original aim is to support CyberConnector.
*/
public class Message {
	
	String a, //performer
	
			  b, //caller
	
			  information, //information sent by performer to caller
			  
			  title, strongmsg, displaymsg;
	
	boolean isdone; 
	
	public Message(String a, String b, String information, boolean isdone) {
		this.a = a;
		this.b = b;
		this.information = information;
		this.isdone = isdone;
	}

	public Message(String a, String b, String information, String title, String strongmsg, String displaymsg,
			boolean isdone) {
		this.a = a;
		this.b = b;
		this.information = information;
		this.title = title;
		this.strongmsg = strongmsg;
		this.displaymsg = displaymsg;
		this.isdone = isdone;
	}
	
	public String getDisplaymsg() {
		return displaymsg;
	}

	public void setDisplaymsg(String displaymsg) {
		this.displaymsg = displaymsg;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getStrongmsg() {
		return strongmsg;
	}

	public void setStrongmsg(String strongmsg) {
		this.strongmsg = strongmsg;
	}

	public boolean isIsdone() {
		return isdone;
	}

	public void setIsdone(boolean isdone) {
		this.isdone = isdone;
	}

	public String getA() {
		return a;
	}

	public void setA(String a) {
		this.a = a;
	}

	public String getB() {
		return b;
	}

	public void setB(String b) {
		this.b = b;
	}

	public String getInformation() {
		return information;
	}

	public void setInformation(String information) {
		this.information = information;
	} 

}
