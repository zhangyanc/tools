package pers.zyc.tools.redis.client.request.client;

import pers.zyc.tools.redis.client.request.AutoCastRequest;

/**
 * @author zhangyancheng
 */
public abstract class Client<R> extends AutoCastRequest<R> {

	Client(byte[]... bulks) {
		super(bulks);
	}

	@Override
	protected String getCommand() {
		return "CLIENT";
	}
}
