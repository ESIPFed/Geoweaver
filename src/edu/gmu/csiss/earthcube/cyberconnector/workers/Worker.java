package edu.gmu.csiss.earthcube.cyberconnector.workers;

import edu.gmu.csiss.earthcube.cyberconnector.tasks.Task;

/**
 *Class Worker.java
 *@author ziheng
 *@time Aug 10, 2015 4:02:19 PM
 *Original aim is to support iGFDS.
 */
public class Worker extends Thread{

	private Task t;
	
	public Worker(Task t){
		this.t = t;
		System.out.println("A task is assigned to a worker.");
	}
	
	public Task getTask(){
		return t;
	}
	
	@Override
	public void run() {
		System.out.println("A worker is started.");
		try {
			t.execute();
			t.responseCallback();
		} catch (Exception e) {
			e.printStackTrace();
			t.failureCallback(e);
		}
		WorkerManager.notifyWorkerManager(this);
		System.out.println("A worker finishes his job.\nA worker is stopped.");
	}
}
