package com.gw;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import net.bytebuddy.utility.RandomString;

import java.lang.reflect.Array;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.security.KeyPair;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;



import com.fasterxml.jackson.databind.ObjectMapper;
import com.gw.jpa.GWUser;
import com.gw.tools.UserTool;
import com.gw.utils.BaseTool;
import com.gw.tools.ExecutionTool;
import com.gw.tools.HistoryTool;
import com.gw.tools.LocalhostTool;
import com.gw.tools.WorkflowTool;
import com.gw.ssh.RSAEncryptTool;
import org.json.simple.parser.JSONParser;
import org.json.simple.JSONObject;


import org.apache.log4j.Logger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WorkflowTest {
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
	ExecutionTool et;

	// @InjectMocks
	// @Autowired
	// ProcessTool pt;
	
    @InjectMocks
	@Autowired
	HistoryTool hist;

	@InjectMocks
	@Autowired
	LocalhostTool ltmock;

	@InjectMocks
	@Autowired
	WorkflowTool wtmock;

    Logger logger  = Logger.getLogger(this.getClass());


	@Test
	String testResourceFiles(){

		Path resourceDirectory = Paths.get("src","test","resources");
		String absolutePath = resourceDirectory.toFile().getAbsolutePath();

		logger.debug(absolutePath);
		assertTrue(absolutePath.contains("resources"));
		return absolutePath;
	}

    @Test
	@DisplayName("Adding workflow and testing execution...")
	void testWorkflow() throws Exception {
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

        // Add test workflow
		String workflowjson = bt.readStringFromFile(this.testResourceFiles()+ "/add_workflow.json" );
    	HttpEntity request = new HttpEntity<>(workflowjson, headers);
		String result = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/add/workflow", 
			request, 
			String.class);
		logger.debug("Addedd Workflow: " + result);
		assertThat(result).contains("id");

		ObjectMapper mapper = new ObjectMapper();
		Map<String,Object> map = mapper.readValue(result, Map.class);

		String pid = String.valueOf(map.get("id"));
		assertNotNull(pid);


		
		//Get RSA key
		result = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/key", 
        null, String.class);

        JSONParser jsonParser=new JSONParser();

        JSONObject jsonobj;

        jsonobj = (JSONObject)jsonParser.parse(result);
        String rsakey = jsonobj.get("rsa_public").toString();
        logger.debug("RSA KEY: "+ rsakey);
        assertNotNull(rsakey);
        
		//Encode the password
		KeyPair kpair = RSAEncryptTool.buildKeyPair();
		String encryppswd = RSAEncryptTool.byte2Base64(RSAEncryptTool.encrypt(kpair.getPublic(), "testpswd"));
        logger.debug("Encrypted Password: "+ encryppswd);

		
		// Execute workflow
		ltmock = Mockito.spy(ltmock);
		doNothing().when(ltmock).authenticate(anyString());
		String historyid = new RandomString(12).nextString();
		
		String[] hosts = {"22hpox", "22hpox"};
		String[] pswd = {encryppswd,encryppswd};
		String[] envs = {"default_option", "default_option"};
		wtmock.execute(historyid, pid, "1", hosts, pswd, envs, "");

		// check if history is generated
		String resp = wtmock.one_history(historyid);
		logger.debug("WORKFLOW EXECUTION HISTORY: "+resp);
		assertThat(resp).contains("\"hid\": \""+historyid+"\"");


		// remove the history
		hist.deleteById(historyid);
		resp = wtmock.one_history(historyid); // This gives back a "NoSuchElementException: No value present", which should be correct given the history was deleted?
		assertThat(resp).isEmpty();



		
		//Remove Workflow
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    	HttpEntity DeleteRequest = new HttpEntity<>("id="+map.get("id")+"&type=workflow", headers);
		String DeleteResult = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/del", 
            DeleteRequest, 
			String.class);
		logger.debug("the result is: " + DeleteResult);
		assertThat(DeleteResult).contains("done");

	}
}
