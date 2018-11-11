package pers.zyc.tools.redis.client.request.string;

import pers.zyc.tools.redis.client.request.AutoCastRequest;

/**
 * MSETNX key value [key value ...]
 * </p>
 *
 * 同时设置一个或多个 key-value 对，当且仅当所有给定 key 都不存在。
 * 即使只有一个给定 key 已存在， MSETNX 也会拒绝执行所有给定 key 的设置操作。
 *
 * MSETNX 是原子性的，因此它可以用作设置多个不同 key 表示不同字段(field)的唯一性逻辑对象(unique logic object)，所有字段要么全
 * 被设置，要么全不被设置。
 * </p>
 *
 * <ul>
 * <li>时间复杂度: O(N) , N 为要设置的 key 数量。</li>
 * <li>Redis版本要求: >=1.0.1</li>
 * <li>返回值: 当所有 key 都成功设置，返回 true 。
 * 			  如果所有给定 key 都设置失败(至少有一个 key 已经存在)，那么返回 false 。
 * </li>
 * </ul>
 *
 * @author zhangyancheng
 */
public class MSetNx extends AutoCastRequest<Boolean> {
}
