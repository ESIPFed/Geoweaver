package com.gw;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.when;

import java.util.Date;

import com.gw.tools.ExecutionTool;
import com.gw.tools.HistoryTool;
import com.gw.tools.LocalhostTool;
import com.gw.tools.ProcessTool;
import com.gw.tools.UserSession;
import com.gw.tools.UserTool;
import com.gw.utils.BaseTool;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OtherFunctionalTest {

	@InjectMocks
	@Autowired
	BaseTool bt;

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

    @InjectMocks
	@Autowired
	UserTool utmock;


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
    void testUserAuth() throws Exception{

		utmock = Mockito.spy(utmock);
		// doNothing().when(utmock).authenticate(anyString());
        assertFalse(utmock.isAuth("nonauthorizeduser", "non-ip"));

        

    }

    @Test
    void testUserSession(){

        utmock = Mockito.spy(utmock);

        utmock.bindSessionUser("xyzsession", "xyzuser", "x.x.x.x");
        
        when(utmock.isAuth("xyzsession", "x.x.x.x")).thenReturn(true);

        assertEquals(utmock.getAuthUserId("xyzsession", "x.x.x.x"), "xyzuser");

        UserSession us = new UserSession();
        us.setJssessionid("xyzsession_expired");
        us.setIp_address("x.x.x.x");
        long oldtime = new Date().getTime() - 25*60*60*1000;
        us.setCreated_time(new Date(oldtime));
        utmock.authsession2user.add(us);
        
        utmock.cleanExpiredAuth();
        when(utmock.isAuth("xyzsession_expired", "x.x.x.x")).thenReturn(true);
        assertEquals(utmock.getAuthUserId("xyzsession_expired", "x.x.x.x"), "111111");

    }


    
}
