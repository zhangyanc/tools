package pers.zyc.tools.utils.retry;


import pers.zyc.tools.utils.ExceptionHandler;

import java.util.concurrent.Callable;

/**
 * 重试任务执行异常处理器
 *
 * @author zhangyancheng
 */
public interface RetryExceptionHandler extends ExceptionHandler<Throwable, Callable<?>, Boolean> {

	/**
	 * 处理执行异常, 并返回是否继续重试
	 *
	 * @param cause 重试任务执行异常
	 * @param callable 重试任务
	 * @return 是否继续重试
	 */
	Boolean handleException(Throwable cause, Callable<?> callable);
}
