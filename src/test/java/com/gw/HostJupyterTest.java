package com.gw;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;

import java.net.URI;
import java.util.Map;

import javax.websocket.CloseReason;
import javax.websocket.Session;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gw.tools.HistoryTool;
import com.gw.tools.LocalhostTool;
import com.gw.tools.UserTool;
import com.gw.utils.BaseTool;
import com.gw.local.LocalSession;
import com.gw.server.Java2JupyterClientEndpoint;
import com.gw.server.JupyterHubRedirectServlet;
import com.gw.server.JupyterLabRedirectServlet;
import com.gw.server.JupyterRedirectServerConfig;

import org.apache.log4j.Logger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HostJupyterTest extends AbstractHelperMethodsTest {

	@Autowired
	UserTool ut;

	@Autowired
	BaseTool bt;

	@Autowired
	private TestRestTemplate testrestTemplate;

	@LocalServerPort
	private int port;

	@Autowired
	HistoryTool hist;

	@Autowired
	Java2JupyterClientEndpoint JupyterEndpoint;

	@Autowired
	LocalhostTool lc;

	@Autowired
	JupyterHubRedirectServlet Jhub;

	@Autowired
	JupyterLabRedirectServlet Jlab;

	JupyterRedirectServerConfig JCon;

	Logger logger = Logger.getLogger(this.getClass());

	@Test
	void contextLoads() {

	}

	// @Test
	@DisplayName("Test Jupyter init method")
	void testJupyterInitialization() {
		// Test Fails. Needs a proper URI to connect to.

		JupyterEndpoint.init(URI.create("ws://"), null, null, null, "");
	}

	@Test
	@DisplayName("Test Jupyter onOpen Websocket Message")
	void testOnOpenJupyter() {

		Session session = Mockito.mock(Session.class);
		JupyterEndpoint.onOpen(session, null);
	}

	@Test
	@DisplayName("Test Jupyter Error Websocket Message")
	void testErrorWebsocketJupyter() throws Throwable {

		Session session = Mockito.mock(Session.class);

		JupyterEndpoint.onError(session, new Throwable("Test Jupyter Websocket Error"));

	}

	@Test
	@DisplayName("Test Jupyter onClose Websocket Message")
	void testOnCloseJupyter() {

		Session session = Mockito.mock(Session.class);
		JupyterEndpoint.onClose(session,
				new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Jupyter Websocket Test"));
	}

	// @Test
	@DisplayName("Test JupyterHub Redirect onOpen")
	void testJupyterHubOnOpen() {
		// Test Fails. Gives back a "null pointer"

		Session session = Mockito.mock(Session.class);

		Jhub.open(session, "111111", null, null, null);
	}

	@Test
	@DisplayName("Test JupyterHub Redirect Error")
	void testJupyterHubError() {

		Session session = Mockito.mock(Session.class);

		assertThrows(Throwable.class, () -> Jhub.error(session, new Throwable("Test JupyterHub Redirect Error")));

	}

	@Test
	@DisplayName("Test JupyterHub Redirect Echo")
	void testJupyterHubEcho() {

		Session session = Mockito.mock(Session.class);

		Jhub.echo("Test JupyterHub Redirect Message", null, session);
	}

	// @Test
	@DisplayName("Test JupyterHub Redirect onClose")
	void testJupyterHubOnClose() {
		// For some reason the close() method is rejecting the mocked session.
		// Throws an error of a null pointer.

		Session session = Mockito.mock(Session.class);

		Jhub.close(session);
	}

	// @Test
	@DisplayName("Test JupyterLab Redirect onOpen")
	void testJupyterLabOnOpen() {
		// Test Fails. Gives back a "null pointer"
		Session session = Mockito.mock(Session.class);

		Jlab.open(session, null, null, null);
	}

	@Test
	@DisplayName("Test JupyterLab Redirect Error")
	void testJupyterLabError() {

		Session session = Mockito.mock(Session.class);

		assertThrows(Throwable.class, () -> Jlab.error(session, new Throwable("Test JupyterLab Redirect Error")));

	}

	@Test
	@DisplayName("Test JupyterLab Redirect Echo")
	void testJupyterLabEcho() {

		Session session = Mockito.mock(Session.class);

		Jlab.echo("Test JupyterLab Redirect Message", null, session);
	}

	// @Test
	@DisplayName("Test JupyterLab Redirect onClose")
	void testJupyterLabOnClose() {
		// For some reason the close() method is rejecting the mocked session.
		// Throws an error of a null pointer.

		Session session = Mockito.mock(Session.class);

		Jlab.close(session);
	}

	// @Test
	@DisplayName("Test Jupyter ServerConfig Handshake")
	void testJupyterHandshake() {
		// Test fails with "NullPointerException"

		HandshakeRequest request = Mockito.mock(HandshakeRequest.class);
		HandshakeResponse response = Mockito.mock(HandshakeResponse.class);
		ServerEndpointConfig conf = Mockito.mock(ServerEndpointConfig.class);
		// JCon = Mockito.spy(JCon);

		JCon.modifyHandshake(conf, request, response);
	}

}
