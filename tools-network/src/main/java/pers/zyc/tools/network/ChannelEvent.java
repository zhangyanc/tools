package pers.zyc.tools.network;

import io.netty.channel.Channel;
import pers.zyc.tools.utils.event.SourcedEvent;

/**
 * @author zhangyancheng
 */
public class ChannelEvent extends SourcedEvent<NettyService> {

	public enum EventType {
		CONNECT,
		CLOSE,
		READ_IDLE,
		WRITE_IDLE,
		EXCEPTION
	}

	public final Channel channel;
	public final EventType eventType;

	public ChannelEvent(NettyService source, Channel channel, EventType type) {
		super(source);
		this.channel = channel;
		this.eventType = type;
	}
}
