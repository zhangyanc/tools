package pers.zyc.tools.redis.client.util;

/**
 * @author zhangyancheng
 */
public interface Promise<R> extends Future<R> {
	void response(Object response);
}