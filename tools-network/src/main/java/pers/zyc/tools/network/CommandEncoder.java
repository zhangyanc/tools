package pers.zyc.tools.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author zhangyancheng
 */
public class CommandEncoder extends MessageToByteEncoder<Command> {

	@Override
	protected void encode(ChannelHandlerContext channelHandlerContext,
						  Command command, ByteBuf byteBuf) throws Exception {
		//byteBuf.writeBytes(command.encode());
	}
}
