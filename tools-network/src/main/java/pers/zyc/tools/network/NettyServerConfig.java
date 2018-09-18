package pers.zyc.tools.network;

import io.netty.channel.epoll.Epoll;

/**
 * @author zhangyancheng
 */
public class NettyServerConfig extends NetworkConfig {

	private boolean useEpoll = Epoll.isAvailable();
	private int backlog = 1024;

	private int port;

	public boolean isUseEpoll() {
		return useEpoll;
	}

	public int getBacklog() {
		return backlog;
	}

	public int getPort() {
		return port;
	}
}
