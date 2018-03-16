package pers.zyc.tools.event;

import java.lang.reflect.Method;

/**
 * @author zhangyancheng
 */
public class MulticastDetail {
	public final Listener listener;
	public final Method method;
	public final Object[] args;

	MulticastDetail(Listener listener, Method method, Object[] args) {
		this.listener = listener;
		this.method = method;
		this.args = args;
	}
}
