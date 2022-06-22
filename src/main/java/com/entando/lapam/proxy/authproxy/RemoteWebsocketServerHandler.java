package com.entando.lapam.proxy.authproxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import java.io.IOException;
import java.net.URI;
import java.security.Principal;
import java.util.concurrent.TimeUnit;

public class RemoteWebsocketServerHandler {

  private final WebSocketSession webSocketClientSession;
  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  public RemoteWebsocketServerHandler(WebSocketSession webSocketClientSession) {
    this.webSocketClientSession = open(webSocketClientSession);
  }


  private WebSocketHttpHeaders getWebSocketHttpHeaders(final WebSocketSession userAgentSession) {
    WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
    Principal principal = userAgentSession.getPrincipal(); /*
    if (principal != null && OAuth2Authentication.class.isAssignableFrom(principal.getClass())) {
      OAuth2Authentication oAuth2Authentication = (OAuth2Authentication) principal;
      OAuth2AuthenticationDetails details = (OAuth2AuthenticationDetails) oAuth2Authentication.getDetails();
      String accessToken = details.getTokenValue();
      headers.put(HttpHeaders.AUTHORIZATION, Collections.singletonList("Bearer " + accessToken));
      if(logger.isDebugEnabled()) {
        logger.debug("Added Oauth2 bearer token authentication header for user " +
          principal.getName() + " to web sockets http headers");
      }
    }
    else {
      if(logger.isDebugEnabled()) {
        logger.debug("Skipped adding basic authentication header since user session principal is null");
      }
    } */
    return headers;
  }

  protected WebSocketSession open(WebSocketSession webSocketServerSession) {
    try {
      System.out.println("-- RICHIESTA APERTURA CONNESSIONE REMOTA --");
      WebSocketHttpHeaders headers = getWebSocketHttpHeaders(webSocketServerSession);
      return new StandardWebSocketClient()
        .doHandshake(new WebSocketProxyClientHandler(webSocketServerSession), headers, new URI(REMOTE_URL))
        .get(3000, TimeUnit.MILLISECONDS);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void forwardMessage(WebSocketMessage<?> webSocketMessage) throws IOException {
    webSocketClientSession.sendMessage(webSocketMessage);
  }

  public void close() throws IOException {
    System.out.println("-- RICHIESTA CHIUSURA CONNESSIONE REMOTA --");
    webSocketClientSession.close();
  }

//  public static final String REMOTE_URL = "wss://ws.postman-echo.com/raw";
  public static final String REMOTE_URL = "ws://34.159.252.151:61500/wish.tcl?cHJvZ3JhbSByZXBvcnQvYmlsYW5jaW9QZXJpb2RpY28gYXJncyB7ICAtdXRlbnRlIDExNzUxMyAtcHJvZyAxIH0=";
}
