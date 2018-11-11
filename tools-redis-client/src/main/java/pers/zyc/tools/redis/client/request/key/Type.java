package pers.zyc.tools.redis.client.request.key;

import pers.zyc.tools.redis.client.ResponseCast;
import pers.zyc.tools.redis.client.exception.RedisClientException;
import pers.zyc.tools.redis.client.request.AutoCastRequest;
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
public class Type extends AutoCastRequest<String> {

	public Type(String key) {
		super(
				TYPE_CAST,
				ByteUtil.toByteArray(key)
		);
	}

	private static final ResponseCast<String> TYPE_CAST = new ResponseCast<String>() {

		@Override
		public String cast(Object response) {
			String type = (String) response;
			switch (type) {
				case "none":
				case "string":
				case "list":
				case "set":
				case "zset":
				case "hash":
					return type;
				default:
					throw new RedisClientException("UnExcepted type: " + type);
			}
		}
	};
}
