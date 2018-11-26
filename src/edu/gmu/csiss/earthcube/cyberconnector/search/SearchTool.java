package edu.gmu.csiss.earthcube.cyberconnector.search;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.XPath;

import edu.gmu.csiss.earthcube.cyberconnector.database.DataBaseOperation;
import edu.gmu.csiss.earthcube.cyberconnector.products.Product;
import edu.gmu.csiss.earthcube.cyberconnector.tools.LocalFileTool;
import edu.gmu.csiss.earthcube.cyberconnector.utils.BaseTool;
import edu.gmu.csiss.earthcube.cyberconnector.utils.SysDir;

/**
*Class SearchTool.java
*@author Ziheng Sun
*@time Feb 3, 2017 10:42:31 AM
*Original aim is to support CyberConnector.
*/
public class SearchTool {
	
	private static Logger logger = Logger.getLogger(SearchTool.class);
	
	static Map<String, String> map = new HashMap<String, String>();
	
	static{
		
		map.put("csw", "http://www.opengis.net/cat/csw/2.0.2");
		
		map.put("gmd", "http://www.isotc211.org/2005/gmd");
		
		map.put("gml", "http://www.opengis.net/gml");
		
		map.put("gmi", "http://www.isotc211.org/2005/gmi");
		
		map.put("gco", "http://www.isotc211.org/2005/gco");
		
		map.put("srv", "http://www.isotc211.org/2005/srv");
		
	}
	/**
	 * Search VDP
	 * @param req
	 * @return
	 */
	public static SearchResponse searchVDP(SearchRequest req){
		
		SearchResponse resp = new SearchResponse();
		
		StringBuffer sql = new StringBuffer("select * from products where name like '%").append(req.searchtext==null?"":req.searchtext).append("%'  ");
		
		sql.append("and NOT ( east < '").append(req.getWest()).append("' or west > '").append(req.getEast()).append("' and south > '").append(req.getNorth()).append("' or north < '").append(req.getSouth()).append("' ) ");
		
		sql.append(" and NOT ( begintime > '")
			.append(req.getEnddatetime())
			.append("' and endtime < '")
			.append(req.getBegindatetime())
			.append("' ) ");
		
		sql.append(" and ifvirtual = '").append(req.isvirtual).append("' ");
		
		sql.append(" ORDER BY lastUpdateDate DESC");

		sql.append(" LIMIT ").append(req.recordsperpage);
		
		int offset = (req.getPageno()-1)*req.getRecordsperpage();
		
		sql.append(" OFFSET ").append(offset).append(";");
		
//		sql.append(" LIMIT ").append(req.getRecordsperpage()).append(";");
		
		logger.debug(sql);
		
		try {
			
			ResultSet rs = DataBaseOperation.query(sql.toString());
			
			List<Product> ps = new ArrayList();
			
			while(rs.next()){
				
				Product p = new Product();
				
				p.setName(rs.getString("name"));
				
				p.setDesc(rs.getString("description"));
				
				p.setAbbr(rs.getString("abbreviation"));
				
				p.setId(rs.getString("identifier"));
				
				p.setKeywords(rs.getString("keywords"));
				
				p.setBegintime(rs.getString("begintime"));
				
				p.setEndtime(rs.getString("endtime"));
				
				p.setWest(rs.getDouble("west"));
				
				p.setEast(rs.getDouble("east"));
				
				p.setSouth(rs.getDouble("south"));
				
				p.setNorth(rs.getDouble("north"));
				
				p.setSrs(rs.getString("srs"));
				
				p.setFormat(rs.getString("dataFormat"));
				
				p.setIfvirtual(rs.getString("ifvirtual"));
				
				p.setIsspatial(rs.getString("isspatial"));
				
				p.setParentmodel(rs.getString("parent_abstract_model"));
				
				p.setAccessurl(rs.getString("accessURL"));
				
				p.setLastupdate(rs.getString("lastUpdateDate"));
				
				p.setUserid(rs.getString("userid"));
				
				p.setOntology(rs.getString("ontology_reference"));
				
				p.setLikes(rs.getInt("likes"));
				
				ps.add(p);
				
			}
			
			resp.setProducts(ps);
			
		} catch (SQLException e) {
			
			e.printStackTrace();
			
			throw new RuntimeException("Error in querying products." + e.getLocalizedMessage());
			
		}finally{
			
			DataBaseOperation.closeConnection();
			
		}
		
		//get overall number of products
		
		StringBuffer sql2 = new StringBuffer("select count(*) as total from products where name like '%").append(req.searchtext).append("%'  ");
		
		sql2.append("and NOT ( east < '").append(req.getWest()).append("' or west > '").append(req.getEast()).append("' and south > '").append(req.getNorth()).append("' or north < '").append(req.getSouth()).append("' ) ");
		
		sql2.append(" and NOT ( begintime > '")
			.append(req.getEnddatetime())
			.append("' and endtime < '")
			.append(req.getBegindatetime())
			.append("' ) ");
		
		sql2.append(" and ifvirtual = '").append(req.isvirtual).append("' ");
		
		logger.debug(sql2);
		
		try {
			
			ResultSet rs = DataBaseOperation.query(sql2.toString());
			
			if(rs.next()){
				
				resp.setProduct_total_number(rs.getInt("total"));
				
				resp.setRecordsperpage(req.getRecordsperpage());
				
			}
			
		}catch(Exception e){
			
			e.printStackTrace();
			
			throw new RuntimeException("Error in querying products." + e.getLocalizedMessage());
			
		}finally{
			
			DataBaseOperation.closeConnection();
			
		}
		
		return resp;
		
	}
	
