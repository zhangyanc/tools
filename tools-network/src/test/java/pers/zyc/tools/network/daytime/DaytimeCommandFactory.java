package pers.zyc.tools.network.daytime;

import pers.zyc.tools.network.Commands;
import pers.zyc.tools.network.RegistrableCommandFactory;

/**
 * @author zhangyancheng
 */
public class DaytimeCommandFactory extends RegistrableCommandFactory {

	{
		register(Commands.DAYTIME, Daytime.class);
		register(Commands.DAYTIME_ACK, DaytimeAck.class);
	}
}
