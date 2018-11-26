
package edu.gmu.csiss.earthcube.cyberconnector.ssh;

import org.junit.Test;

public class SSHTest {

	@Test
	public void testExecuteProcess() {
		
//		ProcessTool.execute();
		
	}
	
	@Test
	public void testDeleteProcess() {
		
//		ProcessTool.del("");
		
	}
	
	@Test
	public void testFinally() {
		
		try {
			
			System.out.println("body");
			
			throw new RuntimeException("throw one");
			
		}catch(Exception e) {
			
			System.out.println("catch");
			
//			throw new RuntimeException("New exception");
			
		}finally {
			
			System.out.println("Finally"); //this line will be executed no matter whether the catch clause has exception thrown out. 
			
		}
		
	}
	
}
