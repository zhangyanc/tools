package pers.zyc.tools.redis.client.request.string;

import pers.zyc.tools.redis.client.request.AutoCastRequest;
import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * APPEND key value
 * </p>
 *
 * 如果 key 已经存在并且是一个字符串，APPEND 命令将 value 追加到 key 原来的值的末尾。
 * 如果 key 不存在，APPEND 就简单地将给定 key 设为 value，就像执行 SET key value 一样。
 * </p>
 *
 * <ul>
 * <li>时间复杂度: O(1)</li>
 * <li>Redis版本要求: >=2.0.0</li>
 * <li>返回值: 追加 value 之后，key 中字符串的长度。</li>
 * </ul>
 *
 * @author zhangyancheng
 */
public class Append extends AutoCastRequest<Long> {

	public Append(String key, String value) {
		super(
				ByteUtil.toByteArray(key),
				ByteUtil.toByteArray(value)
		);
	}
}
