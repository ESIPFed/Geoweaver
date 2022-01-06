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

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import java.security.KeyPair;

import net.bytebuddy.utility.RandomString;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gw.jpa.GWUser;
import com.gw.tools.UserTool;
import com.gw.tools.WorkflowTool;
import com.gw.utils.BaseTool;
import com.gw.tools.ExecutionTool;
import com.gw.tools.HistoryTool;
import com.gw.tools.LocalhostTool;
import com.gw.tools.ProcessTool;
import com.gw.tools.UserSession;
import com.gw.tools.FileTool;
import com.gw.tools.HostTool;
import com.gw.ssh.RSAEncryptTool;
import com.gw.web.GeoweaverController;
import org.json.simple.parser.JSONParser;
import org.json.simple.JSONObject;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;
import org.springframework.stereotype.Service;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ApiTests extends BaseTests {

        
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
	HistoryTool hist;

    @InjectMocks
	@Autowired
	ProcessTool pt;

	@InjectMocks
	@Autowired
	LocalhostTool ltmock;

	@InjectMocks
	@Autowired
	FileTool ftmock;

    @InjectMocks
	@Autowired
    HostTool htmock;

    @InjectMocks
	@Autowired
    UserTool utmock;

    @InjectMocks
	@Autowired
	WorkflowTool wtmock;

    Logger logger  = Logger.getLogger(this.getClass());

    @Test
	@DisplayName("Test /delAllHistory endpoint")
	void testDelAllHistory() throws Exception{

        // Add Host
        String hid = AddHost();


        Map<String, String> body = new HashMap<>();
        body.put("hid", hid);

        HttpHeaders postHeaders = new HttpHeaders();
		postHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity bodyData = new HttpEntity<>(body, postHeaders);
        String Postresult = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/delAllHistory", 
        bodyData, String.class);
        assertThat(Postresult).contains("removed_history_ids");


        // Delete created Host
        deleteResource(hid, "host");

    }

    @Test
	@DisplayName("Test /delNoNotesHistory endpoint")
	void testDelNoNotesHistory() throws Exception{
        
        
        // Add Host
        String hid = AddHost();
        
        
        Map<String, String> body = new HashMap<>();
        body.put("hid", hid);
        
        HttpHeaders postHeaders = new HttpHeaders();
		postHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity bodyData = new HttpEntity<>(body, postHeaders);
        String Postresult = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/delNoNotesHistory", 
        bodyData, String.class);
        assertThat(Postresult).contains("removed_history_ids");
        
        
        // Delete created Host
        deleteResource(hid, "host");

    }

    @Test
	@DisplayName("Test /detail endpoint")
	void testDetail() throws Exception{

        // Add Host
        String hid = AddHost();

        // Add process
        String pid = AddPythonProcess();

        // Add workflow
        String wid = AddWorkflow();


        // Get host details
        HttpHeaders postHeaders = new HttpHeaders();
		postHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity postRequest = new HttpEntity<>("id="+hid+"&type=host", postHeaders);
        String Postresult = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/detail", 
        postRequest, String.class);
        assertThat(Postresult).contains("id");

        // Get process details
        postRequest = new HttpEntity<>("id="+pid+"&type=process", postHeaders);
        Postresult = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/detail", 
        postRequest, String.class);
        assertThat(Postresult).contains("id");

        // Get workflow details
        postRequest = new HttpEntity<>("id="+wid+"&type=workflow", postHeaders);
        Postresult = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/detail", 
        postRequest, String.class);
        assertThat(Postresult).contains("id");


        // Delete created Host
        deleteResource(hid, "host");

        // Delete process
        deleteResource(pid, "process");

        // Delete workflow
        deleteResource(wid, "workflow");
    }

    @Test
	@DisplayName("Test /reset_password endpoint")
	void testResetPassword() throws Exception{
        /* 
        This test could be improved by passing an exsisting email
        to then get correct token and pass to this endpoint.
        As of now, the endpoint triggers and respondes with a 200.  */

        HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String token = new RandomString(12).nextString();

		ResponseEntity<String> result = this.testrestTemplate.getForEntity("http://localhost:" + this.port + "/Geoweaver/user/reset_password?token="+token, 
			String.class);
        logger.debug("/reset_password result: "+result);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);

        
    }

    @Test
	@DisplayName("Test /recent endpoint")
	void testRecentHistory() throws Exception{

        HttpHeaders postHeaders = new HttpHeaders();
		postHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Add Host
        String hid = AddHost();

        // Get host recent history
        HttpEntity postRequest = new HttpEntity<>("id="+hid+"&type=host&number=20", postHeaders);
        String Postresult = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/recent", 
        postRequest, String.class);
        assertThat(Postresult).contains("[");

        // Get process recent history
        postRequest = new HttpEntity<>("type=process&number=20", postHeaders);
        Postresult = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/recent", 
        postRequest, String.class);
        assertThat(Postresult).contains("[");

        // Get workflow recent history
        postRequest = new HttpEntity<>("type=workflow&number=20", postHeaders);
        Postresult = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/recent", 
        postRequest, String.class);
        assertThat(Postresult).contains("[");


        // Delete created Host
        deleteResource(hid, "host");

    }

    @Test
	@DisplayName("Test /downloadworkflow endpoint")
	void testDownloadWorkflow() throws Exception{
        /* 
        This test case executes correctly and returns a path,
         but also triggers a
        java.util.NoSuchElementException: No value present
        at com.gw.tools.ProcessTool.getProcessById(ProcessTool.java:147)
        */
        

        // Add workflow
        String wid = AddWorkflow();



        // Download workflow
        HttpHeaders postHeaders = new HttpHeaders();
		postHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity postRequest = new HttpEntity<>("id="+wid+"&option=workflowwithprocesscodehistory", postHeaders);
        String Postresult = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/downloadworkflow", 
        postRequest, String.class);
        logger.debug("Workflow download result: "+ Postresult);
        // assertThat( Postresult.contains("["));


        // Delete workflow
        deleteResource(wid, "workflow");

    }

    @Test
	@DisplayName("Test /log and /workflow_process_log endpoints")
	void testLog() throws Exception{


        // Add Host
        String hid = AddHost();

        // Add process
        String pid = AddPythonProcess();

        // Add workflow
        String wid = AddWorkflow();




        HttpHeaders postHeaders = new HttpHeaders();
		postHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Get host logs
        // HttpEntity postRequest = new HttpEntity<>("id="+hid+"&type=host", postHeaders);
        // String Postresult = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/log", 
        // postRequest, String.class);
        // logger.debug("Host logs: "+Postresult);
        // assertThat( Postresult.contains("id"));

        // Execute and Get process logs
               
        // Execute Python Process
        String phid = ExecutePythonProcess(pid);

        // Get process log
        HttpEntity postRequest = new HttpEntity<>("id="+phid+"&type=process", postHeaders);
        String Postresult = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/log", 
        postRequest, String.class);
        assertThat(Postresult).contains("hid");



        // Execute and Get workflow logs

        // Execute workflow
        String whid = ExecuteWorkflow(wid);

        // Get workflow logs from /log
        postRequest = new HttpEntity<>("id="+whid+"&type=workflow", postHeaders);
        Postresult = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/log", 
        postRequest, String.class);
        assertThat(Postresult).contains("hid");

        // Get workflow logs from /workflow_process_log
        // No response is returned!! 
        // @TODO
        postRequest = new HttpEntity<>("workflowhistoryid="+whid+"&processid="+wid, postHeaders);
        Postresult = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/workflow_process_log", 
        postRequest, String.class);
        logger.debug("/workflow_process_log result: "+ Postresult);
        // assertThat(Postresult).contains("hid");

        

        // Delete created Host
        deleteResource(hid, "host");

        // Delete process
        deleteResource(pid, "process");

        // Delete workflow
        deleteResource(wid, "workflow");

    }

    @Test
	@DisplayName("Test /logs endpoint")
	void testLogs() throws Exception{

        // Add process
        String pid = AddPythonProcess();
        

        // Execute Python Process
        String phid = ExecutePythonProcess(pid);


        
        HttpHeaders postHeaders = new HttpHeaders();
		postHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Get all process history
        HttpEntity postRequest = new HttpEntity<>("id="+pid+"&type=process&isactive=false", postHeaders);
        String Postresult = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/logs", 
        postRequest, String.class);
        assertThat(Postresult).contains("[");

        // Get active process logs
        postRequest = new HttpEntity<>("id="+pid+"&type=process&isactive=true", postHeaders);
        Postresult = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/logs", 
        postRequest, String.class);
        assertThat(Postresult).contains("[");

        // Add workflow
        String wid = AddWorkflow();


        // Execute workflow
        String whid = ExecuteWorkflow(wid);

        // Get all workflow history
        postRequest = new HttpEntity<>("id="+wid+"&type=workflow&isactive=false", postHeaders);
        Postresult = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/logs", 
        postRequest, String.class);
        assertThat(Postresult).contains("[");

        // Get active workflow logs
        postRequest = new HttpEntity<>("id="+wid+"&type=workflow&isactive=true", postHeaders);
        Postresult = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/logs", 
        postRequest, String.class);
        assertThat(Postresult).contains("[");

    }

    @Test
	@DisplayName("Test /env endpoint")
	void testHostEnv() throws Exception{

        // Add Host
        String hid = AddHost();

        HttpHeaders postHeaders = new HttpHeaders();
		postHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Get host enviroments
        HttpEntity postRequest = new HttpEntity<>("id="+hid, postHeaders);
        String Postresult = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/env", 
        postRequest, String.class);
        assertThat(Postresult).contains("[");

        deleteResource(hid, "host");
    }

    @Test
	@DisplayName("Test /listhostwithenvironments endpoint")
	void testListHostWithEnvironments() throws Exception{

        HttpHeaders postHeaders = new HttpHeaders();
		postHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Get host enviroments
        // HttpEntity postRequest = new HttpEntity<>("id="+hid, postHeaders);
        String Postresult = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/listhostwithenvironments", 
        null, String.class);
        assertThat(Postresult).contains("[");

    }

    @Test
	@DisplayName("Test /checkLiveSession endpoint")
	void testCheckLiveSession() throws Exception{

        // Add host
        String hid = AddHost();

        HttpHeaders postHeaders = new HttpHeaders();
		postHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Get Live session of host
        HttpEntity postRequest = new HttpEntity<>("hostId="+hid, postHeaders);
        String Postresult = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/checkLiveSession", 
        postRequest, String.class);
        logger.debug("/checkLiveSession results: "+Postresult);
        assertThat(Postresult).contains("exist");

        deleteResource(hid, "host");
    }

    // @Test
	@DisplayName("Test /retrievefile endpoint")
	void testRetrieveFile() throws Exception{
        /* 
        This test requires ssh into remote server.
        Test fails.
        */


        String testFilePath = bt.testResourceFiles()+ "/add_ssh_host.txt";

        HttpHeaders postHeaders = new HttpHeaders();
		postHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Get file
        HttpEntity postRequest = new HttpEntity<>("filepath="+testFilePath, postHeaders);
        String Postresult = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/retrievefile", 
        postRequest, String.class);
        logger.debug("/retrievefile results: "+Postresult);
        assertThat(Postresult).contains("success");
    }

    // @Test
	@DisplayName("Test /download/{tempfolder}/{filename} endpoint")
	void testDownloadToTemp() throws Exception{
        /* 
        Endpoint responded correctly, 
        but since no remote hosts can be used, no files are downloaded.
        endpoint returns HTTP 404
        */

        String tempFolder = bt.getFileTransferFolder();
        String fileName = bt.testResourceFiles()+ "/add_ssh_host.txt";

        HttpHeaders postHeaders = new HttpHeaders();
		postHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // download file
        // HttpEntity postRequest = new HttpEntity<>("filepath="+testFilePath, postHeaders);
        String Postresult = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/download/"+tempFolder+"/"+fileName, 
        null, String.class);
        logger.debug("/download/{tempfolder}/{filename} results: "+Postresult);
        assertThat(Postresult).contains("200");

    }

    // @Test
	@DisplayName("Test /updatefile endpoint")
	void testUpdateFile() throws Exception{
        /* 
        Endpoint responded correctly, 
        but since no remote hosts can be used, no files are updated.
        */

        String testFilePath = bt.testResourceFiles()+ "/test1.txt";
        String content = "Test from /updatefile";

        HttpHeaders postHeaders = new HttpHeaders();
		postHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // update file
        HttpEntity postRequest = new HttpEntity<>("filepath="+testFilePath+"&content="+content, postHeaders);
        String Postresult = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/updatefile", 
        postRequest, String.class);
        logger.debug("/updatefile results: "+Postresult);
        assertThat(Postresult).contains("success");

    }

    // @Test
	@DisplayName("Test /readEnvironment endpoint")
	void testReadEnvironment() throws Exception{
        /* 
        Endpoint requires ssh into remote host to read enviroments.
        Test cannot be executed.
        */

        // Add Host
        String hid = AddHost();

        String pswd = "testpswd";

        HttpHeaders postHeaders = new HttpHeaders();
		postHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Get Live session of host
        HttpEntity postRequest = new HttpEntity<>("hostid="+hid+"&pswd="+pswd, postHeaders);
        String Postresult = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/readEnvironment", 
        postRequest, String.class);
        logger.debug("/readEnvironment results: "+Postresult);
        assertThat(Postresult).contains("success");



    }

    @Test
	@DisplayName("Test /addLocalFile endpoint")
	void testAddLocalFile() throws Exception{

        String filePath= bt.testResourceFiles()+ "/add_ssh_host.txt";
        
        String hid = AddHost();
        
        String type = "process";
			
        String content = "test123";
        
        String name = "testFile.txt";

        String ownerid = "111111";

        String confidential = "FALSE";

        String postParams = "filepath="+filePath+"&hid="+hid+"&type="+type+"&content="+content+"&name="+name+"&ownerid="+ownerid+"&confidential="+confidential;

        HttpHeaders postHeaders = new HttpHeaders();
		postHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Add file to DB
        HttpEntity postRequest = new HttpEntity<>(postParams, postHeaders);
        String Postresult = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/addLocalFile", 
        postRequest, String.class);
        logger.debug("/addLocalFile results: "+Postresult);
        assertThat(Postresult).contains("id");


    }

    @Test
	@DisplayName("Test /preload/workflow & /load/workflow endpoints")
	void testPreloadandLoadWorkflow() throws Exception{
        
        // Copy workflow zip folder to temp folder for endpoint to pick up.
        Path toCopy = new File(bt.testResourceFiles()+ "/exportedWorkflow.zip").toPath();
        Path target = new File(bt.getFileTransferFolder()+ "/exportedWorkflow.zip").toPath();
        Files.copy(toCopy, target, StandardCopyOption.REPLACE_EXISTING);

        HttpHeaders postHeaders = new HttpHeaders();
		postHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Preload Zip file
        HttpEntity postRequest = new HttpEntity<>("filename=exportedWorkflow.zip", postHeaders);
        String Postresult = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/preload/workflow", 
        postRequest, String.class);
        logger.debug("/preload/workflow results: "+Postresult);
        assertThat(Postresult).contains("id");

        String wid = AddWorkflow();

        // Get previous test's unzipped folder name
        String foldername = "exportedWorkflow.zip";

        postHeaders = new HttpHeaders();
		postHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Load workflow folder
        postRequest = new HttpEntity<>("id="+wid+"&filename="+foldername, postHeaders);
        Postresult = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/load/workflow", 
        postRequest, String.class);
        logger.debug("/load/workflow results: "+Postresult);
        assertThat(Postresult).contains("id");

        deleteResource(wid, "workflow");

    }


    // @Test
	@DisplayName("Test /openfilebrowse endpoint")
	void testOpenFileBrowser() throws Exception{

        HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Add host
        String hid = AddHost();


        //get RSA key 
        String result = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/key", 
        null, 
        String.class);

        JSONParser jsonParser=new JSONParser();

        JSONObject jsonobj = (JSONObject)jsonParser.parse(result);

        String rsakey = jsonobj.get("rsa_public").toString();
        logger.debug("RSA KEY: "+ rsakey);
        assertNotNull(rsakey);

        bt.setLocalhostPassword("password", false);

        KeyPair kpair = RSAEncryptTool.buildKeyPair();
		String encryppswd = RSAEncryptTool.byte2Base64(RSAEncryptTool.encrypt(kpair.getPublic(), "password"));


        Map<String, String> body = new HashMap<>();
        body.put("hid", hid);
        body.put("pswd", encryppswd);
        body.put("init_path", "/home/");

        HttpHeaders postHeaders = new HttpHeaders();
		postHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity bodyData = new HttpEntity<>(body, postHeaders);
        String Postresult = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/openfilebrowser", 
        bodyData, String.class);
        

        logger.debug("Post result: "+ Postresult);
        assertThat( Postresult.contains("success"));


        // Delete created Host
        deleteResource(hid, "host");

    }




    
}
