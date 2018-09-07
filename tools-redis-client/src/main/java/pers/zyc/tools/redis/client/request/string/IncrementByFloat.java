package pers.zyc.tools.redis.client.request.string;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * INCRBYFLOAT key increment
 * </p>
 *
 * 为 key 中所储存的值加上浮点数增量 increment 。
 * 如果 key 不存在，那么 INCRBYFLOAT 会先将 key 的值设为 0 ，再执行加法操作。
 * 如果命令执行成功，那么 key 的值会被更新为（执行加法之后的）新值，并且新值会以字符串的形式返回给调用者。
 * key 不是数字字符串类型将抛出异常
 * </p>
 *
 * <ul>
 * <li>时间复杂度: O(1)</li>
 * <li>Redis版本要求: >=2.6.0</li>
 * <li>返回值: 执行命令之后 key 的值。</li>
 * </ul>
 *
 * @author zhangyancheng
 */
public class IncrementByFloat extends Request<Double> {

	public IncrementByFloat(String key, double value) {
		super(
				ByteUtil.toByteArray(key),
				ByteUtil.toByteArray(value)
		);
	}

	@Override
	protected String getCommand() {
		return "INCRBYFLOAT";
	}
}
