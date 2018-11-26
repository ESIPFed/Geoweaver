package edu.gmu.csiss.earthcube.cyberconnector.workers;

import java.util.ArrayList;
import java.util.List;


import edu.gmu.csiss.earthcube.cyberconnector.tasks.Task;
import edu.gmu.csiss.earthcube.cyberconnector.tasks.TaskManager;
import edu.gmu.csiss.earthcube.cyberconnector.utils.SysDir;

/**
 *Class WorkerManager.java
 *@author ziheng
 *@time Aug 10, 2015 4:04:25 PM
 *Original aim is to support CyberConnector.
 */
public class WorkerManager {
	
	private static List<Worker> workerlist;
	
	static{
		
		WorkerManager.init();
		
	}
	
	private static void init() {
		
		//startup a list of workers
		workerlist = new ArrayList();
		
		for(int i=0;i<SysDir.worknumber;i++) {
			
			System.out.println("worker manager created a worker ");
			
			Worker w = new Worker();
			
			w.start();
			
			workerlist.add(w);
			
		}
		
	}
	
	public static Worker getNextAvailableWorker() {
		
		Worker w = null;
		
		for(int i=0;i<SysDir.worknumber;i++) {
			
			if(!((Worker)workerlist.get(i)).isStatus()) {
				
				w = workerlist.get(i);
				
				break;
				
			}
			
		}
		
		return w;
		
	}
	
	/**
	 * Get the number of current working workers
	 * @return
	 */
	public static int getCurrentWorkerNumber(){
		
		int num = 0;
		
		for(int i=0;i<SysDir.worknumber;i++) {
			
			if(((Worker)workerlist.get(i)).isStatus()) {
				
				num++;
				
			}
			
		}
		
		return num;
		
	}
	
	/**
	 * only used for maven test
	 * @throws InterruptedException 
	 */
	public static void waitJoin() throws InterruptedException {
		
		for(int i=0;i<SysDir.worknumber;i++) {
			
			workerlist.get(i).join();
			
		}
		
	}
	
	public static Worker createANewWorker(Task t){
		//add the task to a empty worker
		Worker w = getNextAvailableWorker();
		w.setTask(t);
//		w.start();
//		workerlist.add(w);
		return w;
	}

	/**
	 * Get the number of available workers
	 * @return
	 * A number
	 */
	public static int getNumberOfAvailableWorkers(){
		
		int num = 0;
		
		for(int i=0;i<SysDir.worknumber;i++) {
			
			if(!workerlist.get(i).isStatus()) {
				
				num++;
				
			}
			
		}
		return num;
//		return (SysDir.worknumber-workerlist.size());
	}
	/**
	 * notify manager that it is available now
	 * @param w
	 */
	public static void notifyWorkerManager(Worker w){
		
		//check the waiting list to start running new task
		
//		TaskManager.notifyWaitinglist();
		
		TaskManager.done(w.getTask());
		
//		workerlist.remove(w);
	}
	
	public static final void main(String[] args){
//		Worker w = new Worker();
//		w.start();
////		try {
////			w.join();
////		} catch (InterruptedException e) {
////			// TODO Auto-generated catch block
////			e.printStackTrace();
////		}
//		if(w.isAlive()){
//			System.out.println("Worker is alive.");
//		}else{
//			System.out.println("Worker is dead.");
//		}
//		int i=0;
//		if(!w.isAlive()&&i<=100){
//			System.out.println("waiting================"+i);
//			i++;
//		}
	}
}
