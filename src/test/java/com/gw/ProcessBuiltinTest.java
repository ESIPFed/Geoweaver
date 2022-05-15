package com.gw;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gw.tools.BuiltinTool;
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
public class ProcessBuiltinTest {

	@Autowired
	UserTool ut;

	@Autowired
	BaseTool bt;

	@Autowired
	private TestRestTemplate testrestTemplate;

	@LocalServerPort
	private int port;

	@Autowired
	BuiltinTool builtTool;

	Logger logger = Logger.getLogger(this.getClass());

	@Test
	void contextLoads() {

	}

	@Test
	@DisplayName("Test adding builtin process")
	void testAddBuiltinProcess() throws JsonMappingException, JsonProcessingException {

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		String bultinjson = bt.readStringFromFile(bt.testResourceFiles() + "/add_builtin_process.json");
		HttpEntity request = new HttpEntity<>(bultinjson, headers);
		String result = this.testrestTemplate.postForObject(
				"http://localhost:" + this.port + "/Geoweaver/web/add/process",
				request,
				String.class);
		logger.debug("the result is: " + result);
		// assertThat(controller).isNotNull();
		assertThat(result).contains("id");

		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> map = mapper.readValue(result, Map.class);

		// id=2avx48&type=process
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		request = new HttpEntity<>("id=" + map.get("id") + "&type=process", headers);
		result = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/del",
				request,
				String.class);
		logger.debug("the result is: " + result);
		// assertThat(controller).isNotNull();
		assertThat(result).contains("done");

	}

	@Test
	void testSaveHistory() {

		try {

			builtTool.saveHistory(null, null, null, null);
			builtTool.sendMessageWebSocket(null, null, null);
			builtTool.executeCommonTasks(null, null, null, null, null, false);

		} catch (Exception e) {
			// TODO: handle exception
		}

	}

}
