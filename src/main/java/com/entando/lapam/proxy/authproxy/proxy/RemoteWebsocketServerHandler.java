package com.entando.lapam.proxy.authproxy.proxy;

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

  private final Logger logger = LoggerFactory.getLogger(RemoteWebsocketServerHandler.class);

  private final WebSocketSession session;
  private final String uri;

  public RemoteWebsocketServerHandler(WebSocketSession session, String uri) {
    this.uri = uri;
    this.session = open(session);
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

  protected WebSocketSession open(WebSocketSession session) {
    try {
      System.out.println("-- RICHIESTA APERTURA CONNESSIONE REMOTA A: " + uri + " --");

      WebSocketHttpHeaders headers = getWebSocketHttpHeaders(session);
      return new StandardWebSocketClient()
        .doHandshake(new WebSocketProxyClientHandler(session), headers, new URI(uri))
        .get(3000, TimeUnit.MILLISECONDS);
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }

  public boolean isOpen() {
    return session.isOpen();
  }

  public void forwardMessage(WebSocketMessage<?> webSocketMessage) throws IOException {
    session.sendMessage(webSocketMessage);
  }

  public void close() throws IOException {
    System.out.println("-- RICHIESTA CHIUSURA CONNESSIONE REMOTA --");
    session.close();
  }

//  public static final String REMOTE_URL = "wss://ws.postman-echo.com/raw";

}
