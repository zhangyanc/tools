package pers.zyc.tools.redis.client.request.hash;

import pers.zyc.tools.redis.client.request.AutoCastRequest;
import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * HGET key field
 * </p>
 *
 * 返回哈希表 key 中给定域 field 的值。
 *
 * <ul>
 * <li>时间复杂度: O(1)</li>
 * <li>Redis版本要求: >=2.0.0</li>
 * <li>返回值: 给定域的值。当给定域不存在或是给定 key 不存在时，返回 null 。</li>
 * </ul>
 *
 * @author zhangyancheng
 */
public class HGet extends AutoCastRequest<String> {

	public HGet(String key, String field) {
		super(
				ByteUtil.toByteArray(key),
				ByteUtil.toByteArray(field)
		);
	}
}
