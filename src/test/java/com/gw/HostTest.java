package com.gw;

import static org.assertj.core.api.Assertions.assertThat;

import com.gw.tools.UserTool;
import com.gw.utils.BaseTool;

import org.apache.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HostTest {

    @Autowired
	UserTool ut;

	@Autowired
	BaseTool bt;

    @Autowired
	private TestRestTemplate testrestTemplate;

    @LocalServerPort
	private int port;

    Logger logger  = Logger.getLogger(this.getClass());

    // @Test
	void contextLoads() {
		
		
	}

	@Test
	void testLocalhostPassword(){

		bt.setLocalhostPassword("Test1", true);

		String password = bt.getLocalhostPassword();

		assertThat(password).hasSize(128);

		String password2 = bt.getLocalhostPassword();

		assertThat(password).isEqualTo(password2);

	}

    
    
}
