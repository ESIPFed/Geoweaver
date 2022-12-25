package com.gw;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import javax.websocket.Session;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gw.ssh.SSHSession;
import com.gw.server.CommandServlet;
import com.gw.server.WebSocketConfig;
import com.gw.server.WebsocketMessageHandler;
import com.gw.tools.UserTool;
import com.gw.utils.BaseTool;

import org.apache.log4j.Logger;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.Before;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = GeoweaverApplication.class)
public class WebsocketTest extends AbstractHelperMethodsTest {

    @Autowired
    UserTool ut;

    @Autowired
    BaseTool bt;

    @Autowired
    private TestRestTemplate testrestTemplate;

    @Autowired
    CommandServlet servlet;

    WebsocketMessageHandler socketHandler;

    @Autowired
    WebSocketConfig socketConfig;

    @LocalServerPort
    private int port;

    Logger logger = Logger.getLogger(this.getClass());

    @Test
    @DisplayName("Test OnOpen Websocket Message")
    void testOnOpenWebsocket() {

        servlet.open(null, null);

    }

    @Test
    @DisplayName("Test registerSession Websocket")
    void testRegisterSession() {

        servlet.registerSession(null, null);

    }

    @Test
    @DisplayName("Test Error Websocket Message")
    void testErrorWebsocket() throws Throwable {

        try {
            servlet.error(null, null);

        } catch (Exception e) {
            // TODO: handle exception
        }

    }

    @Test
    @DisplayName("Test Echo Websocket Message")
    void testEchoWebsocket() {

        Session session = Mockito.mock(Session.class);
        servlet.echo("history_id:", null);
        servlet.echo("token:", null);
        servlet.echo("", session);
    }

    @Test
    @DisplayName("Test printoutCallStack Websocket")
    void testPrintoutCallStack() {

        servlet.printoutCallStack();
    }

    @Test
    @DisplayName("Test Close Websocket")
    void testCloseWebsocket() {

        Session session = Mockito.mock(Session.class);
        servlet.close(session);
    }

    @Test
    @DisplayName("Test findSessionById Websocket")
    void testFindSessionById() {

        CommandServlet.findSessionById("");

    }

    @Test
    @DisplayName("Test removeSessionById Websocket")
    void testRemoveSessionById() {

        CommandServlet.removeSessionById("");
    }

    @Test
    @DisplayName("Test cleanAll Websocket")
    void testCleanAll() {

        CommandServlet.cleanAll();
    }

    @Test
    @DisplayName("Test Websocket Config serverEndpoint")
    void testWebsocketServerEndpoint() {

        socketConfig.serverEndpoint();
    }

    @Test
    @DisplayName("Test Websocket Message Handler")
    void testWebsocketMessageHandler() {
        // Error creating bean with name 'com.gw.WebsocketTest'

        try {
            socketHandler.onMessage("Test Websocket Message");

        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    // ******************************************************************
    // Below is a different approach to websocket testing. Currently is not working.

    static final String WEBSOCKET_URI = "ws://localhost:" + SpringBootTest.WebEnvironment.RANDOM_PORT + "/Geoweaver/";
    static final String WEBSOCKET_TOPIC = "/workflow-socket";

    BlockingQueue<String> blockingQueue;
    WebSocketStompClient stompClient;

    @Before
    public void setup() {
        blockingQueue = new LinkedBlockingDeque<>();
        stompClient = new WebSocketStompClient(new SockJsClient(
                asList(new WebSocketTransport(new StandardWebSocketClient()))));
    }

    private List<Transport> asList(WebSocketTransport webSocketTransport) {
        return null;
    }

    // @Test
    @DisplayName("Test Websocket Connection")
    void testWebsocket() throws Exception {
        // This keeps throwing a "NullPointer" Error.
        // This approach is apparently how unit testing for websockets should be.
        // For some reason, it's not working in its current state.
        // http://rafaelhz.github.io/testing-websockets/

        StompSession session = stompClient
                .connect(WEBSOCKET_URI, new StompSessionHandlerAdapter() {
                })
                .get(1, TimeUnit.SECONDS);
        session.subscribe(WEBSOCKET_TOPIC, new DefaultStompFrameHandler());

        String message = "MESSAGE TEST";
        session.send(WEBSOCKET_TOPIC, message.getBytes());

        assertEquals(message, blockingQueue.poll(1, TimeUnit.SECONDS));

    }

    class DefaultStompFrameHandler implements StompFrameHandler {
        @Override
        public Type getPayloadType(StompHeaders stompHeaders) {
            return byte[].class;
        }

        @Override
        public void handleFrame(StompHeaders stompHeaders, Object o) {
            blockingQueue.offer(new String((byte[]) o));
        }
    }

}
