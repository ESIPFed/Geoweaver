package edu.gmu.csiss.earthcube.cyberconnector.utils;

import java.util.Random;

/**
 *Class RandomString.java
 *@author ziheng
 *@time Aug 11, 2015 10:52:43 AM
 *Original aim is to support iGFDS.
 */
public class RandomString {

	  private static final char[] symbols;

	  static {
	    StringBuilder tmp = new StringBuilder();
	    for (char ch = '0'; ch <= '9'; ++ch)
	      tmp.append(ch);
	    for (char ch = 'a'; ch <= 'z'; ++ch)
	      tmp.append(ch);
	    symbols = tmp.toString().toCharArray();
	  }   

	  private final Random random = new Random();

	  private final char[] buf;

	  public RandomString(int length) {
	    if (length < 1)
	      throw new IllegalArgumentException("length < 1: " + length);
	    buf = new char[length];
	  }

	  public String nextString() {
	    for (int idx = 0; idx < buf.length; ++idx) 
	      buf[idx] = symbols[random.nextInt(symbols.length)];
	    return new String(buf);
	  }
	  
	  public static String get(int num) {
		  
		  RandomString rs = new RandomString(num);
		  
		  return rs.nextString();
		  
	  }
	  
	  public static final void main(String[] args){
		  RandomString rs = new RandomString(18);
		  for(int i=0;i<8;i++){
			  System.out.println(rs.nextString());
		  }
	  }
}