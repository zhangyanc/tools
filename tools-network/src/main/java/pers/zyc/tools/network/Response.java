package pers.zyc.tools.network;

import pers.zyc.tools.utils.TimeMillis;

/**
 * @author zhangyancheng
 */
public abstract class Response extends Command {

	public Response(int responseType, int requestId) {
		header.headerType(Header.RESPONSE)
				.needAck(false)
				.commandId(requestId)
				.commandTime(TimeMillis.INSTANCE.get())
				.commandType(responseType);
	}

	public Response(int responseType, int requestId, int version) {
		this(responseType, requestId);
		header.commandVersion(version);
	}

	public Response(int responseType, int requestId, int version, long requestTime) {
		this(responseType, requestId, version);
		header.commandTime(requestTime);
	}

	@Override
	public int getType() {
		return header.getCommandType();
	}

	@Override
	public int getEstimatedSize() {
		//预估为0不影响数据编码
		return 0;
	}

	@Override
	public void validate() throws Exception {
	}
}
