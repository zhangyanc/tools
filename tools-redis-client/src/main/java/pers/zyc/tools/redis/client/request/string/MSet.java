package pers.zyc.tools.redis.client.request.string;

import pers.zyc.tools.redis.client.request.AutoCastRequest;
import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * MSET key value [key value ...]
 * </p>
 *
 * 同时设置一个或多个 key-value 对。
 * 如果某个给定 key 已经存在，那么 MSET 会用新值覆盖原来的旧值，如果这不是你所希望的效果，请考虑使用 {@link MSetNx} 命令：它只会在
 * 所有给定 key 都不存在的情况下进行设置操作。
 *
 * MSET 是一个原子性(atomic)操作，所有给定 key 都会在同一时间内被设置，某些给定 key 被更新而另一些给定 key 没有改变的情况，不
 * 可能发生。
 *
 * <ul>
 * <li>时间复杂度: O(N) , N 为要设置的 key 数量。</li>
 * <li>Redis版本要求: >=1.0.1</li>
 * <li>返回值: 无（因为 MSET 不可能失败）</li>
 * </ul>
 *
 * @author zhangyancheng
 */
public class MSet extends AutoCastRequest<Void> {

	public MSet(String... kvs) {
		for (int i = 0; i < kvs.length - 1; i += 2) {
			bulks.add(ByteUtil.toByteArray(kvs[i]));
			bulks.add(ByteUtil.toByteArray(kvs[i + 1]));
		}
	}
}
