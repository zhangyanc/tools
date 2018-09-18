package pers.zyc.tools.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * @author zhangyancheng
 */
public class NettyDecoder extends LengthFieldBasedFrameDecoder {
	private final CommandFactory commandFactory;

	public NettyDecoder(int maxFrameLength, CommandFactory commandFactory) {
		super(maxFrameLength, 0, 4, -4, 0);
		this.commandFactory = commandFactory;
	}

	@Override
	protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
		ByteBuf frame = (ByteBuf) super.decode(ctx, in);
		if (frame == null) {
			//未完整包
			return null;
		}
		Header header = new Header();
		header.decode(frame);

		Command command = commandFactory.createByType(header.getCommandType());
		command.decode(frame);

		return command;
	}

	@Override
	protected ByteBuf extractFrame(ChannelHandlerContext ctx, ByteBuf buffer, int index, int length) {
		return buffer.slice(index, length);
	}
}
