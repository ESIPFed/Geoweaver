package com.gw.server;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.log4j.Logger;

/**
 * Fan-out channel for workspace inventory events (process/workflow create/update). GUI browsers
 * register with their {@code CLIENT_TOKEN} via {@code token:&lt;id&gt;} — the same pattern as {@link
 * WorkflowServlet}. Agent Bearer tokens are not used on this socket.
 */
@ServerEndpoint(value = "/workspace-socket")
public class WorkspaceServlet {

  private static final Logger logger = Logger.getLogger(WorkspaceServlet.class);

  static final Map<String, Session> peers = new ConcurrentHashMap<>();

  @OnOpen
  public void open(Session session) {
    logger.debug("Workspace-Socket channel opened");
  }

  @OnError
  public void error(Session session, Throwable throwable) {
    logger.warn("Workspace-Socket error: " + throwable.getMessage());
  }

  @OnMessage
  public void onMessage(String message, Session session) throws IOException {
    if (message != null && message.startsWith("token:")) {
      String token = message.substring(6).trim();
      if (!token.isEmpty()) {
        peers.put(token, session);
        session.getBasicRemote().sendText("Session_Status:Active");
        logger.debug("Workspace-Socket registered CLIENT_TOKEN session");
      }
      return;
    }
    session.getBasicRemote().sendText("Session_Status:Active");
  }

  @OnClose
  public void close(Session session) {
    peers.entrySet().removeIf(e -> e.getValue() == session);
  }

  /** Send JSON to every registered open GUI session. */
  public static void broadcast(String message) {
    if (message == null || message.isBlank()) {
      return;
    }
    for (Map.Entry<String, Session> e : peers.entrySet()) {
      Session s = e.getValue();
      if (s == null || !s.isOpen()) {
        peers.remove(e.getKey(), s);
        continue;
      }
      try {
        synchronized (s) {
          s.getBasicRemote().sendText(message);
        }
      } catch (IOException ex) {
        logger.warn("Workspace-Socket send failed for token peer: " + ex.getMessage());
        peers.remove(e.getKey(), s);
      }
    }
  }

  /** Test helper: number of registered peers. */
  public static int peerCount() {
    return peers.size();
  }

  /** Test helper: clear registrations. */
  public static void clearPeersForTests() {
    peers.clear();
  }
}
