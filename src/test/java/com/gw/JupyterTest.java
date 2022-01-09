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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class JupyterTest extends HelperMethods {

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
	void contextLoads() {

	}

	@Test
	public String testAddJupyterHost() throws Exception {

		HttpHeaders hostHeaders = new HttpHeaders();
		hostHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		// Add host
		String bultinjson = bt.readStringFromFile(bt.testResourceFiles() + "/add_jupyter_host.txt");
		HttpEntity hostRequest = new HttpEntity<>(bultinjson, hostHeaders);
		String hostResult = this.testrestTemplate.postForObject(
				"http://localhost:" + this.port + "/Geoweaver/web/add",
				hostRequest,
				String.class);
		assertThat(hostResult).contains("id");
		assertThat(hostResult).contains("jupyter");

		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> map = mapper.readValue(hostResult, Map.class);
		String jid = String.valueOf(map.get("id"));

		return jid;

	}

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

	void createJupyterNotebookHistory(String jid) {

		HttpHeaders jupyterHeaders = new HttpHeaders();
		jupyterHeaders.setContentType(MediaType.TEXT_HTML);
		jupyterHeaders.set("referer", "http://localhost:" + this.port + "/Geoweaver/" + jid);
		// Add test notebook body to include in history.
		String jupyterBody = bt.readStringFromFile(bt.testResourceFiles() + "/add_jupyter_body.json");

		// save jupyter notebook modification as history.
		// Each save generates a history id
		hist.saveJupyterCheckpoints(jid, jupyterBody, jupyterHeaders);
		hist.saveJupyterCheckpoints(jid, jupyterBody, jupyterHeaders);
		hist.saveJupyterCheckpoints(jid, jupyterBody, jupyterHeaders);

	}

	@Test
	@DisplayName("Test /delAllHistory endpoint")
	void testDelAllHistory() throws Exception {

		// Add Host
		String jid = testAddJupyterHost();

		// create jupyter host notebook history
		createJupyterNotebookHistory(jid);

		// Get recent history of jupyter notebook
		HttpHeaders postHeaders = new HttpHeaders();
		postHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		HttpEntity bodyData = new HttpEntity<>("id=" + jid, postHeaders);
		String Postresult = this.testrestTemplate.postForObject(
				"http://localhost:" + this.port + "/Geoweaver/web/delAllHistory",
				bodyData, String.class);
		logger.debug("/delAllHistory result: " + Postresult);

		assertThat(Postresult).contains("removed_history_ids");

		// Delete created Host
		deleteResource(jid, "host");

	}

	@Test
	@DisplayName("Test /delNoNotesHistory endpoint")
	void testDelNoNotesHistory() throws Exception {

		// Add Host
		String jid = testAddJupyterHost();

		// create jupyter host notebook history
		createJupyterNotebookHistory(jid);

		// Get recent history with no notes of jupyter notebook
		HttpHeaders postHeaders = new HttpHeaders();
		postHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		HttpEntity bodyData = new HttpEntity<>("id=" + jid, postHeaders);
		String Postresult = this.testrestTemplate.postForObject(
				"http://localhost:" + this.port + "/Geoweaver/web/delNoNotesHistory",
				bodyData, String.class);
		logger.debug("/delNoNotesHistory result: " + Postresult);
		assertThat(Postresult).contains("removed_history_ids");

		// Delete created Host
		deleteResource(jid, "host");

	}

}
