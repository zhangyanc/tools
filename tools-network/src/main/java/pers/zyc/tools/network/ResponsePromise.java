package pers.zyc.tools.network;

import pers.zyc.tools.utils.TimeMillis;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author zhangyancheng
 */
class ResponsePromise extends CountDownLatch implements ResponseFuture {

	private final int requestTimeout;
	private final long createTime = TimeMillis.INSTANCE.get();
	private AtomicReference<Object> responseReference = new AtomicReference<>();

	ResponsePromise(int requestTimeout) {
		super(1);
		this.requestTimeout = requestTimeout;
	}

	boolean isTimeout() {
		if (createTime + requestTimeout > TimeMillis.INSTANCE.get()) {
			return false;
		}
		response(new RequestException.TimeoutException());
		return true;
	}

	@Override
	public boolean isDown() {
		return false;
	}

	@Override
	public Response get() throws InterruptedException {
		if (!await(requestTimeout, TimeUnit.MILLISECONDS)) {
			throw new RequestException.TimeoutException();
		}

		Object response = responseReference.get();
		if (response instanceof Response) {
			return (Response) response;
		} else if (response instanceof RequestException) {
			throw (RequestException) response;
		} else if (response instanceof Throwable) {
			throw new RequestException((Throwable) response);
		}
		throw new Error("UnExcepted response: " + response);
	}

	void response(Object response) {
		if (responseReference.compareAndSet(null, response)) {
			countDown();
		}
	}
}
