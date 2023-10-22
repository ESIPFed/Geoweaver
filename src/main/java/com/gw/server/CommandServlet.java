package com.gw.server;

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

import com.gw.ssh.SSHSession;
import com.gw.utils.BaseTool;
import com.gw.web.GeoweaverController;

/**
 * CommandServlet is a WebSocket server endpoint responsible for managing WebSocket connections and handling non-terminal SSH-related messages.
 *
 * @author JensenSun
 */
@ServerEndpoint(value = "/command-socket")
public class CommandServlet {

    // Logger for logging WebSocket events
    static Logger logger = LoggerFactory.getLogger(CommandServlet.class);

    // List of commands that trigger user logout
    private List<String> logoutCommands = Arrays.asList(new String[]{"logout", "quit"});

    // A map to store WebSocket sessions associated with user tokens
    static Map<String, Session> peers = new HashMap();

    /**
     * Called when a new WebSocket connection is opened.
     *
     * @param session   The WebSocket session that was opened.
     * @param config    Configuration for the WebSocket session.
     */
    @OnOpen
    public void open(Session session, EndpointConfig config) {
        try {
            logger.debug("Command-socket WebSocket channel opened");
            
            // You can register the session here for further tracking
            // For example: this.registerSession(session);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Registers a user's WebSocket session using their access token.
     *
     * @param session   The WebSocket session to register.
     * @param token     The user's access token, which is used as a key to associate the session with the user.
     */
    public void registerSession(Session session, String token) {
        // Cast the session to a WsSession
        WsSession wss = (WsSession) session;

        // Associate the session with the user's token in the map
        peers.put(token, wss);
    }


    /**
     * Handles errors that occur during WebSocket communication. It logs the error message and rethrows it.
     *
     * @param session     The WebSocket session where the error occurred.
     * @param throwable   The error that occurred.
     * @throws Throwable   The rethrown error for further handling.
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
     * Handles incoming WebSocket messages, processes them, and communicates with SSH sessions if required.
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
            }

            // If the token is still null, log the session ID for debugging
            if (tokenfromclient == null) {
                logger.debug("Session ID: " + session.getQueryString());

                // Retrieve the SSH session associated with the WebSocket session ID
                SSHSession sshSession = GeoweaverController.sessionManager.sshSessionByToken.get(session.getId());

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
                        // Check if the WebSocket session is open and send a message indicating no active SSH connection
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


    /**
     * Prints the call stack trace for debugging purposes.
     */
    public void printoutCallStack() {
        System.out.println("Printing stack trace:");
        // Get the call stack trace elements
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        for (int i = 1; i < elements.length; i++) {
            StackTraceElement s = elements[i];
            System.out.println("\tnull websocket trace at " + s.getClassName() + "." + s.getMethodName() + "(" + s.getFileName() + ":" + s.getLineNumber() + ")");
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
                SSHSession sshSession = GeoweaverController.sessionManager.sshSessionByToken.get(session.getId());
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
        javax.websocket.Session se = null;
        if (peers.containsKey(token)) {
            se = peers.get(token);
        }
        return se;
    }

    /**
     * Sends a message to a specific WebSocket session identified by its token.
     *
     * @param token   The token of the target WebSocket session.
     * @param message The message to be sent.
     */
    public static Session sendMessageToSocket(String token, String message) {
        try {
            Session wsout = CommandServlet.findSessionById(token);
            if (!BaseTool.isNull(wsout)){
                if(wsout.isOpen()) {
                    wsout.getBasicRemote().sendText(message);
                }else{
                    CommandServlet.removeSessionById(token);
                }
            }else{
                logger.warn(String.format("cannot find websocket for token %s", token));
            }
            return wsout;
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return null;
    }

    /**
     * Removes a WebSocket session from the peers map based on its token.
     *
     * @param token The token of the WebSocket session to be removed.
     */
    public static void removeSessionById(String token) {
        peers.remove(token);
    }

    /**
     * Clears all WebSocket sessions from the peers map.
     */
    public static void cleanAll() {
        peers.clear();
    }


}
