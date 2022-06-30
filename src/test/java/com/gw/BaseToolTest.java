package com.gw;


import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import com.gw.utils.BaseTool;
import com.gw.utils.OSValidator;
import com.gw.utils.RandomString;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.mock.web.MockHttpServletRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = GeoweaverApplication.class)
public class BaseToolTest {

	@Autowired
	BaseTool bt;

    @InjectMocks
    @Autowired
    BaseTool btmock;

	@LocalServerPort
	private int port;

	Logger logger = Logger.getLogger(this.getClass());

    @Test
    void testLocalhostIdentifier() throws Exception{

        bt.getLocalhostIdentifier();

    }

    @Test
    void testCheckLocalhostPassword() throws Exception{

        assertThat(bt.checkLocalhostPassword("dummypassword")).isFalse();

    }

    @Test
    void testSetLocalhostPassword() throws Exception{

        bt.setSecretfilename(".testsecret");

        String random_code = new RandomString(20).nextString();

        bt.setLocalhostPassword("mercury_code_"+random_code, true);

        assertThat(bt.checkLocalhostPassword("mercury_code_" + random_code)).isTrue();

    }

    @Test
    void testIsLocal(){

        GeoweaverApplication.addLocalhost();

        assertThat(bt.islocal("100001")).isTrue();

    }

    @Test
    void testGetBody(){

        MockHttpServletRequest request = new MockHttpServletRequest();

        request.setContent("sun flare is coming".getBytes());;

        assertThat(bt.getBody(request)).isEqualTo("sun flare is coming");

    }

    @Test
    void testRemoveLob(){

        String earthbstr = "STRINGDECODE( earthb is in another dimension )";

        assertThat(bt.removeLob(earthbstr)).isEqualTo("earthb is in another dimension");

    }

    @Test
    void testEncodeValue(){

        assertThat(bt.encodeValue("http://www.test.com/?xyz=x_ sdfwee&sdf=earth2"))
                     .isEqualTo("http%3A%2F%2Fwww.test.com%2F%3Fxyz%3Dx_+sdfwee%26sdf%3Dearth2");

    }

    @Test
    void testLong2date(){

        assertThat(bt.long2Date(1642376149000L)).contains("2022-01");

    }
    
    @Test
    void testErrorReturn(){

        assertThat(bt.getErrorReturn("aestorid x is going out of way")).contains("status");

    }

    @Test
    void testExecuteLocal(){

        List<String> cmds = new ArrayList();

        if(OSValidator.isMac() || OSValidator.isUnix()){

            cmds.add("pwd");

        }else{

            cmds.add("ping");

        }

        assertThat(bt.executeLocal(cmds)).doesNotContain("Cannot run program");

    }

    @Test
    void testSleep(){

        bt.sleep(1);

    }
    
    @Test
    void testParseXML(){

        Document doc = bt.parseString("<solar><venus>hot</venus></solar>");

        assertThat(doc.getRootElement().getName()).isEqualTo("solar");

    }

    @Test
    void testParseURL(){

        Document earthhtml = bt.parseURL("https://raw.githubusercontent.com/ESIPFed/Geoweaver/master/pom.xml");

        assertThat(earthhtml.getStringValue()).contains("geoweaver");

    }

    @Test
    void testGetLocalPathEnvironment(){

        bt.getLocalPATHEnvironment();

    }

    @Test
    void testRunCMDNoEnv(){

        if(OSValidator.isMac() || OSValidator.isUnix()){
        
            bt.runCmdNoEnv("pwd");
        
        }else{
            
            bt.runCmdNoEnv("ping");

        }

    }

    @Test
    void testRunCMDEnv(){

        if(OSValidator.isMac() || OSValidator.isUnix()){
        
            bt.runCmdEnv("pwd");
        
        }else{
            
            bt.runCmdEnv("ping");

        }

    }

    @Test
    void testParseJupyterURL(){

        bt.parseJupyterURL("http://localhost:8888/api/contents/Documents/Untitled.ipynb?type=notebook&_=1642379059397");

    }

    @Test
    void testParseGoogleEarthURL(){

        bt.parseGoogleEarthURL("https://code.earthengine.google.com/javascript/polyfills/web-animations-next-lite.min.js");

    }

    @Test
    void testPrintoutCallStack(){

        bt.printoutCallStack();

    }

    @Test
    void testGetClassPath(){

        bt.getClassPath();

    }

    @Test
    void testDown(){

        bt.down(bt.getFileTransferFolder()+ "/header.png", 
                "https://raw.githubusercontent.com/ESIPFed/Geoweaver/master/src/main/resources/static/img/header.png");

        File img = new File(bt.getFileTransferFolder() + "/header.png");

        assertThat(img.exists()).isTrue();

        img.delete();

    }

    @Test
    void testGetCurrentMySQLDate(){

        bt.getCurrentMySQLDatetime();

    }

    @Test
    void testGetSQLDate(){

        BaseTool.getCurrentSQLDate();

    }

    @Test
    void testParseSQLDateStr(){

        logger.info(bt.getCurrentMySQLDatetime());

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Paris"));
        cal.setTime(bt.parseSQLDateStr("2022-01-16 20:01:52.000"));
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        assertThat(year).isEqualTo(2022);
        assertThat(month).isEqualTo(0);

    }

    @Test
    void testRunLocalNuxCommand(){

        bt.runLocalNuxCommand("pwd");

    }

    @Test
    void testGetWebAppRootPath(){

        bt.getWebAppRootPath();

    }


}
