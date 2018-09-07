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

	protected Object response;

	public ResponsePromise(ResponseCast<R> responseCast) {
		super(1);
		this.responseCast = responseCast;
	}

	public long getCreateTime() {
		return createTime;
	}

	@Override
	@SuppressWarnings("unchecked")
	public R get() {
		try {
			await();

			if (response instanceof RedisClientException) {
				throw (RedisClientException) response;
			}

			return (R) response;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RedisClientException("Thread Interrupted");
		}
	}

	private void exception(Object response) {
		if (response instanceof RedisClientException) {
			this.response = response;
		} else {
			this.response = new RedisClientException((Throwable) response);
		}
	}

	@Override
	public void response(Object response) {
		if (response instanceof Throwable) {
			exception(response);
		} else {
			try {
				this.response = responseCast.cast(response);
			} catch (Throwable caseException) {
				exception(caseException);
			}
		}
		onRespond();
		countDown();
	}

	protected void onRespond() {
	}
}
