package com.entando.lapam.proxy.authproxy.proxy;

import com.entando.lapam.proxy.authproxy.domain.Metopack;
import com.entando.lapam.proxy.authproxy.domain.keycloak.Profile;
import com.entando.lapam.proxy.authproxy.keycloack.KeycloakClient;
import com.entando.lapam.proxy.authproxy.domain.keycloak.Token;
import com.entando.lapam.proxy.authproxy.dto.ConnectionInfo;
import com.entando.lapam.proxy.authproxy.util.KeycloakUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class WebSocketServerProxyHandler extends AbstractWebSocketHandler {


  private final Map<String, RemoteWebsocketServerHandler> forwardServers = new ConcurrentHashMap<>();


  /**
   *
   * @param info
   * @param userid
   * @return
   */
  protected Metopack getUserLapamProfile(ConnectionInfo info, String userid) {
    ObjectMapper objectMapper = new ObjectMapper();
    Metopack metopack = null;

    try {
      KeycloakClient kc = KeycloakUtils.getCLient();
      Token token = kc.getAdminToken(info);
//      System.out.println("\nAT:\n" + token.getAccessToken());
      Profile profile = kc.getUser(info.getHost(), token, "matteo");
      if (profile != null && profile.getAttributes() != null
        && profile.getAttributes().containsKey(KEY_LAPAM)) {
        String lapam = String.valueOf(profile.getAttributes().get(KEY_LAPAM).get(0));
        metopack = objectMapper.readValue(lapam, Metopack.class);
      }
    } catch (Throwable t) {
      System.out.println("ERRORE PROFILO: " + t);
      t.printStackTrace();
    }
    return metopack;
  }


  @Override
  public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    System.out.println("APERTURA CONNESSIONE LOCALE");
    System.out.println(">ID> " + session.getId());
//    System.out.println(">LADDR> " + session.getLocalAddress());
//    System.out.println(">RADDR> " + session.getRemoteAddress());
//    System.out.println(">ATTR> " + session.getAttributes());
    System.out.println(">URI> " + session.getUri());

    ConnectionInfo info = new ConnectionInfo("https://forumpa.apps.psdemo.eng-entando.com");


    Metopack data = getUserLapamProfile(info, "matteo");
    System.out.println(">>> " + data.getConnection());
    System.out.println(">>> " + data.getProg());
    System.out.println(">>> " + data.getUtente());
    System.out.println(">>> " + data.getModules()[0]);

    // TODO APERTURA PREVENTIVA CONNESSIONE
//    getForwardHandler(session);
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


  public  static final String KEY_LAPAM = "lapam.metopackcloud";

}