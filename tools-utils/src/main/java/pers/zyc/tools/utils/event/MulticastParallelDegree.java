package pers.zyc.tools.utils.event;

/**
 * 广播并行度
 *
 * @author zhangyancheng
 */
public enum MulticastParallelDegree {
	/**
	 * 按事件并行，同一个事件的多次回调（对不同的监听器）在同一个线程按顺序调用
	 */
	BY_EVENT,

	/**
	 * 按调用并行，同一个事件的多次回调在不同线程
	 */
	BY_INVOKE
}
