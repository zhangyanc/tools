package pers.zyc.tools.network;

import pers.zyc.tools.utils.TimeMillis;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 请求基类
 *
 * @author zhangyancheng
 */
public abstract class Request extends Command {
	/**
	 * 请求id生成器
	 */
	private static final AtomicInteger REQUEST_ID = new AtomicInteger();

	public Request(int requestType) {
		super(
				new Header()
				.headerType(Header.REQUEST)
				.needAck(true)
				.commandId(REQUEST_ID.incrementAndGet())
				.commandTime(TimeMillis.INSTANCE.get())
				.commandType(requestType)
		);
	}

	public Request(int requestType, boolean needAck) {
		super(
				new Header()
				.headerType(Header.REQUEST)
				.needAck(needAck)
				.commandId(REQUEST_ID.incrementAndGet())
				.commandTime(TimeMillis.INSTANCE.get())
				.commandType(requestType)
		);
	}

	public Request(int requestType, boolean needAck, long commandTime) {
		super(
				new Header()
				.headerType(Header.REQUEST)
				.needAck(true)
				.commandId(REQUEST_ID.incrementAndGet())
				.commandTime(commandTime)
				.commandType(requestType)
		);
	}

	public Request(Header header) {
		super(header);
	}
}
