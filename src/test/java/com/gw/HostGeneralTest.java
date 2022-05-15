package com.gw;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gw.tools.UserTool;
import com.gw.utils.BaseTool;

import org.apache.log4j.Logger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = GeoweaverApplication.class)
public class HostGeneralTest extends AbstractHelperMethodsTest {

	@Autowired
	UserTool ut;

	@Autowired
	BaseTool bt;

	@Autowired
	private TestRestTemplate testrestTemplate;

	@LocalServerPort
	private int port;

	Logger logger = Logger.getLogger(this.getClass());

	// @Test
	void contextLoads() {

	}

	@Test
	void testLocalhostPassword() {

		bt.setLocalhostPassword("password", false);

		String password = bt.getLocalhostPassword();

		assertThat(password).hasSize(128);

		String password2 = bt.getLocalhostPassword();

		assertThat(password).isEqualTo(password2);

	}

	@Test
	void testSSHHost() throws JsonMappingException, JsonProcessingException {

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		String bultinjson = bt.readStringFromFile(bt.testResourceFiles() + "/add_ssh_host.txt");
		HttpEntity request = new HttpEntity<>(bultinjson, headers);
		String result = this.testrestTemplate.postForObject(
				"http://localhost:" + this.port + "/Geoweaver/web/add",
				request,
				String.class);
		logger.debug("the result is: " + result);
		// assertThat(controller).isNotNull();
		assertThat(result).contains("id");

		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> map = mapper.readValue(result, Map.class);
		String hid = String.valueOf(map.get("id"));

		// remove the added host
		// id=2avx48&type=process
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		request = new HttpEntity<>("id=" + hid + "&type=host", headers);
		result = this.testrestTemplate.postForObject(
				"http://localhost:" + this.port + "/Geoweaver/web/del",
				request,
				String.class);
		logger.debug("the result is: " + result);
		// assertThat(controller).isNotNull();
		assertThat(result).contains("done");

	}

	@Test
	@DisplayName("Test /detail endpoint for host type")
	void testHostDetail() throws Exception {

		// Add Host
		String hid = AddHost();

		// Get host details
		HttpHeaders postHeaders = new HttpHeaders();
		postHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		HttpEntity postRequest = new HttpEntity<>("id=" + hid + "&type=host", postHeaders);
		String Postresult = this.testrestTemplate.postForObject(
				"http://localhost:" + this.port + "/Geoweaver/web/detail",
				postRequest, String.class);
		assertThat(Postresult).contains("id");

		// Delete created host
		String deleteResult = deleteResource(hid, "host");
		assertThat(deleteResult).contains("done");
	}

	@Test
	void testRecent(){

		GeoweaverApplication.addLocalhost();

		HttpHeaders postHeaders = new HttpHeaders();
		postHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		HttpEntity postRequest = new HttpEntity<>("hostid=10001&number=10&type=host", postHeaders);
		String Postresult = this.testrestTemplate.postForObject(
				"http://localhost:" + this.port + "/Geoweaver/web/recent",
				postRequest, String.class);
		assertThat(Postresult).contains("[");

	}

	@Test
	void testHostEdit() throws Exception{

        String hid = AddHost();

        HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String edithostjson = bt.readStringFromFile(bt.testResourceFiles() + "/edit_ssh_host.txt");
        edithostjson = edithostjson.replace("100001", hid).replace("Localhost", "HostNewName");

		HttpEntity request = new HttpEntity<>(edithostjson, headers);
		String result = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/edit",
				request,
				String.class);
		
		logger.debug("the result is: " + result);
		assertThat(result).contains("HostNewName");


    }

	@Test
	void testSSHLoginPageRedirect(){

		ResponseEntity<String> result = this.testrestTemplate.getForEntity("http://localhost:" + this.port + "/Geoweaver/web/geoweaver-ssh?token=venustoken",
				String.class);

		assertThat(result.getStatusCode().value()).isEqualTo(302);

	}

	@Test
	void testSSHLoginPage(){

		String result = this.testrestTemplate.getForObject("http://localhost:" + this.port + "/Geoweaver/web/geoweaver-ssh-login",
				String.class);

		assertThat(result).contains("SSHW Terminal Emulator");

	}

	@Test
	void testSSHCommandLogin(){

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		String loginboxjson = bt.readStringFromFile(bt.testResourceFiles() + "/ssh_login.txt");

		HttpEntity request = new HttpEntity<>(loginboxjson, headers);

		ResponseEntity<String> result = this.testrestTemplate.postForEntity("http://localhost:" + this.port + "/Geoweaver/web/geoweaver-ssh-login",
			request,
			String.class);

		logger.debug("the result is: " + result);
		
		assertThat(result.getStatusCode().value()).isEqualTo(302);

	}

	@Test
	void testSSHLoginBox(){

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		String loginboxjson = bt.readStringFromFile(bt.testResourceFiles() + "/ssh_login_box.json");

		HttpEntity request = new HttpEntity<>(loginboxjson, headers);

		String result = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/geoweaver-ssh-login-inbox",
			request,
			String.class);

		assertThat(result).contains("Internal");

	}

	@Test
	void testSSHLogoutBox(){

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		HttpEntity request = new HttpEntity<>("token=saturntoken", headers);

		String result = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/geoweaver-ssh-logout-inbox?token=venustoken",
			request,
			String.class);

		assertThat(result).contains("done");

	}

	@Test
	void testResetPassword(){

		

	}

}
