/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.gw.utils;

/**
 * Class SOAPClient
 * @author ziheng
 */
public class SOAPClient {
	
	
	public static void main(String[] args) {
		
		System.out.println("Test 123456");
		
		
		
	}
 
//	static Logger  logger = Logger.getLogger(SOAPClient.class);
//	
//    private String soapmessage;
//    private String endpoint;
//    private String respmessage;
//
//    public String getSoapmessage() {
//        return soapmessage;
//    }
//
//    public void setSoapmessage(String soapmessage) {
//        this.soapmessage = soapmessage;
//    }
//
//    public String getEndpoint() {
//        return endpoint;
//    }
//
//    public void setEndpoint(String endpoint) {
//        this.endpoint = endpoint;
//    }
//
//    public String getRespmessage() {
//        return respmessage;
//    }
//
//    public void setRespmessage(String respmessage) {
//        this.respmessage = respmessage;
//    }
// 
//    
//    /*private void send1() throws SOAPException {
//        SOAPMessage message = MessageFactory.newInstance().createMessage();
//        SOAPHeader header = message.getSOAPHeader();
//        header.detachNode();
// 
//        SOAPEnvelope envelope = message.getSOAPPart().getEnvelope();
//        envelope.setAttribute("namespace","namespaceUrl");
// 
//        SOAPBody body = message.getSOAPBody();
//        QName bodyName = new QName("getResponse");
//        SOAPBodyElement bodyElement = body.addBodyElement(bodyName);
//        SOAPElement symbol = bodyElement.addChildElement("name");
//        symbol.addTextNode("Harry Joy");
// 
//        SOAPConnection connection = SOAPConnectionFactory.newInstance().createConnection();
//        SOAPMessage response = connection.call(message, endpoint);
//        connection.close();
// 
//        SOAPBody responseBody = response.getSOAPBody();
//        SOAPBodyElement responseElement = (SOAPBodyElement)responseBody.getChildElements().next();
//        SOAPElement returnElement = (SOAPElement)responseElement.getChildElements().next();
//        if(responseBody.getFault()!=null){
//            logger.debug(returnElement.getValue()+" "+responseBody.getFault().getFaultString());
//        } else {
//            logger.debug(returnElement.getValue());
//        }
// 
//        try {
//            logger.debug(getXmlFromSOAPMessage(message));
//            logger.debug(getXmlFromSOAPMessage(response));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }*/
//    
//    /**
//     * send message to SOAP service
//     * @throws SOAPException 
//     */
//    public void send() throws SOAPException {
//        InputStream in = null;
//        try {
//            String soap = getSoapmessage();
////            BaseTool util = new BaseTool();
//            in = util.convertString2InputStream(soap);
//            
//            MessageFactory mf = MessageFactory.newInstance();
//            MimeHeaders headers = new MimeHeaders();
//            headers.addHeader("Content-Type", "text/xml");
//            headers.addHeader("SOAPAction", "");
//            SOAPMessage message = mf.createMessage(headers, in);
//            SOAPConnection connection = SOAPConnectionFactory.newInstance().createConnection();
//            SOAPMessage response = connection.call(message, getEndpoint());
//            connection.close();
//            String resp = getXmlFromSOAPMessage(response);
//            logger.debug(resp);
//            setRespmessage(resp);
//        } catch (IOException ex) {
//            String errmsg = ex.getClass().getName()+ ex.getLocalizedMessage();
//            logger.error(errmsg);
//            throw new RuntimeException(errmsg);
//        } catch (Exception ex){
//            String errmsg = ex.getClass().getName()+ ex.getLocalizedMessage();
//            logger.error(errmsg);
//            throw new RuntimeException(errmsg);
//        }finally {
//            try {
//                in.close();
//            } catch (IOException ex) {
//                logger.error(ex.getLocalizedMessage());
//            }
//        }
//    }
//    
//    public static void main(String[] args){
//        try {
//            SOAPClient client = new SOAPClient();
//            //client.send1();
//            client.setEndpoint("http://129.174.131.8:9006/GeoprocessingWS/services/Raster_ImageSegment");
//            client.setSoapmessage("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ras=\"http://Raster_ImageSegment.grass.ws.laits.gmu.edu\">\n" +
//"   <soapenv:Header/>\n" +
//"   <soapenv:Body>\n" +
//"      <ras:segmentElement>\n" +
//"         <ras:imgURL>http://www3.csiss.gmu.edu/data/cfpp_clipped.tif</ras:imgURL>\n" +
//"         <ras:sigmaS>6.5</ras:sigmaS>\n" +
//"         <ras:sigmaR>7</ras:sigmaR>\n" +
//"         <ras:minRegion>20</ras:minRegion>\n" +
//"         <ras:speedUpLevel>medium</ras:speedUpLevel>\n" +
//"         <ras:returnFormat>BMP</ras:returnFormat>\n" +
//"      </ras:segmentElement>\n" +
//"   </soapenv:Body>\n" +
//"</soapenv:Envelope>");
//            client.send();
//            logger.debug("The response SOAP message is:\n"+client.getRespmessage());
//        } catch (SOAPException ex) {
//           	ex.printStackTrace();
//        }
// 
//    }
// 
//    private String getXmlFromSOAPMessage(SOAPMessage msg) throws SOAPException, IOException {
//        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
//        msg.writeTo(byteArrayOS);
//        return new String(byteArrayOS.toByteArray());
//    }
 
}