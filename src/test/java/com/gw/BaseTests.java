package com.gw;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.hamcrest.Matchers.equalTo;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.security.KeyPair;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gw.ssh.RSAEncryptTool;
import com.gw.tools.ExecutionTool;
import com.gw.tools.HistoryTool;
import com.gw.tools.LocalhostTool;
import com.gw.tools.ProcessTool;
import com.gw.tools.UserTool;
import com.gw.tools.WorkflowTool;
import com.gw.utils.BaseTool;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.jupiter.api.BeforeEach;
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


public abstract class BaseTests {

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

	@InjectMocks
	@Autowired
	ProcessTool pt;

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
    public String AddHost() throws Exception {

        HttpHeaders hostHeaders = new HttpHeaders();
		hostHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Add host
		String bultinjson = bt.readStringFromFile(bt.testResourceFiles()+ "/add_ssh_host.txt" );
    	HttpEntity hostRequest = new HttpEntity<>(bultinjson, hostHeaders);
		String hostResult = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/add", 
            hostRequest, 
			String.class);
		assertThat(hostResult).contains("id");

        ObjectMapper mapper = new ObjectMapper();
		Map<String,Object> map = mapper.readValue(hostResult, Map.class);
		String hid = String.valueOf(map.get("id"));

        return hid;

    }

    @Test
    public String AddPythonProcess() throws Exception{
        
        // Add process
        HttpHeaders processHeaders = new HttpHeaders();
        processHeaders.setContentType(MediaType.APPLICATION_JSON);
        String pythonjson = bt.readStringFromFile(bt.testResourceFiles()+ "/add_python_process.json" );
        HttpEntity processRequest = new HttpEntity<>(pythonjson, processHeaders);
        String processResult = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/add/process", 
            processRequest, 
            String.class);
        assertThat(processResult).contains("id");

        ObjectMapper mapper = new ObjectMapper();
        Map<String,Object> map = mapper.readValue(processResult, Map.class);
        String pid = String.valueOf(map.get("id"));

        return pid;

    }

    @Test
    public String ExecutePythonProcess(String pid) throws Exception {
        

        // Get RSA key
        String result = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/key", 
			null, 
			String.class);

		JSONParser jsonParser=new JSONParser();
		JSONObject jsonobj = (JSONObject)jsonParser.parse(result);
		String rsakey = jsonobj.get("rsa_public").toString();
		assertNotNull(rsakey);

		//encode the password
		KeyPair kpair = RSAEncryptTool.buildKeyPair();
		String encryppswd = RSAEncryptTool.byte2Base64(RSAEncryptTool.encrypt(kpair.getPublic(), "testpswd"));
		// run the python process
		ltmock = Mockito.spy(ltmock);
		doNothing().when(ltmock).authenticate(anyString());

		String historyid = new RandomString(12).nextString();

		ltmock.executePythonProcess(historyid, pid, "100001", encryppswd, historyid, true, "", "", "");

        return historyid;
    }

    @Test
    public String AddWorkflow() throws Exception {

        HttpHeaders processHeaders = new HttpHeaders();
        processHeaders.setContentType(MediaType.APPLICATION_JSON);

        // Add workflow
        String workflowjson = bt.readStringFromFile(bt.testResourceFiles()+ "/add_workflow.json" );
        HttpEntity workflowRequest = new HttpEntity<>(workflowjson, processHeaders);
        String workflowResult = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/add/workflow", 
            workflowRequest, 
            String.class);
        assertThat(workflowResult).contains("id");

        ObjectMapper mapper = new ObjectMapper();
        Map<String,Object> map = mapper.readValue(workflowResult, Map.class);
        String wid = String.valueOf(map.get("id"));

        return wid;
    }

    @Test
    public String ExecuteWorkflow(String wid) throws Exception {

        HttpHeaders postHeaders = new HttpHeaders();
		postHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Get RSA key
        String result = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/key", 
        null, 
        String.class);

        JSONParser jsonParser=new JSONParser();
        JSONObject jsonobj = (JSONObject)jsonParser.parse(result);
        String rsakey = jsonobj.get("rsa_public").toString();
        assertNotNull(rsakey);

        //encode the password
        KeyPair kpair = RSAEncryptTool.buildKeyPair();
        String encryppswd = RSAEncryptTool.byte2Base64(RSAEncryptTool.encrypt(kpair.getPublic(), "testpswd"));
        // run the python process
        ltmock = Mockito.spy(ltmock);
        doNothing().when(ltmock).authenticate(anyString());

        String historyid = new RandomString(12).nextString();

        // Execute and Get workflow logs
        String[] hosts = {"22hpox", "22hpox"};
        String[] pswd = {encryppswd,encryppswd};
        String[] envs = {"default_option", "default_option"};
        wtmock.execute(historyid, wid, "1", hosts, pswd, envs, "");

        return historyid;

    }


    public void deleteResource(String id, String type) throws Exception {

        HttpHeaders deleteHeaders = new HttpHeaders();
		deleteHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    	HttpEntity deleteRequest = new HttpEntity<>("id="+id+"&type="+type, deleteHeaders);
		String deleteResult = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/del", 
            deleteRequest, 
			String.class);
		assertThat(deleteResult).contains("done");
        

    }
    
}
