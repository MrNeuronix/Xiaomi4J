package ru.iris.xiaomi4j.connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author nix (06.08.2017)
 */
public class GatewayDiscoveryConnection extends Connection {
		private static final int MCAST_PORT = 4321;
		private final Logger logger = LoggerFactory.getLogger(GatewayDiscoveryConnection.class);

		public GatewayDiscoveryConnection() {
			super();
		}

		/**
		 * Sets up the {@link GatewayDiscoveryConnection}.
		 *
		 * Connects the socket to the specific multicast address and port.
		 * Starts the {@link ReceiverThread} for the socket.
		 */
		@Override
		DatagramSocket setupSocket() {
			synchronized (GatewayDiscoveryConnection.class) {
				try {
					logger.debug("Setup discovery socket");
					setSocket(new DatagramSocket(0));
					logger.debug("Initialized socket to {}:{} on {}:{}", getSocket().getInetAddress(),
					             getSocket().getPort(), getSocket().getLocalAddress(), getSocket().getLocalPort());
				} catch (IOException e) {
					logger.error("Setup socket error", e);
				}
			}
			return getSocket();
		}

		/**
		 * Sends a message through the {@link GatewayDiscoveryConnection}
		 * to the MiHome multicast address 224.0.0.50 and port 4321
		 *
		 * @param message - Message to be sent
		 */
		public void sendMessage(String message) {
			try {
				sendMessage(message, InetAddress.getByName(MULTICAST_ADDR), MCAST_PORT);
			} catch (UnknownHostException e) {
				logger.error("Sending error", e);
			}
		}
}
