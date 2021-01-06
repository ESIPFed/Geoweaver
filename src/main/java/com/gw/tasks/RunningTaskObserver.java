package com.gw.tasks;

import java.util.Observable;
import java.util.Observer;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *Class RunningTaskObserver.java
 *@author ziheng
 *@time Aug 11, 2015 11:48:01 AM
 *Original aim is to support iGFDS.
 */
@Service
public class RunningTaskObserver implements Observer {

	@Autowired
	TaskManager tm;
	
	Logger logger = Logger.getLogger(this.getClass());
	
	@Override
	public void update(Observable o, Object arg) {
//		try {
//			Thread.sleep(5000);
//		} catch (InterruptedException e) {
//
//			e.printStackTrace();
//		}
		logger.debug(">>>>>The process of an task is finished.");
		tm.done((Task)arg);
	}
	
}
