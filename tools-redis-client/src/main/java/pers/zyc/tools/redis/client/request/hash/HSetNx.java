package pers.zyc.tools.redis.client.request.hash;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * HSETNX key field value
 * </p>
 *
 * 将哈希表 key 中的域 field 的值设置为 value ，当且仅当域 field 不存在。
 * 若域 field 已经存在，该操作无效。
 * 如果 key 不存在，一个新哈希表被创建并执行 HSETNX 命令。
 * </p>
 *
 * <ul>
 * <li>时间复杂度: O(1)</li>
 * <li>Redis版本要求: >=2.0.0</li>
 * <li>返回值: 设置成功，返回 true。
 *			 如果给定域已经存在且没有操作被执行，返回 false 。
 * </li>
 * </ul>
 *
 * @author zhangyancheng
 */
public class HSetNx extends Request<Boolean> {

	public HSetNx(String key, String field, String value) {
		super(
				ByteUtil.toByteArray(key),
				ByteUtil.toByteArray(field),
				ByteUtil.toByteArray(value)
		);
	}
}
