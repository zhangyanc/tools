package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Protocol;

/**
 * @author zhangyancheng
 */
public class Exists extends BaseRequest {

	public Exists(String key) {
		super(Protocol.toByteArray(key));
	}

	@Override
	public Command getCmd() {
		return Command.EXISTS;
	}
}
