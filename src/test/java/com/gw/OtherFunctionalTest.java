package com.gw;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.security.KeyPair;

import com.gw.ssh.RSAEncryptTool;
import com.gw.tools.UserSession;
import com.gw.tools.UserTool;
import com.gw.utils.BaseTool;

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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = GeoweaverApplication.class)
public class OtherFunctionalTest extends AbstractHelperMethodsTest {

    @InjectMocks
    @Autowired
    BaseTool bt;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate testrestTemplate;

    @InjectMocks
    @Autowired
    UserTool utmock;

    @Test
    void testPassword() {

        String old512str = bt.get_SHA_512_SecurePassword("123456", "xyzuser");

        String new512str = bt.get_SHA_512_SecurePassword("123456", "xyzuser");

        assertEquals(old512str, new512str);

        String new512str2 = bt.get_SHA_512_SecurePassword("123456", "uvwxyzuser");

        assertNotEquals(old512str, new512str2);

        String new512str3 = bt.get_SHA_512_SecurePassword("1234567", "xyzuser");

        assertNotEquals(old512str, new512str3);

    }

    @Test
    void testUserAuth() throws Exception {

        utmock = Mockito.spy(utmock);
        // doNothing().when(utmock).authenticate(anyString());
        assertFalse(utmock.isAuth("nonauthorizeduser", "non-ip"));

    }

    @Test
    void testUserSession() {

        utmock = Mockito.spy(utmock);

        utmock.bindSessionUser("xyzsession", "xyzuser", "x.x.x.x");

        when(utmock.isAuth("xyzsession", "x.x.x.x")).thenReturn(true);

        assertEquals(utmock.getAuthUserId("xyzsession", "x.x.x.x"), "xyzuser");

        UserSession us = new UserSession();
        us.setJssessionid("xyzsession_expired");
        us.setIp_address("x.x.x.x");
        long oldtime = new Date().getTime() - 25 * 60 * 60 * 1000;
        us.setCreated_time(new Date(oldtime));
        utmock.authsession2user.add(us);

        utmock.cleanExpiredAuth();
        when(utmock.isAuth("xyzsession_expired", "x.x.x.x")).thenReturn(true);
        assertEquals(utmock.getAuthUserId("xyzsession_expired", "x.x.x.x"), "111111");

    }

    @Test
    @DisplayName("Test /addLocalFile endpoint")
    void testAddLocalFile() throws Exception {

        String filePath = bt.testResourceFiles() + "/add_ssh_host.txt";

        String hid = AddHost();

        String type = "process";

        String content = "test123";

        String name = "testFile.txt";

        String ownerid = "111111";

        String confidential = "FALSE";

        String postParams = "filepath=" + filePath + "&hid=" + hid + "&type=" + type + "&content=" + content + "&name="
                + name + "&ownerid=" + ownerid + "&confidential=" + confidential;

        HttpHeaders postHeaders = new HttpHeaders();
        postHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Add file to DB
        HttpEntity postRequest = new HttpEntity<>(postParams, postHeaders);
        String Postresult = this.testrestTemplate.postForObject(
                "http://localhost:" + this.port + "/Geoweaver/web/addLocalFile",
                postRequest, String.class);
        System.out.println("/addLocalFile results: " + Postresult);
        assertThat(Postresult).contains("id");

        // Delete created host
        String deleteResult = deleteResource(hid, "host");
        assertThat(deleteResult).contains("done");

    }

    // @Test
    @DisplayName("Test /retrievefile endpoint")
    void testRetrieveFile() throws Exception {
        /*
         * This test requires ssh into remote server.
         * Test fails.
         */

        String testFilePath = bt.testResourceFiles() + "/add_ssh_host.txt";

        HttpHeaders postHeaders = new HttpHeaders();
        postHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Get file
        HttpEntity postRequest = new HttpEntity<>("filepath=" + testFilePath, postHeaders);
        String Postresult = this.testrestTemplate.postForObject(
                "http://localhost:" + this.port + "/Geoweaver/web/retrievefile",
                postRequest, String.class);
        System.out.println("/retrievefile results: " + Postresult);
        assertThat(Postresult).contains("success");
    }

    // @Test
    @DisplayName("Test /download/{tempfolder}/{filename} endpoint")
    void testDownloadToTemp() throws Exception {
        /*
         * Endpoint responded,
         * but since no remote hosts can be used in testing, no files are downloaded.
         * endpoint returns HTTP 404
         */

        String tempFolder = bt.getFileTransferFolder();
        String fileName = bt.testResourceFiles() + "/add_ssh_host.txt";

        HttpHeaders postHeaders = new HttpHeaders();
        postHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // download file
        // HttpEntity postRequest = new HttpEntity<>("filepath="+testFilePath,
        // postHeaders);
        String Postresult = this.testrestTemplate.postForObject(
                "http://localhost:" + this.port + "/Geoweaver/web/download/" + tempFolder + "/" + fileName,
                null, String.class);
        System.out.println("/download/{tempfolder}/{filename} results: " + Postresult);
        assertThat(Postresult).contains("200");

    }

    // @Test
    @DisplayName("Test /updatefile endpoint")
    void testUpdateFile() throws Exception {
        /*
         * Endpoint responded,
         * but since no remote hosts can be used in testing, no files are updated.
         */

        String testFilePath = bt.testResourceFiles() + "/test1.txt";
        String content = "Test from /updatefile";

        HttpHeaders postHeaders = new HttpHeaders();
        postHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // update file
        HttpEntity postRequest = new HttpEntity<>("filepath=" + testFilePath + "&content=" + content, postHeaders);
        String Postresult = this.testrestTemplate.postForObject(
                "http://localhost:" + this.port + "/Geoweaver/web/updatefile",
                postRequest, String.class);
        System.out.println("/updatefile results: " + Postresult);
        assertThat(Postresult).contains("success");

    }

    // @Test
    @DisplayName("Test /openfilebrowse endpoint")
    void testOpenFileBrowser() throws Exception {

        /*
         * This test requires ssh into remote server.
         * Test fails.
         */

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Add host
        String hid = AddHost();

        bt.setLocalhostPassword("password", false);

        KeyPair kpair = RSAEncryptTool.buildKeyPair();
        String encryppswd = RSAEncryptTool.byte2Base64(RSAEncryptTool.encrypt(kpair.getPublic(), "password"));

        Map<String, String> body = new HashMap<>();
        body.put("hid", hid);
        body.put("pswd", encryppswd);
        body.put("init_path", "/home/");

        HttpHeaders postHeaders = new HttpHeaders();
        postHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity bodyData = new HttpEntity<>(body, postHeaders);
        String Postresult = this.testrestTemplate.postForObject(
                "http://localhost:" + this.port + "/Geoweaver/web/openfilebrowser",
                bodyData, String.class);

        logger.debug("Post result: " + Postresult);
        assertThat(Postresult.contains("success"));

        // Delete created host
        String deleteResult = deleteResource(hid, "host");
        assertThat(deleteResult).contains("done");

    }

}
