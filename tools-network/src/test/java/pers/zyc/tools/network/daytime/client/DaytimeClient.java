package pers.zyc.tools.network.daytime.client;

import io.netty.channel.Channel;
import pers.zyc.tools.network.NetClient;
import pers.zyc.tools.network.daytime.Daytime;
import pers.zyc.tools.network.daytime.DaytimeAck;
import pers.zyc.tools.network.daytime.DaytimeCommandFactory;
import pers.zyc.tools.network.daytime.server.DaytimeServer;

/**
 * @author zhangyancheng
 */
public class DaytimeClient {

	public static void main(String[] args) throws InterruptedException {
		NetClient daytimeClient = new NetClient();
		daytimeClient.setCommandFactory(new DaytimeCommandFactory());
		daytimeClient.start();

		Channel channel = daytimeClient.createChannel("localhost", DaytimeServer.PORT);
		int i = 10;
		while (i-- > 0) {
			DaytimeAck ack = (DaytimeAck) daytimeClient.sendSync(channel, new Daytime());
			System.out.println(ack.getDaytime());
			Thread.sleep(2000);
		}
		daytimeClient.stop();
	}
}
