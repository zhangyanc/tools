package pers.zyc.tools.network;

import io.netty.buffer.ByteBuf;

/**
 * @author zhangyancheng
 */
public class Heartbeat extends Request {

	public Heartbeat() {
		super(CommandFactory.HEARTBEAT);

		//心跳不需要应答
		header.needAck(false);
	}

	@Override
	protected void encodeBody(ByteBuf byteBuf) throws Exception {
		//心跳请求没有请求内容, 无需编码
	}

	@Override
	public void decode(ByteBuf byteBuf) throws Exception {
		//心跳请求没有请求内容, 无需解码
	}
}
