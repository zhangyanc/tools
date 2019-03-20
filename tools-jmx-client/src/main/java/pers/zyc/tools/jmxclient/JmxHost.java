package pers.zyc.tools.jmxclient;

import pers.zyc.tools.utils.Pair;

/**
 * @author zhangyancheng
 */
public class JmxHost extends Pair<String, Integer> {

	public JmxHost(String ip, int port) {
		key(ip);
		value(port);
	}

	public String getIp() {
		return key();
	}

	public int getPort() {
		return value();
	}

	public String getHost() {
		return getIp() + ":" + getPort();
	}
}
