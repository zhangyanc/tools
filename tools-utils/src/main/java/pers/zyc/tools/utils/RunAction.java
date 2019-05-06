package pers.zyc.tools.utils;

/**
 * @author zhangyancheng
 */
public interface RunAction<E extends Exception> {

	void run() throws E;
}
