package pers.zyc.tools.redis.client.request.hash;

import pers.zyc.tools.redis.client.request.AutoCastRequest;
import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * HLEN key
 * </p>
 *
 * 返回哈希表 key 中域的数量。
 * </p>
 *
 * <ul>
 * <li>时间复杂度: O(1)</li>
 * <li>Redis版本要求: >=1.0.0</li>
 * <li>返回值: 哈希表中域的数量，当 key 不存在时，返回 0 。</li>
 * </ul>
 *
 * @author zhangyancheng
 */
public class HLength extends AutoCastRequest<Long> {

	public HLength(String key) {
		super(
				ByteUtil.toByteArray(key)
		);
	}

	@Override
	protected String getCommand() {
		return "HLEN";
	}
}
