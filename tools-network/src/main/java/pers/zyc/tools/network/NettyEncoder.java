package pers.zyc.tools.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author zhangyancheng
 */
public class NettyEncoder extends MessageToByteEncoder<Command> {

	private final BufAllocator bufAllocator;

	public NettyEncoder() {
		this.bufAllocator = DEFAULT_BUF_ALLOC;
	}

	public NettyEncoder(BufAllocator bufAllocator) {
		this.bufAllocator = bufAllocator;
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, Command msg, ByteBuf out) throws Exception {
		msg.encode(out);
	}

	@Override
	protected ByteBuf allocateBuffer(ChannelHandlerContext ctx, Command msg, boolean preferDirect) throws Exception {
		return bufAllocator.allocate(ctx, msg, preferDirect);
	}

	private static final BufAllocator DEFAULT_BUF_ALLOC = new BufAllocator() {

		@Override
		public ByteBuf allocate(ChannelHandlerContext ctx, Command command, boolean preferDirect) throws Exception {
			if (preferDirect) {
				return ctx.alloc().ioBuffer();
			} else {
				return ctx.alloc().heapBuffer();
			}
		}
	};
}
