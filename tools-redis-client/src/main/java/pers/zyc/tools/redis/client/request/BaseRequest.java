package pers.zyc.tools.redis.client.request;

/**
 * @author zhangyancheng
 */
abstract class BaseRequest implements Request {

	private final byte[][] args;

	BaseRequest(byte[]... args) {
		this.args = args;
	}

	@Override
	public byte[][] getArgs() {
		return args;
	}
}
