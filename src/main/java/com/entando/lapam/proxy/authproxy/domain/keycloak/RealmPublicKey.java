package com.entando.lapam.proxy.authproxy.domain.keycloak;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/*
{
  "realm": "entando",
  "public_key": "MIIB...",
  "token-service": "https://meh.com/auth/realms/entando/protocol/openid-connect",
  "account-service": "https://meh.com/auth/realms/entando/account",
  "tokens-not-before": 0
}
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RealmPublicKey {

  private String realm;

  @JsonProperty("public_key")
  private String publicKey;

  @JsonProperty("token-service")
  private String tokenService;

  @JsonProperty("account-service")
  private String accountService;

  @JsonProperty("tokens-not-before")
  private Long tokensNotBefore;

  public String getRealm() {
    return realm;
  }

  public void setRealm(String realm) {
    this.realm = realm;
  }

  public String getPublicKey() {
    return publicKey;
  }

  public void setPublicKey(String publicKey) {
    this.publicKey = publicKey;
  }

  public String getTokenService() {
    return tokenService;
  }

  public void setTokenService(String tokenService) {
    this.tokenService = tokenService;
  }

  public String getAccountService() {
    return accountService;
  }

  public void setAccountService(String accountService) {
    this.accountService = accountService;
  }

  public Long getTokensNotBefore() {
    return tokensNotBefore;
  }

  public void setTokensNotBefore(Long tokensNotBefore) {
    this.tokensNotBefore = tokensNotBefore;
  }
}
