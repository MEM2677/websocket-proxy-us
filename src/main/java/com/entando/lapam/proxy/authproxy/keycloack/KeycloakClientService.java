package com.entando.lapam.proxy.authproxy.keycloack;

import com.entando.lapam.proxy.authproxy.domain.keycloak.Profile;
import com.entando.lapam.proxy.authproxy.domain.keycloak.RealmPublicKey;
import com.entando.lapam.proxy.authproxy.domain.keycloak.Token;
import com.entando.lapam.proxy.authproxy.dto.KeycloakClient;

public interface KeycloakClientService {

  /**
   * Get the admin token for provileged REST
   * @param keycloakClient connection info
   * @return
   */
  Token getAdminToken(KeycloakClient keycloakClient);

  /**
   * Get public key used by the realm
   *
   * @param host KC server
   * @return realm info
   */
  RealmPublicKey getRealmPublicKey(String host);

  /**
   * Get user profile
   * @param host KC server
   * @param token Access Token
   * @param id the unique ID name
   * @return user profile
   */
  Profile getUserProfile(String host, Token token, String id);

}
