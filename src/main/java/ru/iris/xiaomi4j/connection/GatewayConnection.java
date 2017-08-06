package ru.iris.xiaomi4j.connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 * @author nix (06.08.2017)
 */
public class GatewayConnection extends Connection {
	private final Logger logger = LoggerFactory.getLogger(GatewayConnection.class);

	public GatewayConnection(int port) {
		super(port);
	}

	/**
	 * Sets up the {@link GatewayConnection}.
	 *
	 * Connects the socket to the specific multicast address and port.
	 * Starts the {@link ReceiverThread} for the socket.
	 */
	@Override
	synchronized DatagramSocket setupSocket() {
		if (getOpenSockets().contains(getPort())) {
			return getOpenSockets().get(getPort());
		}
		try {
			logger.debug("Setup socket");
			setSocket(new MulticastSocket(getPort())); // must bind receive side
			((MulticastSocket) getSocket()).joinGroup(InetAddress.getByName(MULTICAST_ADDR));
			logger.debug("Initialized socket to {}:{} on {}:{}", getSocket().getRemoteSocketAddress(),
			             getSocket().getPort(), getSocket().getLocalAddress(), getSocket().getLocalPort());
		} catch (IOException e) {
			logger.error("Setup socket error", e);
		}
		return getSocket();
	}
}
