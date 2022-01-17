package com.gw.user;

public class GWToken {
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
