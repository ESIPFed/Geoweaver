package com.gw.jpa;

import javax.persistence.Column;
import javax.persistence.Lob;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * Process POJO
 * @author jensensun
 *
 */
@Entity
public class GWProcess {

	@Id
	String id;
	
	String name;
	
	@Lob
	@Column(columnDefinition = "LONGTEXT")
	String description;

	@Lob
	@Column(columnDefinition = "LONGTEXT")
	String code;

	String lang;
	
	String owner;

	//true: private; false: public
	String confidential;


	public String getOwner() {
		return this.owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}
	public String getConfidential() {
		return this.confidential;
	}

	public void setConfidential(String confidential) {
		this.confidential = confidential;
	}
	
	public String getLang() {
		return this.lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
	
}
