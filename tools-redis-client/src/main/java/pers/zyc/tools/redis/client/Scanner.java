package pers.zyc.tools.redis.client;

import pers.zyc.tools.redis.client.util.Future;

/**
 * @author zhangyancheng
 */
public interface Scanner<T> {

	Future<T> scan();

	Future<T> scan(String match);

	Future<T> scan(int count);

	Future<T> scan(String match, int count);
}
