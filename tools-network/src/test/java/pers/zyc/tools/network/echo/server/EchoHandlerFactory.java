package pers.zyc.tools.network.echo.server;

import pers.zyc.tools.network.RequestHandler;
import pers.zyc.tools.network.RequestHandlerFactory;
import pers.zyc.tools.network.echo.Commands;

import java.util.Objects;

/**
 * @author zhangyancheng
 */
public class EchoHandlerFactory implements RequestHandlerFactory {

	private final EchoHandler echoHandler;

	public EchoHandlerFactory(EchoHandler echoHandler) {
		this.echoHandler = Objects.requireNonNull(echoHandler);
	}

	@Override
	public RequestHandler getHandler(int requestType) {
		assert requestType == Commands.ECHO;
		return echoHandler;
	}
}
