package pers.zyc.tools.network;

import pers.zyc.tools.utils.SystemMillis;
import pers.zyc.tools.utils.event.Multicaster;

/**
 * @author zhangyancheng
 */
class ResponsePromise implements ResponseFuture {

	/**
	 * 请求
	 */
	final Request request;

	/**
	 * 请求超时时间
	 */
	private final int requestTimeout;

	/**
	 * 响应最后期限
	 */
	final long deadline;

	/**
	 * 响应广播器
	 */
	private final Multicaster<ResponseFutureListener> multicaster;

	/**
	 * 请求结果（响应或者异常）
	 */
	private Object result;

	/**
	 * 是否完成
	 */
	private volatile boolean done;

	/**
	 * 是否发生了异常
	 */
	private boolean exceptional;

	ResponsePromise(Request request, int requestTimeout, Multicaster<ResponseFutureListener> multicaster) {
		this.request = request;
		this.requestTimeout = requestTimeout;
		this.deadline = requestTimeout + SystemMillis.current();
		this.multicaster = multicaster;
	}

	@Override
	public synchronized void addListener(final ResponseFutureListener listener) {
		if (!done) {
			multicaster.addListener(listener);
		} else {
			//已经完成则给当前监听器通知结果
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
	public boolean isDone() {
		return done;
	}

	@Override
	public Response get() throws InterruptedException {
		return get(requestTimeout);
	}

	@Override
	public Response get(long timeout) throws InterruptedException {
		if (timeout <= 0) {
			throw new IllegalArgumentException("Timeout " + timeout + " <= 0");
		}

		synchronized (this) {
			while (!done && timeout > 0) {
				long now = SystemMillis.current();
				wait(timeout);
				timeout -= SystemMillis.current() - now;
			}
		}

		if (timeout <= 0) {
			throw new NetworkException.TimeoutException();
		}

		if (result instanceof NetworkException) {
			throw (NetworkException) result;
		}
		return (Response) result;
	}

	/**
	 * 请求结束
	 *
	 * @param result 响应或者异常
	 */
	synchronized boolean response(Object result) {
		if (done) {
			//超时线程和响应线程存在并发
			return false;
		}
		done = true;

		exceptional = result instanceof Throwable;
		if (exceptional && !(result instanceof NetworkException)) {
			result = new NetworkException((Throwable) result);
		}
		this.result = result;

		//给所有添加的监听器广播结果
		if (multicaster.hasListeners()) {
			multicast(multicaster.listeners);
		}

		notifyAll();
		return true;
	}

	private void multicast(ResponseFutureListener listener) {
		if (exceptional) {
			listener.exceptionCaught(request, (NetworkException) result);
		} else {
			listener.responseReceived(request, (Response) result);
		}
	}
}
