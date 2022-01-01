package com.gw;


import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;

import com.gw.tools.ExecutionTool;
import com.gw.tools.HistoryTool;
import com.gw.tools.LocalhostTool;
import com.gw.tools.ProcessTool;
import com.gw.tools.UserTool;
import com.gw.utils.BaseTool;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OtherFunctionalTest {

	@InjectMocks
    @Autowired
	UserTool ut;

	@InjectMocks
	@Autowired
	BaseTool bt;

    @Autowired
	private TestRestTemplate testrestTemplate;

    @LocalServerPort
	private int port;

	@InjectMocks
	@Autowired
	ExecutionTool et;

	@InjectMocks
	@Autowired
	ProcessTool pt;

	@InjectMocks
	@Autowired
	HistoryTool hist;

	@InjectMocks
	@Autowired
	LocalhostTool ltmock;


    @Test
    void testPassword(){

        String old512str = bt.get_SHA_512_SecurePassword("123456", "xyzuser");

        String new512str = bt.get_SHA_512_SecurePassword("123456", "xyzuser");

        assertEquals(old512str, new512str);

        String new512str2 = bt.get_SHA_512_SecurePassword("123456", "uvwxyzuser");

        assertNotEquals(old512str, new512str2);

        String new512str3 = bt.get_SHA_512_SecurePassword("1234567", "xyzuser");

        assertNotEquals(old512str, new512str3);

    }

    @Test
    void testUserLogin() throws Exception{

        //test sign up a fake user


        //test sign in using the user


        //test sign out


		ltmock = Mockito.spy(ltmock);
		doNothing().when(ltmock).authenticate(anyString());

        //test deleting the user



    }
    
}
