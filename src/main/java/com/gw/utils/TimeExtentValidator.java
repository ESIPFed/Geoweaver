package com.gw.utils;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *Class TimeExtentValidator.java
 *@author Ziheng Sun
 *@time Sep 2, 2015 11:28:30 AM
 *Original aim is to support Geoweaver.
 */
@Service
public class TimeExtentValidator {
	
	@Autowired
	BaseTool bt;
	
	/**
	 * Validate the time extent
	 * @param begintime
	 * @param endtime
	 * @return
	 * If valid, return true; Else, return false.
	 */
	public boolean validate(String begintime, String endtime, String productcategory){
		
		//check if the format is correct
		Date bd = null;
		Date ed = null;
		Date currentd = new Date();
		boolean valid = false;
		// BaseTool tool = new BaseTool();
		if((bd=bt.parseVCIDate(begintime))==null||
				(ed=bt.parseVCIDate(endtime))==null){
			
		}
		//check if the endtime is bigger than the begintime
		else if(bd.after(ed)){
			
		}
		//check if the endtime is bigger than current time
		else if(ed.after(currentd)){
			
		}
		//check if the days between the begin and end dates are more than 16
		else if(bt.getDaysBetweenTwoDates(bd, ed)>16){
			throw new RuntimeException("The days between the begin and end dates are more than 16.");
		}else {
			valid = true;
		}
		return valid;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		System.out.println(TimeExtentValidator.validate("2015-09-01 11:34:19", "2015-09-02 12:34:19","16 days 250m global customizable VCI"));;
	}

}
