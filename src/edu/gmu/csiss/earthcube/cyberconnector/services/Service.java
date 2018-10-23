package edu.gmu.csiss.earthcube.cyberconnector.services;
/**
*Class Service.java
*@author Ziheng Sun
*@time Feb 2, 2017 5:12:36 PM
*Original aim is to support CyberConnector.
*/
public class Service {
	
	String id, home, name, username, desc, status, expiration, majorversion, minorversion, keywords, servicetype, accessurl, wsdlurl, registerdate;

	public Service(){
		
	}
	
	public Service(String id, String home, String name, String desc, String status, String expiration,
			String majorversion, String minorversion, String keywords, String servicetype, String accessurl,
			String wsdlurl, String registerdate, String user) {
		
		this.id = id;
		
		this.home = home;
		
		this.name = name;
		
		this.desc = desc;
		
		this.status = status;
		
		this.expiration = expiration;
		
		this.majorversion = majorversion;
		
		this.minorversion = minorversion;
		
		this.keywords = keywords;
		
		this.servicetype = servicetype;
		
		this.accessurl = accessurl;
		
		this.wsdlurl = wsdlurl;
		
		this.registerdate = registerdate;
		
		this.username = user;
		
	}
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getRegisterdate() {
		return registerdate;
	}

	public void setRegisterdate(String registerdate) {
		this.registerdate = registerdate;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getHome() {
		return home;
	}

	public void setHome(String home) {
		this.home = home;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getExpiration() {
		return expiration;
	}

	public void setExpiration(String expiration) {
		this.expiration = expiration;
	}

	public String getMajorversion() {
		return majorversion;
	}

	public void setMajorversion(String majorversion) {
		this.majorversion = majorversion;
	}

	public String getMinorversion() {
		return minorversion;
	}

	public void setMinorversion(String minorversion) {
		this.minorversion = minorversion;
	}

	public String getKeywords() {
		return keywords;
	}

	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	public String getServicetype() {
		return servicetype;
	}

	public void setServicetype(String servicetype) {
		this.servicetype = servicetype;
	}

	public String getAccessurl() {
		return accessurl;
	}

	public void setAccessurl(String accessurl) {
		this.accessurl = accessurl;
	}

	public String getWsdlurl() {
		return wsdlurl;
	}

	public void setWsdlurl(String wsdlurl) {
		this.wsdlurl = wsdlurl;
	}
	
	
	
//	CREATE TABLE `service` (
//			`tid` INT(11) NOT NULL AUTO_INCREMENT,
//			`id` VARCHAR(48) NOT NULL,
//			`home` VARCHAR(255) NULL DEFAULT NULL,
//			`name` TEXT NULL,
//			`description` TEXT NULL,
//			`status` VARCHAR(16) NULL DEFAULT NULL,
//			`expiration` DATETIME NULL DEFAULT NULL,
//			`majorVersion` INT(16) NOT NULL DEFAULT '1',
//			`minorVersion` INT(16) NOT NULL DEFAULT '0',
//			`userVersion` VARCHAR(64) NULL DEFAULT NULL,
//			`keywords` VARCHAR(256) NULL DEFAULT NULL,
//			`serviceType` VARCHAR(64) NULL DEFAULT NULL,
//			`accessURL` VARCHAR(256) NULL DEFAULT NULL,
//			`wsdlURL` VARCHAR(256) NULL DEFAULT NULL,
//			PRIMARY KEY (`tid`),
//			INDEX `id` (`id`),
//			INDEX `name` (`name`(64)),
//			INDEX `description` (`description`(255)),
//			INDEX `keywords` (`keywords`),
//			INDEX `serviceType` (`serviceType`),
//			INDEX `accessURL` (`accessURL`)
//		)
//		COMMENT='This table stores the metadata of physical web services.'
//		COLLATE='latin1_swedish_ci'
//		ENGINE=InnoDB
//		AUTO_INCREMENT=108;


}
