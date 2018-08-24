package pers.zyc.tools.redis.client.util;

import pers.zyc.tools.redis.client.ResponseCast;
import pers.zyc.tools.redis.client.exception.RedisClientException;
import pers.zyc.tools.utils.TimeMillis;

import java.util.concurrent.CountDownLatch;

/**
 * @author zhangyancheng
 */
public class ResponsePromise<R> extends CountDownLatch implements Promise<R> {

	private final ResponseCast<R> responseCast;
	private final long createTime = TimeMillis.INSTANCE.get();

	private Object response;

	public ResponsePromise(ResponseCast<R> responseCast) {
		super(1);
		this.responseCast = responseCast;
	}

	public long getCreateTime() {
		return createTime;
	}

	@Override
	public R get() {
		try {
			await();

			if (response instanceof Throwable) {
				if (response instanceof RedisClientException) {
					throw (RedisClientException) response;
				}
				throw new RedisClientException((Throwable) response);
			}

			return responseCast.cast(response);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RedisClientException("Thread Interrupted");
		}
	}

	@Override
	public void response(Object response) {
		this.response = response;
		countDown();
	}
}
