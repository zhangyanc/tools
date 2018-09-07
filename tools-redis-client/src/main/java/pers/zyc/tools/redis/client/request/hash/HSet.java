package pers.zyc.tools.redis.client.request.hash;

import pers.zyc.tools.redis.client.request.AutoCastRequest;
import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * HSET key field value
 * </p>
 *
 * 将哈希表 key 中的域 field 的值设为 value 。
 * 如果 key 不存在，一个新的哈希表被创建并进行 HSET 操作。
 * 如果域 field 已经存在于哈希表中，旧值将被覆盖。
 * </p>
 *
 * <ul>
 * <li>时间复杂度: O(1)</li>
 * <li>Redis版本要求: >=2.0.0</li>
 * <li>返回值: 如果 field 是哈希表中的一个新建域，并且值设置成功，返回 true 。
 *			  如果哈希表中域 field 已经存在且旧值已被新值覆盖，返回 false 。
 * </li>
 * </ul>
 *
 * @author zhangyancheng
 */
public class HSet extends AutoCastRequest<Boolean> {

	public HSet(String key, String field, String value) {
		super(
				ByteUtil.toByteArray(key),
				ByteUtil.toByteArray(field),
				ByteUtil.toByteArray(value)
		);
	}
}
