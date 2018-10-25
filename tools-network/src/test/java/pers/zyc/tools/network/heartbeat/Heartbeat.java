package pers.zyc.tools.network.heartbeat;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import pers.zyc.tools.network.Header;
import pers.zyc.tools.network.Request;

/**
 * @author zhangyancheng
 */
public class Heartbeat extends Request {

	public static final int CMD_TYPE = 100;

	public Heartbeat(Channel channel) {
		super(CMD_TYPE, false);
		setChannel(channel);
	}

	public Heartbeat(Header header) {
		super(header);
	}

	@Override
	protected void encodeBody(ByteBuf byteBuf) throws Exception {
	}

	@Override
	protected void decodeBody(ByteBuf byteBuf) throws Exception {
	}

	@Override
	public String toString() {
		return "Heartbeat{id:" + getId() + "}";
	}
}
