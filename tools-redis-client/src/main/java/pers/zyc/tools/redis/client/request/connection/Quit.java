package pers.zyc.tools.redis.client.request.connection;

import pers.zyc.tools.redis.client.Request;

/**
 * QUIT
 * </p>
 *
 * 请求服务器关闭与当前客户端的连接。
 * 一旦所有等待中的回复(如果有的话)顺利写入到客户端，连接就会被关闭。
 * </p>
 *
 * <ul>
 * <li>时间复杂度: O(1)</li>
 * <li>Redis版本要求: >=1.0.0</li>
 * <li>返回值: 无, 除非异常</li>
 * </ul>
 *
 * @author zhangyancheng
 */
public class Quit extends Request<Void> {
}
