package pers.zyc.tools.network;

import pers.zyc.tools.utils.TimeMillis;
import pers.zyc.tools.utils.event.Multicaster;

import java.util.HashSet;
import java.util.concurrent.Semaphore;

/**
 * @author zhangyancheng
 */
class ResponsePromise implements ResponseFuture {

	final Request request;
	private final int requestTimeout;
	private final Semaphore requestPermits;
	final Multicaster<ResponseFutureListener> multicaster;
	private final long createTime = TimeMillis.INSTANCE.get();

	private Object response;
	private boolean exceptional = false;

	ResponsePromise(int requestTimeout,
					Request request,
					Semaphore requestPermits) {
		this.requestTimeout = requestTimeout;
		this.request = request;

		if (requestPermits != null && !requestPermits.tryAcquire()) {
			throw new RequestException.TooMuchRequestException();
		}
		this.requestPermits = requestPermits;

		this.multicaster = new Multicaster<ResponseFutureListener>() {
			{
				//添加、删除、迭代集合都在锁内, 因此监听器集合可设置为非线程安全的HashSet实例
				setEventListeners(new HashSet<ResponseFutureListener>());
			}
		};
	}

	boolean isTimeout() {
		if (createTime + requestTimeout > TimeMillis.INSTANCE.get()) {
			return false;
		}
		response(new RequestException.TimeoutException());
		return true;
	}

	@Override
	public synchronized void addListener(final ResponseFutureListener listener) {
		if (!isDown()) {
			multicaster.addListener(listener);
		} else {
			multicaster.getMulticastExecutor().execute(new Runnable() {
				@Override
				public void run() {
					if (exceptional) {
						listener.exceptionCaught(request, (RequestException) response);
					} else {
						listener.responseReceived(request, (Response) response);
					}
				}
			});
		}
	}

	@Override
	public synchronized void removeListener(ResponseFutureListener listener) {
		multicaster.removeListener(listener);
	}

	@Override
	public synchronized boolean isDown() {
		return response != null;
	}

	@Override
	public Response get() throws InterruptedException {
		synchronized (this) {
			while (!isDown()) {
				wait();
			}
		}
		return cast();
	}

	private Response cast() {
		if (response instanceof RequestException) {
			throw (RequestException) response;
		}
		return (Response) response;
	}

	@Override
	public Response get(long timeout) throws InterruptedException {
		if (timeout < 0) {
			throw new IllegalArgumentException("Timeout " + timeout + " < 0");
		}

		synchronized (this) {
			while (!isDown() && timeout > 0) {
				long now = TimeMillis.INSTANCE.get();
				wait(timeout);
				timeout -= TimeMillis.INSTANCE.get() - now;
			}
		}

		if (timeout <= 0) {
			throw new RequestException.TimeoutException();
		}
		return cast();
	}

	synchronized void response(Object resp) {
		if (response != null) {
			return;
		}

		exceptional = resp instanceof Throwable;
		if (exceptional && !(resp instanceof RequestException)) {
			resp = new RequestException((Throwable) resp);
		}
		response = resp;

		if (requestPermits != null) {
			requestPermits.release();
		}

		if (multicaster.hasListeners()) {
			if (exceptional) {
				multicaster.listeners.exceptionCaught(request, (RequestException) response);
			} else {
				assert response instanceof Response;
				multicaster.listeners.responseReceived(request, (Response) response);
			}
		}
	}
}
