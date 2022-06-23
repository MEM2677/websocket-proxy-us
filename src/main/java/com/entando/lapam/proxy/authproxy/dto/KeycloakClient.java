package com.entando.lapam.proxy.authproxy.dto;

public class KeycloakClient {

    private String username;
    private String password;
    private String host;

    public KeycloakClient(String host) {
        this.host = host;
    }

    public void setLogin(String username, String password) {
        try {
            this.password = password;
            this.username = username;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }
}
