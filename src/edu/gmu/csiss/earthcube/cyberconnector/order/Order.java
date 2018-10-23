package edu.gmu.csiss.earthcube.cyberconnector.order;

import java.util.Iterator;
import java.util.Map;

/**
*Class Order.java
*@author Ziheng Sun
*@time Feb 2, 2017 5:09:00 PM
*Original aim is to support CyberConnector.
*/
public class Order {
	
	String orderid, status, product, proj, east, south, west, north, ordertime, updatetime,  begintime, endtime, mail, userid, result, cron, termination;
	
	Map<String, String> parametermap;
	
	public Order(){
		
		
	}
	
	public Order(String orderid, String product, String proj, String east, String south, String west, String north, String updatetime, String ordertime,
			String begintime, String endtime, String mail, String userid, String result, Map<String, String> parametermap, String cron, String termination) {
		
		this.orderid = orderid;
		
		this.product = product;
		
		this.proj = proj;
		
		this.east = east;
		
		this.south = south;
		
		this.west = west;
		
		this.north = north;
		
		this.updatetime = updatetime;
		
		this.ordertime = ordertime;
		
		this.begintime = begintime;
		
		this.endtime = endtime;
		
		this.mail = mail;
		
		this.userid = userid;
		
		this.result = result;
		
		this.parametermap = parametermap;
		
		this.cron = cron;
		
		this.termination = termination;
		
	}
	
	public String getTermination() {
		return termination;
	}

	public void setTermination(String termination) {
		this.termination = termination;
	}

	public String getCron() {
		return cron;
	}

	public void setCron(String cron) {
		this.cron = cron;
	}

	public String getResult() {
		return result;
	}
	
	public void setResult(String result) {
		this.result = result;
	}

	public String getOrdertime() {
		return ordertime;
	}

	public void setOrdertime(String ordertime) {
		this.ordertime = ordertime;
	}

	public String getUpdatetime() {
		return updatetime;
	}

	public void setUpdatetime(String updatetime) {
		this.updatetime = updatetime;
	}

	public Map<String, String> getParametermap() {
		return parametermap;
	}

	public void setParametermap(Map<String, String> parametermap) {
		this.parametermap = parametermap;
	}
	
	public String getOrderid() {
		return orderid;
	}

	public void setOrderid(String orderid) {
		this.orderid = orderid;
	}
	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getProduct() {
		return product;
	}

	public void setProduct(String product) {
		this.product = product;
	}

	public String getProj() {
		return proj;
	}

	public void setProj(String proj) {
		this.proj = proj;
	}

	public String getEast() {
		return east;
	}

	public void setEast(String east) {
		this.east = east;
	}

	public String getSouth() {
		return south;
	}

	public void setSouth(String south) {
		this.south = south;
	}

	public String getWest() {
		return west;
	}

	public void setWest(String west) {
		this.west = west;
	}

	public String getNorth() {
		return north;
	}

	public void setNorth(String north) {
		this.north = north;
	}

	public String getBegintime() {
		return begintime;
	}

	public void setBegintime(String begintime) {
		this.begintime = begintime;
	}

	public String getEndtime() {
		return endtime;
	}

	public void setEndtime(String endtime) {
		this.endtime = endtime;
	}

	public String getMail() {
		return mail;
	}

	public void setMail(String mail) {
		this.mail = mail;
	}

	public String getUserid() {
		return userid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}
	
	public String toJSON(){
		
		StringBuffer json = new StringBuffer();
		
		json.append("{").append("\n");
		
		json.append(" \"begintime\" : \"").append(this.getBegintime()).append("\", \n");
		
		json.append(" \"east\" : \"").append(this.getEast()).append("\", \n");
		
		json.append(" \"endtime\" : \"").append(this.getEndtime()).append("\", \n");

		json.append(" \"email\" : \"").append(this.getMail()).append("\", \n");
		
		json.append(" \"north\" : \"").append(this.getNorth()).append("\", \n");
		
		json.append(" \"orderid\" : \"").append(this.getOrderid()).append("\", \n");
		
		json.append(" \"ordertime\" : \"").append(this.getOrdertime()).append("\", \n");
		
		json.append(" \"product\" : \"").append(this.getProduct()).append("\", \n");
		
		json.append(" \"projection\" : \"").append(this.getProj()).append("\", \n");
		
		json.append(" \"result\" : \"").append(this.getResult()).append("\", \n");
		
		json.append(" \"south\" : \"").append(this.getSouth()).append("\", \n");
		
		json.append(" \"status\" : \"").append(this.getStatus()).append("\", \n");
		
		json.append(" \"lastupdatetime\" : \"").append(this.getUpdatetime()).append("\", \n");
		
		json.append(" \"userid\" : \"").append(this.getUserid()).append("\", \n");
		
		json.append(" \"west\" : \"").append(this.getWest()).append("\", \n");
		
		json.append(" \"parametermap\" : [");
		
		if(this.getParametermap()!=null){
			
			json.append("\n");
			
			Iterator it = this.getParametermap().keySet().iterator();
			
			while(it.hasNext()){
				
				String key = (String) it.next();
				
				String value = this.getParametermap().get(key);
				
				json.append("     {  \"key\" : \"").append(key).append("\",  \"value\" : \"").append(value).append("\" }");
				
				if(it.hasNext()) 
					
					json.append(", \n");
				
			}
			
		}
		
		json.append(" ]\n");
		
		json.append("}");
		
		return json.toString();
		
	}

}
