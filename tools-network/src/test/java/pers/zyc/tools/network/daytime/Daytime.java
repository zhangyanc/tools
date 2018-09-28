package pers.zyc.tools.network.daytime;

import io.netty.buffer.ByteBuf;
import pers.zyc.tools.network.Commands;
import pers.zyc.tools.network.Header;
import pers.zyc.tools.network.Request;

/**
 * @author zhangyancheng
 */
public class Daytime extends Request {

	public Daytime() {
		super(Commands.DAYTIME);
	}

	public Daytime(Header header) {
		super(header);
	}

	@Override
	protected void encodeBody(ByteBuf byteBuf) throws Exception {

	}

	@Override
	protected void decodeBody(ByteBuf byteBuf) throws Exception {

	}
}
