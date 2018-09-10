package pers.zyc.tools.redis.client.request.set;

import pers.zyc.tools.redis.client.request.AutoCastRequest;
import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * SCARD key
 * </p>
 *
 * 返回集合 key 的基数(集合中元素的数量)。
 * </p>
 *
 * <ul>
 * <li>时间复杂度: O(1)</li>
 * <li>Redis版本要求: >=1.0.0</li>
 * <li>返回值: 集合中元素的数量，当 key 不存在时，返回 0 。</li>
 * </ul>
 *
 * @author zhangyancheng
 */
public class SCard extends AutoCastRequest<Long> {

	public SCard(String key) {
		super(
				ByteUtil.toByteArray(key)
		);
	}
}
