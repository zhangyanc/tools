package pers.zyc.tools.network.daytime;

import io.netty.buffer.ByteBuf;
import pers.zyc.tools.network.Commands;
import pers.zyc.tools.network.Header;
import pers.zyc.tools.network.Response;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author zhangyancheng
 */
public class DaytimeAck extends Response {

	private static final String DAYTIME_PATTERN = "yyyy/MM/dd HH:mm:ss";

	private static final ThreadLocal<SimpleDateFormat> DATE_FORMAT_THREAD_LOCAL = new ThreadLocal<SimpleDateFormat>() {

		@Override
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat(DAYTIME_PATTERN);
		}
	};

	private String daytime = DATE_FORMAT_THREAD_LOCAL.get().format(new Date());

	public DaytimeAck(int requestId) {
		super(Commands.DAYTIME_ACK, requestId);
	}

	public DaytimeAck(Header header) {
		super(header);
	}

	@Override
	public int getEstimatedSize() {
		return DAYTIME_PATTERN.length();
	}

	@Override
	protected void encodeBody(ByteBuf byteBuf) throws Exception {
		byteBuf.writeBytes(daytime.getBytes());
	}

	@Override
	protected void decodeBody(ByteBuf byteBuf) throws Exception {
		byte[] daytimeBytes = new byte[DAYTIME_PATTERN.length()];
		byteBuf.readBytes(daytimeBytes);
		daytime = new String(daytimeBytes);
	}

	public String getDaytime() {
		return daytime;
	}
}
