package edu.gmu.csiss.earthcube.cyberconnector.tasks;

import java.util.ArrayList;
import java.util.List;

import edu.gmu.csiss.earthcube.cyberconnector.utils.SysDir;
import edu.gmu.csiss.earthcube.cyberconnector.workers.WorkerManager;

/**
 *Class TaskManager.java
 *@author ziheng
 *@time Aug 10, 2015 4:05:28 PM
 *Original aim is to support iGFDS.
 */
public class TaskManager {
	
	private static List<Task> waitinglist;
	private static List<Task> runninglist;
	private static RunningTaskObserver rto;
	private static WaitingTaskObserver wto;
	
	static{
		waitinglist = new ArrayList();
		runninglist = new ArrayList();
		rto = new RunningTaskObserver();
		wto = new WaitingTaskObserver();
	}
	/**
	 * Add a new task to the waiting list
	 * @param oid
	 * @param category
	 * @param east
	 * @param south
	 * @param west
	 * @param north
	 * @param proj
	 * @param begintime
	 * @param endtime
	 * @param mail
	 */
	public static void addANewTask(Task t){
		t.addObserver(wto);
		waitinglist.add(t);
		t.initialize();
	}
	/**
	 * Execute a task
	 * @param t
	 * @return
	 */
	private static boolean executeATask(Task t){
		boolean is = false;
		if(WorkerManager.getCurrentWorkerNumber()<SysDir.worknumber){
			t.addObserver(rto);
			WorkerManager.createANewWorker(t);
			runninglist.add(t);
			is = true;
		}else{
			System.out.println("!!!This function is not called by the method notifyWaitinglist.");
			t.addObserver(wto);
			waitinglist.add(t);
		}
		return is;
	}
	/**
	 * Notify the waiting list that there is at least an available worker
	 */
	public static void notifyWaitinglist(){
		if(waitinglist.size()>0&&WorkerManager.getCurrentWorkerNumber()<SysDir.worknumber){
			Task newtask = waitinglist.get(0);
			waitinglist.remove(newtask);
			newtask.deleteObserver(wto);
			TaskManager.executeATask(newtask);
		}
	}
	/**
	 * A task is done, being triggered to start doing another task.
	 * @param t
	 * The done task.
	 */
	public static void done(Task t){
		t.deleteObserver(rto);
		runninglist.remove(t);
		notifyWaitinglist();
	}
	/**
	 * A new task arrives. Notify the task manager to take care of it.
	 */
	public static void arrive(Task t){
		notifyWaitinglist();
	}
}
