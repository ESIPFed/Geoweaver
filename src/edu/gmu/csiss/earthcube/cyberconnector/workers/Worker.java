package edu.gmu.csiss.earthcube.cyberconnector.workers;

import java.util.concurrent.TimeUnit;

import edu.gmu.csiss.earthcube.cyberconnector.tasks.Task;
import edu.gmu.csiss.earthcube.cyberconnector.utils.RandomString;

/**
 *Class Worker.java
 *@author ziheng
 *@time Aug 10, 2015 4:02:19 PM
 *Original aim is to support iGFDS.
 */
public class Worker extends Thread{

	private String name;
	
	private Task t;
	
	private boolean status; //true: working; false: idle
	
	public Worker() {
		
		name = new RandomString(5).nextString();
		
	}
	
	public Worker(Task t){
		this();
		setTask(t);
	}
	
	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}



	public void setTask(Task t) {
		
		this.t = t;
		
		this.setStatus(true);

		System.out.println("task "+t.getName()+" is assigned to a worker: " + name);
		
//		this.notifyAll();
		
	}
	
	public Task getTask(){
		return t;
	}
	
	private void unloadTask() {
		
//		System.out.println("task unloaded\n notify the manager that a work is freed");
		
		this.t = null;
		
		this.setStatus(false);
		
		WorkerManager.notifyWorkerManager(this);
		
	}
	
	@Override
	public void run() {
		//it should not be like this. The thread should not exit but wait for next task arrives 
		//so that the time cost of starting a new thread (very long) can be saved. 
//		System.out.println("A worker thread "+name+" is started.");
		try {
			
			boolean running = true;
		    
			while(running) {
				
				if(this.t!=null) {
					
//					System.out.println("=== task loaded");
					
					t.execute();
			    	
					t.responseCallback();
					
					unloadTask();
					
				}
		        
		        if (Thread.interrupted()) {
		        	System.out.println("thread " + name + " interrupted");
//		            return;
		        	running = false;
		        }
		        
//		        System.out.println("---thread " + name + " starts to wait");
		        
		        TimeUnit.SECONDS.sleep(1);
		        
//		        wait(); //wait until notify is called
		    }
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
			if(this.t!=null) {
				
				t.failureCallback(e);
				
				unloadTask();
			}
				
			
		}
		
		System.out.println("Worker "+name+" is stopped.");
	}
}
