package pers.zyc.tools.network.echo;

import pers.zyc.tools.network.Command;
import pers.zyc.tools.network.CommandFactory;
import pers.zyc.tools.network.Header;

/**
 * @author zhangyancheng
 */
public class EchoCommandFactory implements CommandFactory {

	@Override
	public Command createByType(Header header) {
		switch (header.getCommandType()) {
			case Commands.ECHO:
				return new Echo(header);
			case Commands.ECHO_ACK:
				return new EchoAck(header);
			default:
				throw new Error("Unexpected command type: " + header.getCommandType());
		}
	}
}
