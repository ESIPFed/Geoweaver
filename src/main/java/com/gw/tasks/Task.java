package com.gw.tasks;


import javax.websocket.Session;

import org.springframework.web.socket.WebSocketSession;

/**
 *Class Task.java
 *@author ziheng
 *@time Aug 10, 2015 4:04:57 PM
 *Original aim is to support Geoweaver.
 */
public abstract class Task{
	/**
	 * Set up a task
	 */
	public abstract void initialize();
	/**
	 * Execute the task
	 */
	public abstract void execute();
	/**
	 * Call back function
	 * Return the execution results
	 */
	public abstract void responseCallback();
	/**
	 * Call back function
	 * If the execution fails
	 */
	public abstract void failureCallback(Exception e);
	
	public abstract String getName();
	
	public abstract String getHistory_id();
	
	public abstract void startMonitor(Session session);
	
}
