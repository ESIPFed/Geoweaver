package com.gw.tasks;

import com.gw.database.HistoryRepository;
import com.gw.jpa.ExecutionStatus;
import com.gw.jpa.History;
import com.gw.utils.BaseTool;
import com.gw.workers.Worker;
import com.gw.workers.WorkerManager;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * Class TaskManager.java
 *
 * <p>updated on 11/17/2018 remove the observer and observable because they are deprecated in the
 * latest JDK (>=9)
 *
 * @author ziheng
 * @time Aug 10, 2015 4:05:28 PM
 */
@Service
@Scope("singleton")
public class TaskManager {

  private CopyOnWriteArrayList<Task> waitinglist;
  private CopyOnWriteArrayList<Task> runninglist;
  private ScheduledExecutorService scheduler; // Scheduler for periodic task checking


  Logger logger = Logger.getLogger(this.getClass());

  @Autowired WorkerManager wm;

  @Autowired BaseTool bt;

  @Autowired HistoryRepository hr;

  @Value("${geoweaver.workernumber}")
  int worknumber;

  {
    waitinglist = new CopyOnWriteArrayList();
    runninglist = new CopyOnWriteArrayList();
  }

  public TaskManager() {
    this.init();
  }

  private void init() {
    // Initialize the scheduler
    scheduler = Executors.newScheduledThreadPool(1);
    scheduler.scheduleAtFixedRate(this::notifyWaitinglist, 0, 1, TimeUnit.MINUTES);
  }

  /** Add a new task to the waiting list */
  public void addANewTask(Task t) {
    //		t.addObserver(wto);
    waitinglist.add(t);
    notifyWaitinglist();
    //		t.initialize();
  }

  public void runDirectly(Task t) throws InterruptedException {

    Worker w = wm.getMustWorker();

    w.setTask(t);

    w.join(7 * 24 * 60 * 60 * 1000); // 7 days maximum

    wm.removeMustWorker(w);
  }

  /**
   * Execute a task
   *
   * @param t
   * @return
   */
  private boolean executeATask(Task t) {
    boolean is = false;
    if (wm.getCurrentWorkerNumber() < worknumber) {
      wm.createANewWorker(t);
      runninglist.add(t);
      is = true;
    } else {
      logger.debug("!!!This function is not called by the method notifyWaitinglist.");
      waitinglist.add(t);
    }
    return is;
  }

  public Task searchByHistoryId(String historyid) {

    Task t = null;

    for (int i = 0; i < waitinglist.size(); i++) {

      if (historyid.equals((waitinglist.get(i)).getHistory_id())) {

        t = waitinglist.get(i);

        break;
      }
    }

    if (t == null) {

      for (int i = 0; i < runninglist.size(); i++) {

        if (historyid.equals((runninglist.get(i)).getHistory_id())) {

          t = runninglist.get(i);

          break;
        }
      }
    }

    return t;
  }

  /**
   * Monitor the status of a task
   *
   * @param sessionid
   * @param taskname
   */
  // public void monitorTask(String historyid, Session session) {

  // 	// search the task with the name in waitinglist and runninglist

  // 	Task t = searchByHistoryId(historyid);

  // 	t.startMonitor(session);

  // }

  /**
   * This function basically put the first priority task without any precondition tasks in the
   * running or waiting list in the front
   */
  public void orderWaitingList() {

    for (int i = 0; i < waitinglist.size(); i++) {
      GeoweaverProcessTask thet = (GeoweaverProcessTask) waitinglist.get(i);
      thet.setIsReady(thet.checkIfReady());
    }

    logger.debug("The waiting list is refreshed..");
  }

  /** Notify the waiting list that there is at least an available worker */
  public synchronized void notifyWaitinglist() {
    logger.debug("notify waiting list to pay attention to the released worker");
    if (waitinglist.size() > 0 && wm.getCurrentWorkerNumber() < worknumber) {
      orderWaitingList();
      for (int i = 0; i < waitinglist.size(); i++) {
        GeoweaverProcessTask newtask = (GeoweaverProcessTask) waitinglist.get(i);
        if (newtask.getIsReady()) {
          waitinglist.remove(newtask);
          if (newtask.checkShouldPassOrNot()){
            newtask.endPrematurely();
            notifyWaitinglist();
          }else{
            executeATask(newtask);
          }
        }
      }
    }
  }

  /**
   * A task is done, being triggered to start doing another task.
   *
   * @param t The done task.
   */
  public void done(Task t) {
    //		t.deleteObserver(rto);
    runninglist.remove(t);
    notifyWaitinglist();
  }

  /** A new task arrives. Notify the task manager to take care of it. */
  public void arrive(Task t) {
    notifyWaitinglist();
  }

  /**
   * This method should only be called by WorkflowTool to avoid potential messup in the workflow
   * history table
   *
   * @param history_id
   */
  public void stopTask(String history_id) {

    try {

      synchronized (waitinglist) {
        Iterator<Task> iterator = waitinglist.iterator();

        while (iterator.hasNext()) {

          GeoweaverProcessTask thet = (GeoweaverProcessTask) iterator.next();

          if (thet.getHistory_id().equals(history_id)) {

            thet.endPrematurely();

            waitinglist.remove(thet); // remove from waiting list
          }
        }
      }

      synchronized (runninglist) {
        Iterator<Task> iterator = runninglist.iterator();

        while (iterator.hasNext()) {

          GeoweaverProcessTask thet = (GeoweaverProcessTask) iterator.next();

          if (thet.getHistory_id().equals(history_id)) {

            // for task ongoing, it will be ended using pt.stop at above level.

            thet.endPrematurely();

            runninglist.remove(thet); // remove from waiting list
          }
        }
      }

    } catch (Exception e) {

      e.printStackTrace();
    }
  }
}
