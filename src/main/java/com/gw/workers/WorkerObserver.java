package com.gw.workers;

import java.util.Observable;
import java.util.Observer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *Class WorkerObserver.java
 *@author Ziheng Sun
 *@time Aug 11, 2015 1:20:01 PM
 *Original aim is to support Geoweaver.
 */
@Service
public class WorkerObserver implements Observer {

	@Autowired
	WorkerManager wm;
	
	@Override
	public void update(Observable o, Object arg) {
		wm.notifyWorkerManager((Worker)arg);
	}

}
