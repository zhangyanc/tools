package pers.zyc.tools.redis.client.request.hash;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * HDEL key field [field ...]
 * </p>
 *
 * 删除哈希表 key 中的一个或多个指定域，不存在的域将被忽略。
 *
 * <ul>
 * <li>时间复杂度: O(N), N 为要删除的域的数量。</li>
 * <li>Redis版本要求: >=2.0.0</li>
 * <li>返回值: 被成功移除的域的数量，不包括被忽略的域。</li>
 * </ul>
 *
 * @author zhangyancheng
 */
public class HDelete extends Request<Long> {

	public HDelete(String key, String... fields) {
		bulks.add(ByteUtil.toByteArray(key));
		for (String field : fields) {
			bulks.add(ByteUtil.toByteArray(field));
		}
	}

	@Override
	protected String getCommand() {
		return "HDEL";
	}
}
