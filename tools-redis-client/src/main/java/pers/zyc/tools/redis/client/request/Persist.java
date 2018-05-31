package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Protocol;

/**
 * @author zhangyancheng
 */
public class Persist extends BaseRequest {

	public Persist(String key) {
		super(Protocol.toByteArray(key));
	}

	@Override
	public Command getCmd() {
		return Command.PERSIST;
	}
}
