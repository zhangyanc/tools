package pers.zyc.tools.redis.client.request.string;

import pers.zyc.tools.redis.client.request.AutoCastRequest;
import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * BITCOUNT key [start] [end]
 * </p>
 *
 * 计算给定字符串中，被设置为 1 的比特位的数量。
 * 一般情况下，给定的整个字符串都会被进行计数，通过指定额外的 start 或 end 参数，可以让计数只在特定的位上进行。
 * start 和 end 参数的设置和 {@link GetRange} 命令类似，都可以使用负数值：比如 -1 表示最后一个位，而 -2 表示倒数第二个位，以此类推。
 * 不存在的 key 被当成是空字符串来处理，因此对一个不存在的 key 进行 BITCOUNT 操作，结果为 0 。
 * </p>
 *
 * <ul>
 * <li>时间复杂度: O(N)</li>
 * <li>Redis版本要求: >=2.6.0</li>
 * <li>返回值: 被设置为 1 的位的数量。</li>
 * </ul>
 *
 * @author zhangyancheng
 */
public class BitCount extends AutoCastRequest<Long> {

	public BitCount(String key) {
		super(
				ByteUtil.toByteArray(key)
		);
	}

	public BitCount(String key, int start) {
		super(
				ByteUtil.toByteArray(key),
				ByteUtil.toByteArray(start)
		);
	}

	public BitCount(String key, int start, int end) {
		super(
				ByteUtil.toByteArray(key),
				ByteUtil.toByteArray(start),
				ByteUtil.toByteArray(end)
		);
	}
}
