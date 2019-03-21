package pers.zyc.tools.jmxclient;

import pers.zyc.tools.utils.Pair;
import pers.zyc.tools.utils.Regex;

/**
 * @author zhangyancheng
 */
public class JmxHost extends Pair<String, Integer> {

	public JmxHost(String ip, int port) {
		if (!Regex.IPV4.matches(ip)) {
			throw new IllegalArgumentException("Invalid ip: " + ip);
		}
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

	/**
	 * 通过ip:port串创建JmxHost
	 *
	 * @param host ip:port字符串
	 */
	public static JmxHost parse(String host) {
		String[] part = host.split(":");
		return new JmxHost(part[0], Integer.parseInt(part[1]));
	}
}
