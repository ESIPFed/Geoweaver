package com.gw;

import org.springframework.stereotype.Service;

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
public class WorkflowTest {

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
	String testResourceFiles(){

		Path resourceDirectory = Paths.get("src","test","resources");
		String absolutePath = resourceDirectory.toFile().getAbsolutePath();

		logger.debug(absolutePath);
		assertTrue(absolutePath.contains("resources"));
		return absolutePath;
	}
    
    @Test
	@DisplayName("Testing adding workflow...")
	void testAddWorkflow() throws JsonMappingException, JsonProcessingException{
		
        //add workflow
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		String jupyterjson = bt.readStringFromFile(this.testResourceFiles()+ "/add_workflow.json" );
    	HttpEntity request = new HttpEntity<>(jupyterjson, headers);
		String result = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/add/workflow", 
			request, 
			String.class);
		logger.debug("the result is: " + result);
		assertThat(result).contains("id");

		ObjectMapper mapper = new ObjectMapper();
		Map<String,Object> map = mapper.readValue(result, Map.class);
		String workflowname = String.valueOf(map.get("name"));
		assertNotNull(workflowname);

		//test workflow landing page
		String wid = String.valueOf(map.get("id"));
		result = this.testrestTemplate.getForObject("http://localhost:" + this.port + "/Geoweaver/landing/" + wid, String.class);
		assertThat(result).contains("Workflow Owner");
		assertThat(result).contains("Workflow Nodes");

		//test removing the workflow
		// id=2avx48&type=workflow
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    	request = new HttpEntity<>("id="+map.get("id")+"&type=workflow", headers);
		result = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/del", 
			request, 
			String.class);
		logger.debug("the result is: " + result);
		// assertThat(controller).isNotNull();
		assertThat(result).contains("done");

	}

}
