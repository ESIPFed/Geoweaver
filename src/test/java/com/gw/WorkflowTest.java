package com.gw;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.KeyPair;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gw.ssh.RSAEncryptTool;
import com.gw.tools.ExecutionTool;
import com.gw.tools.HistoryTool;
import com.gw.tools.LocalhostTool;
import com.gw.tools.UserTool;
import com.gw.tools.WorkflowTool;
import com.gw.utils.BaseTool;

import org.apache.log4j.Logger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import net.bytebuddy.utility.RandomString;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = GeoweaverApplication.class)
public class WorkflowTest extends AbstractHelperMethodsTest {
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

    @Autowired
    BuildProperties buildProperties;

    @InjectMocks
    @Autowired
    HistoryTool hist;

    @InjectMocks
    @Autowired
    LocalhostTool ltmock;

    @InjectMocks
    @Autowired
    WorkflowTool wtmock;

    Logger logger = Logger.getLogger(this.getClass());

    @Test
    String testResourceFiles() {
        Path resourceDirectory = Paths.get("src", "test", "resources");
        String absolutePath = resourceDirectory.toFile().getAbsolutePath();

        System.out.println(absolutePath);
        assertTrue(absolutePath.contains("resources"));
        return absolutePath;
    }

    public String AddWorkflow() throws Exception {

        HttpHeaders processHeaders = new HttpHeaders();
        processHeaders.setContentType(MediaType.APPLICATION_JSON);

        // Add workflow
        String workflowjson = bt.readStringFromFile(bt.testResourceFiles() + "/add_workflow.json");
        HttpEntity workflowRequest = new HttpEntity<>(workflowjson, processHeaders);
        String workflowResult = this.testrestTemplate.postForObject(
                "http://localhost:" + this.port + "/Geoweaver/web/add/workflow",
                workflowRequest,
                String.class);

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = mapper.readValue(workflowResult, Map.class);
        String wid = String.valueOf(map.get("id"));

        return wid;
    }

    public String ExecuteWorkflow(String wid) throws Exception {

        HttpHeaders postHeaders = new HttpHeaders();
        postHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // encode the password
        KeyPair kpair = RSAEncryptTool.buildKeyPair();
        String encryppswd = RSAEncryptTool.byte2Base64(RSAEncryptTool.encrypt(kpair.getPublic(), "testpswd"));
        // run the python process
        ltmock = Mockito.spy(ltmock);
        doNothing().when(ltmock).authenticate(anyString());

        String historyid = new RandomString(12).nextString();

        // Execute and Get workflow logs
        String[] hosts = { "22hpox", "22hpox" };
        String[] pswd = { encryppswd, encryppswd };
        String[] envs = { "default_option", "default_option" };

        wtmock.execute(historyid, wid, "1", hosts, pswd, envs, "");

        return historyid;

    }

    @Test
    @DisplayName("Adding workflow and testing execution...")
    void testWorkflow() throws Exception {

        // Add workflow
        String wid = AddWorkflow();

        // Execute Workflow
        String whid = ExecuteWorkflow(wid);

        // check if history is generated
        String resp = wtmock.one_history(whid);
        assertThat(resp).contains("\"hid\": \"" + whid + "\"");

        // remove the history
        hist.deleteById(whid);
        resp = wtmock.one_history(whid); // This gives back a "NoSuchElementException: No value present", which should
                                         // be correct given the history was deleted
        assertThat(resp).isEmpty();

        // Remove Workflow
        String deleteResult = deleteResource(wid, "workflow");
        assertThat(deleteResult).contains("done");

    }

    @Test
    void testEditWorkflow() throws Exception{

        String wid = AddWorkflow();

        HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

        String editworkflowjson = bt.readStringFromFile(bt.testResourceFiles() + "/edit_workflow.json");
        editworkflowjson = editworkflowjson.replace("t2", "newworkflowname")
                                            .replace("<wid>", wid);

		HttpEntity request = new HttpEntity<>(editworkflowjson, headers);
		String result = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/edit/workflow",
				request,
				String.class);
		
