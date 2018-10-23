package edu.gmu.csiss.earthcube.cyberconnector.workers;

import java.util.Observable;
import java.util.Observer;

/**
 *Class WorkerObserver.java
 *@author Ziheng Sun
 *@time Aug 11, 2015 1:20:01 PM
 *Original aim is to support CyberConnector.
 */
public class WorkerObserver implements Observer {

	@Override
	public void update(Observable o, Object arg) {
		WorkerManager.notifyWorkerManager((Worker)arg);
	}

}
