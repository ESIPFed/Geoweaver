package com.gw.jpa;

import java.sql.Date;

import javax.annotation.Generated;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.springframework.lang.NonNull;

@Entity
public class GWUser {

    @Id
    String id;

    @Column
    @NonNull
    String username;
    
    @Column
    @NonNull
    String password;
    
    @Column
    String role;
    
    @Column
    @NonNull
    String email;

    @Column
    Boolean isactive;

    @Column
    Date registration_date;
    
    @Column
    Date last_login_date;

    @Column
    Boolean loggedIn;

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUsername() {
		return this.username;
	}

	public void setUsername(String username) {
		this.username = username;
	}


	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getRole() {
		return this.role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getEmail() {
		return this.email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Boolean getIsactive() {
		return this.isactive;
	}

	public void setIsactive(Boolean isactive) {
		this.isactive = isactive;
	}

	public Date getRegistration_date() {
		return this.registration_date;
	}

	public void setRegistration_date(Date registration_date) {
		this.registration_date = registration_date;
	}

	public Date getLast_login_date() {
		return this.last_login_date;
	}

	public void setLast_login_date(Date last_login_date) {
		this.last_login_date = last_login_date;
	}

	public Boolean getLoggedIn() {
		return this.loggedIn;
	}

	public void setLoggedIn(Boolean loggedIn) {
		this.loggedIn = loggedIn;
	}



    
}
