package com.gw.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
public class Environment {
	@Id
	String id;
	
	String name, type, bin, pyenv, basedir;

	// String host;

	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hostid")
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Host hostobj;

	@Column(columnDefinition = "LONGTEXT")
	String settings;

	
	public Host getHostobj() {
		return this.hostobj;
	}

	public void setHostobj(Host hostobj) {
		this.hostobj = hostobj;
	}

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
