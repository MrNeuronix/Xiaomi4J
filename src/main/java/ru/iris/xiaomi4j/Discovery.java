package ru.iris.xiaomi4j;

import com.google.gson.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import ru.iris.xiaomi4j.connection.ConnectionListener;
import ru.iris.xiaomi4j.connection.GatewayDiscoveryConnection;
import ru.iris.xiaomi4j.model.GatewayModel;

/**
 * @author nix (06.08.2017)
 */
public class Discovery implements ConnectionListener {
	private final Logger logger = LoggerFactory.getLogger(Discovery.class);
	private List<GatewayModel> gatewayModels = new ArrayList<>();
	private GatewayDiscoveryConnection socket;

	@Override
	public void onDataReceived(JsonObject data) {
		logger.debug("Received message {}", data);
		if (data.get("cmd").getAsString().equals("iam")) {
			getGatewayInfo(data);
		}
	}

	public void startScan() {
		socket = (socket == null) ? new GatewayDiscoveryConnection() : socket;
		socket.intialize();
		logger.debug("Start scan for bridges");
		socket.registerListener(this);
		discoverGateways();
	}

	public synchronized void stopScan() {
		logger.debug("Stop scan");
		if (socket != null) {
			socket.unregisterListener(this);
		}
	}

	private void discoverGateways() {
		socket.sendMessage("{\"cmd\":\"whois\"}");
	}

	private void getGatewayInfo(JsonObject jobject) {
		String serialNumber = jobject.get("sid").getAsString();
		String ipAddress = jobject.get("ip").getAsString();
		int port = jobject.get("port").getAsInt();

		GatewayModel gatewayModel = new GatewayModel();
		gatewayModel.setHost(ipAddress);
		gatewayModel.setPort(port);
		gatewayModel.setSid(serialNumber);

		if(!gatewayModels.contains(gatewayModel)) {
			gatewayModels.add(gatewayModel);
		}

		logger.debug("Discovered Xiaomi Gateway - sid: {} ip: {} port: {}", serialNumber, ipAddress, port);
	}

	public List<GatewayModel> getGatewayModels() {
		return gatewayModels;
	}
}
