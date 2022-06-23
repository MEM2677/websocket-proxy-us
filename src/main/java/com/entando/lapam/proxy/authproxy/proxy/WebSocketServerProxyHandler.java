package com.entando.lapam.proxy.authproxy.proxy;

import com.entando.lapam.proxy.authproxy.domain.Metopack;
import com.entando.lapam.proxy.authproxy.domain.keycloak.Profile;
import com.entando.lapam.proxy.authproxy.util.JWTUtils;
import com.entando.lapam.proxy.authproxy.util.KeycloakUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class WebSocketServerProxyHandler extends AbstractWebSocketHandler {

  private static final Logger logger = LoggerFactory.getLogger(WebSocketServerProxyHandler.class);

  private final Map<String, RemoteWebsocketServerHandler> forwardServers = new ConcurrentHashMap<>();


  protected Metopack getUserLapamProfile(String userid) {
    ObjectMapper objectMapper = new ObjectMapper();
    Metopack metopack = null;

    try {
      Profile profile = KeycloakUtils.getUserProfile("matteo");
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

  protected boolean checkAuthorizations(WebSocketSession session) {
    boolean proceed = false;

    try {
      URI uri = session.getUri();
      String jwt = JWTUtils.extractJWTFromURI(uri);
      // test the integrity of the JWT
      if(StringUtils.isNotBlank(jwt) && JWTUtils.verify(jwt)) {
        // check the payload for Lapam property
        Metopack metopack = JWTUtils.getLapamProperties(jwt);
        System.out.println(">1> " + metopack.getConnection());
        System.out.println(">1> " + metopack.getProg());
        System.out.println(">1> " + metopack.getUtente());
        System.out.println(">1> " + metopack.getModules()[0]);
        // get user
        String user = JWTUtils.getUsername(jwt);
        if (metopack != null && StringUtils.isNotBlank(user)) {
          System.out.println(">>> USER: " + user);
          metopack = getUserLapamProfile(user);

          System.out.println(">2> " + metopack.getConnection());
          System.out.println(">2> " + metopack.getProg());
          System.out.println(">2> " + metopack.getUtente());
          System.out.println(">2> " + metopack.getModules()[0]);
          // finally
          proceed = true;
        }
      }
    } catch (Throwable t) {
      logger.error("Unexpected error verifying grant for the current user");
    }
    return proceed;
  }


  @Override
  public void afterConnectionEstablished(WebSocketSession session) {
    System.out.println("APERTURA CONNESSIONE LOCALE");
    System.out.println(">ID> " + session.getId());
//    System.out.println(">LADDR> " + session.getLocalAddress());
//    System.out.println(">RADDR> " + session.getRemoteAddress());
//    System.out.println(">ATTR> " + session.getAttributes());
//    System.out.println(">URI> " + session.getUri());

    System.out.println("??? " + checkAuthorizations(session));


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


  public static final String KEY_LAPAM = "lapam.metopackcloud";
  public static final String PARAM_JWT = "jwt";

}