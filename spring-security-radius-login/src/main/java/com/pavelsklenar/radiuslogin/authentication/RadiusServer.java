package com.pavelsklenar.radiuslogin.authentication;

public class RadiusServer {

	private String ip;
	private String secret;
	private int timeout;

	public RadiusServer(String ip, String secret, int timeout) {
		super();
		this.ip = ip;
		this.secret = secret;
		this.timeout = timeout;
	}

	public int getTimeout() {
		return timeout;
	}

	public String getIp() {
		return ip;
	}

	public String getSecret() {
		return secret;
	}

}
