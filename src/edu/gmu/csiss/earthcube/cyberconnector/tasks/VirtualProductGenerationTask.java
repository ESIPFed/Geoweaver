package edu.gmu.csiss.earthcube.cyberconnector.tasks;

import java.util.Map;

import org.apache.log4j.Logger;

import edu.gmu.csiss.earthcube.cyberconnector.database.DataBaseBroker;
import edu.gmu.csiss.earthcube.cyberconnector.utils.BaseTool;
import edu.gmu.csiss.earthcube.cyberconnector.utils.SysDir;

/**
 *Class VirtualProductGenerationTask.java
 *@author Ziheng Sun
 *@time Aug 11, 2015 11:17:21 AM
 *Original aim is to support CyberConnector.
 */
public class VirtualProductGenerationTask extends Task {

	String oid, product, productid, proj, east, south, west, north, begintime, endtime, mail;

	BaseTool tool = new BaseTool();
	Logger logger = Logger.getLogger(this.getClass());
	/**
	 * Construction function
	 * @param oid
	 * @param product
	 * @param proj
	 * @param east
	 * @param south
	 * @param west
	 * @param north
	 * @param begintime
	 * @param endtime
	 * @param mail
	 */
	public VirtualProductGenerationTask(String oid, String product, String proj, String east, String south, String west, String north, String begintime, String endtime, String mail){
		initialize(oid, product, proj, east, south, west, north, begintime, endtime, mail);
	}
	
	public VirtualProductGenerationTask(String oid, Map<String, String> parametermap){
		initialize(oid, parametermap);
	}
	
	@Override
	public void initialize() {
		setChanged();
		notifyObservers(this);
	}
	
	
	/**
	 * 
	 * @param oid
	 * @param parametermap
	 */
	public void initialize(String oid, Map<String, String> parametermap){
		this.oid = oid;
		this.productid = parametermap.get("productid");
		this.mail = parametermap.get("email");
		this.product = DataBaseBroker.getProductNameById(productid);
		DataBaseBroker.addNewOrder(oid, product, parametermap);
		tool.sendUserAOrderNotice(mail, oid);
	}
	/**
	 * Initialize the task
	 * @param oid
	 * @param product
	 * @param proj
	 * @param east
	 * @param south
	 * @param west
	 * @param north
	 * @param begintime
	 * @param endtime
	 * @param mail
	 */
	public void initialize(String oid, String product, String proj, String east, String south, String west, String north, String begintime, String endtime, String mail){
		this.oid = oid;
		this.product = product;
		this.proj = proj;
		this.east = east;
		this.south = south;
		this.west = west;
		this.north = north;
		this.begintime = begintime;
		this.endtime = endtime;
		this.mail = mail;
		//save the order into database
		DataBaseBroker.addNewOrder(oid, product, east, south, west, north, proj, begintime, endtime, mail);
		tool.sendUserAOrderNotice(mail, oid);
	}
	
