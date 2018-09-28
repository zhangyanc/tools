package pers.zyc.tools.network;

import pers.zyc.tools.utils.TimeMillis;

/**
 * 响应基类
 *
 * @author zhangyancheng
 */
public abstract class Response extends Command {

	public Response(int responseType, int requestId) {
		super(
				new Header()
				.headerType(Header.RESPONSE)
				.needAck(false)
				.commandId(requestId)
				.commandTime(TimeMillis.INSTANCE.get())
				.commandType(responseType)
		);
	}

	public Response(int responseType, int requestId, long commandTime) {
		super(
				new Header()
				.headerType(Header.RESPONSE)
				.needAck(false)
				.commandId(requestId)
				.commandTime(commandTime)
				.commandType(responseType)
		);
	}

	public Response(Header header) {
		super(header);
	}
}
