package com.gw.server;

import jakarta.websocket.EndpointConfig;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/test-socket2/{hostid}/api/kernels/{uuid1}/channels")
public class Test2SocketServlet {

  public Test2SocketServlet() {

    System.out.println("Test2 websocket servlet is created!");
  }

  @OnOpen
  public void open(
      Session session,
      @PathParam("hostid") String hostid,
      @PathParam("uuid1") String uuid1,
      EndpointConfig config) {}

  @OnError
  public void error(final Session session, final Throwable throwable) throws Throwable {}

  @OnMessage(maxMessageSize = 10000000)
  public void echo(String message, @PathParam("uuid1") String uuid1, Session session) {}

  @OnClose
  public void close(final Session session) {}
}
