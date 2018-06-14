package pers.zyc.tools.redis.client;

/**
 * @author zhangyancheng
 */
public interface ResponseFuture<R> {
	R get();
}
