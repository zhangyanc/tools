package pers.zyc.tools.redis.client.request.key;

import pers.zyc.tools.redis.client.request.AutoCastRequest;

/**
 * RANDOMKEY
 * </p>
 *
 * <ul>
 * <li>时间复杂度: O(1)</li>
 * <li>Redis版本要求: >=1.0.0</li>
 * <li>返回值: 当数据库不为空时，返回一个 key 。当数据库为空时，返回 null 。</li>
 * </ul>
 *
 * @author zhangyancheng
 */
public class RandomKey extends AutoCastRequest<String> {
}
