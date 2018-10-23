package edu.gmu.csiss.earthcube.cyberconnector.utils;
/**
 *Class EmailValidator.java
 *@author Ziheng Sun
 *@time Sep 2, 2015 5:34:55 PM
 *Original aim is to support CyberConnector.
 */
public class EmailValidator {
	/**
	 * Match Email string
	 * @param email
	 * @return
	 */
	public static boolean validate(String email){
		String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(email);
        return m.matches();
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println(EmailValidator.validate("zsun@ gmu edu"));
	}

}
