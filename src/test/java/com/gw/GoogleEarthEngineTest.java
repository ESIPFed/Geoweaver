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
public class GoogleEarthEngineTest extends AbstractHelperMethodsTest {

    @Autowired
	private TestRestTemplate testrestTemplate;

    @LocalServerPort
	private int port;

    @Test
    public void call_rest_api() throws Exception{

        String hid = AddHost();

        HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		HttpEntity request = new HttpEntity<>("type=host", headers);
		String result = this.testrestTemplate.getForObject("http://localhost:" + this.port + "/Geoweaver/GoogleEarth-proxy/"+hid+"/api/contents/?content=1&1647141084884=",
				String.class);
        result = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/GoogleEarth-proxy/"+hid+"/api/contents/Untitled.ipynb/checkpoint",
                request,
                String.class);
        this.testrestTemplate.put("http://localhost:" + this.port + "/Geoweaver/GoogleEarth-proxy/"+hid+"/api/contents/Untitled.ipynb", request, String.class);
        this.testrestTemplate.patchForObject("http://localhost:" + this.port + "/Geoweaver/GoogleEarth-proxy/"+hid+"/api/contents/Untitled.ipynb", 
                request, String.class);

        result = this.testrestTemplate.getForObject("http://localhost:" + this.port + "/Geoweaver/GoogleEarth-proxy/"+hid+"/api/sessions", String.class);
        result = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/GoogleEarth-proxy/"+hid+"/hub/login",
                request,
                String.class);
        result = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/GoogleEarth-proxy/"+hid+"/lab/login",
                request,
                String.class);
        result = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/GoogleEarth-proxy/"+hid+"/login",
                request,
                String.class);
        result = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/GoogleEarth-proxy/"+hid+"/lab/login",
                request,
                String.class);
        this.testrestTemplate.delete("http://localhost:" + this.port + "/Geoweaver/GoogleEarth-proxy/"+hid+"/lab/login");
        result = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/GoogleEarth-proxy/"+hid+"/api/sessions/xxxx", request, String.class);
        result = this.testrestTemplate.getForObject("http://localhost:" + this.port + "/Geoweaver/GoogleEarth-proxy/"+hid+"/api/sessions/xxxx", String.class);

        result = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/GoogleEarth-proxy/"+hid+"", request, String.class);
        result = this.testrestTemplate.getForObject("http://localhost:" + this.port + "/Geoweaver/jupyter-http", String.class);
        result = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/jupyter-https", request, String.class);

    }
    
}
