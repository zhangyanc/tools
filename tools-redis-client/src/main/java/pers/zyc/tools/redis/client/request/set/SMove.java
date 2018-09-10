package pers.zyc.tools.redis.client.request.set;

import pers.zyc.tools.redis.client.request.AutoCastRequest;
import pers.zyc.tools.redis.client.util.ByteUtil;

/**
 * SMOVE source destination member
 * </p>
 *
 * 将 member 元素从 source 集合移动到 destination 集合。
 * SMOVE 是原子性操作。
 *
 * 如果 source 集合不存在或不包含指定的 member 元素，则 SMOVE 命令不执行任何操作，仅返回 0 。否则， member 元素从 source 集合中
 * 被移除，并添加到 destination 集合中去。
 *
 * 当 destination 集合已经包含 member 元素时， SMOVE 命令只是简单地将 source 集合中的 member 元素删除。
 * 当 source 或 destination 不是集合类型时，抛出异常。
 * </p>
 *
 * <ul>
 * <li>时间复杂度: O(1)</li>
 * <li>Redis版本要求: >=1.0.0</li>
 * <li>返回值: 如果 member 元素被成功移除，返回 true 。
 * 			  如果 member 元素不是 source 集合的成员，并且没有任何操作对 destination 集合执行，那么返回 false 。
 * </li>
 * </ul>
 *
 * @author zhangyancheng
 */
public class SMove extends AutoCastRequest<Boolean> {

	public SMove(String source, String destination, String member) {
		super(
				ByteUtil.toByteArray(source),
				ByteUtil.toByteArray(destination),
				ByteUtil.toByteArray(member)
		);
	}
}
