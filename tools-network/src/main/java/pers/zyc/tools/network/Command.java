package pers.zyc.tools.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.util.Objects;

/**
 * @author zhangyancheng
 */
public abstract class Command implements Protocol {

	/**
	 * 3字节长度
	 */
	static final int LENGTH_FIELD_LENGTH = 3;

	/**
	 * 单次网络发送，最大字节数（3字节int最大值）
	 */
	static final int MAX_FRAME_LENGTH = 16777216;//16M

	/**
	 * 命令头部
	 */
	protected final Header header;

	Command(Header header) {
		this.header = Objects.requireNonNull(header);
	}

	/**
	 * 返回命令头部
	 *
	 * @return 命令头部
	 */
	public Header getHeader() {
		return header;
	}

	/**
	 * 返回命令id
	 *
	 * @return 命令id
	 */
	public int getId() {
		return header.getCommandId();
	}

	@Override
	public int getType() {
		return header.getCommandType();
	}

	@Override
	public int getEstimatedSize() {
		//预估为0不影响编码
		return 0;
	}

	@Override
	public void validate() throws Exception {
	}

	@Override
	public void encode(ByteBuf byteBuf) throws Exception {
		validate();

		int writerBegin = byteBuf.writerIndex();
		//空出长度位
		byteBuf.writerIndex(writerBegin + LENGTH_FIELD_LENGTH);

		encodeHead(byteBuf);
		encodeBody(byteBuf);

		int writerEnd = byteBuf.writerIndex();
		//回写总长度
		byteBuf.writerIndex(writerBegin);
		byteBuf.writeMedium(writerEnd - writerBegin);
		byteBuf.writerIndex(writerEnd);
	}

	/**
	 * 编码命令头
	 */
	protected void encodeHead(ByteBuf byteBuf) throws Exception {
		header.encode(byteBuf);
	}

	/**
	 * 编码命令体（命令内容本身）
	 */
	protected abstract void encodeBody(ByteBuf byteBuf) throws Exception;

	/**
	 * 解码响应, Header已经解码, 因此子类重写并解码时已经是body部分
	 *
	 * @param byteBuf 入栈数据
	 */
	@Override
	public void decode(ByteBuf byteBuf) throws Exception {
		decodeBody(byteBuf);

		validate();
	}

	/**
	 * 解码命令体（命令内容本身）
	 */
	protected abstract void decodeBody(ByteBuf byteBuf) throws Exception;

	/**
	 * 命令写网络完成
	 *
	 * @param success 是否写入成功
	 */
	protected void writeComplete(boolean success) {
	}

	/**
	 * 命令编码前分配ByteBuf
	 *
	 * @param ctx ctx
	 * @param preferDirect 优先直接内存
	 * @return 分配用于当前命令编码的ByteBuf
	 */
	protected ByteBuf allocateBuffer(ChannelHandlerContext ctx, boolean preferDirect) {
		//预估初始化大小
		int estimatedCapacity = LENGTH_FIELD_LENGTH + header.getEstimatedSize() + getEstimatedSize();
		if (preferDirect) {
			return ctx.alloc().ioBuffer(estimatedCapacity, MAX_FRAME_LENGTH);
		} else {
			return ctx.alloc().heapBuffer(estimatedCapacity, MAX_FRAME_LENGTH);
		}
	}
}
