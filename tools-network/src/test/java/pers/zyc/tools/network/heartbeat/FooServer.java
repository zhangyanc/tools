package pers.zyc.tools.network.heartbeat;

import pers.zyc.tools.network.NetServer;

/**
 * @author zhangyancheng
 */
public class FooServer {

	static final int FOO_SERVER_PORT = 8000;

	public static void main(String[] args) {
		NetServer fooServer = new NetServer();
		fooServer.setCommandFactory(new HeartbeatCommandFactory());
		fooServer.setRequestHandlerFactory(new HeartbeatRequestHandlerFactory());

		fooServer.setChannelWriteTimeout(3000);
		fooServer.setChannelReadTimeout(10000);

		fooServer.addListener(new ChannelIdleHandler());

		fooServer.setPort(FOO_SERVER_PORT);
		fooServer.start();
	}
}
