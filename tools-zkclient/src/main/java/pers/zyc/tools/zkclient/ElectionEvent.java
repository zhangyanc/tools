package pers.zyc.tools.zkclient;

/**
 * @author zhangyancheng
 */
public enum ElectionEvent {

	/**
	 * 获取主
	 */
	TAKE,

	/**
	 * 失去主
	 */
	LOST,

	/**
	 * 主发生变更(变更非当前elector)
	 */
	LEADER_CHANGED
}
