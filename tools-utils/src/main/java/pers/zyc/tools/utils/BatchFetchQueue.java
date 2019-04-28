package pers.zyc.tools.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author zhangyancheng
 */
public class BatchFetchQueue<E> {

	/**
	 * 缓存数组
	 */
	private final Object[] elements;

	/**
	 * 非公平锁，适合多生产者单消费者场景
	 */
	private final Lock bufferLock = new ReentrantLock();

	/**
	 * 非满条件变量
	 */
	private final Condition notFull = bufferLock.newCondition();

	/**
	 * 非空条件变量
	 */
	private final Condition notEmpty = bufferLock.newCondition();

	/**
	 * 索引和缓存余量
	 */
	private int putIndex, takeIndex, remain;

	/**
	 * @param capacity 容量
	 */
	public BatchFetchQueue(int capacity) {
		elements = new Object[capacity];
	}

	private boolean isFull() {
		return remain == elements.length;
	}

	private void in(Object element) {
		remain++;
		elements[putIndex++] = element;
		if (putIndex == elements.length) {
			putIndex = 0;
		}
		notEmpty.signal();
	}

	@SuppressWarnings("unchecked")
	private List<E> out(int size) {
		remain -= size;
		ArrayList<E> result = new ArrayList<>(size);
		while (size-- > 0) {
			result.add((E) elements[takeIndex++]);
			if (takeIndex == elements.length) {
				takeIndex = 0;
			}
		}
		notFull.signalAll();
		return result;
	}

	/**
	 * 是否为空
	 *
	 * @return 如果队列为空返回true，非空返回false
	 */
	public boolean isEmpty() {
		bufferLock.lock();
		try {
			return remain == 0;
		} finally {
			bufferLock.unlock();
		}
	}

	/**
	 * 元素放入队列，如果队列满了则等待直到成功放入
	 *
	 * @param element 元素
	 * @throws InterruptedException 线程被中断
	 */
	public void put(E element) throws InterruptedException {
		bufferLock.lock();
		try {
			while (isFull()) {
				notFull.await();
			}
			in(element);
		} finally {
			bufferLock.unlock();
		}
	}

	/**
	 * 元素放入队列，如果队列满了返回false
	 *
	 * @param element 元素
	 * @return 成功放入时true，否则false
	 */
	public boolean add(E element) {
		bufferLock.lock();
		try {
			if (isFull()) {
				return false;
			}
			in(element);
			return true;
		} finally {
			bufferLock.unlock();
		}
	}

	/**
	 * 元素放入队列，如果队列满了则等待直到成功放入或者超时
	 *
	 * @param element 元素
	 * @param timeout 超时时间
	 * @param timeUnit 超时时间单位
	 * @return 超时前成功放入返回true，否则返回false
	 * @throws InterruptedException 等待队列非满时线程被中断
	 */
	public boolean add(E element, long timeout, TimeUnit timeUnit) throws InterruptedException {
		long timeoutNanos = timeUnit.toNanos(timeout);
		bufferLock.lock();
		try {
			while (isFull()) {
				if (timeoutNanos <= 0) {
					return false;
				}
				timeoutNanos = notFull.awaitNanos(timeoutNanos);
			}
			in(element);
			return true;
		} finally {
			bufferLock.unlock();
		}
	}

	/**
	 * 取出队列当前全部元素
	 *
	 * @return 元素组
	 */
	public List<E> fetchAll() {
		bufferLock.lock();
		try {
			return out(remain);
		} finally {
			bufferLock.unlock();
		}
	}

	/**
	 * 取出元素，不少于指定条数
	 *
	 * @param count 指定的最少取出条数
	 * @return 元素组
	 * @throws InterruptedException 等待数据时线程被中断
	 */
	public List<E> fetchLeast(int count) throws InterruptedException {
		if (count < 1 || count > elements.length) {
			throw new IllegalArgumentException("At least 'count' must between 1 and " + elements.length);
		}
		bufferLock.lock();
		try {
			while (remain < count) {
				notEmpty.await();
			}
			return out(remain);
		} finally {
			bufferLock.unlock();
		}
	}

