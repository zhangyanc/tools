package pers.zyc.tools.network;

import pers.zyc.tools.utils.TimeMillis;
import pers.zyc.tools.utils.event.Multicaster;

import java.util.HashSet;
import java.util.concurrent.Semaphore;

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
	private final long deadline;

	/**
	 * 请求许可
	 */
	private final Semaphore requestPermits;


	/**
	 * 响应广播器
	 */
	final Multicaster<ResponseFutureListener> multicaster;

	/**
	 * 请求结果（响应或者异常）
	 */
	private Object result;

	/**
	 * 是否发生了异常
	 */
	private boolean exceptional = false;

	ResponsePromise(int requestTimeout,
					Request request,
					Semaphore requestPermits) {
		this.requestTimeout = requestTimeout;
		this.deadline = requestTimeout + TimeMillis.INSTANCE.get();
		this.request = request;

		//如果设置了请求许可则必需获取许可才能发送请求（构造当前对象）
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

	/**
	 * 检查请求是否超时
	 *
	 * @return 是否超时
	 */
	boolean isTimeout() {
		if (deadline > TimeMillis.INSTANCE.get()) {
			return false;
		}
		//超期，响应超时异常
		response(new NetworkException.TimeoutException());
		return true;
	}

	@Override
	public synchronized void addListener(final ResponseFutureListener listener) {
		if (!isDown()) {
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
	public synchronized boolean isDown() {
		return result != null;
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
			while (!isDown() && timeout > 0) {
				long now = TimeMillis.INSTANCE.get();
				wait(timeout);
				timeout -= TimeMillis.INSTANCE.get() - now;
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
	synchronized void response(Object result) {
		if (this.result != null) {
			//超时线程和响应线程存在并发
			return;
		}

		exceptional = result instanceof Throwable;
		if (exceptional && !(result instanceof NetworkException)) {
			result = new NetworkException((Throwable) result);
		}
		this.result = result;
		notifyAll();

		//获取了许可，请求结束了必须释放掉
		if (requestPermits != null) {
			requestPermits.release();
		}

		//给所有添加的监听器广播结果
		if (multicaster.hasListeners()) {
			multicast(multicaster.listeners);
		}
	}

	private void multicast(ResponseFutureListener listener) {
		if (exceptional) {
			listener.exceptionCaught(request, (NetworkException) result);
		} else {
			assert result instanceof Response;
			listener.responseReceived(request, (Response) result);
		}
	}
}
