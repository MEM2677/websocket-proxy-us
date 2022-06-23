package com.entando.lapam.proxy.authproxy.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

/*
{
  "connection": "33.155.255.155:61500",
  "modules": [
    "report/budgetOnline"
  ],
  "utente": "112233",
  "prog": "1"
}
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Metopack {

  private String connection;
  private String[] modules;
  private String utente;
  private String prog;

  public String getConnection() {
    return connection;
  }

  public void setConnection(String connection) {
    this.connection = connection;
  }

  public String[] getModules() {
    return modules;
  }

  public void setModules(String[] modules) {
    this.modules = modules;
  }

  public String getUtente() {
    return utente;
  }

  public void setUtente(String utente) {
    this.utente = utente;
  }

  public String getProg() {
    return prog;
  }

  public void setProg(String prog) {
    this.prog = prog;
  }
}
