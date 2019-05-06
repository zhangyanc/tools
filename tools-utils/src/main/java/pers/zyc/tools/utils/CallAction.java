package pers.zyc.tools.utils;

/**
 * @author zhangyancheng
 */
public interface CallAction<R, E extends Exception> {

	R call() throws E;
}
