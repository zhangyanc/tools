package pers.zyc.tools.network.echo;

import io.netty.buffer.ByteBuf;
import pers.zyc.tools.network.Header;
import pers.zyc.tools.network.Request;

/**
 * @author zhangyancheng
 */
public class Echo extends Request {
	private String msg;

	public Echo(String msg) {
		super(Commands.ECHO);
		this.msg = msg;
	}

	public Echo(Header header) {
		super(header);
	}

	@Override
	public void validate() throws Exception {
		if (msg == null) {
			throw new NullPointerException();
		}
	}

	@Override
	public int getEstimatedSize() {
		return msg.length() * 2;
	}

	@Override
	protected void encodeBody(ByteBuf byteBuf) throws Exception {
		byte[] msgBytes = msg.getBytes(UTF_8);
		byteBuf.writeInt(msgBytes.length);
		byteBuf.writeBytes(msgBytes);
	}

	@Override
	public void decodeBody(ByteBuf byteBuf) throws Exception {
		byte[] msgBytes = new byte[byteBuf.readInt()];
		byteBuf.readBytes(msgBytes);
		this.msg = new String(msgBytes, UTF_8);
	}

	public String getMsg() {
		return msg;
	}
}
