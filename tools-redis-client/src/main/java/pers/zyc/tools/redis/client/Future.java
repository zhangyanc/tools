package pers.zyc.tools.redis.client;

/**
 * @author zhangyancheng
 */
public interface Future<R> {
	R get();
}
