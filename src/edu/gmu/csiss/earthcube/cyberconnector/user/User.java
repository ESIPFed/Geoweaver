package edu.gmu.csiss.earthcube.cyberconnector.user;

import java.util.List;

import edu.gmu.csiss.earthcube.cyberconnector.order.Order;
import edu.gmu.csiss.earthcube.cyberconnector.products.Product;
import edu.gmu.csiss.earthcube.cyberconnector.services.Service;

/**
*Class User.java
*@author Ziheng Sun
*@time Jan 26, 2017 4:06:23 PM
*Original aim is to support CyberConnector.
*/
public class User {

	String name = "", sex  = "", id  = "", type  = "", last_login_time  = "", last_operation_time  = "", 
			status  = "", token  = "", department  = "", institute  = "", last_ip  = "", password  = "", email  = "", 
			phone  = "", address  = "", fullname  = "", reg_time  = "";
	
	List<Order> orders;
	
	List<Service> services;
	
	List<Product> products;
	
	boolean remember = false;
	
	public List<Product> getProducts() {
		return products;
	}

	public void setProducts(List<Product> products) {
		this.products = products;
	}

	public List<Order> getOrders() {
		return orders;
	}

	public void setOrders(List<Order> orders) {
		this.orders = orders;
	}

	public List<Service> getServices() {
		return services;
	}

	public void setServices(List<Service> services) {
		this.services = services;
	}

	public String getReg_time() {
		return reg_time;
	}

	public void setReg_time(String reg_time) {
		this.reg_time = reg_time;
	}

	public String getFullname() {
		return fullname;
	}

	public void setFullname(String fullname) {
		this.fullname = fullname;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSex() {
		return sex;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}
	
	public boolean isRemember() {
		return remember;
	}

	public void setRemember(boolean remember) {
		this.remember = remember;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getLast_login_time() {
		return last_login_time;
	}

	public void setLast_login_time(String last_login_time) {
		this.last_login_time = last_login_time;
	}

	public String getLast_operation_time() {
		return last_operation_time;
	}

	public void setLast_operation_time(String last_operation_time) {
		this.last_operation_time = last_operation_time;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public String getInstitute() {
		return institute;
	}

	public void setInstitute(String institute) {
		this.institute = institute;
	}

	public String getLast_ip() {
		return last_ip;
	}

	public void setLast_ip(String last_ip) {
		this.last_ip = last_ip;
	}
	
	
	
}
