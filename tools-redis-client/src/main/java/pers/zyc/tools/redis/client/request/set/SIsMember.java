package pers.zyc.tools.redis.client.request.set;

import pers.zyc.tools.redis.client.request.AutoCastRequest;
import pers.zyc.tools.redis.client.request.BaseScan;
import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * SISMEMBER key member
 * </p>
 *
 * 判断 member 元素是否集合 key 的成员。
 * </p>
 *
 * <ul>
 * <li>时间复杂度: O(1)</li>
 * <li>Redis版本要求: >=1.0.0</li>
 * <li>返回值: 如果 member 元素是集合的成员，返回 true 。
 * 			  如果 member 元素不是集合的成员，或 key 不存在，返回 false 。
 *  </li>
 * </ul>
 *
 * @author zhangyancheng
 */
public class SIsMember extends AutoCastRequest<BaseScan> {

	public SIsMember(String key, String member) {
		super(
				ByteUtil.toByteArray(key),
				ByteUtil.toByteArray(member)
		);
	}
}
