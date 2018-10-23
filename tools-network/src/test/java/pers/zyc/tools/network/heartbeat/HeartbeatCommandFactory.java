package pers.zyc.tools.network.heartbeat;

import pers.zyc.tools.network.Command;
import pers.zyc.tools.network.CommandFactory;
import pers.zyc.tools.network.Commands;
import pers.zyc.tools.network.Header;

/**
 * @author zhangyancheng
 */
public class HeartbeatCommandFactory implements CommandFactory {

	@Override
	public Command createByType(Header header) {
		assert header.getCommandType() == Commands.HEARTBEAT;
		return new Heartbeat(header);
	}
}
