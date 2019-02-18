package pers.zyc.tools.network.heartbeat;

import pers.zyc.tools.network.Command;
import pers.zyc.tools.network.CommandFactory;
import pers.zyc.tools.network.Header;

/**
 * @author zhangyancheng
 */
public class HeartbeatCommandFactory implements CommandFactory {

	@Override
	public Command createByHeader(Header header) {
		return new Heartbeat(header);
	}
}
