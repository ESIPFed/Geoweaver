package edu.gmu.csiss.earthcube.cyberconnector.user;

public class Token {
	String token;
	long expireDate;
	
	public String getToken() {
		return token;
	}
	
	public void setToken(String token) {
		this.token = token;
	}
	
	public long getExpireDate() {
		return expireDate;
	}
	
	public void setExpireDate(long expireDate) {
		this.expireDate = expireDate;
	}
	
}
