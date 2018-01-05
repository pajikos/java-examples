package com.pavelsklenar.radiuslogin.authentication;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;

import org.tinyradius.packet.AccessRequest;
import org.tinyradius.packet.RadiusPacket;
import org.tinyradius.util.RadiusClient;
import org.tinyradius.util.RadiusException;

/**
 * Client class is responsible for communication with a remote radius server
 *
 * @author pavel.sklenar
 *
 */
public class Client {

    private static final String CALLING_STATION_ID = "Calling-Station-Id";
    private static final String NAS_IP_ADDRESS = "NAS-IP-Address";
    private static final String NAS_PORT_ID = "NAS-Port-Id";
    private static final String RADIUS_ID = "MY_RADIUS_ID";
    private RadiusClient radiusClient;

    public Client(RadiusServer radiusServer) {
    	this.radiusClient = initRadiusClient(radiusServer);
    }
    
    private RadiusClient initRadiusClient(RadiusServer radiusServer)  {
    	try {
    		RadiusClient radiusClient = new RadiusClient(radiusServer.getIp(), radiusServer.getSecret());
        	// Set SO Timeout in milliseconds (originally in seconds)
			radiusClient.setSocketTimeout(radiusServer.getTimeout() * 1000);
			return radiusClient;
		} catch (SocketException e) {
			throw new IllegalStateException(e);
		}
    }

    public RadiusPacket authenticate(String login, String password) throws IOException, RadiusException {
        AccessRequest ar = new AccessRequest(login, password);

        ar.setAuthProtocol(AccessRequest.AUTH_PAP);

        ar.addAttribute(NAS_PORT_ID, RADIUS_ID);

        ar.addAttribute(NAS_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress());

        ar.addAttribute(CALLING_STATION_ID, InetAddress.getLocalHost().getHostAddress());

        RadiusPacket response = radiusClient.authenticate(ar);
        return response;
    }

}
