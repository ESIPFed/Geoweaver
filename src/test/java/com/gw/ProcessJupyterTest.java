package com.gw;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gw.tools.HistoryTool;
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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = GeoweaverApplication.class)
public class ProcessJupyterTest {

	@Autowired
	UserTool ut;

	@Autowired
	BaseTool bt;

	@Autowired
	private TestRestTemplate testrestTemplate;

	@LocalServerPort
	private int port;

	@Autowired
	HistoryTool hist;

	Logger logger = Logger.getLogger(this.getClass());

    @Test
	@DisplayName("Testing adding jupyter process...")
	void testAddJupyterProcess() throws JsonMappingException, JsonProcessingException {

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		String jupyterjson = bt.readStringFromFile(bt.testResourceFiles() + "/add_jupyter_process.json");
		HttpEntity request = new HttpEntity<>(jupyterjson, headers);
		String result = this.testrestTemplate.postForObject(
				"http://localhost:" + this.port + "/Geoweaver/web/add/process",
				request,
				String.class);
		logger.debug("the result is: " + result);
		// assertThat(controller).isNotNull();
		assertThat(result).contains("id");

		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> map = mapper.readValue(result, Map.class);

		// Delete jupyter process
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		request = new HttpEntity<>("id=" + map.get("id") + "&type=process", headers);
		result = this.testrestTemplate.postForObject(
				"http://localhost:" + this.port + "/Geoweaver/web/del",
				request,
				String.class);
		logger.debug("the result is: " + result);
		// assertThat(controller).isNotNull();
		assertThat(result).contains("done");

	}
}
