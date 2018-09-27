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
			throw new NetworkException.TooMuchRequestException();
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
		response(new NetworkException.TimeoutException());
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
					multicast(listener);
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

	private Response cast() {
		if (response instanceof NetworkException) {
			throw (NetworkException) response;
		}
		return (Response) response;
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
			throw new NetworkException.TimeoutException();
		}
		return cast();
	}

	synchronized void response(Object resp) {
		if (response != null) {
			return;
		}

		exceptional = resp instanceof Throwable;
		if (exceptional && !(resp instanceof NetworkException)) {
			resp = new NetworkException((Throwable) resp);
		}
		response = resp;
		notifyAll();

		if (requestPermits != null) {
			requestPermits.release();
		}

		if (multicaster.hasListeners()) {
			multicast(multicaster.listeners);
		}
	}

	private void multicast(ResponseFutureListener listener) {
		if (exceptional) {
			listener.exceptionCaught(request, (NetworkException) response);
		} else {
			assert response instanceof Response;
			listener.responseReceived(request, (Response) response);
		}
	}
}
