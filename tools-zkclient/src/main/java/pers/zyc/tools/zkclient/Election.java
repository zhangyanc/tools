package pers.zyc.tools.zkclient;


/**
 * 选举
 *
 * @author zhangyancheng
 */
public interface Election extends Elector {

	/**
	 * 重新选举(如果当前是主节点则发布LOST事件)
	 */
	void reelect();

	/**
	 * 退出(如果当前是主节点则发布LOST事件)
	 */
	void quit();

	enum EventType {

		/**
		 * 获取主
		 */
		LEADER_TOOK,

		/**
		 * 失去主
		 */
		LEADER_LOST,

		/**
		 * 主发生变更(变更非当前elector)
		 */
		LEADER_CHANGED
	}
}