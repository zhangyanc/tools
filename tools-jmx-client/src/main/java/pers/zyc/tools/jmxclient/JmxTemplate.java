package pers.zyc.tools.jmxclient;

/**
 * @author zhangyancheng
 */
public class JmxTemplate {

	/**
	 * 执行Jmx远程调用
	 *
	 * @param jmxHost jmx远程主机
	 * @param jmxUser jmx认证用户（无需认证则传入null）
	 * @param callback 回调
	 * @param <R> 返回值类型
	 * @return 返回值
	 * @throws JmxException 连接或者jmx调用异常
	 */
	public static <R> R execute(JmxHost jmxHost, JmxUser jmxUser, JmxCallback.ClientCallback<R> callback) {
		JmxClient jmxClient = new JmxClient(jmxHost, jmxUser);
		jmxClient.syncConnect();
		try {
			return callback.call(jmxClient);
		} finally {
			jmxClient.disconnect();
		}
	}

	/**
	 * 执行Jmx远程调用
	 *
	 * @param jmxHost jmx远程主机
	 * @param jmxUser jmx认证用户（无需认证则传入null）
	 * @param clazz 远程bean class
	 * @param objectName ObjectName
	 * @param callback 回调
	 * @param <R> 返回值类型
	 * @param <M> 远程bean类型
	 * @return 返回值
	 * @throws JmxException 连接或者jmx调用异常
	 */
	public static <R, M> R execute(JmxHost jmxHost, JmxUser jmxUser, Class<M> clazz, String objectName,
								   JmxCallback.MBeanCallback<M, R> callback) {
		JmxClient jmxClient = new JmxClient(jmxHost, jmxUser);
		jmxClient.syncConnect();
		try {
			return callback.call(jmxClient.getMBean(clazz, objectName));
		} finally {
			jmxClient.disconnect();
		}
	}
}
