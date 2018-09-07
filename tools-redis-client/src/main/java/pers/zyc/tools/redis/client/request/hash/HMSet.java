package pers.zyc.tools.redis.client.request.hash;

import pers.zyc.tools.redis.client.Request;
import pers.zyc.tools.redis.client.util.ByteUtil;

import java.util.Map;

/**
 * HMSET key field value [field value ...]
 * </p>
 *
 * 同时将多个 field-value (域-值)对设置到哈希表 key 中。
 * 此命令会覆盖哈希表中已存在的域。
 * 如果 key 不存在，一个空哈希表被创建并执行 HMSET 操作。
 * </p>
 *
 * <ul>
 * <li>时间复杂度: O(N)，  N 为 field-value 对的数量。</li>
 * <li>Redis版本要求: >=2.0.0</li>
 * <li>返回值: 当 key 不是哈希表(hash)类型时，抛出异常。</li>
 * </ul>
 *
 * @author zhangyancheng
 */
public class HMSet extends Request<Void> {

	public HMSet(String key, Map<String, String> hash) {
		bulks.add(ByteUtil.toByteArray(key));
		for (Map.Entry<String, String> entry : hash.entrySet()) {
			bulks.add(ByteUtil.toByteArray(entry.getKey()));
			bulks.add(ByteUtil.toByteArray(entry.getValue()));
		}
	}
}
