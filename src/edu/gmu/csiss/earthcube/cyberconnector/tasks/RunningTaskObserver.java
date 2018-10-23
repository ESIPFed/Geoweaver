package edu.gmu.csiss.earthcube.cyberconnector.tasks;

import java.util.Observable;
import java.util.Observer;

/**
 *Class RunningTaskObserver.java
 *@author ziheng
 *@time Aug 11, 2015 11:48:01 AM
 *Original aim is to support iGFDS.
 */
public class RunningTaskObserver implements Observer {

	@Override
	public void update(Observable o, Object arg) {
//		try {
//			Thread.sleep(5000);
//		} catch (InterruptedException e) {
//
//			e.printStackTrace();
//		}
		System.out.println(">>>>>The process of an task is finished.");
		TaskManager.done((Task)arg);
	}
	
}
