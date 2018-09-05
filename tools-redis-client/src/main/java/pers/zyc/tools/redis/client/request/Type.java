package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * TYPE key
 * </p>
 *
 * 返回 key 所储存的值的类型。
 * </p>
 *
 * <ul>
 * <li>时间复杂度: O(1)</li>
 * <li>Redis版本要求: >=1.0.0</li>
 * <li>返回值: none (key不存在)
 * 			  string (字符串)
 * 			  list (列表)
 * 			  set (集合)
 * 			  zset (有序集)
 * 			  hash (哈希表)
 * </li>
 * </ul>
 *
 * @author zhangyancheng
 */
public class Type extends Request<KeyType> {

	public Type(String key) {
		super(
				ByteUtil.toByteArray(key)
		);
	}

	@Override
	public KeyType cast(Object response) {
		return KeyType.valueOf((String) response);
	}
}
