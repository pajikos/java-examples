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

	private List<Client> clients = new ArrayList<>();

	@PostConstruct
	public void initServers() {
		List<RadiusServer> servers = RadiusUtil.parseServerConfigurationToken(serverConfigurationToken);
		servers.forEach(it -> {
			clients.add(new Client(it));
		});
	}

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		String name = authentication.getName();
		String password = authentication.getCredentials().toString();
		return authenticateInternally(name, password);
	}

	private Authentication authenticateInternally(String username, String password) {
		RadiusPacket response = null;
		int attemptCount = 0;
		while (response == null && attemptCount++ < clients.size()) {
			Client client = clients.get(attemptCount - 1);
			logger.info("Calling radius server to authenticate user {}", username);
			try {
				response = client.authenticate(username, password);
			} catch (Exception e) {
				logger.error("Exception when calling remote radius server.", e);
			}
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

	@Override
	public boolean supports(Class<?> authentication) {
		return authentication.equals(UsernamePasswordAuthenticationToken.class);
	}

}