	/**
	 * Construct CSW Request
	 * @param req
	 * @return
	 */
	public static String constructCSWRequest(SearchRequest req){
		
		int startpos = (req.pageno)*req.recordsperpage + 1;
		
		StringBuffer cswreq = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?> ")
			.append("	<GetRecords ")
			.append("	    xmlns=\"http://www.opengis.net/cat/csw/2.0.2\" ")
			.append("	    xmlns:ogc=\"http://www.opengis.net/ogc\" ")
			.append("	    xmlns:gml=\"http://www.opengis.net/gml\" ")
			.append("	    xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" ")
			.append("	    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/cat/csw/2.0.2 http://schemas.opengis.net/csw/2.0.2/CSW-discovery.xsd\" service=\"CSW\" version=\"2.0.2\" resultType=\"results\" outputFormat=\"application/xml\" outputSchema=\"http://www.isotc211.org/2005/gmd\" startPosition=\"")
			.append(startpos)
			.append("\" maxRecords=\"")
			.append(req.recordsperpage)
			.append("\"> ")
			.append("	    <Query typeNames=\"gmd:MD_Metadata\"> ")
			.append("	        <ElementSetName>full</ElementSetName> ")
			.append("	        <Constraint version=\"1.1.0\"> ")
			.append("	            <ogc:Filter> ")
			.append("	                <ogc:And> ")
			.append("	                    <ogc:PropertyIsLike wildCard=\"%\" singleChar=\"_\" escapeChar=\"\"> ")
			.append("	                        <ogc:PropertyName>apiso:Identifier</ogc:PropertyName> ")
			.append("	                        <ogc:Literal>%")
			.append(req.searchtext)
			.append("%</ogc:Literal> ")
			.append("	                    </ogc:PropertyIsLike> ");
		
		if(!req.distime && !BaseTool.isNull(req.begindatetime)){
			cswreq.append("	                <ogc:PropertyIsGreaterThanOrEqualTo> ")
			.append("	                        <ogc:PropertyName>apiso:TempExtent_begin</ogc:PropertyName> ")
			.append("	                        <ogc:Literal>")
			.append(								req.begindatetime)
			.append("							</ogc:Literal> ")
			.append("	                    </ogc:PropertyIsGreaterThanOrEqualTo> ")
			.append("	                    <ogc:PropertyIsLessThanOrEqualTo> ")
			.append("	                        <ogc:PropertyName>apiso:TempExtent_end</ogc:PropertyName> ")
			.append("	                        <ogc:Literal>")
			.append(								req.enddatetime)
			.append("							</ogc:Literal> ")
			.append("	                    </ogc:PropertyIsLessThanOrEqualTo> ");
		}
		
		cswreq.append("	                    <ogc:BBOX> ")
			.append("	                        <ogc:PropertyName>ows:BoundingBox</ogc:PropertyName> ")
			.append("	                        <gml:Envelope> ")
			.append("	                            <gml:lowerCorner>"+req.south + " " + req.west +"</gml:lowerCorner> ")
			.append("	                            <gml:upperCorner>"+req.north + " " + req.east +"</gml:upperCorner> ")
			.append("	                        </gml:Envelope> ")
			.append("	                    </ogc:BBOX> ")
			.append("	                </ogc:And> ")
			.append("	            </ogc:Filter> ")
			.append("	        </Constraint> ")
			.append("	    </Query> ")
			.append("	</GetRecords>");
		
		return cswreq.toString();
		
	}
	/**
	 * Construct a CSW request for a single record
	 * @param id
	 * @return
	 */
	public static String constructSingleCSWReq(String id){
		
		StringBuffer getreq = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?> ")
				.append("	<GetRecords ")
				.append("	    xmlns=\"http://www.opengis.net/cat/csw/2.0.2\" ")
				.append("	    xmlns:ogc=\"http://www.opengis.net/ogc\" ")
				.append("	    xmlns:gml=\"http://www.opengis.net/gml\" ")
				.append("	    xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" ")
				.append("	    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/cat/csw/2.0.2 http://schemas.opengis.net/csw/2.0.2/CSW-discovery.xsd\" service=\"CSW\" version=\"2.0.2\" resultType=\"results\" outputFormat=\"application/xml\" outputSchema=\"http://www.isotc211.org/2005/gmd\" startPosition=\"1\" maxRecords=\"10\"> ")
				.append("	    <Query typeNames=\"gmd:MD_Metadata\"> ")
				.append("	        <ElementSetName>full</ElementSetName> ")
				.append("	        <Constraint version=\"1.1.0\"> ")
				.append("	            <ogc:Filter> ")
				.append("	                    <ogc:PropertyIsEqualTo> ")
				.append("	                        <ogc:PropertyName>apiso:Identifier</ogc:PropertyName> ")
				.append("	                        <ogc:Literal>").append(id).append("</ogc:Literal> ")
				.append("	                    </ogc:PropertyIsEqualTo> ")
				.append("	            </ogc:Filter> ")
				.append("	        </Constraint> ")
				.append("	    </Query> ")
				.append("	</GetRecords>");
		
		return getreq.toString();
		
	}
	/**
	 * Get single ISO metadata
	 * @param resp
	 * @return
	 */
	public static String getSingleISOMetadata(String resp){		
		
		Document document= BaseTool.parseString(resp);
		
		if(document==null){
			throw new RuntimeException("Fail to get the full metadata");
		}
		
		XPath md_xpath = DocumentHelper.createXPath("//csw:GetRecordsResponse/csw:SearchResults/gmi:MI_Metadata"); //list all the records
		
		md_xpath.setNamespaceURIs(map);
		
		Node nd = md_xpath.selectSingleNode(document);
		
		return nd.asXML();
		
	}
	/**
	 * Get current url in ISO metadata
	 * @param md
	 * @param rawurl
	 * @param newurl
	 * @return
	 */
	public static String getCurrentURLinISO(String md){
		
		Document doc = BaseTool.parseString(md);
		
		if(doc==null){
			
			throw new RuntimeException("Fail to parse the ISO metadata");
			
		}
		
		//for gmi
		XPath accessoptions = DocumentHelper.createXPath("//gmi:MI_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification[contains(@id,'HTTP')]/srv:containsOperations/srv:SV_OperationMetadata/srv:connectPoint/gmd:CI_OnlineResource/gmd:linkage/gmd:URL");
		
		accessoptions.setNamespaceURIs(map);
		
		Node httpdownload = accessoptions.selectSingleNode(doc);
		
		if(httpdownload==null){
			
			throw new RuntimeException("The Metadata of this data has no regular HTTP downloading information. Cannot procced.");
			
		}
		
		String currenturl = httpdownload.getText();
		
		return currenturl;
		
	}
	
	/**
	 * Replace old data HTTP downloading url with new one (cached one)
	 * @param md
	 * @param newurl
	 * @return
	 */
	public static String replaceDownloadURLInISO(String md, String rawurl, String newurl){
		
		Document doc = BaseTool.parseString(md);
		
		if(doc==null){
			
			throw new RuntimeException("Fail to parse the ISO metadata");
			
		}
		
		//for gmi
		XPath accessoptions = DocumentHelper.createXPath("//gmi:MI_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification[contains(@id,'HTTP')]/srv:containsOperations/srv:SV_OperationMetadata/srv:connectPoint/gmd:CI_OnlineResource/gmd:linkage/gmd:URL");
		
		accessoptions.setNamespaceURIs(map);
		
		Node httpdownload = accessoptions.selectSingleNode(doc);
		
		if(httpdownload==null){
			
			throw new RuntimeException("The Metadata of this data has no regular HTTP downloading information. Cannot procced.");
			
		}
		
		httpdownload.setText(newurl);
		
		return doc.asXML();
		
	}
	/**
	 * Remove the XML declaration
	 * @param xml
	 * @return
	 */
	public static String removeXMLDeclaration(String xml){
		
		return xml.substring(xml.indexOf('\n')+1);
		
	}
	
	/**
	 * Update Existing Records with New Http Download URL
	 * @param id
	 * @param newurl
	 * @return
	 */
	public static boolean updatePyCSWDataURL(String id, String rawurl){
		
		boolean success  = false;
		
		if(!rawurl.startsWith(SysDir.CACHE_SERVICE_URL)){
			
			//get the original ISO metadata
			
			String getreq = constructSingleCSWReq(id);
			
			String getresp = BaseTool.POST(getreq.toString(), SysDir.CSISS_CSW_URL);
			
			String single_md = getSingleISOMetadata(getresp);
			
			String currenturl = getCurrentURLinISO(single_md);
			
			if(!currenturl.startsWith(SysDir.CACHE_SERVICE_URL)){ //avoid duplicated caching
				
				//download the data from external links to server
		    	
//		    	String newurl = BaseTool.cacheData(rawurl);
				
				String newurl = BaseTool.cacheDataLocally(rawurl);
				
				single_md = replaceDownloadURLInISO(single_md, rawurl, newurl);
				
				single_md = removeXMLDeclaration(single_md);
				
				StringBuffer req = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
						.append("<csw:Transaction xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:csw=\"http://www.opengis.net/cat/csw/2.0.2\" xmlns:ows=\"http://www.opengis.net/ows\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/cat/csw/2.0.2 http://schemas.opengis.net/csw/2.0.2/CSW-publication.xsd\" service=\"CSW\" version=\"2.0.2\">")
						.append("  <csw:Update>")
						.append( single_md) //the full ISO metadata
						.append(" 	</csw:Update>")
						.append("</csw:Transaction>");
				
				String cswresp = BaseTool.POST(req.toString(), SysDir.CSISS_CSW_URL);
				
				logger.debug(cswresp);
				
				success = parseUpdateResponse(cswresp);
				
			}else{
				
				logger.debug("The data is already cached.");
				
				success = true;
				
			}
			
		}else{
			
			logger.debug("the URL is cached. Skip this step.");
			
			success = true;
			
		}
		
		return success;
		
	}
	
	/**
	 * 
	 * @param resp
	 * @return
	 */
	public static boolean parseUpdateResponse(String resp){
		
		Document doc = BaseTool.parseString(resp);
		
		if(doc ==null){
			
			throw new RuntimeException("Fail to parse the response of CSW update request.");
			
		}
		
		XPath updatedpath =  DocumentHelper.createXPath("//csw:TransactionResponse/csw:TransactionSummary/csw:totalUpdated");
		
		updatedpath.setNamespaceURIs(map);
		
		Node updatenumnode = updatedpath.selectSingleNode(doc);
		
		boolean success = false;
		
		if(updatenumnode==null){
			
			throw new RuntimeException("Fail to get the updated element from the response XML.");
			
		}else{
			
			String updatednum = updatenumnode.getText();
			
			if("1".equals(updatednum)){
				
				success = true;
				
			}
			
		}
		
		
		return success;
		
	}
	
	/**
	 * Search CSISS PyCSW for UCAR thredds server
	 * @param req
	 * @return
	 */
	public static SearchResponse searchUCARCSW(SearchRequest req){
		
		String cswreq = SearchTool.constructCSWRequest(req);
		
		logger.debug(cswreq);
		
		String cswresp = BaseTool.POST(cswreq, SysDir.CSISS_CSW_URL);
		
		logger.debug(cswresp);
		
		SearchResponse resp = SearchTool.parseCSWResponse(cswresp);
		
		return resp;
		
	}
	/**
	 * Parse CSW response
	 * @param resp
	 * @return
	 */
	public static SearchResponse parseCSWResponse(String resp){
		
		SearchResponse respobj = new SearchResponse();
		
		Document document= BaseTool.parseString(resp);
		
		if(document!=null){
			
//			xmlns:fes20="http://www.opengis.net/fes/2.0" xmlns:inspire_common="http://inspire.ec.europa.eu/schemas/common/1.0" xmlns:rim="urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:ows20="http://www.opengis.net/ows/2.0" xmlns:ows="http://www.opengis.net/ows" xmlns:gml="http://www.opengis.net/gml" xmlns:ebrim="http://www.opengis.net/cat/wrs/1.0" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:inspire_ds="http://inspire.ec.europa.eu/schemas/inspire_ds/1.0" xmlns:wrs="http://www.opengis.net/cat/wrs/1.0" xmlns:fgdc="http://www.opengis.net/cat/csw/csdgm" xmlns:csw="http://www.opengis.net/cat/csw/2.0.2" xmlns:soapenv="http://www.w3.org/2003/05/soap-envelope" xmlns:ows11="http://www.opengis.net/ows/1.1" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:atom="http://www.w3.org/2005/Atom" xmlns:dct="http://purl.org/dc/terms/" xmlns:ogc="http://www.opengis.net/ogc" xmlns:gm03="http://www.interlis.ch/INTERLIS2.3" xmlns:apiso="http://www.opengis.net/cat/csw/apiso/1.0" xmlns:dif="http://gcmd.gsfc.nasa.gov/Aboutus/xml/dif/" xmlns:csw30="http://www.opengis.net/cat/csw/3.0" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:srv="http://www.isotc211.org/2005/srv" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:os="http://a9.com/-/spec/opensearch/1.1/" xmlns:sitemap="http://www.sitemaps.org/schemas/sitemap/0.9" version="2.0.2" xsi:schemaLocation="http://www.opengis.net/cat/csw/2.0.2 http://schemas.opengis.net/csw/2.0.2/CSW-discovery.xsd"
			
			XPath nextrecordpath = DocumentHelper.createXPath("//csw:GetRecordsResponse/csw:SearchResults/@nextRecord");
			
			int nextrecordindex = ((Double)nextrecordpath.numberValueOf(document)).intValue();
			
			XPath numberOfRecordsMatchedPath = DocumentHelper.createXPath("//csw:GetRecordsResponse/csw:SearchResults/@numberOfRecordsMatched");
			
			int numberOfRecordsMatched = ((Double)numberOfRecordsMatchedPath.numberValueOf(document)).intValue();
			
			XPath numberOfRecordsReturnedPath = DocumentHelper.createXPath("//csw:GetRecordsResponse/csw:SearchResults/@numberOfRecordsReturned");
			
			int numberOfRecordsReturned = ((Double)numberOfRecordsReturnedPath.numberValueOf(document)).intValue();
			
			logger.debug("NextRecord:" + nextrecordindex + "\nNumber of Records Matched :" + numberOfRecordsMatched + "\nNumber of Records Returned : " + numberOfRecordsReturned);
			
			respobj.setProduct_total_number(numberOfRecordsMatched);
			
			respobj.setStartposition(nextrecordindex-numberOfRecordsReturned);
			
			respobj.setRecordsperpage(numberOfRecordsReturned);
			
			XPath xpath = DocumentHelper.createXPath("//csw:GetRecordsResponse/csw:SearchResults/gmi:MI_Metadata"); //list all the records
			
			XPath identifierpath = DocumentHelper.createXPath("gmd:fileIdentifier/gco:CharacterString"); 
			
			XPath titlepath = DocumentHelper.createXPath("gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString");

			XPath hierarchylevel = DocumentHelper.createXPath("gmd:hierarchyLevel/gmd:MD_ScopeCode");
			
			XPath begintimepath = DocumentHelper.createXPath("gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod/gml:beginPosition");
			
			XPath endtimepath = DocumentHelper.createXPath("gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod/gml:endPosition");
			
			XPath westpath = DocumentHelper.createXPath("gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:westBoundLongitude/gco:Decimal");
			
			XPath eastpath = DocumentHelper.createXPath("gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:eastBoundLongitude/gco:Decimal");
			
			XPath northpath = DocumentHelper.createXPath("gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:northBoundLatitude/gco:Decimal");
			
			XPath southpath = DocumentHelper.createXPath("gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:southBoundLatitude/gco:Decimal");
			
			//for gmd
//			XPath accessoptions = DocumentHelper.createXPath("gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine");
			
			//for gmi
			XPath accessoptions = DocumentHelper.createXPath("gmd:identificationInfo/srv:SV_ServiceIdentification[contains(@id,'HTTP')]/srv:containsOperations/srv:SV_OperationMetadata/srv:connectPoint/gmd:CI_OnlineResource/gmd:linkage/gmd:URL");
			
			XPath accessoptions_opendap = DocumentHelper.createXPath("gmd:identificationInfo/srv:SV_ServiceIdentification[contains(@id,'OPeNDAP')]/srv:containsOperations/srv:SV_OperationMetadata/srv:connectPoint/gmd:CI_OnlineResource/gmd:linkage/gmd:URL");
			
			XPath accesslink = DocumentHelper.createXPath("gmd:CI_OnlineResource/gmd:linkage/gmd:URL");
			
			XPath accessinfo = DocumentHelper.createXPath("gmd:CI_OnlineResource/gmd:name/gco:CharacterString");

			XPath collection_url = DocumentHelper.createXPath("gmd:identificationInfo/gmd:MD_DataIdentification[@id = 'DataIdentification']/gmd:aggregationInfo[1]/gmd:MD_AggregateInformation/gmd:aggregateDataSetIdentifier/gmd:MD_Identifier/gmd:code/gco:CharacterString");
			
			xpath.setNamespaceURIs(map);
			
			List list = xpath.selectNodes(document);
			
			Iterator it = list.iterator();
			
			List products = new ArrayList();
			
			while(it.hasNext()){
				
				//each record
				
				Product p = new Product();
				
				Element ele = (Element)it.next();
				
				String identifier = identifierpath.selectSingleNode(ele).getText();

				p.setName(identifier);
				p.setDesc(identifier);
				
				//identifier must be escaped : and /

				identifier = identifier.replaceAll(":", "__y__");
				
				identifier = identifier.replaceAll("/", "__x__");
				
				p.setId(identifier);
				
				logger.debug("identifier : " + identifier);
				
				if(titlepath.selectSingleNode(ele)!=null){
					
					String title = titlepath.selectSingleNode(ele).getText();
					title = title.replaceAll("_", " ");
					p.setTitle(title);
				}
				
				if(begintimepath.selectSingleNode(ele)!=null){
				
					String begintime = begintimepath.selectSingleNode(ele).getText();
					
					p.setBegintime(begintime);
					
				}
				
				if(endtimepath.selectSingleNode(ele)!=null){
				
					String endtime = endtimepath.selectSingleNode(ele).getText();
					
					p.setEndtime(endtime);
					
				}
				
				String west = westpath.selectSingleNode(ele).getText();
				
				if(west!=null){
					
					p.setIsspatial("1");
					
					p.setWest(Double.valueOf(west));
					
					String east = eastpath.selectSingleNode(ele).getText();
					
					p.setEast(Double.valueOf(east));
					
					String north = northpath.selectSingleNode(ele).getText();
					
					p.setNorth(Double.valueOf(north));
					
					String south = southpath.selectSingleNode(ele).getText();
					
					p.setSouth(Double.valueOf(south));
					
				}
				
				//for gmd
				
//				List onlinenodes = accessoptions.selectNodes(ele);
//				
//				for(int i=0, len =onlinenodes.size() ; i<len; i++){
//					
//					String acsname = accessinfo.selectSingleNode(onlinenodes.get(i)).getText();
//					
//					if(acsname.toUpperCase().contains("HTTP")){
//						
//						String acslink =  accesslink.selectSingleNode(onlinenodes.get(i)).getText();
//						
//						logger.debug("Access Name : " + acsname + " - Access Link : " + acslink);
//						
//						p.setAccessurl(acslink);
//						
//						break;
//						
//					}
//					
//				}

				String level = hierarchylevel.selectSingleNode(ele).getText();
				if(level.contains("series")) {

					String curl = collection_url.selectSingleNode(ele).getText();
					p.setAccessurl(curl);
					p.setIscollection("1");

					String title = p.getTitle();
					p.setTitle(title + " [COLLECTION]");

				} else {

					p.setIscollection("0");

					//for gmi
					if (accessoptions.selectSingleNode(ele) == null) {
						logger.warn("There is no HTTP down link. We don't officially favor such records. Every time a CSW patrol find it, it will be deleted. Since the client already touches it, it will be returned with its OPeNDAP client link.");

						String accessurl = accessoptions_opendap.selectSingleNode(ele).getText() + ".html";
						p.setAccessurl(accessurl);

					} else {

						String accessurl = accessoptions.selectSingleNode(ele).getText();
						p.setAccessurl(accessurl);
					}
				}
				
				p.setIfvirtual("0");
				
				products.add(p);
				
			}
			
			respobj.setProducts(products);
			
		}

		return respobj;
		
	}
	
	public static SearchResponse searchBCube(SearchRequest req) {
		
		SearchResponse resp = new SearchResponse();
		
		return resp;
		
	}
	
	/**
	 * Merge two responses into one
	 * @param resp1
	 * @param resp2
	 * @return
	 */
	public static SearchResponse merge(SearchResponse resp1, SearchResponse resp2) {
		
		resp1.setProduct_total_number(resp1.getProduct_total_number()+resp2.getProduct_total_number());
		
		if(resp1.getProduct_total_number()!=0) {
			
			for(Product p : resp2.getProducts()) {
				
				if(resp1.getProducts().size()<resp1.getRecordsperpage()) {
					
					resp1.getProducts().add(p);
					
				}else {
					
					break;
					
				}
				
			}
			
		}else {
			
			resp1.setProducts(resp2.getProducts());
			
		}
		
		
		
		resp1.setRecordsFiltered(resp1.getProduct_total_number());
		
		resp1.setRecordsTotal(resp1.getProduct_total_number());
		
		
		return resp1;
	}
	/**
	 * Search two folders: the uploaded folder and the public folder. 
	 * The uploaded folder is the first one.
	 * @param req
	 * @return
	 */
	public static SearchResponse searchLocal(SearchRequest req) {
		
		List formats = Arrays.asList(req.getFormats().split("\\s* \\s*"));
		
		return LocalFileTool.search(req.searchtext, req.recordsperpage, req.pageno, formats);
		
	}
	
	public static SearchResponse searchCWIC(SearchRequest req){
		
		SearchResponse resp = new SearchResponse();
		
		String cswreq = SearchTool.constructCSWRequest(req);
		
		
		
		return resp;
		
		
	}
	
	public static SearchResponse searchRealData(SearchRequest req){
		
		SearchResponse resp = null;
		
//		VDP catalog (0), pycsw_unidata (1), Public&Uploaded files (2), BCube (3), CSISS Landsat Catalog (4). 
		
		if("1".equals(req.csw)){
			
			resp = SearchTool.searchUCARCSW(req);
			
		}else if("2".equals(req.csw)){
			
			resp = SearchTool.searchLocal(req);
			
		}else if("3".equals(req.csw)) {
			
			throw new RuntimeException("This catalog is not supported at present.");
			
		}else if("4".equals(req.csw)) {
			
			throw new RuntimeException("This catalog is not supported at present.");
			
		}
		
		return resp;
		
	}

	/**
	 * Search Entry
	 * @param req
	 * @return
	 */
	public static SearchResponse search(SearchRequest req){
		
		logger.debug("Request Name :" + req.name);
		
		logger.debug("Request Description :" + req.desc);
		
		logger.debug("Request Keywords :" + req.keywords);
		
		logger.debug("Is Virtual :  " + req.isvirtual); //0 : real; 1: virtual; 2: both
		
		logger.debug("Disable time: " + req.distime);
		
		SearchResponse resp = null;
		
		if(req.isvirtual.equals("1")){
			
			logger.debug("This is for VDP. Search in CyberConnector database..");
			
			resp = SearchTool.searchVDP(req);
			
		}else if(req.isvirtual.equals("0")){
			
			logger.debug("This is for real data. Search in PyCSW for Unidata..");
			
			resp = SearchTool.searchRealData(req);
			
		}
		
		//check the caching status
		if(resp!=null){
			
			for(int i=0, len=resp.getProducts().size(); i<len; i++){
				
				Product p = resp.getProducts().get(i);
				
				if(p.isCached()||p.getAccessurl().startsWith(SysDir.CACHE_DATA_URLPREFIX)){
					
					p.setCached(true);
					
				}else{
					
					p.setCached(false);
					
				}
				
				resp.getProducts().set(i, p);
				
			}
			
		}
		
		
		return resp;
		
	}
	
	public static void main(String[] args){
		
//		String testresponse = BaseTool.readStringFromFile(BaseTool.getClassPath() + "discovery.xml");
//		
//		SearchTool.parseCSWResponse(testresponse);
		
		boolean success = SearchTool.updatePyCSWDataURL("edu.ucar.unidata:grib/NCEP/NDFD/NWS/CONUS/CONDUIT/NDFD_NWS_CONUS_conduit_2p5km_20170613_1830.grib2", "http://cube.csiss.gmu.edu/cc_cache/NDFD_NWS_CONUS_conduit_2p5km_20170613_1700.grib2");
//		
//		System.out.println("Update: " + success);
		
		
		
	}
	
}
