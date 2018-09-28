package pers.zyc.tools.network.daytime.server;

import pers.zyc.tools.network.Commands;
import pers.zyc.tools.network.Response;
import pers.zyc.tools.network.SingleRequestSupportRequestHandler;
import pers.zyc.tools.network.SyncExecutor;
import pers.zyc.tools.network.daytime.Daytime;
import pers.zyc.tools.network.daytime.DaytimeAck;

/**
 * @author zhangyancheng
 */
public class DaytimeRequestHandler extends SingleRequestSupportRequestHandler<Daytime> {

	{
		//daytime命令处理非常简单，在handle调用线程执行
		setExecutor(SyncExecutor.INSTANCE);
	}

	@Override
	public int supportedRequestType() {
		return Commands.DAYTIME;
	}

	@Override
	protected Response handle0(Daytime request) {
		return new DaytimeAck(request.getId());
	}
}
