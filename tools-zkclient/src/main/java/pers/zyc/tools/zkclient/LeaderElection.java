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
	 *                     2. 选举节点必须存在, 且不能被删除
	 * @param elector 选举人
	 */
	void elect(String electionPath, Elector elector);

	/**
	 * 退出选举
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
	 *     1. 如果当前不是主节点忽略此次操作
	 *     2. 如果当前是主但zookeeper未连通则release失败, 且不会改变内部状态,
	 *
	 *
	 * @return 当且仅当为主节点且成功释放返回true
	 */
	boolean releaseLeader();
}