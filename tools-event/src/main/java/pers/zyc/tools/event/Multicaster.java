package pers.zyc.tools.event;

import java.lang.reflect.*;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executor;

/**
 * 事件广播器
 * @param <L> 监听器接口泛型
 * @author zhangyancheng
 */
public abstract class Multicaster<L extends Listener> implements Listenable<L> {

	/**
	 * 同步执行器, 在multicaster调用线程中执行广播
	 */
	private static final Executor SYNC_EXECUTOR = new Executor() {
		@Override
		public void execute(Runnable command) {
			command.run();
		}
	};

	/**
	 * 泛型接口的代理实例, 在代理实例上调用事件, 则将此调用广播给所有已添加的监听器
	 *
	 * 定义为listeners在调用时会像作用在所有监听器上一样(事实上确实是)如: multicaster.listeners.onXxxEvent();
	 */
	public final L listeners;

	/**
	 * 读取子类设置的listener接口类型, 用于创建代理
	 */
	private final Class<L> listenerInterfaceClass;
	/**
	 * 广播异常处理器
	 */
	private MulticastExceptionHandler exceptionHandler;

	/**
	 * 广播执行器
	 */
	private Executor multicastExecutor = SYNC_EXECUTOR;
	/**
	 * 监听器set, 同一个监听器不会重复添加
	 */
	private Set<L> eventListeners = new CopyOnWriteArraySet<>();

	@SuppressWarnings("unchecked")
	protected Multicaster() {
		//子类继承后需声明listener接口类型, 如果为null或者泛型不是接口Multicaster无法工作, 抛出异常
		listenerInterfaceClass = Objects.requireNonNull((Class<L>) ((ParameterizedType)
				getClass().getGenericSuperclass()).getActualTypeArguments()[0]);
		if (!listenerInterfaceClass.isInterface()) {
			throw new IllegalArgumentException(listenerInterfaceClass + " is not Interface!");
		}
		this.listeners = createProxy();
	}

	@Override
	public void addListener(L listener) {
		eventListeners.add(Objects.requireNonNull(listener));
	}

	@Override
	public void removeListener(L listener) {
		eventListeners.remove(listener);
	}

	/**
	 * 清空已添加的所有监听器
	 */
	public void removeAllListeners() {
		eventListeners.clear();
	}

	/**
	 * 创建针对监听器接口的代理对象
	 *
	 * @return 代理对象
	 */
	protected L createProxy() {
		Object proxy = Proxy.newProxyInstance(listenerInterfaceClass.getClassLoader(),
				new Class[] { listenerInterfaceClass }, new MulticastInvocationHandler());
		return listenerInterfaceClass.cast(proxy);
	}

	protected class MulticastInvocationHandler implements InvocationHandler {

		@Override
		public Object invoke(Object proxy, final Method method, final Object[] args) throws Throwable {
			//回调所有监听器
			for (final Listener listener : eventListeners) {
				multicastExecutor.execute(new Runnable() {
					@Override
					public void run() {
						try {
							method.invoke(listener, args);
						} catch (Throwable throwable) {
							if (exceptionHandler != null) {
								if (throwable instanceof InvocationTargetException) {
									//保留反射调用的原始异常
									throwable = ((InvocationTargetException) throwable).getTargetException();
								}
								try {
									exceptionHandler.handleException(throwable,
											new MulticastDetail(listener, method, args));
								} catch (Throwable ignored) {
								}
							}
						}
					}
				});
			}
			//回调不支持返回值
			return null;
		}
	}

	public MulticastExceptionHandler getExceptionHandler() {
		return exceptionHandler;
	}

	/**
	 * 设置监听器回调异常处理器
	 * @param exceptionHandler 监听器回调异常处理器
	 */
	public void setExceptionHandler(MulticastExceptionHandler exceptionHandler) {
		this.exceptionHandler = exceptionHandler;
	}

	/**
	 * @return 广播执行器
	 */
	public Executor getMulticastExecutor() {
		return multicastExecutor;
	}

	/**
	 * 设置广播执行器, 如果为ExecutorService类型则广播具备并行能力
	 * @param multicastExecutor 广播执行器
	 * @throws NullPointerException multicastExecutor时抛出
	 * @see java.util.concurrent.ExecutorService
	 */
	public void setMulticastExecutor(Executor multicastExecutor) {
		this.multicastExecutor = Objects.requireNonNull(multicastExecutor);
	}
}
