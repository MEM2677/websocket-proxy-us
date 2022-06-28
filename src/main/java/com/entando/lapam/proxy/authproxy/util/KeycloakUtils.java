package com.entando.lapam.proxy.authproxy.util;

import com.entando.lapam.proxy.authproxy.domain.keycloak.Profile;
import com.entando.lapam.proxy.authproxy.domain.keycloak.Token;
import com.entando.lapam.proxy.authproxy.dto.KeycloakClient;
import com.entando.lapam.proxy.authproxy.keycloack.impl.KeycloakClientServiceImpl;

public class KeycloakUtils {

  private static KeycloakUtils instance;

  private KeycloakClientServiceImpl keycloakService;
  private KeycloakClient keycloakClient;
  private String publicKeyPath;

  private KeycloakUtils() { }

  public static KeycloakUtils getInstance() {
    if (instance == null) {
      instance = new KeycloakUtils();
    }
    return instance;
  }

  public static KeycloakClientServiceImpl getService() {
    return getInstance().getKeycloakService();
  }

  public static KeycloakClient getClient() {
    return getInstance().getKeycloakClient();
  }

  public static String getPKPath() {
    return getInstance().getPublicKeyPath();
  }

  public static void setup(KeycloakClientServiceImpl client, KeycloakClient info, String path) {
    getInstance().setKeycloakService(client);
    getInstance().setKeycloakClient(info);
    getInstance().setPublicKeyPath(path);
  }

  public KeycloakClientServiceImpl getKeycloakService() {
    return keycloakService;
  }

  public void setKeycloakService(KeycloakClientServiceImpl keycloakService) {
    this.keycloakService = keycloakService;
  }

  public KeycloakClient getKeycloakClient() {
    return keycloakClient;
  }

  public void setKeycloakClient(KeycloakClient keycloakClient) {
    this.keycloakClient = keycloakClient;
  }

  public static Token getAdminToken() {
    KeycloakClientServiceImpl service = getInstance().getKeycloakService();
    KeycloakClient client = getInstance().getKeycloakClient();

    return service.getAdminToken(client);
  }


  public String getPublicKeyPath() {
    return publicKeyPath;
  }

  public void setPublicKeyPath(String publicKeyPath) {
    this.publicKeyPath = publicKeyPath;
  }


}
