package com.gw.server;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import org.apache.log4j.Logger;
import org.apache.tomcat.websocket.WsSession;
import com.gw.utils.BaseTool;
import com.gw.utils.BeanTool;
import com.gw.utils.CommunicationConfig;

/**
 * This class is used for monitoring workflow execution
 *
 * @author JensenSun
 */
// ws://localhost:8080/Geoweaver/jupyter-socket/api/kernels/884447f1-bac6-4913-be86-99da11b2a78a/channels?session_id=42b8261488884e869213604975141d8c
@ServerEndpoint(value = "/workflow-socket")
public class WorkflowServlet {

  Logger logger = Logger.getLogger(WorkflowServlet.class);

  // private Session wsSession;

  static Map<String, Session> peers = new HashMap();

  //    private HttpSession httpSession;

  @OnOpen
  public void open(Session session, EndpointConfig config) {

    try {

      logger.debug("Workflow-Socket websocket channel openned");

      // this.wsSession = session;

      // session.setMaxIdleTimeout(0);

      // this.registerSession(session);

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void registerSession(Session session, String token) {

    WsSession wss = (WsSession) session;

    logger.debug("Web Socket Session ID:" + wss.getHttpSessionId());

    // List<String> originHeader = (List<String>)session.getUserProperties()
    // .get("TheUpgradeOrigin");

    // if(wss.getHttpSessionId()==null){
    // 	throw new RuntimeException("The HTTP Session ID shouldn't be null.");
    // }else{

    // logger.debug("Websocket original headers: " + originHeader);

    // Session existingsession = WorkflowServlet.findSessionByToken(wss.getHttpSessionId());

    // if(existingsession==null || !existingsession.isOpen()){

    // peers.put(wss.getHttpSessionId(), session);

    // }
    // }

    peers.put(token, session);
  }

  @OnError
  public void error(final Session session, final Throwable throwable) throws Throwable {

    logger.error("websocket channel error" + throwable.getLocalizedMessage());

    throw throwable;
  }

  @OnMessage
  public void echo(String message, Session session) {

    try {

      logger.debug("Received message: " + message);

      if (message.indexOf("token:") != -1) {

        message = message.substring(6);

        this.registerSession(session, message);
      }

      session.getBasicRemote().sendText("Session_Status:Active");

      // String received = session.getQueryString();

      // if(message!=null && message.startsWith("token:")){

      // 	message = message.substring(6);

      // 	logger.debug(" - Token: " + message);

      // 	WsSession wss = (WsSession) session;

      // 	logger.debug("Web Socket Session ID:" + wss.getHttpSessionId());

      // 	peers.put(message, session);

      // }

    } catch (Exception e) {

      e.printStackTrace();
    }
  }

  @OnClose
  public void close(final Session session) {

    try {

      logger.info("Channel closed.");

      WsSession wss = (WsSession) session;

    } catch (Exception e) {

      e.printStackTrace();
    }
  }

  /**
   * Finds a WebSocket session by its token.
   *
   * @param token The token used to identify the WebSocket session.
   * @return The WebSocket session, or null if not found.
   */
  public static javax.websocket.Session findSessionByToken(String token) {
    javax.websocket.Session se = null;
    if (token != null && peers.containsKey(token)) {
      se = peers.get(token);
    }
    return se;
  }
  
  /**
   * Send a message to a specific client identified by token.
   *
   * @param token The client token
   * @param message The message to send
   * @return The WebSocket session if successful, null otherwise
   */
  public static Session sendMessageToSocket(String token, String message) {
    if (token == null || token.isEmpty() || message == null) {
      Logger.getLogger(WorkflowServlet.class).warn("Attempted to send message with null token or message");
      return null;
    }

    String channelUsed = "none";

    try {
      // Use long polling as the primary communication method
      boolean longPollingSuccess = sendViaLongPolling(token, message);
      if (longPollingSuccess) {
        Logger.getLogger(WorkflowServlet.class).debug("Message sent via long polling");
        channelUsed = "longpolling";
      } else {
        // For backward compatibility, try WebSocket as fallback
        Session wsout = findSessionByToken(token);
        if (wsout != null && wsout.isOpen()) {
          wsout.getBasicRemote().sendText(message);
          channelUsed = "websocket";
          return wsout;
        }
      }
    } catch (Exception e) {
      Logger.getLogger(WorkflowServlet.class).error("Failed to send message: " + e.getMessage());
    }

    Logger.getLogger(WorkflowServlet.class).debug("Message sent via: " + channelUsed);
    return null;
  }
  
  /**
   * Attempts to send a message via WebSocket.
   * 
   * @param token The client token
   * @param message The message to send
   * @return true if the message was sent successfully, false otherwise
   */
  private static boolean sendViaWebSocket(String token, String message) {
    if (token == null || token.isEmpty() || message == null) {
      Logger.getLogger(WorkflowServlet.class).error("Cannot send WebSocket message: token or message is null/empty");
      return false;
    }
    
    try {
      // Find the WebSocket session by token
      Session wsout = WorkflowServlet.findSessionByToken(token);
      
      // Check if session exists
      if (wsout == null) {
        Logger.getLogger(WorkflowServlet.class).warn("WebSocket session not found for token: " + token);
        return false;
      }
      
      // Check if session is open
      if (!wsout.isOpen()) {
        Logger.getLogger(WorkflowServlet.class).warn("WebSocket session found but not open for token: " + token);
        // Remove the closed session from the peers map
        WorkflowServlet.removeSessionById(token);
        return false;
      }
      
      // Send the message
      wsout.getBasicRemote().sendText(message);
      Logger.getLogger(WorkflowServlet.class).debug("Message sent via WebSocket to token: " + token);
      return true;
      
    } catch (IOException e) {
      Logger.getLogger(WorkflowServlet.class).error("WebSocket communication error for token '" + token + "': " + e.getMessage());
      e.printStackTrace();
      return false;
    }
  }
  
  /**
   * Attempts to send a message via HTTP long polling.
   * 
   * @param token The client token
   * @param message The message to send
   * @return true if the message was sent successfully, false otherwise
   */
  private static boolean sendViaLongPolling(String token, String message) {
    if (token == null || token.isEmpty() || message == null) {
      Logger.getLogger(WorkflowServlet.class).error("Cannot send long polling message: token or message is null/empty");
      return false;
    }
    
    try {
      // Get the LongPollingController bean from Spring context
      LongPollingController longPollingController = 
          BeanTool.getBean(LongPollingController.class);
      
      if (longPollingController == null) {
        Logger.getLogger(WorkflowServlet.class).error("Could not get LongPollingController bean");
        return false;
      }
      
      boolean sent = longPollingController.sendMessageToClient(token, message);
      if (sent) {
        Logger.getLogger(WorkflowServlet.class).debug("Message sent via long polling to token: " + token);
        return true;
      } else {
        Logger.getLogger(WorkflowServlet.class).error("Failed to send message via long polling to token: " + token);
        return false;
      }
    } catch (Exception e) {
      Logger.getLogger(WorkflowServlet.class).error("Error using long polling for token '" + token + "': " + e.getMessage());
      e.printStackTrace();
      return false;
    }
  }

  /**
   * Removes a WebSocket session from the peers map based on its token.
   *
   * @param token The token of the WebSocket session to be removed.
   */
  public static void removeSessionById(String token) {
    if (token == null || token.isEmpty()) {
      Logger.getLogger(WorkflowServlet.class).warn("Cannot remove session with null or empty token");
      return;
    }
    
    boolean existed = peers.containsKey(token);
    peers.remove(token);
    
    if (existed) {
      Logger.getLogger(WorkflowServlet.class).debug("WebSocket session removed for token: " + token);
    } else {
      Logger.getLogger(WorkflowServlet.class).debug("No WebSocket session found to remove for token: " + token);
    }
  }
}
