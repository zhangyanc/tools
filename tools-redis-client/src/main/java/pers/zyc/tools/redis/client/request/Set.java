package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Protocol;

/**
 * @author zhangyancheng
 */
public class Set extends Request {

	public Set(String key, String value) {
		super(Protocol.toByteArray(key), Protocol.toByteArray(value));
	}

	public Set(String key, String value, String nxxx, String expx, long time) {
		super(Protocol.toByteArray(key), Protocol.toByteArray(value),
				Protocol.toByteArray(nxxx), Protocol.toByteArray(expx), Protocol.toByteArray(time));
	}

	public Set(String key, String value, String nxxx) {
		super(Protocol.toByteArray(key), Protocol.toByteArray(value), Protocol.toByteArray(nxxx));
	}
}