		logger.debug("the result is: " + result);
		assertThat(result).contains(wid);

    }

    @Test
    @DisplayName("Test /downloadworkflow endpoint")
    void testDownloadWorkflow() throws Exception {
        /*
         * This test case executes correctly and returns a path,
         * but also triggers a
         * java.util.NoSuchElementException: No value present
         * at com.gw.tools.ProcessTool.getProcessById(ProcessTool.java:147)
         * This error isn't breaking the test.
         */

        // Add workflow
        String wid = AddWorkflow();

        // Download workflow
        HttpHeaders postHeaders = new HttpHeaders();
        postHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity postRequest = new HttpEntity<>("id=" + wid + "&option=workflowwithprocesscodehistory", postHeaders);
        String Postresult = this.testrestTemplate.postForObject(
                "http://localhost:" + this.port + "/Geoweaver/web/downloadworkflow",
                postRequest, String.class);

        // remove unnecesary path from result
        Postresult = Postresult.replaceAll("download/temp/", "");

        // Check if zip file exists
        String zipPath = bt.getFileTransferFolder() + Postresult;
        boolean check = new File(zipPath).exists();
        assertTrue(check);

        // No need to unzip, endpoint unzips folder in target dir automatically
        // Check unzipped folder exists
        String folderPath = bt.getFileTransferFolder() + Postresult.replaceAll(".zip", "");
        check = new File(folderPath).exists();
        assertTrue(check);

        // Check if "code" subdir exists in downloaded unzipped folder
        check = new File(folderPath + "/code").exists();
        assertTrue(check);

        // Check if "workflow.json" file exists in downloaded unzipped folder
        check = new File(folderPath + "/workflow.json").exists();
        assertTrue(check);

        // check if created workflow id matches id value in workflow.json file
        String workflowjson = bt.readStringFromFile(folderPath + "/workflow.json");
        assertThat(workflowjson).contains(wid);

        // Delete workflow from DB
        String deleteResult = deleteResource(wid, "workflow");
        assertThat(deleteResult).contains("done");

    }

    @Test
    @DisplayName("Test /preload/workflow & /load/workflow endpoints")
    void testPreloadandLoadWorkflow() throws Exception {

        // Copy workflow zip folder to temp folder for endpoint to pick up.
        Path toCopy = new File(bt.testResourceFiles() + "/exportedWorkflow.zip").toPath();
        Path target = new File(bt.getFileTransferFolder() + "/exportedWorkflow.zip").toPath();
        Files.copy(toCopy, target, StandardCopyOption.REPLACE_EXISTING);

        HttpHeaders postHeaders = new HttpHeaders();
        postHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Preload Zip file
        HttpEntity postRequest = new HttpEntity<>("filename=exportedWorkflow.zip", postHeaders);
        String Postresult = this.testrestTemplate.postForObject(
                "http://localhost:" + this.port + "/Geoweaver/web/preload/workflow",
                postRequest, String.class);
        assertThat(Postresult).contains("id");

        String wid = AddWorkflow();

        // Get test resourcess workflow zipped folder name
        String foldername = "exportedWorkflow.zip";

        postHeaders = new HttpHeaders();
        postHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Load workflow folder to backend
        postRequest = new HttpEntity<>("id=" + wid + "&filename=" + foldername, postHeaders);
        Postresult = this.testrestTemplate.postForObject(
                "http://localhost:" + this.port + "/Geoweaver/web/load/workflow",
                postRequest, String.class);
        assertThat(Postresult).contains("id");

        String deleteResult = deleteResource(wid, "workflow");
        assertThat(deleteResult).contains("done");

    }

    @Test
    @DisplayName("Test /preload/workflow & /load/workflow endpoints with invalid file")
    void testPreloadWorkflowWithWrongInput() throws Exception {
        
         // Copy workflow zip folder to temp folder for endpoint to pick up.
         Path toCopy = new File(bt.testResourceFiles() + "/exportedWorkflow-invalid.zip").toPath();
         Path target = new File(bt.getFileTransferFolder() + "/exportedWorkflow-invalid.zip").toPath();
         Files.copy(toCopy, target, StandardCopyOption.REPLACE_EXISTING);
 
         HttpHeaders postHeaders = new HttpHeaders();
         postHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
 
         // Preload Zip file
         HttpEntity postRequest = new HttpEntity<>("filename=exportedWorkflow-invalid.zip", postHeaders);
         String Postresult = this.testrestTemplate.postForObject(
                 "http://localhost:" + this.port + "/Geoweaver/web/preload/workflow",
                 postRequest, String.class);
         assertThat(Postresult).contains("error");
 
         String wid = AddWorkflow();
 
         // Get test resourcess workflow zipped folder name
         String foldername = "exportedWorkflow-invalid.zip";
 
         postHeaders = new HttpHeaders();
         postHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
 
         // Load workflow folder to backend
         postRequest = new HttpEntity<>("id=" + wid + "&filename=" + foldername, postHeaders);
         Postresult = this.testrestTemplate.postForObject(
                 "http://localhost:" + this.port + "/Geoweaver/web/load/workflow",
                 postRequest, String.class);
         assertThat(Postresult).contains("error");
 
         String deleteResult = deleteResource(wid, "workflow");
         assertThat(deleteResult).contains("done");
 
    }


    @Test
    @DisplayName("Test /preload/workflow & /load/workflow endpoints with valid file but with mismatched zip and folder names")
    void testPreloadandLoadWorkflowWithMismatchedZipAndFolderName() throws Exception {
        
        // Copy workflow zip folder to temp folder for endpoint to pick up.
        Path toCopy = new File(bt.testResourceFiles() + "/exportedMismatchWorkflowName.zip").toPath();
        Path target = new File(bt.getFileTransferFolder() + "/exportedMismatchWorkflowName.zip").toPath();
        Files.copy(toCopy, target, StandardCopyOption.REPLACE_EXISTING);

        HttpHeaders postHeaders = new HttpHeaders();
        postHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Preload Zip file
        HttpEntity postRequest = new HttpEntity<>("filename=exportedMismatchWorkflowName.zip", postHeaders);
        String Postresult = this.testrestTemplate.postForObject(
                "http://localhost:" + this.port + "/Geoweaver/web/preload/workflow",
                postRequest, String.class);
        assertThat(Postresult).contains("id");

        String wid = AddWorkflow();

        // Get test resourcess workflow zipped folder name
        String foldername = "exportedMismatchWorkflowName.zip";

        postHeaders = new HttpHeaders();
        postHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Load workflow folder to backend
        postRequest = new HttpEntity<>("id=" + wid + "&filename=" + foldername, postHeaders);
        Postresult = this.testrestTemplate.postForObject(
                "http://localhost:" + this.port + "/Geoweaver/web/load/workflow",
                postRequest, String.class);
        assertThat(Postresult).contains("id");

        String deleteResult = deleteResource(wid, "workflow");
        assertThat(deleteResult).contains("done");

    }

    @Test
    @DisplayName("Test /detail endpoint for workflow resource")
    void testWorkflowDetail() throws Exception {

        // Add workflow
        String wid = AddWorkflow();

        HttpHeaders postHeaders = new HttpHeaders();
        postHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Get workflow details
        HttpEntity postRequest = new HttpEntity<>("id=" + wid + "&type=workflow", postHeaders);
        String Postresult = this.testrestTemplate.postForObject(
                "http://localhost:" + this.port + "/Geoweaver/web/detail",
                postRequest, String.class);
        assertThat(Postresult).contains("id");

        // Delete workflow
        String deleteResult = deleteResource(wid, "workflow");
        assertThat(deleteResult).contains("done");
    }

    @Test
    @DisplayName("Test /log and /workflow_process_log endpoints for workflow execution")
    void testWorkflowLog() throws Exception {

        // Add workflow
        String wid = AddWorkflow();

        HttpHeaders postHeaders = new HttpHeaders();
        postHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Execute and Get workflow logs
        // Execute workflow
        String whid = ExecuteWorkflow(wid);

        bt.sleep(5); // sleep to allow time for process execution to finish

        // Get workflow logs from /log
        HttpEntity postRequest = new HttpEntity<>("id=" + whid + "&type=workflow", postHeaders);
        String Postresult = this.testrestTemplate.postForObject(
                "http://localhost:" + this.port + "/Geoweaver/web/log",
                postRequest, String.class);
        assertThat(Postresult).contains("hid");

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = mapper.readValue(Postresult, Map.class);

        String wehid = String.valueOf(map.get("hid")); // workflow execution history id
        assertEquals(whid, wehid);

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

        // Get workflow logs from /workflow_process_log
        String nodeProcessIds = String.valueOf(map.get("input"));
        nodeProcessIds = nodeProcessIds.trim();
        String[] nodeIds = nodeProcessIds.substring(1, nodeProcessIds.length() - 1).trim().split("\\s*,\\s*");

        postRequest = new HttpEntity<>("workflowhistoryid=" + whid + "&processid=" + nodeIds[0], postHeaders);
        Postresult = this.testrestTemplate.postForObject(
                "http://localhost:" + this.port + "/Geoweaver/web/workflow_process_log",
                postRequest, String.class);
        assertThat(Postresult).contains("history_id");

        String processNodeId = nodeIds[0].split("-")[0];
        assertThat(Postresult).contains(processNodeId);

        // Delete workflow
        String deleteResult = deleteResource(wid, "workflow");
        assertThat(deleteResult).contains("done");

    }

    @Test
    @DisplayName("Test /logs endpoint for workflow resource")
    void testWorkflowLogs() throws Exception {

        HttpHeaders postHeaders = new HttpHeaders();
        postHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Add workflow
        String wid = AddWorkflow();

        // Execute workflow
        String whid = ExecuteWorkflow(wid);

        bt.sleep(5); // sleep to allow time for process execution to finish

        // Get all workflow history
        HttpEntity postRequest = new HttpEntity<>("id=" + wid + "&type=workflow&isactive=false", postHeaders);
        String Postresult = this.testrestTemplate.postForObject(
                "http://localhost:" + this.port + "/Geoweaver/web/logs",
                postRequest, String.class);
        assertThat(Postresult).contains("history_input");

        ObjectMapper mapper = new ObjectMapper();
        ArrayList<Map> mapped = mapper.readerForListOf(Map.class).readValue(Postresult);
        Map map = mapped.get(0);

        String wehid = String.valueOf(map.get("history_id")); // workflow execution hist id
        assertEquals(whid, wehid);

        String weid = String.valueOf(map.get("history_process")); // workflow id
        assertEquals(wid, weid);

        // Get active workflow logs
        postRequest = new HttpEntity<>("id=" + wid + "&type=workflow&isactive=true", postHeaders);
        Postresult = this.testrestTemplate.postForObject(
                "http://localhost:" + this.port + "/Geoweaver/web/logs",
                postRequest, String.class);
        assertThat(Postresult).contains("history_input");

        mapper = new ObjectMapper();
        mapped = mapper.readerForListOf(Map.class).readValue(Postresult);
        map = mapped.get(0);

        wehid = String.valueOf(map.get("history_id")); // workflow execution hist id
        assertEquals(whid, wehid);

        weid = String.valueOf(map.get("history_process")); // workflow id
        assertEquals(wid, weid);

        // Delete workflow
        String deleteResult = deleteResource(wid, "workflow");
        assertThat(deleteResult).contains("done");

    }

    @Test
    @DisplayName("Test /recent endpoint for workflow executions")
    void testWorkflowRecentHistory() throws Exception {

        // Add workflow
        String wid = AddWorkflow();

        // Execute workflow
        String whid = ExecuteWorkflow(wid);

        bt.sleep(5); // sleep to allow time for process execution to finish

        HttpHeaders postHeaders = new HttpHeaders();
        postHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Get workflow recent history
        HttpEntity postRequest = new HttpEntity<>("type=workflow&number=20", postHeaders);
        String Postresult = this.testrestTemplate.postForObject(
                "http://localhost:" + this.port + "/Geoweaver/web/recent",
                postRequest, String.class);
        System.out.println("workflow /recent hist: " + Postresult);
        assertThat(Postresult).contains("id");

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
        String[] end_time_no_ms = end_time.split("\\.");
        assertNotNull(end_time_no_ms[0]);
        // assert end time matches format
        Date end_time_parsed = sdf.parse(end_time_no_ms[0]);
        String end_time_format = sdf.format(end_time_parsed);
        assertEquals(end_time_format, end_time_no_ms[0]);

        // Delete workflow
        String deleteResult = deleteResource(wid, "workflow");
        assertThat(deleteResult).contains("done");

    }

    @Test
    void testLandingPage() throws Exception{

        GeoweaverApplication.addDefaultPublicUser();

        // Add workflow
        String wid = AddWorkflow();

        String result = this.testrestTemplate.getForObject("http://localhost:" + this.port + "/Geoweaver/landing/" + wid,
				String.class);

		assertThat(result).contains("Geoweaver Workflow");

    }

}
