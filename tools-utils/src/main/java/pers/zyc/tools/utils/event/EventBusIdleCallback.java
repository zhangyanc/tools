package pers.zyc.tools.utils.event;

/**
 * @author zhangyancheng
 */
public interface EventBusIdleCallback<E> {

	/**
	 * @return 空闲时间（ms）
	 */
	long getIdleTimeMillis();

	/**
	 * 空闲回调
	 *
	 * @param eventBus 事件总线
	 */
	void onIdle(EventBus<E> eventBus);
}
