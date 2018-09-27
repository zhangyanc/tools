package pers.zyc.tools.network.echo.server;

import pers.zyc.tools.network.Request;
import pers.zyc.tools.network.RequestHandler;
import pers.zyc.tools.network.Response;
import pers.zyc.tools.network.echo.Echo;
import pers.zyc.tools.network.echo.EchoAck;

import java.util.concurrent.Executor;

/**
 * @author zhangyancheng
 */
public class EchoHandler implements RequestHandler {

	private static final Executor ECHO_EXECUTOR = new Executor() {
		@Override
		public void execute(Runnable command) {
			command.run();
		}
	};

	@Override
	public Executor getExecutor() {
		return ECHO_EXECUTOR;
	}

	@Override
	public Response handle(Request request) {
		Echo echo = (Echo) request;
		return new EchoAck(echo.getId(), echo.getMsg());
	}
}
