package pers.zyc.tools.redis.client.request.key;

import pers.zyc.tools.redis.client.request.AutoCastRequest;
import pers.zyc.tools.redis.client.util.ByteUtil;

import java.util.Set;

/**
 * KEYS pattern
 * </p>
 *
 * 查找所有符合给定模式 pattern 的 key 。
 *
 * KEYS * 匹配数据库中所有 key 。
 * KEYS h?llo 匹配 hello ， hallo 和 hxllo 等。
 * KEYS h*llo 匹配 hllo 和 heeeeello 等。
 * KEYS h[ae]llo 匹配 hello 和 hallo ，但不匹配 hillo 。
 * 特殊符号用 \ 隔开
 * </p>
 *
 * <ul>
 * <li>时间复杂度: O(N)， N 为数据库中 key 的数量。</li>
 * <li>Redis版本要求: >=1.0.0</li>
 * <li>返回值: 符合给定模式的 key 集合。</li>
 * </ul>
 *
 * @author zhangyancheng
 */
public class Keys extends AutoCastRequest<Set<String>> {

	public Keys(String pattern) {
		super(
				ByteUtil.toByteArray(pattern)
		);
	}
}
