package pers.zyc.tools.redis.client.request.string;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * INCRBY key increment
 * </p>
 *
 * 将 key 所储存的值加上增量 increment 。
 * 如果 key 不存在，那么 key 的值会先被初始化为 0 ，然后再执行 INCRBY 命令。
 * 如果值包含错误的类型，或字符串类型的值不能表示为数字，那么抛出异常。
 * </p>
 *
 * <ul>
 * <li>时间复杂度: O(1)</li>
 * <li>Redis版本要求: >=1.0.0</li>
 * <li>返回值: 加上 increment 之后， key 的值。</li>
 * </ul>
 *
 * @author zhangyancheng
 */
public class IncrementBy extends Request<Long> {

	public IncrementBy(String key, long integer) {
		super(
				ByteUtil.toByteArray(key),
				ByteUtil.toByteArray(integer)
		);
	}

	@Override
	protected String getCommand() {
		return "INCRBY";
	}
}
