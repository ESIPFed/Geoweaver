package com.gw;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gw.jpa.GWUser;
import com.gw.tools.UserTool;
import com.gw.utils.BaseTool;
import com.gw.web.GeoweaverController;

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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GeoweaverApplicationTests {

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
	@DisplayName("Testing adding jupyter process...")
	void testAddJupyterProcess() throws JsonMappingException, JsonProcessingException{
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		String jupyterjson = bt.readStringFromFile(this.testResourceFiles()+ "/add_jupyter_process.json" );
    	HttpEntity request = new HttpEntity<>(jupyterjson, headers);
		String result = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/add/process", 
			request, 
			String.class);
		logger.debug("the result is: " + result);
		// assertThat(controller).isNotNull();
		assertThat(result).contains("id");

		ObjectMapper mapper = new ObjectMapper();
		Map<String,Object> map = mapper.readValue(result, Map.class);

		// id=2avx48&type=process
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    	request = new HttpEntity<>("id="+map.get("id")+"&type=process", headers);
		result = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/del", 
			request, 
			String.class);
		logger.debug("the result is: " + result);
		// assertThat(controller).isNotNull();
		assertThat(result).contains("done");

	}

	@Test
	@DisplayName("Test adding builtin process")
	void testAddBuiltinProcess() throws JsonMappingException, JsonProcessingException{

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		String bultinjson = bt.readStringFromFile(this.testResourceFiles()+ "/add_builtin_process.json" );
    	HttpEntity request = new HttpEntity<>(bultinjson, headers);
		String result = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/add/process", 
			request, 
			String.class);
		logger.debug("the result is: " + result);
		// assertThat(controller).isNotNull();
		assertThat(result).contains("id");

		ObjectMapper mapper = new ObjectMapper();
		Map<String,Object> map = mapper.readValue(result, Map.class);

		// id=2avx48&type=process
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    	request = new HttpEntity<>("id="+map.get("id")+"&type=process", headers);
		result = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/del", 
			request, 
			String.class);
		logger.debug("the result is: " + result);
		// assertThat(controller).isNotNull();
		assertThat(result).contains("done");

	}

	@Test
	@DisplayName("Test adding python process")
	void testAddPythonProcess() throws JsonMappingException, JsonProcessingException{

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		String pythonjson = bt.readStringFromFile(this.testResourceFiles()+ "/add_python_process.json" );
    	HttpEntity request = new HttpEntity<>(pythonjson, headers);
		String result = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/add/process", 
			request, 
			String.class);
		logger.debug("the result is: " + result);
		// assertThat(controller).isNotNull();
		assertThat(result).contains("id");

		ObjectMapper mapper = new ObjectMapper();
		Map<String,Object> map = mapper.readValue(result, Map.class);

		// id=2avx48&type=process
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    	request = new HttpEntity<>("id="+map.get("id")+"&type=process", headers);
		result = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/del", 
			request, 
			String.class);
		logger.debug("the result is: " + result);
		// assertThat(controller).isNotNull();
		assertThat(result).contains("done");

	}

	@Test
	@DisplayName("Test adding shell process")
	void testAddShellProcess() throws JsonMappingException, JsonProcessingException{

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		String shelljson = bt.readStringFromFile(this.testResourceFiles()+ "/add_shell_process.json" );
    	HttpEntity request = new HttpEntity<>(shelljson, headers);
		String result = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/add/process", 
			request, 
			String.class);
		logger.debug("the result is: " + result);
		// assertThat(controller).isNotNull();
		assertThat(result).contains("id");

		ObjectMapper mapper = new ObjectMapper();
		Map<String,Object> map = mapper.readValue(result, Map.class);

		// id=2avx48&type=process
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    	request = new HttpEntity<>("id="+map.get("id")+"&type=process", headers);
		result = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/del", 
			request, 
			String.class);
		logger.debug("the result is: " + result);
		// assertThat(controller).isNotNull();
		assertThat(result).contains("done");

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
	@DisplayName("Testing adding workflow...")
	void testAddWorkflow() throws JsonMappingException, JsonProcessingException{
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		String jupyterjson = bt.readStringFromFile(this.testResourceFiles()+ "/add_workflow.json" );
    	HttpEntity request = new HttpEntity<>(jupyterjson, headers);
		String result = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/add/workflow", 
			request, 
			String.class);
		logger.debug("the result is: " + result);
		// assertThat(controller).isNotNull();
		assertThat(result).contains("id");

		ObjectMapper mapper = new ObjectMapper();
		Map<String,Object> map = mapper.readValue(result, Map.class);

		String workflowname = String.valueOf(map.get("name"));

		assertNotNull(workflowname);

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
