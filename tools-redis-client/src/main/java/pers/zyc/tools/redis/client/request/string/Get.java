package pers.zyc.tools.redis.client.request.string;

import pers.zyc.tools.redis.client.request.AutoCastRequest;
import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * GET key
 * </p>
 *
 * 返回 key 所关联的字符串值。
 * 如果 key 不存在返回 null 。
 * 假如 key 储存的值不是字符串类型，抛出异常，因为 GET 只能用于处理字符串值。
 * </p>
 *
 * <ul>
 * <li>时间复杂度: O(1)</li>
 * <li>Redis版本要求: >=1.0.0</li>
 * <li>返回值: 当 key 不存在时，返回 null ，否则，返回 key 的值。</li>
 * </ul>
 *
 *
 * @author zhangyancheng
 */
public class Get extends AutoCastRequest<String> {

	public Get(String key) {
		super(
				ByteUtil.toByteArray(key)
		);
	}
}
