/*
 * Copyright 2017 Nikolay A. Viguro
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.iris.xiaomi4j;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;

import ru.iris.xiaomi4j.connection.ConnectionListener;
import ru.iris.xiaomi4j.connection.GatewayConnection;
import ru.iris.xiaomi4j.watchers.*;

public class Gateway implements ConnectionListener {

	private final Logger LOGGER = LoggerFactory.getLogger(Gateway.class.getName());
	private Watcher watcher = null;
	private boolean initialized = false;
	private GatewayConnection connection;
	private InetAddress host;
	private int port;

	private static final int DISCOVERY_LOCK_TIME_MILLIS = 10000;
	private static final JsonParser PARSER = new JsonParser();
	private static final EncryptionHelper CRYPTER = new EncryptionHelper();

	private String gatewayToken;
	private String sid;
	private String encrKey;
	private long lastDiscoveryTime;

	public Gateway(InetAddress host, int port) {
		this(host, port, null, null, null);
	}

	public Gateway(InetAddress host, int port, String sid, String encrKey, Watcher watcher) {
		this.watcher = watcher;
		this.host = host;
		this.port = port;
		this.sid = sid;
		this.encrKey = encrKey;
		this.connection = new GatewayConnection(port);
		this.connection.intialize();
		this.connection.registerListener(this);
		this.watcher = watcher;
		this.initialized = true;
	}

	public String getSid() {
		return sid;
	}

	public void setSid(String sid) {
		this.sid = sid;
	}

	public String getEncrKey() {
		return encrKey;
	}

	public void setEncrKey(String encrKey) {
		this.encrKey = encrKey;
	}

	public void addWatcher(Watcher watcher) {
		this.watcher = watcher;
	}

	public void close() {
		LOGGER.debug("Закрывается устройство Gateway");
		this.connection.unregisterListener(this);
	}

	public InetAddress getHost() {
		return host;
	}

	@Override
	public void onDataReceived(JsonObject message) {
		Notification notification = new Notification();
		notification.setRawMessage(message.toString());

		LOGGER.trace("Received message {}", message);
		String sid = message.has("sid") ? message.get("sid").getAsString() : null;
		String command = message.get("cmd").getAsString();

		switch (command) {
			case "iam":
				return;
			case "heartbeat":
				if (message.has("token")) {
					this.gatewayToken = message.get("token").getAsString();
				}
				break;
			case "get_id_list_ack":
				JsonArray devices = PARSER.parse(message.get("data").getAsString()).getAsJsonArray();
				for (JsonElement deviceId : devices) {
					String device = deviceId.getAsString();
					sendCommandToBridge("read", device);
				}
				// as well get gateway status
				sendCommandToBridge("read", getSid());
				return;
			case "read_ack":
				LOGGER.debug("Device {} honored read request", sid);
				break;
			case "write_ack":
				LOGGER.debug("Device {} honored write request", sid);
				break;
		}

		watcher.onNotification(notification);
	}

	private void sendMessageToBridge(String message) {
		LOGGER.debug("Send to bridge {}: {}", this.host.getHostAddress(), message);
		connection.sendMessage(message, host, port);
	}

	private void sendCommandToBridge(String cmd) {
		sendCommandToBridge(cmd, null, null, null);
	}

	private void sendCommandToBridge(String cmd, String[] keys, Object[] values) {
		sendCommandToBridge(cmd, null, keys, values);
	}

	private void sendCommandToBridge(String cmd, String sid) {
		sendCommandToBridge(cmd, sid, null, null);
	}

	private void sendCommandToBridge(String cmd, String sid, String[] keys, Object[] values) {
		StringBuilder message = new StringBuilder("{");
		message.append("\"cmd\": \"").append(cmd).append("\"");
		if (sid != null) {
			message.append(", \"sid\": \"").append(sid).append("\"");
		}
		if (keys != null) {
			for (int i = 0; i < keys.length; i++) {
				message.append(", ").append("\"").append(keys[i]).append("\"").append(": ");

				// write value
				message.append(toJsonValue(values[i]));
			}
		}
		message.append("}");

		sendMessageToBridge(message.toString());
	}

	void writeToDevice(String itemId, String[] keys, Object[] values) {
		sendCommandToBridge("write", new String[] { "sid", "data" },
		                    new Object[] { itemId, createDataJsonString(keys, values) });
	}

	void writeToBridge(String[] keys, Object[] values) {
		sendCommandToBridge("write", new String[] { "model", "sid", "short_id", "data" },
		                    new Object[] { "gateway", getSid(), "0", createDataJsonString(keys, values) });
	}

	private String createDataJsonString(String[] keys, Object[] values) {
		return "{" + createDataString(keys, values) + ", \\\"key\\\": \\\"" + getEncryptedKey() + "\"}";
	}

	private String getEncryptedKey() {
		if (encrKey == null) {
			LOGGER.warn("No key set in the gateway settings. Edit it in the configuration.");
			return "";
		}
		return CRYPTER.encrypt(gatewayToken, encrKey);
	}

	private String createDataString(String[] keys, Object[] values) {
		StringBuilder builder = new StringBuilder();

		if (keys.length != values.length) {
			return "";
		}

		for (int i = 0; i < keys.length; i++) {
			if (i > 0) {
				builder.append(",");
			}

			// write key
			builder.append("\\\"").append(keys[i]).append("\\\"").append(": ");

			// write value
			builder.append(escapeQuotes(toJsonValue(values[i])));
		}
		return builder.toString();
	}

	private String toJsonValue(Object o) {
		if (o instanceof String) {
			return "\"" + o + "\"";
		} else {
			return o.toString();
		}
	}

	private String escapeQuotes(String string) {
		return string.replaceAll("\"", "\\\\\"");
	}

	public void discoverItems() {
		long lockedFor = DISCOVERY_LOCK_TIME_MILLIS - (System.currentTimeMillis() - lastDiscoveryTime);
		if (lockedFor <= 0) {
			LOGGER.debug("Triggered discovery");
			sendCommandToBridge("get_id_list");
			lastDiscoveryTime = System.currentTimeMillis();
		} else {
			LOGGER.debug("Triggered discovery, but locked for {}ms", lockedFor);
		}
	}
}
