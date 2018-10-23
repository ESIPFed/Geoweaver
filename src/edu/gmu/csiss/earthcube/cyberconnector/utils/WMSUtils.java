package edu.gmu.csiss.earthcube.cyberconnector.utils;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;
//import org.geotools.data.DataStore;
//import org.geotools.data.DataStoreFinder;
//import org.geotools.data.FeatureSource;
//import org.geotools.geometry.jts.ReferencedEnvelope;
//import org.geotools.referencing.ReferencingF±±actoryFinder;
//import org.opengis.feature.simple.SimpleFeature;
//import org.opengis.feature.simple.SimpleFeatureType;
//import org.opengis.geometry.BoundingBox;
//import org.opengis.referencing.crs.CoordinateReferenceSystem;

import net.opengis.wms.v_1_3_0.Layer;
import net.opengis.wms.v_1_3_0.OperationType;
import net.opengis.wms.v_1_3_0.WMSCapabilities;
/**
 * updated by Li Lin on 3/28/2016
 * 
 * updated by Ziheng Sun on 4/11/2016 - make the getting and parsing wms capability response work
 * 
 * Updated by Lei Hu on 4/18/2016 return the Layer to jsp
 * 
 * @author Administrator
 */
public class WMSUtils {
	
	private static Logger logger = Logger.getLogger(WMSUtils.class);
	
	public static void main(String[] args) {
		WMSUtils.parseWMS("http://ogc.bgs.ac.uk/cgi-bin/exemplars/BGS_Bedrock_and_Superficial_Geology/ows?service=WMS&request=GetCapabilities&version=1.3.0");
		//WMSUtils.parseWMS("http://tb12.cubewerx.com/a041/cubeserv?","service=WMS&request=GetCapabilities&version=1.3.0");
		//WMSUtils.parseWMS("http://sampleserver1.arcgisonline.com/ArcGIS/services/Specialty/ESRI_StatesCitiesRivers_USA/MapServer/WMSServer?", "service=WMS&request=GetCapabilities&version=1.3.0");
	}	
	
