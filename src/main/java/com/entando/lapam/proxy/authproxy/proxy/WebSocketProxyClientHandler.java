package com.entando.lapam.proxy.authproxy.proxy;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

public class WebSocketProxyClientHandler extends AbstractWebSocketHandler {

  private final WebSocketSession webSocketServerSession;

  public WebSocketProxyClientHandler(WebSocketSession webSocketServerSession) {
    this.webSocketServerSession = webSocketServerSession;
  }

  @Override
  public void afterConnectionEstablished(WebSocketSession session) {
    System.out.println("-- SERVER REMOTO CONNESSIONE APERTA (" + session.getId() + ") --");
  }

  @Override
  public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
    webSocketServerSession.sendMessage(message);
  }

  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
    System.out.println("-- SERVER REMOTO CONNESSIONE CHIUSA (" + status.getCode() + ") --");
  }

}