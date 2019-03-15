package pers.zyc.tools.jmxclient;

import pers.zyc.tools.utils.Pair;

/**
 * @author zhangyancheng
 */
public class JmxUser extends Pair<String, String> {

	public JmxUser(String user, String password) {
		key(user);
		value(password);
	}

	public String getUser() {
		return key();
	}

	public String getPassword() {
		return value();
	}
}
