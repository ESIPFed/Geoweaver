package com.gw;


import static org.assertj.core.api.Assertions.assertThat;

import com.gw.utils.EmailValidator;
import com.gw.utils.GmailAPI;
import com.gw.utils.MasterRequestObject;
import com.gw.utils.SpatialExtentValidator;
import com.gw.utils.Message;

import org.apache.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = GeoweaverApplication.class)

public class UtilsTest {

    @Autowired
	private TestRestTemplate testrestTemplate;

	@LocalServerPort
	private int port;

    Logger logger = Logger.getLogger(this.getClass());

    @Test
    void test_spatialextentvalidator(){
        SpatialExtentValidator.validate("38.479394673276445", "37.37015718405753", "-78.84201049804688", "-77.78732299804688", "ESPG:4326", "MODIS");
    }

    @Test
    void testemailvalidator(){
        assertThat(EmailValidator.validate("zsun@ gmu edu")).isFalse();
        assertThat(EmailValidator.validate("zsun@gmu.edu")).isTrue();
    }

    @Test
    void random_test(){

        MasterRequestObject mro = new MasterRequestObject();
        
        Message msg = new Message(null, null, null, null, null, null, false);
        msg.getA();
        msg.getB();
        msg.getDisplaymsg();
        msg.getInformation();
        msg.getStrongmsg();
        msg.getTitle();

        try{
            GmailAPI.getGmailService();

            GmailAPI.getMailBody("Google");

            // GmailOperations functionality has been removed
        }catch(Exception e){

        }finally{
            //don't do anything for now
        }
        

    }
    
}