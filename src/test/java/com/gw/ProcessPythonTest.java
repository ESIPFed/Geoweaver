package com.gw;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;

import java.security.KeyPair;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gw.ssh.RSAEncryptTool;
import com.gw.tools.HistoryTool;
import com.gw.tools.LocalhostTool;
import com.gw.tools.ProcessTool;
import com.gw.tools.UserTool;
import com.gw.utils.BaseTool;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import net.bytebuddy.utility.RandomString;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = GeoweaverApplication.class)
public class ProcessPythonTest extends AbstractHelperMethodsTest {

	@InjectMocks
	@Autowired
	UserTool ut;

	@InjectMocks
	@Autowired
	BaseTool bt;

	@Autowired
	private TestRestTemplate testrestTemplate;

	@LocalServerPort
	private int port;

	@InjectMocks
	@Autowired
	ProcessTool pt;

	@InjectMocks
	@Autowired
	HistoryTool hist;

	

	Logger logger = Logger.getLogger(this.getClass());

	
	@Test
	public void testPythonProcess() throws Exception {
		logger.info("Start to test creating, running, deleting python process..");
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		String pythonjson = bt.readStringFromFile(bt.testResourceFiles() + "/add_python_process.json");
		HttpEntity request = new HttpEntity<>(pythonjson, headers);
		String result = this.testrestTemplate.postForObject(
				"http://localhost:" + this.port + "/Geoweaver/web/add/process",
				request,
				String.class);
		logger.debug("the result is: " + result);
		// assertThat(controller).isNotNull();
		assertThat(result).contains("id");

		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> map = mapper.readValue(result, Map.class);
		String pid = String.valueOf(map.get("id"));

		// get key
		result = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/key",
				null,
				String.class);

		JSONParser jsonParser = new JSONParser();

		JSONObject jsonobj = (JSONObject) jsonParser.parse(result);

		String rsakey = jsonobj.get("rsa_public").toString();

		assertNotNull(rsakey);

		// encode the password

		KeyPair kpair = RSAEncryptTool.buildKeyPair();

		// PublicKey pubkey = KeyFactory.getInstance("RSA")
		// .generatePublic(new X509EncodedKeySpec(rsakey.getBytes()));

		String encryppswd = RSAEncryptTool.byte2Base64(RSAEncryptTool.encrypt(kpair.getPublic(), "testpswd"));

		logger.info(encryppswd);

		// run the python process
		ltmock = Mockito.spy(ltmock);
		doNothing().when(ltmock).authenticate(anyString());

		String historyid = new RandomString(12).nextString();

		ltmock.executePythonProcess(historyid, pid, "100001", encryppswd, historyid, true, "", "", "");

		// check if history is generated
		String resp = pt.one_history(historyid);
		assertThat(resp).contains("\"hid\": \"" + historyid + "\"");

		// remove the history
		hist.deleteById(historyid);
		resp = pt.one_history(historyid);
		assertThat(resp).isEmpty();

