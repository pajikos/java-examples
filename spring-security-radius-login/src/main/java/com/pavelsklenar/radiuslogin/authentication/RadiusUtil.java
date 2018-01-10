package com.pavelsklenar.radiuslogin.authentication;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RadiusUtil {

	private static final String SERVER_DELIMITER = ";";
	private static final String PARAM_DELIMITER = ",";

	private RadiusUtil() {
	}

	public static List<RadiusServer> parseServerConfigurationToken(String serverConfigurationToken) {
		if (serverConfigurationToken == null) {
			throw new IllegalArgumentException("Radius configuration token cannot be empty.");
		}
		return Stream.of(serverConfigurationToken.split(SERVER_DELIMITER))
				.map(s -> s.split(PARAM_DELIMITER))
				.map(p -> new RadiusServer(p[0], p[1], Integer.parseInt(p[2])))
				.collect(Collectors.toList());

	}

}
