package pers.zyc.tools.network.heartbeat;

import io.netty.channel.Channel;
import pers.zyc.tools.network.NetClient;

import static pers.zyc.tools.network.heartbeat.FooServer.FOO_SERVER_PORT;

/**
 * @author zhangyancheng
 */
public class FooClient {

	public static void main(String[] args) throws InterruptedException {
		NetClient fooClient = new NetClient();
		fooClient.setCommandFactory(new HeartbeatCommandFactory());
		fooClient.setRequestHandlerFactory(new HeartbeatRequestHandlerFactory());

		fooClient.setChannelWriteTimeout(3000);
		fooClient.setChannelReadTimeout(10000);

		fooClient.addListener(new ChannelIdleHandler());
		fooClient.start();

		Channel channel = fooClient.createChannel("localhost", FOO_SERVER_PORT);
	}
}
