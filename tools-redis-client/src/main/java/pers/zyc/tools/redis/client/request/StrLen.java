package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * STRLEN key
 * </p>
 *
 * 返回 key 所储存的字符串值的长度。
 * 当 key 储存的不是字符串值时，抛出异常。
 * </p>
 *
 * <ul>
 * <li>时间复杂度: O(1)</li>
 * <li>Redis版本要求: >=2.2.0</li>
 * <li>返回值: 字符串值的长度。当 key 不存在时，返回 0 。</li>
 * </ul>
 *
 * @author zhangyancheng
 */
public class StrLen extends Request<Long> {

	public StrLen(String key) {
		super(
				ByteUtil.toByteArray(key)
		);
	}
}
