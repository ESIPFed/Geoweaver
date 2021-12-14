package com.gw;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gw.tools.UserTool;
import com.gw.utils.BaseTool;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EnvironmentTest {
    
    @Autowired
	UserTool ut;

	@Autowired
	BaseTool bt;

    @Autowired
	private TestRestTemplate testrestTemplate;

    @LocalServerPort
	private int port;

    Logger logger  = Logger.getLogger(this.getClass());

    // @Test
	void contextLoads() {
		
		
	}

    
	String getRSAKey() throws ParseException{

		//get key
		String result = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/key", 
			null, 
			String.class);

		JSONParser jsonParser=new JSONParser();

		JSONObject jsonobj = (JSONObject)jsonParser.parse(result);

		String rsakey = jsonobj.get("rsa_public").toString();
		assertNotNull(rsakey);

        return rsakey;
        

	}

    @Test
	String testResourceFiles(){

		Path resourceDirectory = Paths.get("src","test","resources");
		String absolutePath = resourceDirectory.toFile().getAbsolutePath();

		logger.debug(absolutePath);
		assertTrue(absolutePath.contains("resources"));
		return absolutePath;
	}

    // This test could easily fail due to the environment differences
	@Test
	void testReadEnvironment() throws ParseException{

        String rsa_key = getRSAKey();

        String encryptpswd = ""; //how to encrypt pswd here

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		String testjson = bt.readStringFromFile(this.testResourceFiles()+ "/readenvironment.txt" ); //so far, it only tests localhost
        testjson.replace("<pswdencrypt>", encryptpswd);
    	HttpEntity request = new HttpEntity<>(testjson, headers);
		String result = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/readEnvironment", 
			request, 
			String.class);
		logger.debug("the result is: " + result);
		// assertThat(result).contains("id");

	}

}
