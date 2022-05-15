package com.gw;

import org.mockito.InjectMocks;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
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

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public abstract class AbstractHelperMethodsTest {

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
    LocalhostTool ltmock;

    Logger logger = Logger.getLogger(this.getClass());

    // This method is kept here in this file due to 3 other files
    // (EnviromentTest.java, OtherFunctionalTests.java, and HostTest.java)
    // utilizing this method. Leaving this here will prevent code repetition
    // among the test case files.
    public String AddHost() throws Exception {

        HttpHeaders hostHeaders = new HttpHeaders();
        hostHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Add host
        String bultinjson = bt.readStringFromFile(bt.testResourceFiles() + "/add_ssh_host.txt");
        HttpEntity hostRequest = new HttpEntity<>(bultinjson, hostHeaders);
        String hostResult = this.testrestTemplate.postForObject(
                "http://localhost:" + this.port + "/Geoweaver/web/add",
                hostRequest,
                String.class);

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = mapper.readValue(hostResult, Map.class);
        String hid = String.valueOf(map.get("id"));

        return hid;

    }

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

    public void stopProcess(String pid, String type) throws Exception {

        // Add process
        HttpHeaders processHeaders = new HttpHeaders();
        processHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity processRequest = new HttpEntity<>("id=" + pid + "&type=" + type, processHeaders);
        String processResult = this.testrestTemplate.postForObject(
                "http://localhost:" + this.port + "/Geoweaver/web/stop",
                processRequest,
                String.class);

    }

    public String deleteResource(String id, String type) throws Exception {

        HttpHeaders deleteHeaders = new HttpHeaders();
        deleteHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity deleteRequest = new HttpEntity<>("id=" + id + "&type=" + type, deleteHeaders);
        String deleteResult = this.testrestTemplate.postForObject(
                "http://localhost:" + this.port + "/Geoweaver/web/del",
                deleteRequest,
                String.class);

        return deleteResult;

    }

    public String AddPythonProcess() throws Exception {

        // Add process
        HttpHeaders processHeaders = new HttpHeaders();
        processHeaders.setContentType(MediaType.APPLICATION_JSON);
        String pythonjson = bt.readStringFromFile(bt.testResourceFiles() + "/add_python_process.json");
        HttpEntity processRequest = new HttpEntity<>(pythonjson, processHeaders);
        String processResult = this.testrestTemplate.postForObject(
                "http://localhost:" + this.port + "/Geoweaver/web/add/process",
                processRequest,
                String.class);

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = mapper.readValue(processResult, Map.class);
        String pid = String.valueOf(map.get("id"));

        return pid;

    }

    public String ExecutePythonProcess(String pid) throws Exception {

        // encode the password
        KeyPair kpair = RSAEncryptTool.buildKeyPair();
        String encryppswd = RSAEncryptTool.byte2Base64(RSAEncryptTool.encrypt(kpair.getPublic(), "testpswd"));
        // run the python process
        ltmock = Mockito.spy(ltmock);
        doNothing().when(ltmock).authenticate(anyString());

        String historyid = new RandomString(12).nextString();

        ltmock.executePythonProcess(historyid, pid, historyid, encryppswd, historyid, true, "", "", "");

        return historyid;
    }

}
