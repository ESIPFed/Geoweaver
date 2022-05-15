package com.gw;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.gw.tools.UserTool;
import com.gw.utils.BaseTool;
import com.gw.jpa.Environment;
import com.gw.jpa.GWUser;
import com.gw.jpa.LogActivity;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = GeoweaverApplication.class)
public class EnvironmentTest extends AbstractHelperMethodsTest {

    @Autowired
    UserTool ut;

    @Autowired
    BaseTool bt;

    @Autowired
    private TestRestTemplate testrestTemplate;

    @LocalServerPort
    private int port;

    Environment env;

    Logger logger = Logger.getLogger(this.getClass());

    // @Test
    void contextLoads() {

    }

    String getRSAKey() throws ParseException {

        // get key
        String result = this.testrestTemplate.postForObject("http://localhost:" + this.port + "/Geoweaver/web/key",
                null,
                String.class);

        JSONParser jsonParser = new JSONParser();

        JSONObject jsonobj = (JSONObject) jsonParser.parse(result);

        String rsakey = jsonobj.get("rsa_public").toString();
        assertNotNull(rsakey);

        return rsakey;

    }

    @Test
    String testResourceFiles() {

        Path resourceDirectory = Paths.get("src", "test", "resources");
        String absolutePath = resourceDirectory.toFile().getAbsolutePath();

        logger.debug(absolutePath);
        assertTrue(absolutePath.contains("resources"));
        return absolutePath;
    }

    // This test could easily fail due to the environment differences
    // @Test
    // void testReadEnvironment() throws ParseException{

    // String rsa_key = getRSAKey();

    // String encryptpswd = ""; //how to encrypt pswd here

    // HttpHeaders headers = new HttpHeaders();
    // headers.setContentType(MediaType.APPLICATION_JSON);
    // String testjson = bt.readStringFromFile(this.testResourceFiles()+
    // "/readenvironment.txt" ); //so far, it only tests localhost
    // testjson.replace("<pswdencrypt>", encryptpswd);
    // HttpEntity request = new HttpEntity<>(testjson, headers);
    // String result = this.testrestTemplate.postForObject("http://localhost:" +
    // this.port + "/Geoweaver/web/readEnvironment",
    // request,
    // String.class);
    // logger.debug("the result is: " + result);
    // assertThat(result).contains("id");

    // }

    @Test
    @DisplayName("Test /env endpoint")
    void testHostEnv() throws Exception {

        // Add Host
        String hid = AddHost();

        HttpHeaders postHeaders = new HttpHeaders();
        postHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Get host enviroments
        HttpEntity postRequest = new HttpEntity<>("id=" + hid, postHeaders);
        String Postresult = this.testrestTemplate.postForObject(
                "http://localhost:" + this.port + "/Geoweaver/web/env",
                postRequest, String.class);
        assertThat(Postresult).contains("[");

        // Delete created host
        String deleteResult = deleteResource(hid, "host");
        assertThat(deleteResult).contains("done");
    }

    @Test
    @DisplayName("Test /listhostwithenvironments endpoint")
    void testListHostWithEnvironments() throws Exception {

        // Add Host
        String hid = AddHost();

        HttpHeaders postHeaders = new HttpHeaders();
        postHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Get host enviroments
        // HttpEntity postRequest = new HttpEntity<>("id="+hid, postHeaders);
        String Postresult = this.testrestTemplate.postForObject(
                "http://localhost:" + this.port + "/Geoweaver/web/listhostwithenvironments",
                null, String.class);
        System.out.println("/end result: " + Postresult);
        assertThat(Postresult).contains("id");
        assertThat(Postresult).contains("type");
        assertThat(Postresult).contains("url");
        assertThat(Postresult).contains("envs");

        // Delete created host
        String deleteResult = deleteResource(hid, "host");
        assertThat(deleteResult).contains("done");

    }

    // @Test
    @DisplayName("Test /readEnvironment endpoint")
    void testReadEnvironment() throws Exception {
        /*
         * Endpoint requires ssh into remote host to read enviroments.
         * Test cannot be executed.
         */

        // Add Host
        String hid = AddHost();

        String pswd = "testpswd";

        HttpHeaders postHeaders = new HttpHeaders();
        postHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Get Live session of host
        HttpEntity postRequest = new HttpEntity<>("hostid=" + hid + "&pswd=" + pswd, postHeaders);
        String Postresult = this.testrestTemplate.postForObject(
                "http://localhost:" + this.port + "/Geoweaver/web/readEnvironment",
                postRequest, String.class);
        logger.debug("/readEnvironment results: " + Postresult);
        assertThat(Postresult).contains("success");

        // Delete created host
        String deleteResult = deleteResource(hid, "host");
        assertThat(deleteResult).contains("done");

    }

    @Test
    void testEnvs() {

        Environment instance = new Environment();

        instance.setHostobj(null);
        instance.setId("");
        instance.setName("");
        instance.setType("");
        instance.setBin("");
        instance.setPyenv("");
        instance.setBasedir("");
        instance.setSettings("");
        instance.getHostobj();
        instance.getId();
        instance.getName();
        instance.getType();
        instance.getBin();
        instance.getPyenv();
        instance.getBasedir();
        instance.getSettings();

    }

    @Test
    void testLogActivity() {

        LogActivity instance = new LogActivity();

        instance.setObjname(null);
        instance.setId("");
        instance.setOperator("");
        instance.setCategory("");
        instance.setObjectid("");
        instance.setOperation("");
        instance.getObjname();
        instance.getId();
        instance.getOperator();
        instance.getCategory();
        instance.getObjectid();
        instance.getOperation();
    }

    @Test
    void testGWUser() {

        GWUser instance = new GWUser();

        instance.setRole("");
        instance.setRegistration_date(null);
        instance.setLast_login_date(null);
        instance.setLoggedIn(null);
        instance.getRegistration_date();
        instance.getLast_login_date();
        instance.getLoggedIn();

    }

}
