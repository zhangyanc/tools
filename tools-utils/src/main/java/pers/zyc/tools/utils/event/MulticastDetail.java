package pers.zyc.tools.utils.event;

import java.lang.reflect.Method;
import java.util.Arrays;

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

	@Override
	public String toString() {
		return "MulticastDetail{" +
				"listener=" + listener +
				", method=" + method +
				", args=" + Arrays.toString(args) +
				'}';
	}
}
