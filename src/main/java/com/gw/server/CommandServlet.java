package com.gw.server;

import com.gw.ssh.SSHSession;
import com.gw.utils.BaseTool;
import com.gw.web.GeoweaverController;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import org.apache.tomcat.websocket.WsSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CommandServlet is a WebSocket server endpoint responsible for managing WebSocket connections and
 * handling non-terminal SSH-related messages.
 *
 * @author JensenSun
 */
@ServerEndpoint(value = "/command-socket")
public class CommandServlet {

  // Logger for logging WebSocket events
  static Logger logger = LoggerFactory.getLogger(CommandServlet.class);

  // List of commands that trigger user logout
  private List<String> logoutCommands = Arrays.asList(new String[] {"logout", "quit"});

  // A map to store WebSocket sessions associated with user tokens
  static Map<String, Session> peers = new HashMap();

  /**
   * Called when a new WebSocket connection is opened.
   *
   * @param session The WebSocket session that was opened.
   * @param config Configuration for the WebSocket session.
   */
  @OnOpen
  public void open(Session session, EndpointConfig config) {
    try {
      logger.debug("Command-socket WebSocket channel opened");

      // Store the session ID temporarily until we receive a token from the client
      // The actual registration with a token happens in the echo method when a token message is received
      logger.debug("WebSocket session opened with ID: " + session.getId());
      
      // We don't register the session here because we don't have a token yet
      // The session will be registered when a token message is received in the echo method
    } catch (Exception e) {
      logger.error("Error opening WebSocket connection: " + e.getMessage());
      e.printStackTrace();
    }
  }

  /**
   * Registers a user's WebSocket session using their access token.
   *
   * @param session The WebSocket session to register.
   * @param token The user's access token, which is used as a key to associate the session with the
   *     user.
   */
  public void registerSession(Session session, String token) {
    if (session == null || token == null || token.isEmpty()) {
      logger.warn("Cannot register null session or empty token");
      return;
    }
    
    // Cast the session to a WsSession
    WsSession wss = (WsSession) session;
    
    // Log the registration for debugging
    logger.debug("Registering WebSocket session for token: " + token);
    
    // Associate the session with the user's token in the map
    peers.put(token, wss);
    
    // Verify registration was successful
    if (peers.containsKey(token)) {
      logger.debug("WebSocket session successfully registered for token: " + token);
    } else {
      logger.error("Failed to register WebSocket session for token: " + token);
    }
  }

  /**
   * Handles errors that occur during WebSocket communication. It logs the error message and
   * rethrows it.
   *
   * @param session The WebSocket session where the error occurred.
   * @param throwable The error that occurred.
   * @throws Throwable The rethrown error for further handling.
   */
  @OnError
  public void error(final Session session, final Throwable throwable) throws Throwable {
    try {
      // Log the error message and details
      logger.error("WebSocket channel error: " + throwable.getLocalizedMessage());

      // Rethrow the error to propagate it further, e.g., for handling at a higher level
      throw throwable;
    } catch (Throwable e) {
      e.printStackTrace();
    }
  }