	@Override
	public void execute() {
		try{
			//set the status of an order as running
			logger.info(">>> Update the status of the order to Running");
			DataBaseBroker.updateAnOrderStatus(oid, "Running", "The order starts to be processed.");
			//fetch the abstract model of the specified virtual product
			logger.info(">>> Fetch abstract model of the virutal data product");
			String abstractmodelid = DataBaseBroker.queryAbstractModelIdByProductName(product);
			//comment by Ziheng - 8/27/2015
			//ParameterConnections XML is useless since LPM 2.0
			String[] abstractmodelxml = DataBaseBroker.queryAbstracModelXMLById(abstractmodelid); //0 is process connection; 1 is parameter connection.
			//generate messagetype by logicprocess
			logger.info(">>> Generating messagetype by logicprocess");
			String req = abstractmodelxml[0];
			String resp = BaseTool.POST(req, SysDir.instantiationservletaddress0);
			String mt = resp;
			//generate a workflow by the virtual product's abstract model and message type
			logger.info(">>> Instantiating logicprocess and messagetype into a BPEL workflow");
			req = "$INSTANTIATIONLPM2$"+abstractmodelxml[0]+"$LPMT$"+mt.trim();
			
	        resp = BaseTool.POST(req, SysDir.instantiationservletaddress);
	        if(resp.indexOf("Sorry, exception happens.")!=-1){
	        	throw new RuntimeException("Fail to instantiate the abstract model into an executable workflow."+resp);
	        }
	        logger.info("The response of the initiation servlet: "+ resp);
	        //need a parameter map containing spatial and temporal extent as input values
	        
			//execute the workflow with the spatial extent and temperal extent as the inputs
//			req = "$EXECUTEPRODUCTGENENRATIONWORKFLOW$"+resp+"$P$"+east+","+south+","+west+","+north+"$ST$"+begintime+"$BE$"+endtime;
	        req = "$EXECUTEPRODUCTGENENRATIONWORKFLOW$" + resp + "$P$" + DataBaseBroker.getParametermap(oid).trim();
	        //updated by Ziheng Sun on 10/28/2015
	        //get the request id
	        String requestid = BaseTool.POST(req, SysDir.executionservletaddress);
	        String producturl = null;
	        long begintime = System.currentTimeMillis();

        	int sleeptime = 1000; //initialized sleep time is 1 second.
        	int loopnumber = 0;
	        while(true){
	        	loopnumber++;
	        	sleeptime = 1000*(int)(Math.pow(loopnumber, 2)/2); //the time algorithm: y = pow(x, 2)/2;
	        	logger.info("Current sleep time is" +  sleeptime);
	        	Thread.sleep(sleeptime);
	        	logger.info("This is number " + loopnumber + " status checking.");
	        	req = "$CHECKSTATUSOFWORKFLOW$"+requestid.trim();
	        	String ret = BaseTool.POST(req, SysDir.executionservletaddress).trim();
	        	if(ret.startsWith("SUCCESS")){
	        		producturl = ret.substring("SUCCESS.".length()).trim();
	        		logger.info("The loop is over with success response.");
	        		break;
	        	}else if(ret.startsWith("ERR.")||ret.startsWith("UNKNOWN TASK")){
	        		throw new RuntimeException(ret);
	        	}
	        	long currenttime = System.currentTimeMillis();
	        	logger.info("The overall lasting time: " + (currenttime-begintime));
	        	if((currenttime-begintime)>36*60*60*1000){
	        		logger.warn("The while loop lasts too long (>36 hours). Self terminated.");
	        		break;
	        	}
	        }
	        if(BaseTool.isNull(producturl)){
	        	throw new RuntimeException("The result url is null.");
	        }
//	        else if(!producturl.trim().toLowerCase().startsWith("http")){
//	        	throw new RuntimeException("The result url is incorrect." + producturl);
//	        }
//			String producturl = BaseTool.Longtime_POST(req, SysDir.executionservletaddress);
	        //add by Ziheng Sun on 7/3/2016
	        DataBaseBroker.addNewDataSet(oid, product+" instance product",  producturl);
	        
	        //send the results to the user provided E-mail
//	        tool.notifyUserByEmail(mail, producturl);
			tool.sendUserAResultMail(oid, mail, producturl);
		}catch(Exception e){
//			tool.notifyUserByEmail(mail, "Failed. "+e.getLocalizedMessage());
			e.printStackTrace();
			tool.sendUserAErrorMail(oid, mail, "Failed. "+e.getLocalizedMessage());
			DataBaseBroker.updateAnOrderStatus(oid, "Failed", e.getLocalizedMessage());
			throw new RuntimeException(e.getLocalizedMessage());
		}
	}

	@Override
	public void responseCallback() {
		//set the status of the order into the database
		DataBaseBroker.updateAnOrderStatus(oid, "Done", "The order is finished.");
		//notify the task list observer
		setChanged();
		notifyObservers(this);
	}

	@Override
	public void failureCallback(Exception e) {
		//set the status of the order into the database
		DataBaseBroker.updateAnOrderStatus(oid, "Failed", e.getLocalizedMessage());
		//notify the task list observer
		setChanged();
		notifyObservers(this);
	}

}
