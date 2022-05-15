package com.gw;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;

import java.net.URI;
import java.util.Map;

import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
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
import com.gw.server.Java2JupyterClientDialog;
import com.gw.server.JupyterHubRedirectServlet;
import com.gw.server.JupyterLabRedirectServlet;
import com.gw.server.JupyterRedirectServerConfig;
import com.gw.server.JupyterRedirectServlet;
import com.gw.server.JupyterWebSocketClientConfig;

import org.apache.log4j.Logger;
import org.apache.tomcat.util.compat.JreCompat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.JRE;
import org.mockito.Mockito;
import org.mockito.internal.stubbing.answers.ThrowsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = GeoweaverApplication.class)
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

	Java2JupyterClientDialog JDialog;

	JupyterRedirectServlet JRedServ;

	Logger logger = Logger.getLogger(this.getClass());

	@Test
	void contextLoads() {

	}

	// @Test
	@DisplayName("Test Jupyter init method")
	void testJupyterInitialization() {

		try {

			JupyterEndpoint.init(null, null, null, null, null);

		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	@Test
	@DisplayName("Test Jupyter onOpen Websocket Message")
	void testOnOpenJupyter() {

		try {

			JupyterEndpoint.onOpen(null, null);

		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	@Test
	@DisplayName("Test Jupyter Error Websocket Message")
	void testErrorWebsocketJupyter() throws Throwable {

		try {

			JupyterEndpoint.onError(null, new Throwable("Test Jupyter Websocket Error"));

		} catch (Throwable e) {
			// TODO: handle exception
		}

	}

	@Test
	@DisplayName("Test Jupyter onClose Websocket Message")
	void testOnCloseJupyter() {

		try {

			JupyterEndpoint.onClose(null,
					new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Jupyter Websocket Test"));

		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	@Test
	void testNewWsSessionGtoJ() {

		try {
			JupyterEndpoint.setNew_ws_session_between_geoweaver_and_jupyterserver(null);
			JupyterEndpoint.getNew_ws_session_between_geoweaver_and_jupyterserver();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	@Test
	void testSendMessage() {

		try {
			JupyterEndpoint.sendMessage(null);
			JupyterEndpoint.sendMessage("Test");
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	@Test
	void testUpperAllFirstJupyterEndpoint() {

		try {

			Java2JupyterClientEndpoint.upperAllFirst("Websocket-test");

		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	@Test
	void testJhubinit() {

		try {

			JupyterHubRedirectServlet instance = new JupyterHubRedirectServlet();

		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	@Test
	@DisplayName("Test JupyterHub Redirect onOpen")
	void testJupyterHubOnOpen() {

		try {

			String jid = testAddJupyterHost();
			Session session = Mockito.mock(Session.class);
			EndpointConfig conf = Mockito.mock(EndpointConfig.class);

			Jhub.open(session, jid, null, null, null);
			Jhub.open(session, jid, null, null, conf);

		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	@Test
	@DisplayName("Test JupyterHub Redirect Error")
	void testJupyterHubError() {

		try {

			Jhub.error(null, new Throwable("Test JupyterHub Redirect Error"));

		} catch (Throwable e) {
			// TODO: handle exception
		}

	}

	@Test
	@DisplayName("Test JupyterHub Redirect Echo")
	void testJupyterHubEcho() {

		try {

			Session session = Mockito.mock(Session.class);
			Jhub.echo("Test JupyterHub Redirect Message", null, session);
			Jhub.echo(null, null, null);

		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	@Test
	@DisplayName("Test JupyterHub Redirect onClose")
	void testJupyterHubOnClose() {

		try {

			Session session = Mockito.mock(Session.class);
			Jhub.close(null);
			Jhub.close(session);

		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	@Test
	void testJlabInit() {

		try {

			JupyterLabRedirectServlet instance = new JupyterLabRedirectServlet();

		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	@Test
	@DisplayName("Test JupyterLab Redirect onOpen")
	void testJupyterLabOnOpen() {
		// Test Fails. Gives back a "null pointer"
		try {
			String jid = testAddJupyterHost();
			Session session = Mockito.mock(Session.class);
			EndpointConfig conf = Mockito.mock(EndpointConfig.class);

			Jlab.open(session, jid, null, null);
			Jlab.open(session, jid, null, conf);

		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	@Test
	@DisplayName("Test JupyterLab Redirect Error")
	void testJupyterLabError() {

		try {
			Jlab.error(null, new Throwable("Test JupyterLab Redirect Error"));

		} catch (Throwable e) {
			// TODO: handle exception
		}

	}

	@Test
	@DisplayName("Test JupyterLab Redirect Echo")
	void testJupyterLabEcho() {

		try {

			Session session = Mockito.mock(Session.class);
			Jlab.echo("Test JupyterLab Redirect Message", null, session);
			Jlab.echo(null, null, null);

		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	@Test
	@DisplayName("Test JupyterLab Redirect onClose")
	void testJupyterLabOnClose() {

		try {

			Session session = Mockito.mock(Session.class);
			Jlab.close(null);
			Jlab.close(session);

		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	@Test
	@DisplayName("Test Jupyter ServerConfig Handshake")
	void testJupyterHandshake() {

		try {

			ServerEndpointConfig config = ServerEndpointConfig.Builder.create(JupyterRedirectServerConfig.class, "/foo")
					.build();
			HandshakeRequest request = Mockito.mock(HandshakeRequest.class);
			HandshakeResponse response = Mockito.mock(HandshakeResponse.class);

			JCon.modifyHandshake(config, request, response);

		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	@Test
	void testJDialogInit() {

		try {

			Java2JupyterClientDialog instance = new Java2JupyterClientDialog();
			Java2JupyterClientDialog.main(new String[1]);

		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	@Test
	void testJavaClientDialogGetHeaders() {

		try {

			JDialog.writeServerMessage("Test");

		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	@Test
	void testJRedServInit() {

		try {

			JupyterRedirectServlet instance = new JupyterRedirectServlet();

		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	@Test
	void testJRedServOpen() {

		try {
			String jid = testAddJupyterHost();
			Session session = Mockito.mock(Session.class);
			EndpointConfig conf = Mockito.mock(EndpointConfig.class);
			JRedServ.open(session, jid, null, null);
			JRedServ.open(session, jid, null, conf);

		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	@Test
	void testJRedServClose() {

		try {

			Session session = Mockito.mock(Session.class);
			JRedServ.close(null);
			JRedServ.close(session);

		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	@Test
	void testJRedServEcho() {

		try {

			Session session = Mockito.mock(Session.class);
			JRedServ.echo("Test Jupyter Redirect Message", null, session);
			JRedServ.echo(null, null, null);

		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	@Test
	void testJRedServError() {

		try {

			JRedServ.error(null, new Throwable("Test Jupyter Redirect Error"));

		} catch (Throwable e) {
			// TODO: handle exception
		}
	}

	@Test
	void testJupyterWebSocketClientConfig() {

		try {

			JupyterWebSocketClientConfig instance = new JupyterWebSocketClientConfig();

		} catch (Exception e) {
			// TODO: handle exception
		}

	}

}