  /**
   * Handles incoming WebSocket messages, processes them, and communicates with SSH sessions if
   * required.
   *
   * @param message The incoming message.
   * @param session The WebSocket session where the message was received.
   */
  @OnMessage
  public void echo(String message, Session session) {
    try {
      // Log a debug message indicating that a message has been received
      logger.debug("Received message: " + message);
      String tokenfromclient = null;

      // Check if the received message is not null
      if (message != null) {
        // Check if the message starts with "history_id:"
        if (message.startsWith("history_id:")) {
          // Extract the token from the message (substring from the 11th character)
          tokenfromclient = message.substring(11);
          // Log the history ID for debugging
          logger.debug(" - History ID: " + tokenfromclient);
        }
        // Check if the message starts with "token:"
        else if (message.startsWith("token:")) {
          // Extract the token from the message (substring from the 6th character)
          tokenfromclient = message.substring(6);
          // Log the token for debugging
          logger.debug(" - Token: " + tokenfromclient);
          // Register the WebSocket session with the token
          this.registerSession(session, tokenfromclient);
        }
        // Check if the message starts with "execution:"
        else if (message.startsWith("execution:")) {
          // Extract the history ID from the message (substring from the 10th character)
          String historyId = message.substring(10);
          // Log the execution history ID for debugging
          logger.debug(" - Execution History ID: " + historyId);
          // Register the WebSocket session with the history ID
          this.registerSession(session, historyId);
          // Send confirmation message back to client
          if (session.isOpen()) {
            session.getBasicRemote().sendText(historyId + BaseTool.log_separator + "WebSocket session connected to execution: " + historyId);
          }
        }
      }

      // If the token is still null, log the session ID for debugging
      if (tokenfromclient == null) {
        logger.debug("Session ID: " + session.getQueryString());

        // Retrieve the SSH session associated with the WebSocket session ID
        SSHSession sshSession =
            GeoweaverController.sessionManager.sshSessionByToken.get(session.getId());

        // If there's no associated SSH session
        if (sshSession == null) {
          // Log the linkage between WebSocket session and token for debugging
          logger.debug("Linking " + session.getId() + " - " + tokenfromclient);

          // Attempt to retrieve the SSH session using the token
          sshSession = GeoweaverController.sessionManager.sshSessionByToken.get(tokenfromclient);

          // If an SSH session is found, associate it with the WebSocket session
          if (sshSession != null) {
            GeoweaverController.sessionManager.sshSessionByToken.put(session.getId(), sshSession);
          }
          // If no SSH session is found
          else {
            // Check if the WebSocket session is open and send a message indicating no active SSH
            // connection
            if (session.isOpen()) {
              session.getBasicRemote().sendText("No SSH connection is active");
            }
          }
        }
        // If there's an associated SSH session
        else {
          // Log the message received in the SSH session
          logger.debug("Message in " + session.getId() + ": " + message);
          // Write the message to the SSH session's output stream and flush it
          sshSession.getSSHOutput().write((message + '\n').getBytes());
          sshSession.getSSHOutput().flush();

          // Check if the received message is a valid logout command
          if (logoutCommands.contains(message.trim().toLowerCase())) {
            // Log the receipt of a valid logout command
            logger.debug("Valid logout command received: " + message);
            // Perform a logout on the SSH session
            sshSession.logout();
          }
        }
      } else {
        // Send a message to confirm that the session is active
        session.getBasicRemote().sendText("Session_Status:Active");
      }
    } catch (Exception e) {
      // Handle any exceptions that might occur during message processing
      e.printStackTrace();
    }
  }

  /** Prints the call stack trace for debugging purposes. */
  public void printoutCallStack() {
    System.out.println("Printing stack trace:");
    // Get the call stack trace elements
    StackTraceElement[] elements = Thread.currentThread().getStackTrace();
    for (int i = 1; i < elements.length; i++) {
      StackTraceElement s = elements[i];
      System.out.println(
          "\tnull websocket trace at "
              + s.getClassName()
              + "."
              + s.getMethodName()
              + "("
              + s.getFileName()
              + ":"
              + s.getLineNumber()
              + ")");
    }
  }

