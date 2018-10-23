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
		//startup a list of workers
		workerlist = new ArrayList();
	}
	
	public static int getCurrentWorkerNumber(){
		return workerlist.size();
	}
	
	public static Worker createANewWorker(Task t){
		Worker w = new Worker(t);
		w.start();
		workerlist.add(w);
		return w;
	}

	/**
	 * Get the number of available workers
	 * @return
	 * A number
	 */
	public static int getNumberOfAvailableWorkers(){
		
		return (SysDir.worknumber-workerlist.size());
	}
	/**
	 * 
	 * @param w
	 */
	public static void notifyWorkerManager(Worker w){
		workerlist.remove(w);
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
