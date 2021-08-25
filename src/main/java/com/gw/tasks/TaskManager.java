package com.gw.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.gw.database.HistoryRepository;
import com.gw.jpa.ExecutionStatus;
import com.gw.jpa.History;
import com.gw.utils.BaseTool;
import com.gw.workers.Worker;
import com.gw.workers.WorkerManager;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 *Class TaskManager.java
 *
 *updated on 11/17/2018
 *remove the observer and observable because they are deprecated in the latest JDK (>=9)
 *
 *@author ziheng
 *@time Aug 10, 2015 4:05:28 PM
 */
@Service
@Scope("singleton")
public class TaskManager {
	
	private List<Task> waitinglist;
	private List<Task> runninglist;
	
	Logger logger = Logger.getLogger(this.getClass());
	
	@Autowired
	WorkerManager wm;

	@Autowired
	BaseTool bt;

	@Autowired
	HistoryRepository hr;
	
	@Value("${geoweaver.workernumber}")
	int worknumber;
	
	{
		waitinglist = new ArrayList();
		runninglist = new ArrayList();
	}
	/**
	 * Add a new task to the waiting list
	 */
	public void addANewTask(Task t){
//		t.addObserver(wto);
		waitinglist.add(t);
		notifyWaitinglist();
//		t.initialize();
	}
	
	public void runDirectly(Task t) throws InterruptedException {
		
		Worker w = wm.getMustWorker();
		
		w.setTask(t);
		
		w.join(7*24*60*60*1000); // 7 days maximum
		
		wm.removeMustWorker(w);
		
	}
	/**
	 * Execute a task
	 * @param t
	 * @return
	 */
	private boolean executeATask(Task t){
		boolean is = false;
		if(wm.getCurrentWorkerNumber()<worknumber){
//			t.addObserver(rto);
			wm.createANewWorker(t);
			runninglist.add(t);
			is = true;
		}else{
			logger.debug("!!!This function is not called by the method notifyWaitinglist.");
//			t.addObserver(wto);
			waitinglist.add(t);
		}
		return is;
	}
	
	public Task searchByHistoryId(String historyid) {
		
		Task t = null;
		
		for(int i=0;i<waitinglist.size();i++) {
			
			if(historyid.equals((waitinglist.get(i)).getHistory_id())) {
				
				t = waitinglist.get(i);
				
				break;
				
			}
			
		}
		
		if(t==null) {
			
			for(int i=0;i<runninglist.size();i++) {
				
				if(historyid.equals((runninglist.get(i)).getHistory_id())) {
					
					t = runninglist.get(i);
					
					break;
					
				}
				
			}
			
		}
		
		return t;
		
	}
	
	/**
	 * Monitor the status of a task
	 * @param sessionid
	 * @param taskname
	 */
	// public void monitorTask(String historyid, Session session) {
		
	// 	// search the task with the name in waitinglist and runninglist
		
	// 	Task t = searchByHistoryId(historyid);
		
	// 	t.startMonitor(session);
		
	// }
	
	/**
	 * This function basically put the first priority task without any precondition tasks in the running or waiting list in the front
	 */
	public void orderWaitingList(){

		List<Integer> labels = new ArrayList();

		// logger.debug("Before refresh: " + waitinglist);

		for(int i=0;i<waitinglist.size();i++){

			GeoweaverProcessTask thet = (GeoweaverProcessTask) waitinglist.get(i);

			thet.setIsReady(checkIfReady(thet));
			
		}

		// for(int i=0;i<waitinglist.size();i++){

		// 	if(labels.get(i)==0){

		// 		Task ct = waitinglist.get(i);

		// 		for(int j=0;j<waitinglist.size();j++){

		// 			if(labels.get(j)==1){

		// 				waitinglist.set(i, waitinglist.get(j));

		// 				waitinglist.set(j, ct);

		// 				labels.set(j, 0);

		// 				labels.set(i, 1);

		// 			}

		// 		}

		// 	}

		// }

		logger.debug("The waiting list is refreshed..");;
		// logger.debug("After refresh: " + waitinglist);

	}

	/**
	 * Check if a task is ready to execute
	 * @param thet
	 * @return
	 */
	public boolean checkIfReady(GeoweaverProcessTask thet){

		boolean isready = false;

		List prehistoryid = thet.getPreconditionProcesses();

		if(!bt.isNull(prehistoryid) && prehistoryid.size()>0){

			int check = 0;

			for(int i=0;i<prehistoryid.size();i++){

				Optional<History> ho = hr.findById((String)prehistoryid.get(i));

				if(ho.isPresent()){

					String current_status = ho.get().getIndicator();

					if(bt.isNull(current_status) || current_status.equals(ExecutionStatus.RUNNING) || current_status.equals(ExecutionStatus.READY)){
			
						check = 1;
						break;
			
					}
				}else{
					check = 1;
					break;
				}


			}

			if(check==0)
				isready = true;

		}else{
			isready = true;
		}

		return isready;
	}

	/**
	 * Notify the waiting list that there is at least an available worker
	 */
	public synchronized void notifyWaitinglist(){
		logger.debug("notify waiting list to pay attention to the released worker");
		if(waitinglist.size()>0&&wm.getCurrentWorkerNumber()<worknumber){
			orderWaitingList();
			for(int i=0;i< waitinglist.size();i++){
				GeoweaverProcessTask newtask = (GeoweaverProcessTask)waitinglist.get(i);
				if(newtask.getIsReady()){

					waitinglist.remove(newtask);
		//			newtask.deleteObserver(wto);
					executeATask(newtask);

				}
				
			}
			
		}
	}
	/**
	 * A task is done, being triggered to start doing another task.
	 * @param t
	 * The done task.
	 */
	public void done(Task t){
//		t.deleteObserver(rto);
		runninglist.remove(t);
		notifyWaitinglist();
	}
	
	/**
	 * A new task arrives. Notify the task manager to take care of it.
	 */
	public void arrive(Task t){
		notifyWaitinglist();
	}

	/**
	 * This method should only be called by WorkflowTool to avoid potential messup in the workflow history table
	 * @param history_id
	 */
    public void stopTask(String history_id) {

		for(Task runningtask: runninglist){

			GeoweaverProcessTask thet = (GeoweaverProcessTask) runningtask;

			if(thet.getHistory_id().equals(history_id)){
				//to avoid mess of the thread, we currently don't kill the running workers
				// do nothing
			}

		}

		for(Task waitingtask: waitinglist){

			GeoweaverProcessTask thet = (GeoweaverProcessTask)waitingtask;

			if(thet.getHistory_id().equals(history_id)){

				thet.endPrematurely();

				waitinglist.remove(thet); //remove from waiting list

			}

		}

    }
	
}
