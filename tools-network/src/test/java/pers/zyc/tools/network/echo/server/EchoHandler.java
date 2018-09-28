package pers.zyc.tools.network.echo.server;

import pers.zyc.tools.network.Response;
import pers.zyc.tools.network.SingleRequestSupportRequestHandler;
import pers.zyc.tools.network.echo.Commands;
import pers.zyc.tools.network.echo.Echo;
import pers.zyc.tools.network.echo.EchoAck;

import java.util.concurrent.Executor;

/**
 * @author zhangyancheng
 */
public class EchoHandler extends SingleRequestSupportRequestHandler<Echo> {

	{
		//echo命令处理非常简单，在handle调用线程执行
		setExecutor(new Executor() {
			@Override
			public void execute(Runnable command) {
				command.run();
			}
		});
	}

	@Override
	public int supportedRequestType() {
		return Commands.ECHO;
	}

	@Override
	protected Response handle0(Echo echo) {
		return new EchoAck(echo.getId(), echo.getMsg());
	}
}
