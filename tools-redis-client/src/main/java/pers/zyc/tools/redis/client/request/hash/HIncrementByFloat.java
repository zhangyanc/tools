package pers.zyc.tools.redis.client.request.hash;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * HINCRBYFLOAT key field increment
 * </p>
 *
 * 为哈希表 key 中的域 field 加上浮点数增量 increment 。
 * 如果哈希表中没有域 field ，那么 HINCRBYFLOAT 会先将域 field 的值设为 0 ，然后再执行加法操作。
 * 如果键 key 不存在，那么 HINCRBYFLOAT 会先创建一个哈希表，再创建域 field ，最后再执行加法操作。
 * 域 field 的值不是数字字符串类型将抛出异常
 * </p>
 *
 * <ul>
 * <li>时间复杂度: O(1)</li>
 * <li>Redis版本要求: >=2.6.0</li>
 * <li>返回值: 执行加法操作之后 field 域的值。</li>
 * </ul>
 *
 * @author zhangyancheng
 */
public class HIncrementByFloat extends Request<Double> {

	public HIncrementByFloat(String key, String field, double value) {
		super(
				ByteUtil.toByteArray(key),
				ByteUtil.toByteArray(field),
				ByteUtil.toByteArray(value)
		);
	}

	@Override
	protected String getCommand() {
		return "HINCRBYFLOAT";
	}
}
