package pers.zyc.tools.redis.client.request.set;

import pers.zyc.tools.redis.client.request.AutoCastRequest;
import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * SRANDMEMBER key [count]
 * </p>
 *
 * 返回集合中的一个随机元素。
 * </p>
 *
 * <ul>
 * <li>时间复杂度: O(1)</li>
 * <li>Redis版本要求: >=1.0.0</li>
 * <li>返回值: 一个随机元素；如果集合为空，返回 null 。</li>
 * </ul>
 *
 * @see SRandomMemberWithCount
 * @author zhangyancheng
 */
public class SRandomMember extends AutoCastRequest<String> {

	public SRandomMember(String key) {
		super(
				ByteUtil.toByteArray(key)
		);
	}

	@Override
	protected String getCommand() {
		return "SRANDMEMBER";
	}
}
