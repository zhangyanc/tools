package pers.zyc.tools.network.echo.server;

import pers.zyc.tools.network.RegistrableRequestHandlerFactory;

/**
 * @author zhangyancheng
 */
public class EchoHandlerFactory extends RegistrableRequestHandlerFactory {
	{
		register(new EchoHandler());
	}
}
