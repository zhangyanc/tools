package pers.zyc.tools.network;

import io.netty.buffer.ByteBuf;

import java.util.Objects;

/**
 * @author zhangyancheng
 */
public abstract class Command implements Protocol {

	protected final Header header;

	public Command() {
		this.header = new Header();
	}

	protected Command(Header header) {
		this.header = Objects.requireNonNull(header);
	}

	public Header getHeader() {
		return header;
	}

	public boolean isRequest() {
		return header.isRequest();
	}

	@Override
	public void encode(ByteBuf byteBuf) throws Exception {
		int begin = byteBuf.writerIndex();

		//预估大小, 提前扩容
		int estimatedSize = begin + 4 + header.getEstimatedSize() + getEstimatedSize();
		if (byteBuf.capacity() < estimatedSize) {
			byteBuf.capacity(estimatedSize);
		}

		byteBuf.writeInt(0);//写长度(先填0占位)

		header.encode(byteBuf);

		encodeBody(byteBuf);

		//回写总长度
		int size = byteBuf.writerIndex() - begin;
		byteBuf.writerIndex(begin);
		byteBuf.writeInt(size);
	}

	protected abstract void encodeBody(ByteBuf byteBuf) throws Exception;

	/**
	 * 解码响应, Header已经解码, 因此子类重写并解码时已经是body部分
	 *
	 * @param byteBuf 入栈数据
	 */
	@Override
	public abstract void decode(ByteBuf byteBuf) throws Exception;
}