		// edit the python process
		pythonjson = bt.readStringFromFile(bt.testResourceFiles() + "/edit_python_process.json");
		pythonjson = pythonjson.replace("<python_process_id>", pid);
		headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		request = new HttpEntity<>(pythonjson, headers);
		result = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/edit/process",
				request,
				String.class);
		logger.debug("the result is: " + result); // {"id" : "beqbtr"}
		assertThat(result).contains("id");

		// remove the python process
		// id=2avx48&type=process
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		request = new HttpEntity<>("id=" + pid + "&type=process", headers);
		result = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/del",
				request,
				String.class);
		logger.debug("the result is: " + result);
		// assertThat(controller).isNotNull();
		assertThat(result).contains("done");

	}

	@Test
	@DisplayName("Test /detail endpoint for Python resource")
	void testPythonDetail() throws Exception {

		// Add process
		String pid = AddPythonProcess();

		HttpHeaders postHeaders = new HttpHeaders();
		postHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		// Get process details
		HttpEntity postRequest = new HttpEntity<>("id=" + pid + "&type=process", postHeaders);
		String Postresult = this.testrestTemplate.postForObject(
				"http://localhost:" + this.port + "/Geoweaver/web/detail",
				postRequest, String.class);
		assertThat(Postresult).contains("id");

		// Delete process
		String deleteResult = deleteResource(pid, "process");
		assertThat(deleteResult).contains("done");

	}

	@Test
	@DisplayName("Test /log endpoints for python process execution")
	void testPythonLog() throws Exception {

		// Add process
		String pid = AddPythonProcess();

		HttpHeaders postHeaders = new HttpHeaders();
		postHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		// Get process logs
		// Execute Python Process
		String phid = ExecutePythonProcess(pid);

		// Stop process
		// stopProcess(phid, "process");

		bt.sleep(10); // sleep to allow time for process execution to finish

		// Get process log
		HttpEntity postRequest = new HttpEntity<>("id=" + phid + "&type=process", postHeaders);
		String Postresult = this.testrestTemplate.postForObject(
				"http://localhost:" + this.port + "/Geoweaver/web/log",
				postRequest, String.class);
		assertThat(Postresult).contains("hid");

		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> map = mapper.readValue(Postresult, Map.class);

		String pehid = String.valueOf(map.get("hid")); // process execution history id
		assertEquals(phid, pehid);

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
		String[] end_time_no_ms = end_time.split("\\.");
		assertNotNull(end_time_no_ms[0]);
		// assert end time matches format
		Date end_time_parsed = sdf.parse(end_time_no_ms[0]);
		String end_time_format = sdf.format(end_time_parsed);
		assertEquals(end_time_format, end_time_no_ms[0]);

		// Delete process
		String deleteResult = deleteResource(pid, "process");
		assertThat(deleteResult).contains("done");

	}

	@Test
	@DisplayName("Test /logs endpoint for python resource")
	void testPythonLogs() throws Exception {

		// Add process
		String pid = AddPythonProcess();

		// Execute Python Process
		String phid = ExecutePythonProcess(pid);

		bt.sleep(10); // sleep to allow time for process execution to finish

		HttpHeaders postHeaders = new HttpHeaders();
		postHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		// Get all process history
		HttpEntity postRequest = new HttpEntity<>("id=" + pid + "&type=process&isactive=false", postHeaders);
		String Postresult = this.testrestTemplate.postForObject(
				"http://localhost:" + this.port + "/Geoweaver/web/logs",
				postRequest, String.class);
		System.out.println("All process hist: " + Postresult);
		assertThat(Postresult).contains("history_id");

		ObjectMapper mapper = new ObjectMapper();
		ArrayList<Map> mapped = mapper.readerForListOf(Map.class).readValue(Postresult);
		Map map = mapped.get(0);

		String pehid = String.valueOf(map.get("history_id")); // process execution hist id
		assertEquals(phid, pehid);

		String peid = String.valueOf(map.get("history_process")); // process id
		assertEquals(pid, peid);

		// Get active process logs
		postRequest = new HttpEntity<>("id=" + pid + "&type=process&isactive=true", postHeaders);
		Postresult = this.testrestTemplate.postForObject(
				"http://localhost:" + this.port + "/Geoweaver/web/logs",
				postRequest, String.class);
		System.out.println("Active process hist: " + Postresult);
		assertThat(Postresult).contains("history_id");

		mapper = new ObjectMapper();
		mapped = mapper.readerForListOf(Map.class).readValue(Postresult);
		map = mapped.get(0);

		pehid = String.valueOf(map.get("history_id"));
		assertEquals(phid, pehid);

		peid = String.valueOf(map.get("history_process"));
		assertEquals(pid, peid);

		// Delete process
		String deleteResult = deleteResource(pid, "process");
		assertThat(deleteResult).contains("done");

	}

	

}
