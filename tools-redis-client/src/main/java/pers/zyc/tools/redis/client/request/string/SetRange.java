package pers.zyc.tools.redis.client.request.string;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * SETRANGE key offset value
 * </p>
 *
 * 用 value 参数覆写(overwrite)给定 key 所储存的字符串值，从偏移量 offset 开始。
 * 不存在的 key 当作空白字符串处理。
 * 如果给定 key 原来储存的字符串长度比偏移量小，那么原字符和偏移量之间的空白将用零字节来填充。
 * </p>
 *
 * <ul>
 * <li>时间复杂度: O(M)，M 为 value 参数的长度。</li>
 * <li>Redis版本要求: >=2.2.0</li>
 * <li>返回值: 被 SETRANGE 修改之后，字符串的长度。</li>
 * </ul>
 *
 * @author zhangyancheng
 */
public class SetRange extends Request<Long> {

	public SetRange(String key, long offset, String value) {
		super(
				ByteUtil.toByteArray(key),
				ByteUtil.toByteArray(offset),
				ByteUtil.toByteArray(value)
		);
	}
}
