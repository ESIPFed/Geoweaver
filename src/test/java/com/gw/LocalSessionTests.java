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
import com.gw.local.LocalSessionNixImpl;
import com.gw.local.LocalSessionWinImpl;
import com.gw.ssh.SSHSession;

import org.apache.log4j.Logger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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

import net.bytebuddy.utility.RandomString;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = GeoweaverApplication.class)
public class LocalSessionTests extends AbstractHelperMethodsTest {

    LocalSession ls;

    LocalSessionNixImpl lsN;

    @Autowired
    private TestRestTemplate testrestTemplate;

    @LocalServerPort
    private int port;

    @Autowired
    SSHSession session;

    @Test
    void testRunJupyter() throws Exception {

        HttpHeaders hostHeaders = new HttpHeaders();
        hostHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Add host
        String bultinjson = bt.readStringFromFile(bt.testResourceFiles() + "/add_jupyter_host.txt");
        HttpEntity hostRequest = new HttpEntity<>(bultinjson, hostHeaders);
        String hostResult = this.testrestTemplate.postForObject(
                "http://localhost:" + this.port + "/Geoweaver/web/add",
                hostRequest,
                String.class);

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = mapper.readValue(hostResult, Map.class);
        String jid = String.valueOf(map.get("id"));

        String jupyterBody = bt.readStringFromFile(bt.testResourceFiles() + "/add_jupyter_body.json");

        String historyid = new RandomString(12).nextString();

        session.runJupyter(historyid, jupyterBody, jid, false, "", "", "", historyid);

    }

    @Test
    void testRunBash() throws JsonMappingException, JsonProcessingException {

        try {

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String shelljson = bt.readStringFromFile(bt.testResourceFiles() + "/add_shell_process.json");
            HttpEntity request = new HttpEntity<>(shelljson, headers);
            String result = this.testrestTemplate.postForObject(
                    "http://localhost:" + this.port + "/Geoweaver/web/add/process",
                    request,
                    String.class);
            logger.debug("the result is: " + result);
            // assertThat(controller).isNotNull();
            assertThat(result).contains("id");

            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> map = mapper.readValue(result, Map.class);
            String pid = String.valueOf(map.get("id"));

            String historyid = new RandomString(12).nextString();

            // lsN.runBash(historyid, "echo", pid, true, "");
            lsN.runBash(historyid, "echo", pid, false, "");

        } catch (Exception e) {
            // TODO: handle exception
        }

    }

    @Test
    void testSaveHistory() {

        try {

            lsN.saveHistory("", "");

        } catch (Exception e) {
            // TODO: handle exception
        }

    }

    @Test
    void testClean() {

        try {

            lsN.clean();

        } catch (Exception e) {
            // TODO: handle exception
        }

    }

    @Test
    void testSettersGettersNix() {

        LocalSessionNixImpl instance = new LocalSessionNixImpl();

        instance.setHistory(null);
        instance.getToken();
        instance.getHistory();
        instance.getLocalInput();
        instance.isClose();
        instance.isTerminal();

    }

    @Test
    void testSettersGettersWin() {

        LocalSessionWinImpl instance = new LocalSessionWinImpl();

        instance.setHistory(null);
        instance.getToken();
        instance.getHistory();
        instance.getLocalInput();
        instance.isClose();
        instance.isTerminal();

    }

}
