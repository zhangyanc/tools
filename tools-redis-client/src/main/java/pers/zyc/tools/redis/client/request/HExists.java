package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * HEXISTS key field
 * </p>
 *
 * 查看哈希表 key 中，给定域 field 是否存在。
 *
 * <ul>
 * <li>时间复杂度: O(1)</li>
 * <li>Redis版本要求: >=2.0.0</li>
 * <li>返回值: 如果哈希表含有给定域，返回 true 。如果哈希表不含有给定域，或 key 不存在，返回 false 。</li>
 * </ul>
 *
 * @author zhangyancheng
 */
public class HExists extends Request<Boolean> {

	public HExists(String key, String field) {
		super(
				ByteUtil.toByteArray(key),
				ByteUtil.toByteArray(field)
		);
	}
}
