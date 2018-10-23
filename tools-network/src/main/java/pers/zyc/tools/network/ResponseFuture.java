package pers.zyc.tools.network;

import pers.zyc.tools.utils.event.Listenable;

/**
 * 异步请求，响应Future
 *
 * @author zhangyancheng
 */
public interface ResponseFuture extends Listenable<ResponseFutureListener> {

	/**
	 * 是否已完成（异常或者响应返回）
	 *
	 * @return 是否完成
	 */
	boolean isDone();

	/**
	 * 获取响应，如果未完成将会阻塞直到完成或者超时
	 *
	 * 超时时间为请求发送时设置的超时时间
	 *
	 * @return 响应
	 * @throws InterruptedException 阻塞过程中线程被中断
	 * @throws NetworkException.TimeoutException 超时
	 * @throws NetworkException 其他异常都将被包装为此RE返回
	 */
	Response get() throws InterruptedException;

	/**
	 * 获取响应，如果未完成将会阻塞直到完成或者超时
	 *
	 * 传入的超时时间只有比请求发送时设置的超时时间小才有意义
	 *
	 * @param timeout 等待时间（ms）
	 * @return 响应
	 * @throws InterruptedException 阻塞过程中线程被中断
	 * @throws NetworkException.TimeoutException 超时
	 * @throws NetworkException 其他异常都将被包装为此RE返回
	 */
	Response get(long timeout) throws InterruptedException;
}
