package com.entando.lapam.proxy.authproxy.proxy;

import com.entando.lapam.proxy.authproxy.domain.Metopack;
import com.entando.lapam.proxy.authproxy.util.JWTUtils;
import com.entando.lapam.proxy.authproxy.util.LapamUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class WebSocketServerProxyHandler extends AbstractWebSocketHandler {

  private static final Logger logger = LoggerFactory.getLogger(WebSocketServerProxyHandler.class);


  private final Map<String, RemoteWebsocketServerHandler> forwardServers = new ConcurrentHashMap<>();


  protected boolean checkAuthorizations(WebSocketSession session) {
    boolean proceed = false;

    try {
      URI uri = session.getUri();
      String jwt = LapamUtils.extractParameter(uri, PARAM_JWT);
      String modulo = LapamUtils.extractParameter(uri, PARAM_MODULE);
      // test the integrity of the JWT
      if (StringUtils.isNotBlank(jwt) && JWTUtils.verify(jwt)) {
        // look for Lapam property
        Metopack metopack = LapamUtils.getLapamProperties(jwt);
        if (metopack != null) {
          // controllare il payload per Lapam websocket
          if (LapamUtils.checkModule(modulo, metopack)) {
            // get user
            String user = JWTUtils.getUsername(jwt);
            // TODO legame fra nome utente e utenza Lapam?
            proceed = true;
          } else {
            logger.debug("module mismatch!");
          }
        } else {
          logger.debug("no metopack data found!");
        }
      } else {
        logger.debug("JWT check failed!");
      }
    } catch (Throwable t) {
      t.printStackTrace();
      logger.error("Unexpected error verifying grant for the current user");
    }
    return proceed;
  }


  @Override
  public void afterConnectionEstablished(WebSocketSession session) {
    try {
      System.out.println("APERTURA CONNESSIONE LOCALE");
      System.out.println(">ID> " + session.getId());
//    System.out.println(">LADDR> " + session.getLocalAddress());
//    System.out.println(">RADDR> " + session.getRemoteAddress());
//    System.out.println(">ATTR> " + session.getAttributes());
//    System.out.println(">URI> " + session.getUri());

      if (checkAuthorizations(session)) {
        // TODO APERTURA PREVENTIVA CONNESSIONE
        logger.debug("Opening connection to server ");
        getForwardHandler(session);
      } else {
        logger.warn("Connection refused!");
        // deliver 403 message
        TextMessage rejected = new TextMessage(MSG_FORBIDDEN);
        session.sendMessage(rejected);
        // close the connection
        session.close();
      }
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }

  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
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
    try {
//      RemoteWebsocketServerHandler remote = getForwardHandler(session);
//      if (remote.isOpen()) {
        getForwardHandler(session).forwardMessage(message);
//      } else {
//        System.out.println("-- CONNESSIONE REMOTA CHIUSA DAL CLIENT REMOTO --");
//        session.close();
//      }
    } catch (Throwable e) {
      logger.error("Error forwarding message, closing session: " + e.getMessage());
      session.close();
    }
  }

  /**
   * Generate the Lapam address to connect to
   *
   * @param session current ws session
   * @return the handler to forward messages to
   * @throws Throwable
   */
  private RemoteWebsocketServerHandler getForwardHandler(WebSocketSession session) throws Throwable {
    RemoteWebsocketServerHandler remoteServer = forwardServers.get(session.getId());

    if (remoteServer == null) {
      URI uri = session.getUri();

      String jwt = LapamUtils.extractParameter(uri, PARAM_JWT);
      String modulo = LapamUtils.extractParameter(uri, PARAM_MODULE);
      Metopack metopack = LapamUtils.getLapamProperties(jwt);
      String lapamUri = LapamUtils.generateLapamURI(modulo, metopack);

      remoteServer = new RemoteWebsocketServerHandler(session, lapamUri);
      forwardServers.put(session.getId(), remoteServer);
      System.out.println("AGGIUNTO FORWARD " + session.getId());
    }
    return remoteServer;
  }

  public static final String PARAM_JWT = "jwt";
  public static final String PARAM_MODULE = "mod";

  public static final String MSG_FORBIDDEN = "403 forbidden";

}