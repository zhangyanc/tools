package pers.zyc.tools.network.heartbeat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.zyc.tools.network.ChannelEvent;
import pers.zyc.tools.utils.event.EventListener;

/**
 * @author zhangyancheng
 */
public class ChannelIdleHandler implements EventListener<ChannelEvent> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ChannelIdleHandler.class);

	@Override
	public void onEvent(ChannelEvent event) {
		switch (event.eventType) {
			case WRITE_IDLE:
				try {
					Heartbeat heartbeat = new Heartbeat(event.channel);
					event.getSource().sendOneWay(heartbeat);
				} catch (Exception e) {
					LOGGER.error("Heartbeat error", e);
				}
				break;
			case READ_IDLE:
				event.channel.close();
				break;
		}
	}
}