	/**
	 * 取出元素，不少于指定条数。超时未够最少条数将抛出异常
	 *
	 * @param count 指定的最少取出条数
	 * @param timeout 超时时间
	 * @param timeUnit 超时时间单位
	 * @return 元素组
	 * @throws InterruptedException 等待数据时线程被中断
	 * @throws TimeoutException 超时未够最少条数
	 */
	public List<E> fetchLeast(int count, long timeout, TimeUnit timeUnit)
			throws InterruptedException, TimeoutException {
		if (count < 1 || count > elements.length) {
			throw new IllegalArgumentException("At least 'count' must between 1 and " + elements.length);
		}
		long timeoutNanos = timeUnit.toNanos(timeout);
		bufferLock.lock();
		try {
			while (remain < count) {
				if (timeoutNanos <= 0) {
					throw new TimeoutException("Only remain " + remain);
				}
				timeoutNanos = notEmpty.awaitNanos(timeoutNanos);
			}
			return out(remain);
		} finally {
			bufferLock.unlock();
		}
	}

	/**
	 * 取出元素，尝试不少于指定条数。如果超时仍未够指定条数返回剩余条数
	 *
	 * @param count 指定条数
	 * @param timeout 超时时间
	 * @param timeUnit 超时时间单位
	 * @return 元素组
	 * @throws InterruptedException 等待数据时线程被中断
	 */
	public List<E> tryFetchLeast(int count, long timeout, TimeUnit timeUnit) throws InterruptedException {
		if (count < 1 || count > elements.length) {
			throw new IllegalArgumentException("At least 'count' must between 1 and " + elements.length);
		}
		long timeoutNanos = timeUnit.toNanos(timeout);
		bufferLock.lock();
		try {
			while (remain < count && timeoutNanos > 0) {
				timeoutNanos = notEmpty.awaitNanos(timeoutNanos);
			}
			return out(remain);
		} finally {
			bufferLock.unlock();
		}
	}

	/**
	 * 取出元素，不多余指定条数。返回后至少包含一条数据
	 *
	 * @param count 指定的最多取出条数
	 * @return 元素组
	 * @throws InterruptedException 等待数据时线程被中断
	 */
	public List<E> fetchMost(int count) throws InterruptedException {
		if (count < 1) {
			throw new IllegalArgumentException("At least 'count' must > 0");
		}
		bufferLock.lock();
		try {
			while (remain == 0) {
				notEmpty.await();
			}
			return out(Math.min(remain, count));
		} finally {
			bufferLock.unlock();
		}
	}

	/**
	 * 取出元素，不多余指定条数。返回元素组可能为空
	 *
	 * @param count 指定的最多取出条数
	 * @param timeout 超时时间
	 * @param timeUnit 超时时间单位
	 * @return 元素组
	 * @throws InterruptedException 等待数据时线程被中断
	 */
	public List<E> fetchMost(int count, int timeout, TimeUnit timeUnit) throws InterruptedException {
		if (count < 1) {
			throw new IllegalArgumentException("At least 'count' must > 0");
		}
		long timeoutNanos = timeUnit.toNanos(timeout);
		bufferLock.lock();
		try {
			while (remain == 0 && timeoutNanos > 0) {
				timeoutNanos = notEmpty.awaitNanos(timeoutNanos);
			}
			return out(Math.min(remain, count));
		} finally {
			bufferLock.unlock();
		}
	}

	/**
	 * 取出元素，不多余指定条数。返回元素组可能为空
	 *
	 * @param count 指定最多取出条数
	 * @param timeout 超时时间
	 * @param timeUnit 超时时间单位
	 * @return 元素组
	 * @throws InterruptedException 等待数据时线程被中断
	 */
	public List<E> tryFetchMost(int count, int timeout, TimeUnit timeUnit) throws InterruptedException {
		if (count < 1) {
			throw new IllegalArgumentException("At least 'count' must > 0");
		}
		long timeoutNanos = timeUnit.toNanos(timeout);
		bufferLock.lock();
		try {
			while (remain < count && timeoutNanos > 0) {
				timeoutNanos = notEmpty.awaitNanos(timeoutNanos);
			}
			return out(Math.min(remain, count));
		} finally {
			bufferLock.unlock();
		}
	}
}
