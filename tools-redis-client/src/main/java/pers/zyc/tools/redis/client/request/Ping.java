package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Request;

/**
 * PING
 * </p>
 *
 * 使用客户端向 Redis 服务器发送一个 PING ，如果服务器运作正常的话，会返回一个 PONG 。
 * 通常用于测试与服务器的连接是否仍然生效，或者用于测量延迟值。
 * </p>
 *
 * <ul>
 * <li>时间复杂度: O(1)</li>
 * <li>Redis版本要求: >=1.0.0</li>
 * <li>返回值: 如果连接正常就返回一个 PONG ，否则返回一个异常。</li>
 * </ul>
 *
 * @author zhangyancheng
 */
public class Ping extends Request<String> {
}
