package pers.zyc.tools.zkclient;

import pers.zyc.tools.event.EventSource;

/**
 * 选举
 *
 * @author zhangyancheng
 */
public interface LeaderElection extends EventSource<ElectionEvent> {

	/**
	 * 启动选举
	 *
	 * <p>
	 *     如果已经启动了选举则忽略此次操作
	 *     成功启动后在elector获取主、失去主、主节点变更时广播事件
	 *
	 * @param electionPath 选举节点
	 *                     1. 节点必须符合zookeeper path格式,且不能是临时节点
	 *                     2. 选举节点必须存在, 且不能被删除, 否则选举将发生错误
	 * @param elector 选举人
	 */
	void elect(String electionPath, Elector elector);

	/**
	 * 退出选举(如果是主则会release)
	 */
	void quit();

	/**
	 * @return 当前member节点名
	 */
	String member();

	/**
	 * @return leader节点名
	 */
	String leader();

	/**
	 * 放开主角色
	 *
	 * <p>
	 *     如果是主节则放开主并发布LOST事件, 否则忽略此次操作
	 *
	 */
	void releaseLeader();
}