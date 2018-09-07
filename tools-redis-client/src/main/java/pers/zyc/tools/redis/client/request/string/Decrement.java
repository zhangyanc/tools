package pers.zyc.tools.redis.client.request.string;

import pers.zyc.tools.redis.client.request.AutoCastRequest;
import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * DECR key
 * </P>
 *
 * 将 key 中储存的数字值减一。
 * 如果 key 不存在，那么 key 的值会先被初始化为 0 ，然后再执行 DECR 操作。
 * 如果值包含错误的类型，或字符串类型的值不能表示为数字，将抛出异常。
 * </p>
 *
 * <ul>
 * <li>时间复杂度: O(1)</li>
 * <li>Redis版本要求: >=1.0.0</li>
 * <li>返回值: 执行 DECR 命令之后 key 的值</li>
 * </ul>
 *
 * @author zhangyancheng
 */
public class Decrement extends AutoCastRequest<Long> {

	public Decrement(String key) {
		super(
				ByteUtil.toByteArray(key)
		);
	}

	@Override
	protected String getCommand() {
		return "DECR";
	}
}
