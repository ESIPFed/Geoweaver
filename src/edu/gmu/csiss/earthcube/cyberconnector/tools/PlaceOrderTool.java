package edu.gmu.csiss.earthcube.cyberconnector.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.gmu.csiss.earthcube.cyberconnector.order.CronRunnable;
import edu.gmu.csiss.earthcube.cyberconnector.order.KillCronRunnable;
import edu.gmu.csiss.earthcube.cyberconnector.tasks.TaskManager;
import edu.gmu.csiss.earthcube.cyberconnector.tasks.VirtualProductGenerationTask;
import edu.gmu.csiss.earthcube.cyberconnector.utils.BaseTool;
import edu.gmu.csiss.earthcube.cyberconnector.utils.RandomString;
import it.sauronsoftware.cron4j.Scheduler;

/**
 *Class PlaceOrderTool.java
 *@author ziheng
 *@time Aug 10, 2015 12:10:54 PM
 *Original aim is to support CyberConnector.
 */
public class PlaceOrderTool {
	
	
	private String preOutput, preOrderId;
	
	public static Scheduler scheduler = new Scheduler(); 
	
	public static Map<String, String> oid2scheduler = new HashMap(); //record the relationship between order and its taskid.
	
	public static Map<String, String> oid2killscheduler = new HashMap(); //record the relationship between order and its stop schedular.
	
	public static Map<String, String> oid2termination = new HashMap(); //record the termination condition of each order
	
	public static List toBeDeletedScheduler = new ArrayList();
	
	private Logger log = Logger.getLogger(this.getClass());
	
	static{
		
		scheduler.start();
		
	}
	
	/**
	 * Place an order
	 * @param category
	 * @param proj
	 * @param east
	 * @param south
	 * @param west
	 * @param north
	 * @param begintime
	 * @param endtime
	 * @param mail
	 * @return
	 * An order number
	 */
	public String placeOrder(String category, String proj, String east, String south, String west, String north, String begintime, String endtime, String mail){
		
		//generate a new order number
		
		String orderid = new RandomString(18).nextString();
		
		//set the order to the task waiting list
		
		VirtualProductGenerationTask vt = new VirtualProductGenerationTask(orderid, category, proj, east, south, west, north,  begintime, endtime, mail);
		
		TaskManager.addANewTask(vt);
		
		return orderid;
	
	}
	/**
	 * 
	 * add by Ziheng Sun on 10/16/2015
	 * @param parmatermap
	 * @return
	 */
	public String placeOrder(Map<String, String> parmatermap){
		
		String orderid = null;
		
		//precheck if the same order is already executed
		
		if(preCheck(parmatermap)){
			
			//generate a new order number
			
			orderid = new RandomString(18).nextString();
			
			//add for cron job - Z.S. on 3/24/2017
			
			String cron = parmatermap.get("cron");
			
			String termination = parmatermap.get("termination");
			
			oid2termination.put(orderid, termination);
			
			VirtualProductGenerationTask vt = new VirtualProductGenerationTask(orderid, parmatermap);
    		
    		if(BaseTool.isNull(cron)||BaseTool.isNull(cron.trim())){
    			
    			//one time order
    			
    			TaskManager.addANewTask(vt);
    			
    			log.info("The order has been assigned to task manager");
    			
    		}else{
    			
    			//cron order
    			
    			//check if the scheduler stack is full. If yes, refuse to accept this order. If no, go ahead. 
    			
    			//the limit of schedulers is 200 for now. (200 threads are a lot)
    			
    			if(oid2scheduler.size()>=200){
    				
    				throw new RuntimeException("The scheduler stack is full. Cannot add new order at present. Wait or stop some zombie orders.");
    				
    			}else{
    				
        			// Schedule a once-a-minute task.
        			
        			String taskid = scheduler.schedule(cron, new CronRunnable(vt, orderid));
        			
        			oid2scheduler.put(orderid, taskid);
        			
        			//create another scheduler if the order is supposed to stop on a specific time
        			
        			if(termination.startsWith("1")){
        				
        				//1 2017-05-18 11:08:52
        				
        				String[] ts = termination.split(" ");
        				
        				String[] hms = ts[2].split(":");
        				
        				String[] ymd = ts[1].split("-");
        				
        				StringBuffer killcron = new StringBuffer();
        				
        				killcron.append(hms[1]).append(" ").append(hms[0]).append(" ").append(ymd[2]).append(" ").append(ymd[1]).append(" ?");
        				
            			// Schedule a once-a-minute task.
            			
            			String ktid = scheduler.schedule(cron, new KillCronRunnable(orderid));
            			
            			oid2killscheduler.put(orderid, ktid);
        				
        			}
        			
        			log.info("The scheduler of the order is started.");
    				
    			}
    			
    		}
			
		}else{
			
			//the order already exists, return the old order id to user
			
			orderid = preOrderId;
			
			log.info("The order already exists. The old order id is returned. No placement is made");
			
		}
		
		return orderid;
	}
	/**
	 * Kill a recurring order
	 * @param oid
	 */
	public static void killARecurringOrder(String oid){
		
		System.out.println("Stop recurring order scheduler of order " + oid);
		
		String tid = oid2scheduler.get(oid);
		
		if(!BaseTool.isNull(tid)){
			
			scheduler.deschedule(tid);
			
			oid2scheduler.remove(oid);
			
			System.out.println("Test why this stop is useless.");
			
		}
		
	}
	
	public static void deleteSchedule(String oid){
		
		killARecurringOrder(oid);
		
		stopAKillScheduler(oid);
		
	}
	
	/**
	 * 
	 * @param oid
	 */
	public static void stopAKillScheduler(String oid){
		
		System.out.println("Stop killerscheduler of order " + oid);
		
		String tid = oid2killscheduler.get(oid);
		
		if(!BaseTool.isNull(tid)){
			
			scheduler.deschedule(tid);
			
			oid2killscheduler.remove(oid);
			
		}
		
	}
	
	
	
	/**
	 * Precheck if the order is duplicated and the output is already available
	 * add by Ziheng Sun on 11/19/2015
	 * @return
	 * true: valid for new order
	 * false: output already exists
	 */
	public boolean preCheck(Map<String, String> parmatermap){
		//to be finished
		
		return true;
	}
	
	public static void main(String[] args){
		//test the multiple thread mechanism
		PlaceOrderTool tool = new PlaceOrderTool();
		for(int i=0;i<10;i++){
			String orderid = tool.placeOrder(null, null, null, null, null, null, null, null, null);
			System.out.println("|||||||||||||||||||||||||||||||\nOrder "+i +" id is: " + orderid+" is placed.");
		}
	}
}
