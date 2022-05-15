package com.gw;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gw.jpa.GWUser;
import com.gw.jpa.Workflow;
import com.gw.tools.UserTool;
import com.gw.user.GWToken;
import com.gw.utils.BaseTool;
import com.gw.web.GeoweaverController;

import org.apache.log4j.Logger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
public class UserTest {

    @Autowired
	UserTool ut;

	@Autowired
	BaseTool bt;

	@Autowired
	private TestRestTemplate testrestTemplate;

	@LocalServerPort
	private int port;

	Logger logger = Logger.getLogger(this.getClass());


    @Test
    void testResetPassword(){

        ut.token2date.put("dummytoken", new Date());
        ut.token2userid.put("dummytoken", "testuser");

        HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		HttpEntity request = new HttpEntity<>("token=dummytoken", headers);
		String result = this.testrestTemplate.postForObject(
				"http://localhost:" + this.port + "/Geoweaver/web/reset_password",
				request,
				String.class);
		logger.debug("the result is: " + result);
		assertThat(result).contains("Forgot Password");
        
    }


    @Test
    void testToken(){

        GWToken t = new GWToken();

		t.setExpireDate(1000000000);

		t.setToken("space fake go");

		assertThat(t.getToken()).isEqualTo("space fake go");

		assertThat(t.getExpireDate()).isEqualTo(1000000000);

    }

	@Test
	void touch_api_endpoints(){

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		HttpEntity request = new HttpEntity<>("type=host", headers);
		String result = this.testrestTemplate.getForObject("http://localhost:" + this.port + "/Geoweaver/user/reset_password",
				String.class);
				logger.debug(result);

		result = this.testrestTemplate.getForObject("http://localhost:" + this.port + "/Geoweaver/user/profile",
				String.class);
				logger.debug(result);
		result = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/user/profile",
				request,
				String.class);
				logger.debug(result);
				result = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/user/login",
				request,
				String.class);
				logger.debug(result);
		result = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/user/logbackin",
				request,
				String.class);
				logger.debug(result);
		result = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/user/logout",
				request,
				String.class);
				logger.debug(result);

	}
    
}
