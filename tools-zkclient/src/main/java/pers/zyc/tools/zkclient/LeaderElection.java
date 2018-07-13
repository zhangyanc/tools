package pers.zyc.tools.zkclient;


import pers.zyc.tools.utils.event.EventSource;

/**
 * 选举
 *
 * @author zhangyancheng
 */
public interface LeaderElection extends EventSource<ElectionEvent> {

	/**
	 * 加入选举
	 *
	 * <p>
	 *     如果已经启动了选举则忽略此次操作
	 *     成功启动后在elector获取主、失去主、主节点变更时广播事件
	 *
	 * @param elector 选举人
	 */
	void elect(Elector elector);

	/**
	 * @return 当前member节点名
	 */
	String member();

	/**
	 * @return leader节点名
	 */
	String leader();

	/**
	 * 重新选举(如果当前是主节点则发布LOST事件)
	 */
	void reelect();

	/**
	 * 退出(如果当前是主节点则发布LOST事件)
	 */
	void quit();
}