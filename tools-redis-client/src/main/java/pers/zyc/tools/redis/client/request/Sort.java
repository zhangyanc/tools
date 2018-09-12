package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.util.ByteUtil;

import java.util.List;

/**
 * SORT key [BY pattern] [LIMIT offset count] [GET pattern [GET pattern ...]] [ASC | DESC] [ALPHA] [STORE destination]
 * </p>
 *
 * @author zhangyancheng
 */
public class Sort extends AutoCastRequest<List<String>> {

	public Sort(String key) {
		super(
				ByteUtil.toByteArray(key)
		);
	}

	//TODO add constructor
}
