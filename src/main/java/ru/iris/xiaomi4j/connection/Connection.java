package ru.iris.xiaomi4j.connection;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import ru.iris.xiaomi4j.Discovery;
import ru.iris.xiaomi4j.Gateway;

/**
 * @author nix (06.08.2017)
 */
public abstract class Connection {
	private final Logger logger = LoggerFactory.getLogger(Connection.class);

	static final String MULTICAST_ADDR = "224.0.0.50";
	private static final int BUFFER_LENGTH = 1024;
	private DatagramPacket datagramPacket = new DatagramPacket(new byte[BUFFER_LENGTH], BUFFER_LENGTH);
	private static final JsonParser parser = new JsonParser();
	private static ConcurrentHashMap<Integer, DatagramSocket> openSockets = new ConcurrentHashMap<>();
	private List<ConnectionListener> listeners = new CopyOnWriteArrayList<>();

	private int port;
	private DatagramSocket socket;
	private Thread socketReceiveThread;

	public Connection() {
		this(0);
	}

	public Connection(int port) {
		this.port = port;
	}


	/**
	 * Sends a message through the {@link Connection} to a specific address and port
	 *
	 * @param message - Message to be sent
	 * @param address - Address, to which the message shall be sent
	 * @param port - - Port, through which the message shall be sent
	 */
	public void sendMessage(String message, InetAddress address, int port) {
		try {
			byte[] sendData = message.getBytes("UTF-8");
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, address, port);
			logger.trace("Sending message: {} to {}:{}", message, address, port);
			getSocket().send(sendPacket);
		} catch (IOException e) {
			logger.error("Sending error", e);
		}
	}

	/**
	 * @return - the port number of this {@link Connection}
	 */
	public int getPort() {
		return port;
	}

	public DatagramSocket getSocket() {
		return socket;
	}

	public void setSocket(DatagramSocket socket) {
		this.socket = socket;
	}

	public void intialize() {
		setupSocket();
		runReceiveThread();
	}

	protected void runReceiveThread() {
		socketReceiveThread = new ReceiverThread();
		socketReceiveThread.start();
		if (getSocket() != null) {
			getOpenSockets().put(getSocket().getLocalPort(), getSocket());
			logger.debug("There are {} open sockets: {}", getOpenSockets().size(), getOpenSockets());
		}
	}

	abstract DatagramSocket setupSocket();

	/**
	 * @return - a list of already open sockets
	 */
	public static ConcurrentHashMap<Integer, DatagramSocket> getOpenSockets() {
		return openSockets;
	}

	/**
	 * Interrupts the {@link ReceiverThread} and closes the {@link Connection}.
	 */
	private void closeSocket() {
		synchronized (Connection.class) {
			if (socketReceiveThread != null) {
				logger.debug("Interrupting Thread {}", socketReceiveThread);
				socketReceiveThread.interrupt();
			}
			if (getSocket() != null) {
				logger.debug("Closing socket {}", getSocket());
				openSockets.remove(getSocket().getLocalPort());
				getSocket().close();
				setSocket(null);
			}
		}
	}

	protected List<ConnectionListener> getListeners() {
		return listeners;
	}

	/**
	 * Registers a {@link ConnectionListener} to be called back, when data is received.
	 * If no {@link Connection} exists, when the method is called, it is being set up.
	 *
	 * @param listener - {@link ConnectionListener} to be called back
	 */
	public synchronized void registerListener(ConnectionListener listener) {
		if (!getListeners().contains(listener)) {
			logger.trace("Adding socket listener {}", listener);
			getListeners().add(listener);
		}
		if (getSocket() == null) {
			intialize();
		}
	}

	/**
	 * Unregisters a {@link ConnectionListener}. If there are no listeners left,
	 * the {@link Connection} is being closed.
	 *
	 * @param listener - {@link ConnectionListener} to be unregistered
	 */
	public synchronized void unregisterListener(ConnectionListener listener) {
		getListeners().remove(listener);

		if (getListeners().isEmpty()) {
			closeSocket();
		}
	}

	/**
	 * The thread, which waits for data on the {@link Connection} and handles it, when received
	 *
	 * @author Patrick Boos - Initial contribution
	 * @author Dieter Schmidt - comments and synchronized block for callback instead of copy
	 *
	 */
	private class ReceiverThread extends Thread {

		@Override
		public void run() {
			logger.trace("Staring receiver thread for socket on port {}", getSocket().getLocalPort());
			receiveData(getSocket(), datagramPacket);
		}

		/**
		 * If the socket has data, it parses the data to a json object and calls all
		 * {@link ConnectionListener} and passes the data to them.
		 *
		 * @param socket - The multicast socket to listen to
		 * @param dgram - The datagram to receive
		 */
		private void receiveData(DatagramSocket socket, DatagramPacket dgram) {
			try {
				while (true) {
					logger.trace("Thread {} waiting for data on port {}", this, socket.getLocalPort());
					socket.receive(dgram);
					InetAddress address = dgram.getAddress();
					logger.debug("Received Datagram from {}:{} on Port {}", address.getHostAddress(), dgram.getPort(),
					             socket.getLocalPort());
					String sentence = new String(dgram.getData(), 0, dgram.getLength());
					JsonObject message = parser.parse(sentence).getAsJsonObject();
					notifyAll(getListeners(), message, address);
					logger.trace("Data received and notified {} listeners", getListeners().size());
				}
			} catch (IOException e) {
				if (!isInterrupted()) {
					logger.error("Error while receiving", e);
				} else {
					logger.trace("Receiver thread was interrupted");
				}
			}
			logger.debug("Receiver thread ended");
		}

		/**
		 * Notifies all {@link ConnectionListener} on the parent {@link Connection}.
		 *
		 * @param listeners - a list of all {@link ConnectionListener} to notify
		 * @param message - the data message as {@link JsonObject}
		 */
		synchronized void notifyAll(List<ConnectionListener> listeners, JsonObject message, InetAddress address) {
			for (ConnectionListener listener : listeners) {
				if (listener instanceof Gateway) {
					if (((Gateway) listener).getHost().equals(address)) {
						listener.onDataReceived(message);
					}
				} else if (listener instanceof Discovery) {
					listener.onDataReceived(message);
				}
			}
		}
	}
}