  /**
   * Closes the WebSocket session.
   *
   * @param session The WebSocket session to be closed.
   */
  @OnClose
  public void close(final Session session) {
    try {
      logger.debug("Geoweaver Shell Channel closed.");
      logger.debug("WebSocket session closed: " + session.getId());

      // Close the associated SSH session
      if (GeoweaverController.sessionManager != null) {
        SSHSession sshSession =
            GeoweaverController.sessionManager.sshSessionByToken.get(session.getId());
        if (sshSession != null && sshSession.isTerminal()) { // Only close when it is a shell
          sshSession.logout();
        }
        GeoweaverController.sessionManager.sshSessionByToken.remove(session.getId());
      }

      // Remove the WebSocket session from the peers map
      // WsSession wss = (WsSession) session;
      // peers.remove(wss.getHttpSessionId());
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
  public static javax.websocket.Session findSessionById(String token) {
    if (token == null || token.isEmpty()) {
      logger.warn("Cannot find session with null or empty token");
      return null;
    }
    
    javax.websocket.Session se = null;
    if (peers.containsKey(token)) {
      se = peers.get(token);
      if (se == null) {
        logger.warn("Session found in peers map for token '" + token + "' but is null");
      }
    } else {
      logger.debug("No WebSocket session found for token: " + token);
    }
    return se;
  }

  /**
   * Sends a message to a specific WebSocket session identified by its token.
   * Uses the configured default communication channel (WebSocket or HTTP long polling)
   * based on the geoweaver.communication.default-channel property.
   *
   * @param token The token of the target WebSocket session.
   * @param message The message to be sent.
   * @return The WebSocket session if successful via WebSocket, null if sent via long polling or failed.
   */
  public static Session sendMessageToSocket(String token, String message) {
    if (token == null || token.isEmpty() || message == null) {
      logger.error("Cannot send message: token or message is null/empty");
      return null;
    }
    
    boolean messageSent = false;
    Session wsout = null;
    String channelUsed = "none";
    
    // Get the communication config to determine which channel to try first
    com.gw.utils.CommunicationConfig communicationConfig = null;
    try {
      communicationConfig = com.gw.utils.BeanTool.getBean(com.gw.utils.CommunicationConfig.class);
    } catch (Exception e) {
      logger.warn("Could not get CommunicationConfig bean, defaulting to WebSocket first");
    }
    
    boolean tryWebSocketFirst = communicationConfig == null || communicationConfig.isWebSocketDefault();
    
    // Try the configured default channel first
    if (tryWebSocketFirst) {
      // Try WebSocket first
      boolean webSocketSuccess = sendViaWebSocket(token, message);
      if (webSocketSuccess) {
        wsout = findSessionById(token);
        messageSent = true;
        channelUsed = "websocket";
        logger.debug("Message sent successfully via WebSocket to token: " + token);
      } else {
        // Fallback to long polling
        boolean longPollingSuccess = sendViaLongPolling(token, message);
        if (longPollingSuccess) {
          messageSent = true;
          channelUsed = "longpolling";
          logger.debug("Message sent successfully via long polling to token: " + token);
        }
      }
    } else {
      // Try long polling first
      boolean longPollingSuccess = sendViaLongPolling(token, message);
      if (longPollingSuccess) {
        messageSent = true;
        channelUsed = "longpolling";
        logger.debug("Message sent successfully via long polling to token: " + token);
      } else {
        // Fallback to WebSocket
        boolean webSocketSuccess = sendViaWebSocket(token, message);
        if (webSocketSuccess) {
          wsout = findSessionById(token);
          messageSent = true;
          channelUsed = "websocket";
          logger.debug("Message sent successfully via WebSocket to token: " + token);
        }
      }
    }
    
    if (!messageSent) {
      logger.error("Failed to send message to token '" + token + "' via any communication channel");
    }
    
    return wsout; // Only return non-null if WebSocket was used successfully
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
      logger.error("Cannot send WebSocket message: token or message is null/empty");
      return false;
    }
    
    try {
      // Find the WebSocket session by token
      Session wsout = CommandServlet.findSessionById(token);
      
      // Check if session exists
      if (wsout == null) {
        logger.warn("WebSocket session not found for token: " + token);
        return false;
      }
      
      // Check if session is open
      if (!wsout.isOpen()) {
        logger.warn("WebSocket session found but not open for token: " + token);
        // Remove the closed session from the peers map
        CommandServlet.removeSessionById(token);
        return false;
      }
      
      // Send the message
      wsout.getBasicRemote().sendText(message);
      logger.debug("Message sent via WebSocket to token: " + token);
      return true;
      
    } catch (IOException e) {
      logger.error("WebSocket communication error for token '" + token + "': " + e.getMessage());
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
      logger.error("Cannot send long polling message: token or message is null/empty");
      return false;
    }
    
    try {
      // Get the LongPollingController bean from Spring context
      LongPollingController longPollingController = 
          com.gw.utils.BeanTool.getBean(LongPollingController.class);
      
      if (longPollingController == null) {
        logger.error("Could not get LongPollingController bean");
        return false;
      }
      
      boolean sent = longPollingController.sendMessageToClient(token, message);
      if (sent) {
        logger.debug("Message sent via long polling to token: " + token);
        return true;
      } else {
        logger.error("Failed to send message via long polling to token: " + token);
        return false;
      }
    } catch (Exception e) {
      logger.error("Error using long polling for token '" + token + "': " + e.getMessage());
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
      logger.warn("Cannot remove session with null or empty token");
      return;
    }
    
    boolean existed = peers.containsKey(token);
    peers.remove(token);
    
    if (existed) {
      logger.debug("WebSocket session removed for token: " + token);
    } else {
      logger.debug("No WebSocket session found to remove for token: " + token);
    }
  }

  /** Clears all WebSocket sessions from the peers map. */
  public static void cleanAll() {
    peers.clear();
  }
}
