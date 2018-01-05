package com.pavelsklenar.radiuslogin.authentication;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class RadiusUtil {

	private static final String SERVER_DELIMITER = ";";

	private RadiusUtil() {
	}

	public static List<RadiusServer> parseServerConfigurationToken(String serverConfigurationToken) {
		StringTokenizer tokenizer = new StringTokenizer(serverConfigurationToken, SERVER_DELIMITER);

		List<RadiusServer> servers = new ArrayList<RadiusServer>();

		while (tokenizer.hasMoreTokens()) {
			String ip = tokenizer.nextToken();
			String secret;

			if (tokenizer.hasMoreElements())
				secret = tokenizer.nextToken();
			else
				break;

			if (tokenizer.hasMoreElements()) {
				RadiusServer ser = new RadiusServer(ip, secret);
				ser.setTimeout(Integer.parseInt(tokenizer.nextToken()));
				servers.add(ser);
			}
		}

		return servers;
	}

}
