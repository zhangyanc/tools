package pers.zyc.tools.network.echo.client;

import pers.zyc.tools.network.NetClient;
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
		NetClient echoClient = new NetClient();
		echoClient.setCommandFactory(new EchoCommandFactory());
		echoClient.start();

		int i = 10;
		while (i-- > 0) {
			Echo echo = new Echo(TimeMillis.INSTANCE.get() + " - " + Math.random());
			echo.setChannel(echoClient.createChannel("localhost", EchoServer.PORT));

			Response response = echoClient.sendSync(echo, 1000);

			EchoAck ack = (EchoAck) response;

			if (!echo.getMsg().equals(ack.getMsg())) {
				throw new Error();
			}
			echo.getChannel().close().awaitUninterruptibly();
			Thread.sleep(1000);
		}
		echoClient.stop();
	}
}
