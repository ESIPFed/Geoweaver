package com.gw;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.security.KeyPair;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gw.jpa.GWUser;
import com.gw.jpa.History;
import com.gw.jpa.Workflow;
import com.gw.tools.HistoryTool;
import com.gw.tools.UserTool;
import com.gw.utils.BaseTool;
import com.gw.utils.RandomString;
import com.gw.web.GeoweaverController;

import org.apache.log4j.Logger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;
import org.springframework.stereotype.Service;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = GeoweaverApplication.class)
public class HistoryTest extends AbstractHelperMethodsTest{

    @Autowired
	UserTool ut;

	@Autowired
	BaseTool bt;

    @Autowired
    HistoryTool hist;

	@Autowired
	private TestRestTemplate testrestTemplate;

	@LocalServerPort
	private int port;

	Logger logger = Logger.getLogger(this.getClass());

    @Test
    void testHistoryDelete(){

        History dummyhis = new History();
        dummyhis.setHistory_id("testdummyhisid");
        hist.saveHistory(dummyhis);

        HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		HttpEntity request = new HttpEntity<>("id=testdummyhisid&type=history", headers);
		String result = this.testrestTemplate.postForObject(
				"http://localhost:" + this.port + "/Geoweaver/web/del",
				request,
				String.class);
		logger.debug("the result is: " + result);
		assertThat(result).contains("done");

    }

    @Test
	@DisplayName("Test /recent endpoint for process type")
	void testProcessRecentHistory() throws Exception {

		String pid = AddPythonProcess();

		// run the python process
		ltmock = Mockito.spy(ltmock);
		doNothing().when(ltmock).authenticate(anyString());

		String historyid = new RandomString(12).nextString();

		ltmock.executePythonProcess(historyid, pid, "100001", "dummyrsastring", historyid, true, "", "", "");

		HttpHeaders postHeaders = new HttpHeaders();
		postHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		// Get process recent history
		HttpEntity postRequest = new HttpEntity<>("type=process&number=20", postHeaders);
		String Postresult = this.testrestTemplate.postForObject(
				"http://localhost:" + this.port + "/Geoweaver/web/recent",
				postRequest, String.class);
		assertThat(Postresult).contains("[");
		assertThat(Postresult).contains("id");
		assertThat(Postresult).contains("Done");
		assertThat(Postresult).contains("name");

		ObjectMapper mapper = new ObjectMapper();
		ArrayList<Map> mapped = mapper.readerForListOf(Map.class).readValue(Postresult);
		Map map = mapped.get(0);

		// SDF has a weird bug where if the milliseconds of the time string passed
		// starts with a 0 (e.g. 2022-01-08 17:33:03.[0]39 ) SDF automatically
		// removes the 0 from the string when parsed causing this test to fail.
		// The milliseconds will be removed when asserting to avoid test breaking.
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		// assert start time not empty
		String begin_time = String.valueOf(map.get("begin_time"));
		String[] begin_time_no_ms = begin_time.split("\\.");
		assertNotNull(begin_time_no_ms[0]);
		// assert start time matches format
		Date begin_time_parsed = sdf.parse(begin_time_no_ms[0]);
		String begin_time_format = sdf.format(begin_time_parsed);
		assertEquals(begin_time_format, begin_time_no_ms[0]);

		// assert end time not empty
		String end_time = String.valueOf(map.get("end_time"));
		System.out.println(end_time);
		String[] end_time_no_ms = end_time.split("\\.");
		assertNotNull(end_time_no_ms[0]);

		// assert end time matches format
		Date end_time_parsed = sdf.parse(end_time_no_ms[0]);
		String end_time_format = sdf.format(end_time_parsed);
		assertEquals(end_time_format, end_time_no_ms[0]);

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

		// Delete created host
		String deleteResult = deleteResource(jid, "host");
		assertThat(deleteResult).contains("done");

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

		// Delete created host
		String deleteResult = deleteResource(jid, "host");
		assertThat(deleteResult).contains("done");

	}

    
}
