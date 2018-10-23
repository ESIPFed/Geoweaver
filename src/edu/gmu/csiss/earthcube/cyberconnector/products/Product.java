package edu.gmu.csiss.earthcube.cyberconnector.products;

import java.util.List;
import java.util.Map;

/**
*Class Product.java
*@author Ziheng Sun
*@time Feb 2, 2017 5:48:43 PM
*Original aim is to support CyberConnector.
*/
public class Product {

	String id, abbr, desc, keywords, name, srs, begintime, endtime, ifvirtual,
				isspatial, modelid, format, accessurl, parentmodel, ontology, 
				lastupdate, userid, iscollection;
	
	int likes;
	
	boolean cached;
	
	double east, south, west, north;
	
	List<Input> inputlist;
	
	public Product(){
		
		
	}
	/**
	 * 
	 * @param id
	 * @param abbr
	 * @param desc
	 * @param keywords
	 * @param name
	 * @param srs
	 * @param parentmodel
	 * @param begintime
	 * @param endtime
	 * @param ifvirtual
	 * @param isspatial
	 * @param modelid
	 * @param format
	 * @param accessurl
	 * @param ontology
	 * @param lastupdate
	 * @param userid
	 * @param east
	 * @param south
	 * @param west
	 * @param north
	 * @param inputmap
	 */
	public Product(String id, String abbr, String desc, String keywords, String name, String srs, String parentmodel, String begintime,
			String endtime, String ifvirtual, String isspatial, String iscollection, String modelid, String format, String accessurl, String ontology,
			String lastupdate, String userid, double east, double south, double west, double north, List<Input> inputlist, int likes) {
		super();
		this.id = id;
		this.parentmodel = parentmodel;
		this.abbr = abbr;
		this.desc = desc;
		this.keywords = keywords;
		this.name = name;
		this.srs = srs;
		this.begintime = begintime;
		this.endtime = endtime;
		this.ifvirtual = ifvirtual;
		this.isspatial = isspatial;
		this.modelid = modelid;
		this.format = format;
		this.accessurl = accessurl;
		this.ontology = ontology;
		this.lastupdate = lastupdate;
		this.userid = userid;
		this.east = east;
		this.south = south;
		this.west = west;
		this.north = north;
		this.inputlist = inputlist;
		this.likes = likes;
		this.iscollection = iscollection;
	}

	public String getIscollection() {return iscollection; }
	public void setIscollection(String iscollection) { this.iscollection = iscollection; }

	public boolean isCached() {
		return cached;
	}
	
	public void setCached(boolean cached) {
		this.cached = cached;
	}
	
	public int getLikes() {
		return likes;
	}
	
	public void setLikes(int likes) {
		this.likes = likes;
	}
	
	public List<Input> getInputlist() {
		return inputlist;
	}
	
	public void setInputlist(List<Input> inputlist) {
		this.inputlist = inputlist;
	}
	
	public String getIsspatial() {
		return isspatial;
	}

	public void setIsspatial(String isspatial) {
		this.isspatial = isspatial;
	}

	public String getParentmodel() {
		return parentmodel;
	}

	public void setParentmodel(String parentmodel) {
		this.parentmodel = parentmodel;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getAbbr() {
		return abbr;
	}

	public void setAbbr(String abbr) {
		this.abbr = abbr;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getKeywords() {
		return keywords;
	}

	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSrs() {
		return srs;
	}

	public void setSrs(String srs) {
		this.srs = srs;
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

	public String getIfvirtual() {
		return ifvirtual;
	}

	public void setIfvirtual(String ifvirtual) {
		this.ifvirtual = ifvirtual;
	}

	public String getModelid() {
		return modelid;
	}

	public void setModelid(String modelid) {
		this.modelid = modelid;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public String getAccessurl() {
		return accessurl;
	}

	public void setAccessurl(String accessurl) {
		this.accessurl = accessurl;
	}

	public String getOntology() {
		return ontology;
	}

	public void setOntology(String ontology) {
		this.ontology = ontology;
	}

	public String getLastupdate() {
		return lastupdate;
	}

	public void setLastupdate(String lastupdate) {
		this.lastupdate = lastupdate;
	}

	public String getUserid() {
		return userid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}

	public double getEast() {
		return east;
	}

	public void setEast(double east) {
		this.east = east;
	}

	public double getSouth() {
		return south;
	}

	public void setSouth(double south) {
		this.south = south;
	}

	public double getWest() {
		return west;
	}

	public void setWest(double west) {
		this.west = west;
	}

	public double getNorth() {
		return north;
	}

	public void setNorth(double north) {
		this.north = north;
	} 
	
	
	
//	CREATE TABLE `products` (
//			`identifier` VARCHAR(50) NOT NULL DEFAULT '',
//			`abbreviation` VARCHAR(50) NULL DEFAULT NULL,
//			`description` TINYTEXT NULL,
//			`keywords` TINYTEXT NULL,
//			`name` VARCHAR(100) NOT NULL,
//			`east` DOUBLE(20,6) NULL DEFAULT NULL,
//			`south` DOUBLE(20,6) NULL DEFAULT NULL,
//			`west` DOUBLE(20,6) NULL DEFAULT NULL,
//			`north` DOUBLE(20,6) NULL DEFAULT NULL,
//			`srs` VARCHAR(50) NULL DEFAULT 'EPSG:4326',
//			`begintime` DATE NULL DEFAULT NULL,
//			`endtime` DATE NULL DEFAULT NULL,
//			`ifvirtual` CHAR(1) NOT NULL DEFAULT '0',
//			`parent_abstract_model` VARCHAR(50) NULL DEFAULT NULL,
//			`dataFormat` VARCHAR(50) NULL DEFAULT NULL,
//			`accessURL` TINYTEXT NULL,
//			`ontology_reference` TINYTEXT NULL,
//			`lastUpdateDate` DATE NULL DEFAULT NULL,
//			`userid` INT(10) NULL DEFAULT NULL,
//			PRIMARY KEY (`identifier`),
//			UNIQUE INDEX `identifier` (`identifier`),
//			UNIQUE INDEX `name` (`name`),
//			INDEX `ifvirtual` (`ifvirtual`),
//			INDEX `parent_abstract_model` (`parent_abstract_model`)
//		)
//		COMMENT='This table archives the metadata of VDPs.'
//		COLLATE='latin1_swedish_ci'
//		ENGINE=InnoDB;

	
}
