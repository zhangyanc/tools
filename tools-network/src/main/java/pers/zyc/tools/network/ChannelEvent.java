package pers.zyc.tools.network;

import io.netty.channel.Channel;
import pers.zyc.tools.utils.event.SourcedEvent;

/**
 * @author zhangyancheng
 */
public class ChannelEvent extends SourcedEvent<NetService> {

	public enum EventType {
		/**
		 * 连接就绪
		 */
		CONNECT,
		/**
		 * 连接关闭
		 */
		CLOSE,
		/**
		 * 连接读空闲
		 */
		READ_IDLE,
		/**
		 * 连接写空闲
		 */
		WRITE_IDLE,
		/**
		 * 连接异常
		 */
		EXCEPTION
	}

	public final Channel channel;
	public final EventType eventType;

	public ChannelEvent(NetService source, Channel channel, EventType type) {
		super(source);
		this.channel = channel;
		this.eventType = type;
	}
}
