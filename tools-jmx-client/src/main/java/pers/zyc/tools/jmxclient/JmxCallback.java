package pers.zyc.tools.jmxclient;

/**
 * @author zhangyancheng
 */
public interface JmxCallback<T, R> {

	R call(T t);

	interface ClientCallback<R> extends JmxCallback<JmxClient, R> {}

	interface MBeanCallback<M, R> extends JmxCallback<M, R> {}
}
