package edu.gmu.csiss.earthcube.cyberconnector.order;

import org.apache.log4j.Logger;

import edu.gmu.csiss.earthcube.cyberconnector.tools.PlaceOrderTool;

/**
*Class KillCronRunnable.java
*@author Ziheng Sun
*@time Mar 27, 2017 11:54:26 AM
*Original aim is to support CyberConnector.
*/
public class KillCronRunnable implements Runnable{

	String oid;
	
	Logger log = Logger.getLogger(this.getClass());
	
	public KillCronRunnable(String oid){
		
		this.oid = oid;
		
	}
	
	
	@Override
	public void run() {
		
		log.info("Begin to kill the recurring order : " + oid);
		
		PlaceOrderTool.killARecurringOrder(oid);
		
		log.info("The order scheduler has been stopped. Now stop this killtask itself.");
		
		//kill itself. This is supposed to be run only once.
		
		PlaceOrderTool.stopAKillScheduler(oid);
		
		log.info("The scheduler suicides successfully.");
	}

	
	
}
