package com.pavelsklenar.radiuslogin.authentication;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.tinyradius.packet.RadiusPacket;

/**
 * Component responsible for authentication against radius server
 * 
 * @author pavel.sklenar
 *
 */
public class RadiusAuthenticationProvider implements AuthenticationProvider {

	private static final Logger logger = LoggerFactory.getLogger(RadiusAuthenticationProvider.class);

	@Value("${com.pavelsklenar.radius.server}")
	private String serverConfigurationToken;

	private List<NetworkAccessServer> clients = new ArrayList<>();

	@PostConstruct
	public void initServers() {
		List<RadiusServer> servers = RadiusUtil.parseServerConfigurationToken(serverConfigurationToken);
		servers.forEach(it -> {
			clients.add(new NetworkAccessServer(it));
		});
	}

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		String username = authentication.getName();
		RadiusPacket response = null;
		int attemptCount = 0;
		while (response == null && attemptCount++ < clients.size()) {
			response = authenticateInternally(clients.get(attemptCount - 1), username,
					authentication.getCredentials().toString());
		}
		if (response == null) {
			logger.warn("User {}, calling radius does not return any value.", username);
			return null;
		}
		if (response.getPacketType() == RadiusPacket.ACCESS_ACCEPT) {
			logger.info("User {} successfully authenticated using radius", username);
			return new UsernamePasswordAuthenticationToken(username, "", new ArrayList<>());
		} else {
			logger.warn("User {}, returned response {}", username, response);
			return null;
		}
	}

	private RadiusPacket authenticateInternally(NetworkAccessServer client, String username, String password) {
		logger.info("Calling radius server to authenticate user {}", username);
		try {
			return client.authenticate(username, password);
		} catch (Exception e) {
			logger.error("Exception when calling remote radius server.", e);
			return null;
		}
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return authentication.equals(UsernamePasswordAuthenticationToken.class);
	}

}
