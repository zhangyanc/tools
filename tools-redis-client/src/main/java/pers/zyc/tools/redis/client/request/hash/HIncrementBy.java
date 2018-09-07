package pers.zyc.tools.redis.client.request.hash;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * HINCRBY key field increment
 * </p>
 *
 * 为哈希表 key 中的域 field 的值加上增量 increment。
 * 增量可以为负数，相当于对给定域进行减法操作。
 * 如果 key 不存在，一个新的哈希表被创建并执行 HINCRBY 命令。
 * 如果域 field 不存在，那么在执行命令前, 域的值被初始化为 0。
 * 对一个储存字符串值的域 field 执行 HINCRBY 命令将抛出异常。
 * </p>
 *
 * <ul>
 * <li>时间复杂度: O(1)</li>
 * <li>Redis版本要求: >=2.0.0</li>
 * <li>返回值: 执行 HINCRBY 命令之后，哈希表 key 中域 field 的值。</li>
 * </ul>
 *
 * @author zhangyancheng
 */
public class HIncrementBy extends Request<Long> {

	public HIncrementBy(String key, String field, long value) {
		super(
				ByteUtil.toByteArray(key),
				ByteUtil.toByteArray(field),
				ByteUtil.toByteArray(value)
		);
	}

	@Override
	protected String getCommand() {
		return "HINCRBY";
	}
}
