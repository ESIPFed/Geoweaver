package edu.gmu.csiss.earthcube.cyberconnector.tasks;

import java.util.Observable;
import java.util.Observer;

/**
 *Class WaitingTaskObserver.java
 *@author Ziheng Sun
 *@time Aug 11, 2015 12:19:04 PM
 *Original aim is to support CyberConnector.
 */
public class WaitingTaskObserver implements Observer {

	@Override
	public void update(Observable o, Object arg) {
		System.out.println(">>>>>>>A new task is added in the waiting list.");
		TaskManager.arrive((Task)arg);
	}

}
