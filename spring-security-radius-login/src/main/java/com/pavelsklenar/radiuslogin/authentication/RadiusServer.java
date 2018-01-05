package com.pavelsklenar.radiuslogin.authentication;

public class RadiusServer {

    private String ip;
    private String secret;
    private int timeout;


    public RadiusServer(String ip, String secret) {
        super();
        this.ip = ip;
        this.secret = secret;
    }


    public int getTimeout() {
        return timeout;
    }


    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }


    public String getIp() {
        return ip;
    }


    public String getSecret() {
        return secret;
    }


    public void setIp(String ip) {
        this.ip = ip;
    }


    public void setSecret(String secret) {
        this.secret = secret;
    }

}
