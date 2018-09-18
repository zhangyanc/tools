package pers.zyc.tools.network;

import pers.zyc.tools.utils.TimeMillis;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zhangyancheng
 */
public abstract class Request extends Command {
	private static final AtomicInteger REQUEST_ID = new AtomicInteger();

	public Request(int requestType) {
		header.headerType(Header.REQUEST)
				.needAck(true)
				.commandId(REQUEST_ID.incrementAndGet())
				.commandTime(TimeMillis.INSTANCE.get())
				.commandType(requestType);
	}

	public Request(int requestType, int version) {
		this(requestType);
		header.commandVersion(version);
	}

	public Request(int requestType, int version, long requestTime) {
		this(requestType, version);
		header.commandTime(requestTime);
	}

	public int getRequestId() {
		return header.getCommandId();
	}

	@Override
	public int getEstimatedSize() {
		//预估为0不影响数据编码
		return 0;
	}

	@Override
	public int getType() {
		return header.getCommandType();
	}

	@Override
	public void validate() throws Exception {
	}
}
