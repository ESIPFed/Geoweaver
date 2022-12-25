package com.gw.tasks;

import java.util.Observable;
import java.util.Observer;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *Class WaitingTaskObserver.java
 *@time Aug 11, 2015 12:19:04 PM
 *Original aim is to support Geoweaver.
 */
@Deprecated
@Service
public class WaitingTaskObserver implements Observer {
	
	@Autowired
	TaskManager tm;
	
	Logger logger = Logger.getLogger(this.getClass());

	@Override
	public void update(Observable o, Object arg) {
		logger.debug(">>>>>>>A new task is added in the waiting list.");
		tm.arrive((Task)arg);
	}

}
