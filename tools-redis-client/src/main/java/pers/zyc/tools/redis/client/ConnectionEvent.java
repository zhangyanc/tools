package pers.zyc.tools.redis.client;

import pers.zyc.tools.event.SourcedEvent;

/**
 * @author zhangyancheng
 */
class ConnectionEvent extends SourcedEvent<Connection> {

	ConnectionEvent(Connection source) {
		super(source);
	}
}
