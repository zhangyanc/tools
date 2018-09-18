package pers.zyc.tools.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author zhangyancheng
 */
public interface BufAllocator {

	ByteBuf allocate(ChannelHandlerContext ctx, Command command, boolean preferDirect) throws Exception;
}
