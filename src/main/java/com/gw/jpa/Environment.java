package com.gw.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Environment {
	
//	StringBuffer sql = new StringBuffer("insert into environment (id, name, type, bin, pyenv, host, basedir, settings) values ('");
	@Id
	String id;
	
	String name, type, bin, pyenv, host, basedir;
	
	@Column(columnDefinition = "LONGTEXT")
	String settings;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getBin() {
		return bin;
	}

	public void setBin(String bin) {
		this.bin = bin;
	}

	public String getPyenv() {
		return pyenv;
	}

	public void setPyenv(String pyenv) {
		this.pyenv = pyenv;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getBasedir() {
		return basedir;
	}

	public void setBasedir(String basedir) {
		this.basedir = basedir;
	}

	public String getSettings() {
		return settings;
	}

	public void setSettings(String settings) {
		this.settings = settings;
	}

	
	
}
