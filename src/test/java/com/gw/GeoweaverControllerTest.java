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
public class GeoweaverControllerTest extends AbstractHelperMethodsTest {

    @Autowired
	private TestRestTemplate testrestTemplate;

    @LocalServerPort
	private int port;

        Logger logger = Logger.getLogger(this.getClass());

    @Test
    public void call_rest_api() throws Exception{

        String hid = AddHost();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity request = new HttpEntity<>("type=host", headers);
        String result = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/add/host",
                        request,
                        String.class);
                        logger.debug(result);
        
        request = new HttpEntity<>("type=process", headers);
        result = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/add",
                        request,
                        String.class);
                        logger.debug(result);
        request = new HttpEntity<>("type=workflow", headers);
        result = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/add",
                        request,
                        String.class);
                        logger.debug(result);
        result = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/reset_password",
                        request,
                        String.class);
                        logger.debug(result);
        result = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/closefilebrowser",
                        request,
                        String.class);
                        logger.debug(result);
        result = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/openfilebrowser",
                        request,
                        String.class);
                        logger.debug(result);
        result = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/retrievefile",
                        request,
                        String.class);
                        logger.debug(result);
        result = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/download/tempfolder/filename",
                        request,
                        String.class);
                        logger.debug(result);
        result = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/updatefile",
                        request,
                        String.class);
                        logger.debug(result);
        result = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/readEnvironment",
                        request,
                        String.class);
                        logger.debug(result);
        result = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/executeWorkflow",
                        request,
                        String.class);
                        logger.debug(result);
        
        
    }
    
}
