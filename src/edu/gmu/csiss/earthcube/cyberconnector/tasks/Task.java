package edu.gmu.csiss.earthcube.cyberconnector.tasks;

import java.util.Observable;

/**
 *Class Task.java
 *@author ziheng
 *@time Aug 10, 2015 4:04:57 PM
 *Original aim is to support CyberConnector.
 */
public abstract class Task extends Observable{
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
}
