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
import com.gw.search.GWSearchTool;
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
class GeneralTests extends AbstractHelperMethodsTest {

	@LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate testrestTemplate;

	Logger logger = Logger.getLogger(this.getClass());

	@Autowired
	UserTool ut;

	@Autowired
	BaseTool bt;

	@Autowired
	BuildProperties buildProperties;

	@Autowired
	UserRepository userRepository;

	@Test
	void contextLoads() {

	}

	@Test
	void testBuildInfo() {

		logger.info("The current version is " + buildProperties.getVersion());

	}

	@Test
	@DisplayName("Testing adding/editing/removing user...")
	void testUser() {

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		HttpEntity request = new HttpEntity<>("type=host", headers);
		String result = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/list",
				request,
				String.class);
		logger.debug("the result is: " + result);
		// assertThat(controller).isNotNull();
		assertThat(result).contains("[");

	}

	@Test
	String testResourceFiles() {

		Path resourceDirectory = Paths.get("src", "test", "resources");
		String absolutePath = resourceDirectory.toFile().getAbsolutePath();

		logger.debug(absolutePath);
		assertTrue(absolutePath.contains("resources"));
		return absolutePath;
	}

	@Test
	@DisplayName("Subscription message service test ")
	void testSubscriptionMessage() {

		GWUser u = ut.getUserById("111111");
		if (!BaseTool.isNull(u))
			assertEquals(u.getUsername(), "publicuser");

	}

	@Test
	@DisplayName("Testing if the front page is accessible..")
	void testFrontPage() {

		String result = this.testrestTemplate.getForObject("http://localhost:" + this.port + "/Geoweaver/web/geoweaver",
				String.class);
		assertThat(result).contains("Geoweaver");

	}

	@Test
	@DisplayName("Testing Dashboard...")
	void testDashboard() {
		// ResponseEntity<String> result =
		// testrestTemplate.getForEntity("http://localhost:" + this.port +
		// "/Geoweaver/web/dashboard", String.class);
		ResponseEntity result = this.testrestTemplate.postForEntity(
				"http://localhost:" + this.port + "/Geoweaver/web/dashboard",
				"",
				String.class);
		// logger.debug("the dashboard result is: " + result);
		// assertThat(controller).isNotNull();
		assertEquals(200, result.getStatusCode().value());
		assertThat(result.getBody().toString()).contains("process_num");
	}

	@Test
	@DisplayName("Testing list of host, process, and workflow...")
	void testList() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		HttpEntity request = new HttpEntity<>("type=host", headers);
		String result = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/list",
				request,
				String.class);
		logger.debug("the result is: " + result);
		assertThat(result).contains("[");

		request = new HttpEntity<>("type=process", headers);
		result = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/list",
				request,
				String.class);
		assertThat(result).contains("[");

		request = new HttpEntity<>("type=workflow", headers);
		result = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/list",
				request,
				String.class);
		assertThat(result).contains("[");
	}

	@Test
	void testJSONEscape() {

		String jsonstr = "import os\nimport time";

		if (jsonstr.contains("\nimport")) {

			logger.debug("import is detected");

		} else {

			logger.debug("import is not detected");
		}

		String jsonstr2 = "{\"cells\":[{\"cell_type\":\"markdown\"";

		if (jsonstr2.contains("\"cells\"")) {

			logger.debug("cell is detected");

		} else {

			logger.debug("cell is not detected");
		}

	}

	@Test
	void testSearchClass() {

		GWSearchTool instance = new GWSearchTool();

		instance.search("", "all");
		instance.search("", "host");
		instance.search("", "workflow");
		instance.search(null, null);
	}

	// Geoweaver/web/search
	@Test
	@DisplayName("Testing search of host, process, and workflow...")
	void testSearchGlobal() {

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		// Search for all available hosts
		logger.debug("\n\n##############\nTesting search of all hosts\n##############\n");
		HttpEntity request = new HttpEntity<>("type=host", headers);
		String result = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/search",
				request,
				String.class);
		// assertThat(controller).isNotNull();
		assertThat(result).contains("[");
		// logger.debug("Result contains specific host: " + result.contains("New Host
		// GoogleE"));

		// Search for all available processes
		logger.debug("\n\n##############\nTesting search of all processes\n##############\n");
		request = new HttpEntity<>("type=process", headers);
		result = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/search",
				request,
				String.class);
		assertThat(result).contains("[");

		// Search for all available workflows
		logger.debug("\n\n##############\nTesting search of all workflows\n##############\n");
		request = new HttpEntity<>("type=workflow", headers);
		result = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/search",
				request,
				String.class);
		assertThat(result).contains("[");
	}

	@Test
	@DisplayName("Testing search of specific host.")
	void testSearchHost() throws Exception {

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		// Add new host
		String bultinjson = bt.readStringFromFile(bt.testResourceFiles() + "/add_ssh_host.txt");
		HttpEntity request = new HttpEntity<>(bultinjson, headers);
		String result = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/add",
				request,
				String.class);
		assertThat(result).contains("id");

		// Search for recently created host
		logger.debug("\n\n##############\nTesting search of specific host\n##############\n");
		HttpEntity searchRequest = new HttpEntity<>("type=host&keywords=", headers);
		String searchResult = this.testrestTemplate.postForObject(
				"http://localhost:" + this.port + "/Geoweaver/web/search",
				searchRequest,
				String.class);
		assertThat(searchResult).contains("New Host");

		// Remove the added host
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> map = mapper.readValue(result, Map.class);
		String hid = String.valueOf(map.get("id"));

		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		HttpEntity DeleteRequest = new HttpEntity<>("id=" + hid + "&type=host", headers);
		String DeleteResult = this.testrestTemplate.postForObject(
				"http://localhost:" + this.port + "/Geoweaver/web/del",
				DeleteRequest,
				String.class);
		assertThat(DeleteResult).contains("done");

	}

	@Test
	@DisplayName("Testing search of specific python process.")
	void testSearchPythonProcess() throws Exception {

		// Add Python Process
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		String pythonjson = bt.readStringFromFile(bt.testResourceFiles() + "/add_python_process.json");
		HttpEntity request = new HttpEntity<>(pythonjson, headers);
		String result = this.testrestTemplate.postForObject(
				"http://localhost:" + this.port + "/Geoweaver/web/add/process",
				request,
				String.class);
		assertThat(result).contains("id");

		// Search for specific python processes
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		logger.debug("\n\n##############\nTesting search of specific python process\n##############\n");
		HttpEntity SearchRequest = new HttpEntity<>("type=process", headers);
		String SearchResult = this.testrestTemplate.postForObject(
				"http://localhost:" + this.port + "/Geoweaver/web/search",
				SearchRequest,
				String.class);
		logger.debug("Result contains specific python process: " + SearchResult.contains("testpython2"));

		// Delete added python process
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> map = mapper.readValue(result, Map.class);
		String pid = String.valueOf(map.get("id"));

		HttpEntity DeleteRequest = new HttpEntity<>("id=" + pid + "&type=process", headers);
		String DeleteResult = this.testrestTemplate.postForObject(
				"http://localhost:" + this.port + "/Geoweaver/web/del",
				DeleteRequest,
				String.class);
		assertThat(DeleteResult).contains("done");

	}

	@Test
	@DisplayName("Testing search of specific workflow.")
	void testSearchWorkflow() throws Exception {

		// Add workflow
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		String workflowjson = bt.readStringFromFile(this.testResourceFiles() + "/add_workflow.json");
		HttpEntity request = new HttpEntity<>(workflowjson, headers);
		String result = this.testrestTemplate.postForObject(
				"http://localhost:" + this.port + "/Geoweaver/web/add/workflow",
				request,
				String.class);
		assertThat(result).contains("id");

		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> map = mapper.readValue(result, Map.class);

		String pid = String.valueOf(map.get("id"));
		assertNotNull(pid);

		// Search for specific workflow
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		logger.debug("\n\n##############\nTesting search of specific workflow\n##############\n");
		request = new HttpEntity<>("type=workflow", headers);
		result = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/search",
				request,
				String.class);
		logger.debug("Result contains specific workflow: " + result.contains("t2"));

		// Delete added workflow
		HttpEntity DeleteRequest = new HttpEntity<>("id=" + map.get("id") + "&type=workflow", headers);
		String DeleteResult = this.testrestTemplate.postForObject(
				"http://localhost:" + this.port + "/Geoweaver/web/del",
				DeleteRequest,
				String.class);
		assertThat(DeleteResult).contains("done");

	}

	void testRegisterUser(String username, String password, String email) {

		HttpHeaders userHeaders = new HttpHeaders();
		userHeaders.setContentType(MediaType.APPLICATION_JSON);

		// Required data for user creation
		Map<String, String> newUser = new HashMap<>();
		newUser.put("email", email);
		newUser.put("password", password);
		newUser.put("username", username);

		HttpEntity registerRequest = new HttpEntity<>(newUser, userHeaders);
		String registerResult = this.testrestTemplate.postForObject(
				"http://localhost:" + this.port + "/Geoweaver/user/register",
				registerRequest,
				String.class);
		System.out.println("/register result: " + registerResult);

	}

	@Test
	@DisplayName("Test /forgetpassword & /reset_password endpoint")
	void testResetPassword() throws Exception {
		/*
		 * /reset_password is depended
		 * on /forgetpassword to be called.
		 * /reset_password will return "invalid_token"
		 * if executed by itself.
		 * Additionally, this test executes correctly every time,
		 * but returns a java.lang.NullPointerException due to
		 * springboot server not starting in testing mode.
		 * This error doesn't break the test.
		 * 
		 * com.gw.utils.HttpUtil.getSiteURL(HttpUtil.java:17)
		 * e.g. "Password Reset URL for test37886 :
		 * null/../../user/reset_password?token=klwhrm24tic19ecukgiyihhr9vttc6"
		 */

		Iterable<GWUser> users = userRepository.findAll();

		// Create random data for user to ensure
		// avoiding duplicate user error,
		// and ability to rerun test multiple times.
		int random = (int) (Math.random() * 50000);
		String username = "test" + random;
		String email = "test" + random + "@gmail.com";
		String password = "Test123456";
		// Loop through all exsisting users
		for (GWUser user : users) {

			// Check if randomly generated user exsists
			if (!user.getEmail().equals(email)) {
				System.out.println("GREAT! registering: " + email);
				testRegisterUser(username, password, email);
				System.out.println("REGESTIRED!: " + email);

				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_JSON);

				// Required data for user forget password request
				Map<String, String> userReset = new HashMap<>();
				userReset.put("email", email);

				HttpEntity forgetPasswordRequest = new HttpEntity<>(userReset, headers);
				String result = this.testrestTemplate.postForObject(
						"http://localhost:" + this.port + "/Geoweaver/user/forgetpassword",
						forgetPasswordRequest,
						String.class);
				System.out.println("/forgetpassword result: " + result);
				assertThat(result).contains("a password reset email has been sent");
				break;

			} else {

				random = (int) (Math.random() * 50000);
				username = "test" + random;
				email = "test" + random + "@gmail.com";
				password = "Test123456";
			}

		}

	}

	@Test
	@DisplayName("Test /checkLiveSession endpoint")
	void testCheckLiveSession() throws Exception {

		// Add host
		String hid = AddHost();

		HttpHeaders postHeaders = new HttpHeaders();
		postHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		// Get Live session of host
		HttpEntity postRequest = new HttpEntity<>("hostId=" + hid, postHeaders);
		String Postresult = this.testrestTemplate.postForObject(
				"http://localhost:" + this.port + "/Geoweaver/web/checkLiveSession",
				postRequest, String.class);
		System.out.println("/checkLiveSession results: " + Postresult);
		assertThat(Postresult).contains("exist");

		// Delete created host
		String deleteResult = deleteResource(hid, "host");
		assertThat(deleteResult).contains("done");
	}

	@Test
	void testErrorPage() {

		String Postresult = this.testrestTemplate.postForObject(
				"http://localhost:" + this.port + "/Geoweaver/web/error",
				null, String.class);
		assertThat(Postresult).contains("So sorry, something went wrong!");

	}

	@Test
	void testPortalController() {

		ResponseEntity<String> getresult = this.testrestTemplate.getForEntity(
				"http://localhost:" + this.port + "/Geoweaver/geoweaver", String.class);
		assertThat(getresult.getStatusCode().value()).isEqualTo(302);

		getresult = this.testrestTemplate.getForEntity(
				"http://localhost:" + this.port + "/Geoweaver/web", String.class);
		assertThat(getresult.getStatusCode().value()).isEqualTo(302);

	}

	@Test
	void testComputerUniqueID() throws Exception {

		String firstattempt = bt.getLocalhostIdentifier();

		String secondattempt = bt.getLocalhostIdentifier();

		assertEquals(firstattempt, secondattempt);

	}

}
