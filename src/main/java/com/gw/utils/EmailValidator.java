package com.gw.utils;
/**
 *Class EmailValidator.java
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
