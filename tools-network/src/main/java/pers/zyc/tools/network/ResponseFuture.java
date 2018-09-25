package pers.zyc.tools.network;

import pers.zyc.tools.utils.event.Listenable;

/**
 * @author zhangyancheng
 */
public interface ResponseFuture extends Listenable<ResponseFutureListener> {

	/**
	 * @return 响应是否已返回
	 */
	boolean isDown();

	/**
	 * 获取响应，如果未结束将会阻塞等待直到响应返回或者超时
	 *
	 * 超时时间为请求发送时设置的超时时间
	 *
	 * @return 响应
	 * @throws InterruptedException 阻塞过程中线程被中断
	 * @throws RequestException.TimeoutException 超时
	 * @throws RequestException 其他异常都将被包装为此RE返回
	 */
	Response get() throws InterruptedException;

	/**
	 * 获取响应，如果未结束将会阻塞等待直到响应返回或者超时
	 *
	 * 传入的超时时间只有比请求发送时设置的超时时间小才有意义
	 *
	 * @param timeout 等待时间（ms）
	 * @return 响应
	 * @throws InterruptedException 阻塞过程中线程被中断
	 * @throws RequestException.TimeoutException 超时
	 * @throws RequestException 其他异常都将被包装为此RE返回
	 */
	Response get(long timeout) throws InterruptedException;
}
