package com.gw;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gw.jpa.GWUser;
import com.gw.tools.UserTool;
import com.gw.utils.BaseTool;

import org.apache.log4j.Logger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GeneralTests {

	@LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate testrestTemplate;
	

	Logger logger  = Logger.getLogger(this.getClass());

	@Autowired
	UserTool ut;

	@Autowired
	BaseTool bt;

	@Test
	void contextLoads() {
		
		
	}

	@Test
	@DisplayName("Testing adding/editing/removing user...")
	void testUser(){
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    	HttpEntity request = new HttpEntity<>("type=host", headers);
		String result = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/list", 
			request, 
			String.class);
		logger.debug("the result is: " + result);
		// assertThat(controller).isNotNull();
		assertThat(result).contains("[");

	}

	@Test
	String testResourceFiles(){

		Path resourceDirectory = Paths.get("src","test","resources");
		String absolutePath = resourceDirectory.toFile().getAbsolutePath();

		logger.debug(absolutePath);
		assertTrue(absolutePath.contains("resources"));
		return absolutePath;
	}


	@Test
   	@DisplayName("Subscription message service test ")
   	void testSubscriptionMessage() {
		
      	GWUser u = ut.getUserById("111111");

      	assertEquals(u.getUsername(), "publicuser");
   	}

	@Test
	@DisplayName("Testing if the front page is accessible..")
	void testFrontPage(){
		String result = this.testrestTemplate.getForObject("http://localhost:" + this.port + "/Geoweaver/web/geoweaver", String.class);
		// logger.debug("the result is: " + result);
		// assertThat(controller).isNotNull();
		assertThat(result).contains("Geoweaver");
		
	}

	@Test
	@DisplayName("Testing Dashboard...")
	void testDashboard(){
		// ResponseEntity<String> result = testrestTemplate.getForEntity("http://localhost:" + this.port + "/Geoweaver/web/dashboard", String.class);
		ResponseEntity result = this.testrestTemplate.postForEntity("http://localhost:" + this.port + "/Geoweaver/web/dashboard",
			"",
			String.class);
		// logger.debug("the dashboard result is: " + result);
		// assertThat(controller).isNotNull();
		assertEquals(200, result.getStatusCode().value());
		assertThat(result.getBody().toString()).contains("process_num");
	}

	@Test
	@DisplayName("Testing list of host, process, and workflow...")
	void testList(){
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    	HttpEntity request = new HttpEntity<>("type=host", headers);
		String result = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/list", 
			request, 
			String.class);
		logger.debug("the result is: " + result);
		// assertThat(controller).isNotNull();
		assertThat(result).contains("[");

		request = new HttpEntity<>("type=process", headers);
		result = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/list", 
			request, 
			String.class);
		// logger.debug("the result is: " + result);
		// assertThat(controller).isNotNull();
		assertThat(result).contains("[");

		request = new HttpEntity<>("type=workflow", headers);
		result = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/list", 
			request, 
			String.class);
		// logger.debug("the result is: " + result);
		// assertThat(controller).isNotNull();
		assertThat(result).contains("[");
	}

	

	


	@Test
	void testJSONEscape(){

		String jsonstr = "import os\nimport time";

		if(jsonstr.contains("\nimport")){

			logger.debug("import is detected");

		}else{

			logger.debug("import is not detected");
		}

		
		String jsonstr2 = "{\"cells\":[{\"cell_type\":\"markdown\"";

		if(jsonstr2.contains("\"cells\"")){

			logger.debug("cell is detected");

		}else{

			logger.debug("cell is not detected");
		}


	}

}
