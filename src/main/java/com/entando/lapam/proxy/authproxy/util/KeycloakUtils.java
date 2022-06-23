package com.entando.lapam.proxy.authproxy.util;

import com.entando.lapam.proxy.authproxy.keycloack.KeycloakClient;
import com.entando.lapam.proxy.authproxy.dto.ConnectionInfo;

public class KeycloakUtils {

  private static KeycloakUtils instance;

  private KeycloakClient client;
  private ConnectionInfo conn;

  private KeycloakUtils() { }

  public static KeycloakUtils getInstance() {
    if (instance == null) {
      instance = new KeycloakUtils();
    }
    return instance;
  }

  public static KeycloakClient getCLient() {
    return getInstance().getClient();
  }

  public static ConnectionInfo getConnectionInfo() {
    return getInstance().getConn();
  }

  public static void setup(KeycloakClient client, ConnectionInfo info) {
    getInstance().setClient(client);
    getInstance().setConn(info);
  }

  public KeycloakClient getClient() {
    return client;
  }

  public void setClient(KeycloakClient client) {
    this.client = client;
  }

  public ConnectionInfo getConn() {
    return conn;
  }

  public void setConn(ConnectionInfo conn) {
    this.conn = conn;
  }
}
