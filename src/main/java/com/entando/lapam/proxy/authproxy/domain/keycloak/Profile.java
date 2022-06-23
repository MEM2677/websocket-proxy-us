package com.entando.lapam.proxy.authproxy.domain.keycloak;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.Map;

/*
 {
    "id": "b2196277-9332-4e63-b14e-1d3e2947fca3",
    "createdTimestamp": 1655906999358,
    "username": "matteo",
    "enabled": true,
    "totp": false,
    "emailVerified": true,
    "firstName": "Matteo",
    "lastName": "Minnai",
    "email": "m.minnai@entando.com",
    "attributes": {
      "lapam.metopackcloud": [
        "{   \"connection\": \"33.155.255.155:61500\",   \"modules\": [\"report/budgetOnline\"],   \"utente\": \"112233\",   \"prog\": \"1\" }"
      ]
    },
    "disableableCredentialTypes": [],
    "requiredActions": [],
    "notBefore": 0,
    "access": {
      "manageGroupMembership": true,
      "view": true,
      "mapRoles": true,
      "impersonate": true,
      "manage": true
    }
  }
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Profile {

  private String id;
  private Long createdTimestamp;
  private String username;
  private Boolean enabled;
  private Boolean totp;
  private Boolean emailVerified;
  private String firstName;
  private String lastName;
  private String email;
  private Map<String, ArrayList<Object>> attributes;
  private String[] disableableCredentialTypes;
  private String[] requiredActions;
  private Long notBefore;
  private Map<String, Object> access;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Long getCreatedTimestamp() {
    return createdTimestamp;
  }

  public void setCreatedTimestamp(Long createdTimestamp) {
    this.createdTimestamp = createdTimestamp;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public Boolean getEnabled() {
    return enabled;
  }

  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
  }

  public Boolean getTotp() {
    return totp;
  }

  public void setTotp(Boolean totp) {
    this.totp = totp;
  }

  public Boolean getEmailVerified() {
    return emailVerified;
  }

  public void setEmailVerified(Boolean emailVerified) {
    this.emailVerified = emailVerified;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public Map<String, ArrayList<Object>> getAttributes() {
    return attributes;
  }

  public void setAttributes(Map<String, ArrayList<Object>> attributes) {
    this.attributes = attributes;
  }

  public String[] getDisableableCredentialTypes() {
    return disableableCredentialTypes;
  }

  public void setDisableableCredentialTypes(String[] disableableCredentialTypes) {
    this.disableableCredentialTypes = disableableCredentialTypes;
  }

  public String[] getRequiredActions() {
    return requiredActions;
  }

  public void setRequiredActions(String[] requiredActions) {
    this.requiredActions = requiredActions;
  }

  public Long getNotBefore() {
    return notBefore;
  }

  public void setNotBefore(Long notBefore) {
    this.notBefore = notBefore;
  }

  public Map<String, Object> getAccess() {
    return access;
  }

  public void setAccess(Map<String, Object> access) {
    this.access = access;
  }
}
