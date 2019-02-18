package pers.zyc.tools.network.heartbeat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.zyc.tools.network.*;

/**
 * @author zhangyancheng
 */
public class HeartbeatRequestHandlerFactory implements RequestHandlerFactory {

	private static final Logger LOGGER = LoggerFactory.getLogger(HeartbeatRequestHandlerFactory.class);

	private final RequestHandler heartbeatHandler = new SingleTypeRequestHandler<Heartbeat>() {

		@Override
		public int supportedRequestType() {
			return Heartbeat.CMD_TYPE;
		}

		@Override
		protected Response handle0(Heartbeat heartbeat) {
			LOGGER.info("Received {}, Channel{}", heartbeat, heartbeat.getChannel());
			return null;
		}
	};

	@Override
	public RequestHandler getHandler(int requestType) {
		return heartbeatHandler;
	}
}
