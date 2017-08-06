package ru.iris.xiaomi4j.connection;

import com.google.gson.JsonObject;

/**
 * @author nix (06.08.2017)
 */
public interface ConnectionListener {
	/**
	 * Callback method for the {@link ConnectionListener}
	 *
	 * @param message - The received message in JSON format
	 */
	void onDataReceived(JsonObject message);
}
