package pers.zyc.tools.network.echo.client;

import io.netty.channel.Channel;
import pers.zyc.tools.network.NettyClient;
import pers.zyc.tools.network.Response;
import pers.zyc.tools.network.echo.Echo;
import pers.zyc.tools.network.echo.EchoAck;
import pers.zyc.tools.network.echo.EchoCommandFactory;
import pers.zyc.tools.network.echo.server.EchoServer;
import pers.zyc.tools.utils.TimeMillis;

/**
 * @author zhangyancheng
 */
public class EchoClient {

	public static void main(String[] args) throws InterruptedException {
		NettyClient echoClient = new NettyClient();
		echoClient.setCommandFactory(new EchoCommandFactory());
		echoClient.start();


		int i = 10;

		while (i-- > 0) {
			Channel channel = echoClient.createChannel("localhost", EchoServer.PORT);
			Echo echo = new Echo(TimeMillis.INSTANCE.get() + " - " + Math.random());
			System.err.println(echo.getMsg());
			Response response = echoClient.syncSend(channel, echo, 1000);

			EchoAck ack = (EchoAck) response;
			System.err.println(ack.getMsg());
			channel.close().awaitUninterruptibly();

			Thread.sleep(3000);
		}

		echoClient.stop();
	}
}