	/**
	 * Parse WMS capabilities document to Layer list
	 * created by Lei Hu on 4/30/2016
	 * @return
	 */
	public static List<Layer> parseWMS(String url){
//		String wmsurl = "http://ws.csiss.gmu.edu:8080/geoserver/wms";
//		String wmsurl = "https://nassgeodata.gmu.edu/cgi-bin/wms_cdl_ia.cgi?";
	//	BBOX box = new BBOX("EPSG:4326", -77.527282, 38.934311, -76.887893, 39.353648);	
	//	BBOX box = new BBOX("EPSG:32618", 280940.92757638777, 4312524.259656975, 337335.10634472733, 4357722.154561454);
//		BBOX box = new BBOX("EPSG:4326", -96,40,-90,43);
		List<Layer> layers = null;
		try {		
//			Vector<GeoFeature> gfVec2 = getWMSFeatures(wmsurl, box);
//			logger.info("--------------------------------");
//			logger.info("gfVec2.size()=" + gfVec2.size());
//			for(GeoFeature gf:gfVec2) {
//				logger.info("Name=" + gf.getName());
//			}
			
			WMSCapabilities ca = WMSUtils.parseWMSCapabilityURL(url);
			
			logger.info("Layers : " + ca.getCapability().getLayer().getLayer().size());
			logger.info("WMS Name : " + ca.getCapability().getLayer().getName());
			logger.info("WMS Title: " + ca.getCapability().getLayer().getTitle());
			
			logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\nWMS Layer List:");
			layers = ca.getCapability().getLayer().getLayer();
			
			for(int i=0;i<layers.size();i++){
				
				Layer l = layers.get(i);
				
				logger.info("Layer No : " + i);
				
				logger.info("Layer Title : " + l.getTitle());
				
				logger.info(l.getBoundingBox().get(0).getCRS());
				logger.info(l.getBoundingBox().get(0).getMaxy());
				logger.info(l.getAttribution().getLogoURL().getFormat());
				logger.info("height="+l.getAttribution().getLogoURL().getHeight());
				logger.info("width="+l.getAttribution().getLogoURL().getWidth());
				
				if(l.getIdentifier().size()!=0){
					logger.info("Layer Identifier : " + l.getIdentifier().get(0));
				}
				
				logger.info("==================");
			}
			
			
			//send request to WMS 
//			String resp = MyHttpUtils.doPost("URL", getmaprequest);
			//parse resp
//			JAXBElement<GetMap> respele = WMSUtils.parseWMSGetCapabilitiesResponse(resp);
			
//			String WMSURL = respele.getDataURL();
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		return layers;
		
	}
	
//	private static Employee jaxbXMLToObject() {
//        try {
//            JAXBContext context = JAXBContext.newInstance(Employee.class);
//            Unmarshaller un = context.createUnmarshaller();
//            Employee emp = (Employee) un.unmarshal(new File(FILE_NAME));
//            return emp;
//        } catch (JAXBException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
	/**
	 * Parse WMS capabilities document
	 * created by Lei Hu on 4/30/2016
	 * @param capability_doc_url
	 * @return
	 */
	public static WMSCapabilities parseWMSCapabilityURL(String capability_doc_url){
				
		String getCapabilitiesResponse = null;
		WMSCapabilities ca =  null;
		//added by Lei Hu 8/12/2016
		try {
			if(!capability_doc_url.contains("version")){			
	    		capability_doc_url = capability_doc_url + "&version=1.3.0";
	    	}else if(!capability_doc_url.contains("1.3.0")){
	    		capability_doc_url = capability_doc_url.replace("1.3.2","1.3.0");
	    	}
			
			getCapabilitiesResponse = MyHttpUtils.doGet(capability_doc_url);
			logger.info(getCapabilitiesResponse);
			JAXBContext jaxbContext = null;
			ca = JAXB.unmarshal(new StringReader(getCapabilitiesResponse), WMSCapabilities.class);
		    //logger.info(ca.getCapability().getRequest().getGetFeatureInfo().getDCPType().get(0).getHTTP().getGet().getOnlineResource().getHref());
		    //Added by Lei Hu
			//for DGIWG wms
			logger.info(ca.getCapability().getRequest().getGetCapabilities().getDCPType().get(0).getHTTP().getGet().getOnlineResource().getHref());
			logger.info(ca.getCapability().getRequest().getGetMap().getDCPType().get(0).getHTTP().getGet().getOnlineResource().getHref());
		
		    
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return ca;
	}
	
	public static String getWMSGetCapabilitiesResponse(String prefix, String content) throws Exception{
		String url = prefix + content;
		return MyHttpUtils.doGet(url);
	}
/**
 * Added by Lei Hu 
 * Return the WMS Layer List
*/
	public static List<Layer> getWMSLayers(String wmsurl) throws Exception {

		wmsurl = wmsurl.trim();
		String wmsstr = wmsurl;
		String wmscont;
		
    	if(wmsurl.endsWith("?"))
    		wmscont = "service=WMS&version=1.3.0&request=GetCapabilities";
    	else
    		wmscont = "?service=WMS&version=1.3.0&request=GetCapabilities";
    	return WMSUtils.parseWMS(wmsstr);
	}

	/**
	 * Get Layer List of WMS
	 * @return
	 */
	public static List<Layer> getLayerList(WMSCapabilities wmsc ){
		//List<Layer> ll = wmsc.getCapability().getLayer().getLayer();
		//updated by Lei Hu 8/13/2016
		//For the DGIWG WMS
		List<Layer> l = wmsc.getCapability().getLayer().getLayer().get(0).getLayer();
		for(int i=0;i<l.size();i++){
			Layer ll = l.get(i);
			if(ll.getName() != null){
				logger.info(ll.getName());
				logger.info(ll.getTitle());
			}else{
				for(int j=0;j<ll.getLayer().size();j++){
					Layer lll = ll.getLayer().get(j);
					logger.info(lll.getName());
					logger.info(lll.getTitle());
				}
			}
		}
		return l;
	}

	//updated by Lei Hu 8/16/2016
	public static List<Layer> getLayerL(WMSCapabilities wmsc ){
		List<Layer> l = wmsc.getCapability().getLayer().getLayer();
		//List<Layer> l = wmsc.getCapability().getLayer().getLayer().get(0).getLayer();
		for(int i=0;i<l.size();i++){
			Layer ll = l.get(i);
			if(ll.getName() != null){
				System.out.println(ll.getName());
				System.out.println(ll.getTitle());
			}else{
				for(int j=0;j<ll.getLayer().size();j++){
					Layer lll = ll.getLayer().get(j);
					System.out.println(lll.getName());
					System.out.println(lll.getTitle());
				}
			}
		}
		return l;
	}
	
	public static String convertJAXBElementToXML(JAXBElement ele){
		StringWriter writer = new StringWriter();
		
		Marshaller m;
		try {
			JAXBContext context = JAXBContext.newInstance(OperationType.class);            
			m = context.createMarshaller();

			m.marshal(ele, writer);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// output string to console
		String theXML = writer.toString();
		logger.info(theXML);
		return theXML;
	}
	
	public static WMSCapabilities parseWMSCapabilityResponse(String xml) throws JAXBException{
		
		JAXBContext jaxbContext;
		WMSCapabilities ca = null;
		ca = JAXB.unmarshal(new StringReader(xml), WMSCapabilities.class);
		System.out.print(ca);
//		ObjectFactory of = new ObjectFactory();
////		WMSCapabilities wc = of.createWMSCapabilities();
////		wc.getCapability();
//		OperationType ot = of.createOperationType();
//		JAXBElement<OperationType> getcapabilityreq = of.createGetCapabilities(ot);
//		//turn jaxbelement to xml or json
//		logger.info("GetCapability Request: " + WMSUtils.convertJAXBElementToXML(getcapabilityreq));
		//return xml or json back
		return ca;
		
	}
	
	
//	public static Vector<GeoFeature> getWMSFeatures(String wmsurl, BBOX box) throws Exception {
//
//		Vector<GeoFeature> featureVec = new Vector<GeoFeature>(); 
//		wmsurl = wmsurl.trim();
//		String wmsstr = wmsurl;
//    	if(wmsurl.endsWith("?"))
//    		wmsurl += "service=WMS&version=1.3.0&request=GetCapabilities";
//    	else
//    		wmsurl += "?service=WMS&version=1.3.0&request=GetCapabilities";
//		
//		Map connectionParameters = new HashMap();
//		connectionParameters.put("WMSDataStoreFactory:GET_CAPABILITIES_URL", wmsurl);
//		
//		DataStore dstore = DataStoreFinder.getDataStore(connectionParameters);	
//		//logger.info("dstore=" + dstore);
//		String typeNames[] = dstore.getTypeNames();		
//		if(typeNames == null)
//			return null;
//		
//		String reqcrs[] = box.crs.split(":");
//		CoordinateReferenceSystem reqepsg = ReferencingFactoryFinder.getCRSAuthorityFactory(reqcrs[0], null).createCoordinateReferenceSystem(reqcrs[1]);
//		BoundingBox reqbound;
//		if(box.crs.equalsIgnoreCase("EPSG:4326"))
//			reqbound = new ReferencedEnvelope(box.miny, box.maxy, box.minx, box.maxx, reqepsg);
//		else
//			reqbound = new ReferencedEnvelope(box.minx, box.maxx, box.miny, box.maxy, reqepsg);
//		
//		logger.info("out bbox is " +box.toString());
//		logger.info("Required bbox is " +reqbound);
//		
//		logger.info("Interscted WMS Layers are listed as");
//		for(String tname: typeNames) {			
//			FeatureSource<SimpleFeatureType, SimpleFeature> fsource = dstore.getFeatureSource(tname);
//			ReferencedEnvelope databounds = fsource.getBounds();					
//			BoundingBox datatarnsbounds = databounds.toBounds(reqepsg);												
//			if(datatarnsbounds != null) {	
//				logger.info( "src Bounds:"+ databounds);
//				logger.info( "src Trans Bounds:"+ datatarnsbounds);
//				logger.info( "req Bounds:"+ reqbound);
//				if(!datatarnsbounds.intersects(reqbound)) {
//				/*	logger.info("Not intersected");*/
//					continue;
//				}
//				GeoFeature feature = new GeoFeature();			
//				feature.setName(tname);
//				feature.setTitle(tname);
//				feature.setDescription(tname);
//				feature.setWmsUrl(wmsstr);
//				BBOX fbox;
//				if(box.crs.equalsIgnoreCase("EPSG:4326"))
//					fbox = new BBOX(box.crs, datatarnsbounds.getMinY(), datatarnsbounds.getMinX(), datatarnsbounds.getMaxY(), datatarnsbounds.getMaxX());
//				else
//					fbox = new BBOX(box.crs, datatarnsbounds.getMinX(), datatarnsbounds.getMinY(), datatarnsbounds.getMaxX(), datatarnsbounds.getMaxY());
//				logger.info(tname + "--" +fbox.toString());
//				feature.addBBOX(fbox);			
//				featureVec.add(feature);
//			}
//		}		
//		if(featureVec.size() > 0)
//			return featureVec;
//		else
//			return null;
//	}
}
