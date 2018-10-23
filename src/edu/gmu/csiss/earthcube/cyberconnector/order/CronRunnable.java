package edu.gmu.csiss.earthcube.cyberconnector.order;

import org.apache.log4j.Logger;

import edu.gmu.csiss.earthcube.cyberconnector.tasks.Task;
import edu.gmu.csiss.earthcube.cyberconnector.tasks.TaskManager;
import edu.gmu.csiss.earthcube.cyberconnector.tools.PlaceOrderTool;
import edu.gmu.csiss.earthcube.cyberconnector.utils.BaseTool;

/**
*Class CronRunnable.java
*@author Ziheng Sun
*@time Mar 24, 2017 6:16:12 PM
*Original aim is to support CyberConnector.
*/
public class CronRunnable implements Runnable{
	
	Task t = null;
	
	String oid = null;
	
	int runtime = 0 ;
	
	Logger log = Logger.getLogger(this.getClass());
	
	BaseTool tool = new BaseTool();
	
	public CronRunnable(Task t, String oid){
		
		this.t = t;
		
		this.oid = oid;
		
	}
	
	@Override
	public void run() {
		
		log.info("Get ready to recur the running of a placed order..");
		
		log.info("This is the " + (runtime++) + "th running of order:" + oid);
		
		String termination = PlaceOrderTool.oid2termination.get(oid);
		
		if(BaseTool.isNull(termination)){
			
			throw new RuntimeException("The termination condition is empty. This order will very likely become endless dead loop. We refuse to accept such orders.");
			
		}else {
			
			TaskManager.addANewTask(this.t);
			
			String[] cons = termination.split(" ");
			
			int category = Integer.parseInt(cons[0]);
			
			if(category == 1){
				
				//stop on a specific time
				
				//this is taken care by the scheduler program in PlaceOrderTool
				
			}else if(category == 2){
				
				//stop after a number of times
				
				int times = Integer.parseInt(cons[1]);
				
				if(runtime==times){
					
					//kill the scheduler of this order. The order will not be run any more.
					
					PlaceOrderTool.killARecurringOrder(oid);
					
				}
				
			}else if(category == 3){
				
				//never stop. This option is very risky. The maximum times we support right now is 1000. More options will be evaluated further.
				
				if(runtime==1000){
					
					//kill the scheduler of this order. The order will not be run any more.
					
					PlaceOrderTool.killARecurringOrder(oid);
					
				}
				
			}
			
		}
		
		
		
		log.info("A new order has been added to the waiting list. This order is a recuring order.");
		
	}
	
}
