package com.gw;

import org.mockito.InjectMocks;

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gw.tools.UserTool;
import com.gw.utils.BaseTool;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public abstract class HelperMethods {

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

}
