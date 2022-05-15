package com.gw;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gw.database.UserRepository;
import com.gw.jpa.GWUser;
import com.gw.tools.ExecutionTool;
import com.gw.tools.UserTool;
import com.gw.utils.BaseTool;

import org.apache.log4j.Logger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = GeoweaverApplication.class)
public class ProcessGeneralTest extends AbstractHelperMethodsTest{

    @LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate testrestTemplate;

	Logger logger = Logger.getLogger(this.getClass());

	@Autowired
	UserTool ut;

	@Autowired
	BaseTool bt;

	@Autowired
	ExecutionTool et;

    @Test
    void testExecuteProcessAPI(){

        HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        String runpythonjson = bt.readStringFromFile(bt.testResourceFiles() + "/run_python_process.json");
		HttpEntity request = new HttpEntity<>(runpythonjson, headers);
		String result = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/executeProcess",
				request,
				String.class);
		logger.debug("the result is: " + result);
		// assertThat(controller).isNotNull();
		assertThat(result).contains("Internal Server Error"); //this should fail as rsa string is wrong


    }

    @Test
    void testEditProcess() throws Exception{

        String pid = AddPythonProcess();

        HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

        String edithostjson = bt.readStringFromFile(bt.testResourceFiles() + "/edit_python_process.json");
        edithostjson = edithostjson.replace("testpython2", "testpython3")
								   .replace("<python_process_id>", pid);

		HttpEntity request = new HttpEntity<>(edithostjson, headers);
		String result = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/edit/process",
				request,
				String.class);
		
		logger.debug("the result is: " + result);
		assertThat(result).contains(pid);


    }

	@Test
	void testStopProcess() throws Exception{

		String pid = AddPythonProcess();

		String histid = ExecutePythonProcess(pid);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		HttpEntity request = new HttpEntity<>("type=process&id="+histid, headers);
		String result = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/stop",
				request,
				String.class);
		logger.debug("the result is: " + result);
		assertThat(result).contains("stopped");
	}

	@Test
	void directTestExecutionTool(){

		// et.executeProcess(history_id, id, hid, pswd, httpsessionid, isjoin, bin, pyenv, basedir)

	}
    
}
