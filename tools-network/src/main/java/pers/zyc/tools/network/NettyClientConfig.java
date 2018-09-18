package pers.zyc.tools.network;

/**
 * @author zhangyancheng
 */
public class NettyClientConfig extends NetworkConfig {

	private int connectTimeout = 2000;

	public int getConnectTimeout() {
		return connectTimeout;
	}
}
