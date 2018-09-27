package pers.zyc.tools.network;

import pers.zyc.tools.utils.TimeMillis;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zhangyancheng
 */
public abstract class Request extends Command {
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

	public Request(Header header) {
		super(header);
	}
}
