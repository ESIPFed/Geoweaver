package edu.gmu.csiss.earthcube.cyberconnector.order;

import it.sauronsoftware.cron4j.Scheduler;

/**
*Class TestCron.java
*@author Ziheng Sun
*@time Mar 27, 2017 4:10:30 PM
*Original aim is to support CyberConnector.
*/
public class TestCron {

	public static void main(String[] args) {

		// Creates a Scheduler instance.
		Scheduler s = new Scheduler();
		// Schedule a once-a-minute task.
		s.schedule("* * * * *", new Runnable() {
			public void run() {
				System.out.println("Another minute ticked away...");
			}
		});
		// Starts the scheduler.
		s.start();
		// Will run for ten minutes.
		try {
			Thread.sleep(1000L * 60L * 2L);
		} catch (InterruptedException e) {
			;
		}
		// Stops the scheduler.
		s.stop();
		
		System.out.println("Test if the stop will close the started thread.");
		

	}

}
