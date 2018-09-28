package pers.zyc.tools.network.daytime.server;

import pers.zyc.tools.network.RegistrableRequestHandlerFactory;

/**
 * @author zhangyancheng
 */
public class DaytimeRequestHandlerFactory extends RegistrableRequestHandlerFactory {

	{
		register(new DaytimeRequestHandler());
	}
}
