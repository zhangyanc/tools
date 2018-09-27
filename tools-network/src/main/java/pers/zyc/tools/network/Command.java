package pers.zyc.tools.network;

import io.netty.buffer.ByteBuf;

import java.util.Objects;

/**
 * @author zhangyancheng
 */
public abstract class Command implements Protocol {

	/**
	 * 单次网络发送，最大字节数
	 */
	static final int MAX_FRAME_LENGTH = 16777216;//16M

	static final int LENGTH_FIELD_LENGTH = 3;

	protected final Header header;

	Command(Header header) {
		this.header = Objects.requireNonNull(header);
	}

	public Header getHeader() {
		return header;
	}

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

		int begin = byteBuf.writerIndex();

		//预估大小, 提前扩容
		int estimatedSize = begin + 3 + header.getEstimatedSize() + getEstimatedSize();
		if (byteBuf.capacity() < estimatedSize) {
			byteBuf.capacity(estimatedSize);
		}

		byteBuf.writerIndex(begin + 3);//写长度(先填0占位)

		header.encode(byteBuf);

		encodeBody(byteBuf);

		int end = byteBuf.writerIndex();
		//回写3字节总长度
		byteBuf.writerIndex(begin);
		byteBuf.writeMedium(end - begin);
		byteBuf.writerIndex(end);
	}

	protected abstract void encodeBody(ByteBuf byteBuf) throws Exception;

	protected void writeComplete(boolean success) {
	}

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

	protected abstract void decodeBody(ByteBuf byteBuf) throws Exception;
}
