package pers.zyc.tools.network.echo.server;

import pers.zyc.tools.network.NettyServer;
import pers.zyc.tools.network.echo.EchoCommandFactory;

import java.io.IOException;

/**
 * @author zhangyancheng
 */
public class EchoServer {

	public static final int PORT = 8806;

	public static void main(String[] args) throws IOException {
		NettyServer echoServer = new NettyServer();
		echoServer.setPort(PORT);

		echoServer.setRequestHandlerFactory(new EchoHandlerFactory(new EchoHandler()));
		echoServer.setCommandFactory(new EchoCommandFactory());
		echoServer.start();

		char quit = (char) System.in.read();
		if (quit == '9') {
			echoServer.stop();
		}
	}
}
