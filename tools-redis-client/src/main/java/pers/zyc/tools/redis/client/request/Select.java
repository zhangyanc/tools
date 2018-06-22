package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.Util;

/**
 * @author zhangyancheng
 */
public class Select extends Request {

	public Select(int index) {
		super(
				Util.toByteArray(index)
		);
	}
}
