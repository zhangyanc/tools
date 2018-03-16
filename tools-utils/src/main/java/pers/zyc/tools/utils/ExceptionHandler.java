package pers.zyc.tools.utils;

/**
 * 异常处理器
 * @author zhangyancheng
 */
public interface ExceptionHandler<E extends Throwable, D, R> {
	/**
	 * @param cause 异常
	 * @param dto Data Transfer Object
	 * @return 处理结果
	 */
	R handleException(E cause, D dto);
}
