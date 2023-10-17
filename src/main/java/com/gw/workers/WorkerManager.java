package com.gw.workers;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.gw.tasks.Task;
import com.gw.tasks.TaskManager;
import com.gw.utils.SysDir;

/**
 *Class WorkerManager.java
 *@author ziheng
 *@time Aug 10, 2015 4:04:25 PM
 *Original aim is to support Geoweaver.
 */
@Service
@Scope("singleton")
public class WorkerManager {
	
	private List<Worker> workerlist;
	
	@Autowired
	TaskManager tm;
	
	@Value("${geoweaver.workernumber}")
	String worknumber2;
	
	int worknumber = 5;
	
	Logger logger = Logger.getLogger(this.getClass());
	
	public WorkerManager(){
		
		this.init();
		
	}
	
	private void init() {
		
		//startup a list of workers
		workerlist = new ArrayList();
		
		logger.debug("The worknumber setting: " + worknumber);
		
		for(int i=0;i<worknumber;i++) {
			
			logger.debug("worker manager created a worker ");
			
			Worker w = new Worker();
			
			w.start();
			
			workerlist.add(w);
			
		}
		
	}
	
	/**
	 * This worker might exceed the worker number limit, use with caution
	 * @return
	 */
	public Worker getMustWorker() {
		
		Worker	w = new Worker(true);
		
		w.start();
			
		workerlist.add(w);
		
		return w;
	}
	
	public void removeMustWorker(Worker w) {
		
		workerlist.remove(w);
		
	}
	
	public Worker getNextAvailableWorker() {
		
		logger.debug("Get next available worker..");
		
		Worker w = null;
		
		for(int i=0;i<worknumber;i++) {
			
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
	public int getCurrentWorkerNumber(){
		
		int num = 0;
		
		if(worknumber<workerlist.size()) this.init();
		
		for(int i=0;i<worknumber;i++) {
			
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
	public void waitJoin() throws InterruptedException {
		
		for(int i=0;i<worknumber;i++) {
			
			workerlist.get(i).join();
			
		}
		
	}
	
	public Worker createANewWorker(Task t){
		//add the task to a empty worker
		Worker w = getNextAvailableWorker();
		logger.debug("Load task into worker.." + t.getName());
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
	public int getNumberOfAvailableWorkers(){
		
		int num = 0;
		
		for(int i=0;i<worknumber;i++) {
			
			if(!workerlist.get(i).isStatus()) {
				
				num++;
				
			}
			
		}
		return num;
	}
	/**
	 * notify manager that it is available now
	 * @param w
	 */
	public void notifyWorkerManager(Worker w){
		//check the waiting list to start running new task
		tm.done(w.getTask());
	}
	
	public static final void main(String[] args){
//		Worker w = new Worker();
//		w.start();
////		try {
////			w.join();
////		} catch (InterruptedException e) {
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
