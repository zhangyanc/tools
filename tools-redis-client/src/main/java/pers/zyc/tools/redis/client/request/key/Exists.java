package pers.zyc.tools.redis.client.request.key;

import pers.zyc.tools.redis.client.request.AutoCastRequest;
import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * EXISTS key
 * </p>
 *
 * 检查给定 key 是否存在。
 * </p>
 *
 * <ul>
 * <li>时间复杂度: O(1)</li>
 * <li>Redis版本要求: >=2.0.0</li>
 * <li>返回值: 若 key 存在，返回 true ，否则返回 false 。</li>
 * </ul>
 *
 * @author zhangyancheng
 */
public class Exists extends AutoCastRequest<Boolean> {

	public Exists(String key) {
		super(
				ByteUtil.toByteArray(key)
		);
	}
}
