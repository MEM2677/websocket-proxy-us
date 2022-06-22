package com.entando.lapam.proxy.authproxy;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketServerProxyHandler extends AbstractWebSocketHandler {

  private final Map<String, RemoteWebsocketServerHandler> forwardServers = new ConcurrentHashMap<>();

  @Override
  public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    System.out.println("APERTURA CONNESSIONE LOCALE");
    System.out.println(">ID> " + session.getId());
//    System.out.println(">LADDR> " + session.getLocalAddress());
//    System.out.println(">RADDR> " + session.getRemoteAddress());
//    System.out.println(">ATTR> " + session.getAttributes());
    System.out.println(">URI> " + session.getUri());
    // TODO APERTURA PREVENTIVA CONNESSIONE
    getForwardHandler(session);
  }

  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
    System.out.println("CHIUSA CONNESSIONE LOCALE (" + status.getCode() + ")");
    try {
      RemoteWebsocketServerHandler remoteServer = getForwardHandler(session);
      remoteServer.close();
      if (forwardServers.containsKey(session.getId())) {
        forwardServers.remove(session.getId());
        System.out.println("ELIMINATO FORWARD " + session.getId());
      } else {
        throw new RuntimeException("ERRORE CANCELLAZIONE FORWARD SCONOSCIUTO! " + session.getId());
      }
    } catch (Throwable t) {
      System.out.println("NEGAZIONE RIMOZIONE FORWARD [" + session.getId() + "] per errori in chiusura");
    }
  }

  @Override
  public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
    getForwardHandler(session).forwardMessage(message);
  }

  private RemoteWebsocketServerHandler getForwardHandler(WebSocketSession session) {
    RemoteWebsocketServerHandler remoteServer = forwardServers.get(session.getId());
    if (remoteServer == null) {
      remoteServer = new RemoteWebsocketServerHandler(session);
      forwardServers.put(session.getId(), remoteServer);
      System.out.println("AGGIUNTO FORWARD " + session.getId());
    }
    return remoteServer;
  }

}