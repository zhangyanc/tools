package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * INCR key
 * </p>
 *
 * 将 key 中储存的数字值增一。
 * 如果 key 不存在，那么 key 的值会先被初始化为 0 ，然后再执行 INCR 操作。
 * 如果值包含错误的类型，或字符串类型的值不能表示为数字，那么抛出异常。
 * </p>
 *
 * <ul>
 * <li>时间复杂度: O(1)</li>
 * <li>Redis版本要求: >=1.0.0</li>
 * <li>返回值: 执行 INCR 命令之后 key 的值。</li>
 * </ul>
 *
 * @author zhangyancheng
 */
public class Increment extends Request<Long> {

	public Increment(String key) {
		super(
				ByteUtil.toByteArray(key)
		);
	}

	@Override
	protected String getCommand() {
		return "INCR";
	}
}
