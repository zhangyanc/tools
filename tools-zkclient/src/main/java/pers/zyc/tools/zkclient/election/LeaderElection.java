package pers.zyc.tools.zkclient.election;

import pers.zyc.tools.event.EventSource;
import pers.zyc.tools.lifecycle.Lifecycle;

/**
 * @author zhangyancheng
 */
public interface LeaderElection extends Lifecycle, EventSource<LeaderEvent> {

	/**
	 * @return 当前member节点名
	 */
	String member();

	/**
	 * @return leader节点名
	 */
	String leader();

	/**
	 * @return 是否已选为主节点
	 */
	boolean isLeader();

	/**
	 * 放开主角色
	 *
	 * @return 当且仅当为主节点且成功释放返回true
	 */
	boolean releaseLeader();

	/**
	 * @return 选主信息
	 */
	ElectInfo getElectInfo();
}