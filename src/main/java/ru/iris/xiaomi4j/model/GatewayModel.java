package ru.iris.xiaomi4j.model;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author nix (06.08.2017)
 */
public class GatewayModel {

	private String sid;
	private InetAddress host;
	private int port;

	public String getSid() {
		return sid;
	}

	public void setSid(String sid) {
		this.sid = sid;
	}

	public InetAddress getHost() {
		return host;
	}

	public void setHost(String host) {
		try {
			this.host = InetAddress.getByName(host);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		GatewayModel that = (GatewayModel) o;

		if (port != that.port)
			return false;
		if (sid != null ? !sid.equals(that.sid) : that.sid != null)
			return false;
		return host != null ? host.equals(that.host) : that.host == null;
	}

	@Override
	public int hashCode() {
		int result = sid != null ? sid.hashCode() : 0;
		result = 31 * result + (host != null ? host.hashCode() : 0);
		result = 31 * result + port;
		return result;
	}
}
