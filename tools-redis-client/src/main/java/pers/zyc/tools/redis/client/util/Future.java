package pers.zyc.tools.redis.client.util;

/**
 * @author zhangyancheng
 */
public interface Future<R> {
	R get();
}
