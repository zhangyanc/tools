package pers.zyc.tools.redis.client.request.string;

import pers.zyc.tools.redis.client.request.AutoCastRequest;
import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * GETBIT key offset
 * </p>
 *
 * 对 key 所储存的字符串值，获取指定偏移量上的位(bit)，位为1时返回 true, 否则返回 false。。
 * 当 offset 比字符串值的长度大，或者 key 不存在时，返回 false 。
 *
 * <ul>
 * <li>时间复杂度: O(1)</li>
 * <li>Redis版本要求: >=2.2.0</li>
 * <li>返回值: 位为1时返回 true, 否则返回 false。</li>
 * </ul>
 *
 * @author zhangyancheng
 */
public class GetBit extends AutoCastRequest<Boolean> {

	public GetBit(String key, long offset) {
		super(
				ByteUtil.toByteArray(key),
				ByteUtil.toByteArray(offset)
		);
	}
}
