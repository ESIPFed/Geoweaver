package com.gw.server;

import jakarta.websocket.MessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WebSocket Message Handler
 *
 * @author JensenSun
 * @date 12/31/2020
 */
public class WebsocketMessageHandler implements MessageHandler.Whole<String> {

  jakarta.websocket.Session jssession = null;

  String pairid;

  Logger logger = LoggerFactory.getLogger(this.getClass());

  public WebsocketMessageHandler(String pairid) {

    this.pairid = pairid;
  }

  @Override
  public void onMessage(String message) {

  }
}
