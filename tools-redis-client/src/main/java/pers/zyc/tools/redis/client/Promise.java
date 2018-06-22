package pers.zyc.tools.redis.client;

/**
 * @author zhangyancheng
 */
abstract class Promise<R> implements Future<R> {

	abstract void response(Object response);
}