package pers.zyc.tools.redis.client.request.string;

import pers.zyc.tools.redis.client.request.AutoCastRequest;
import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * SETBIT key offset value
 * </p>
 *
 * 对 key 所储存的字符串值，设置或清除指定偏移量上的位(bit)。
 * 位的设置或清除取决于 value 参数，可以是 true(1) 也可以是 false(0)
 * 当 key 不存在时，自动生成一个新的字符串值。
 * 字符串会进行伸展(grown)以确保它可以将 value 保存在指定的偏移量上。当字符串值进行伸展时，空白位置以 0 填充。
 * offset 参数必须大于或等于 0 ，小于 2^32。
 * </p>
 *
 * <ul>
 * <li>时间复杂度: O(1)</li>
 * <li>Redis版本要求: >=2.2.0</li>
 * <li>返回值: 指定偏移量原来储存的位(1-true, 0-false)。</li>
 * </ul>
 *
 * @author zhangyancheng
 */
public class SetBit extends AutoCastRequest<Boolean> {

	public SetBit(String key, long offset, boolean value) {
		super(
				ByteUtil.toByteArray(key),
				ByteUtil.toByteArray(offset),
				ByteUtil.toByteArray(value)
		);
	}

	public SetBit(String key, long offset, String value) {
		super(
				ByteUtil.toByteArray(key),
				ByteUtil.toByteArray(offset),
				ByteUtil.toByteArray(value)
		);
	}
}
