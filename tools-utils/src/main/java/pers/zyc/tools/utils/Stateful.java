package pers.zyc.tools.utils;

/**
 * @author zhangyancheng
 */
public interface Stateful<S> {
	/**
	 * @return 当前状态
	 */
	S getState();

	/**
	 * 检查当前是否为给定的状态
	 * 等同于：getState().equals(state);
	 */
	boolean checkState(S state);
}
