package pers.zyc.tools.redis.client.request.string;

import pers.zyc.tools.redis.client.request.AutoCastRequest;
import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * GETSET key value
 * </p>
 *
 * 将给定 key 的值设为 value ，并返回 key 的旧值(old value)。
 * 当 key 存在但不是字符串类型时，抛出异常。
 * </p>
 *
 * <ul>
 * <li>时间复杂度: O(1)</li>
 * <li>Redis版本要求: >=1.0.0</li>
 * <li>返回值: 返回给定 key 的旧值，当 key 没有旧值时，也即是， key 不存在时，返回 null 。</li>
 * </ul>
 *
 * @author zhangyancheng
 */
public class GetSet extends AutoCastRequest<String> {

	public GetSet(String key, String value) {
		super(
				ByteUtil.toByteArray(key),
				ByteUtil.toByteArray(value)
		);
	}
}
