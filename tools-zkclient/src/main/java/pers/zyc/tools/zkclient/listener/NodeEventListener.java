package pers.zyc.tools.zkclient.listener;


import pers.zyc.tools.utils.event.Listener;

/**
 * 节点事件监听器(不同于ZooKeeper Watcher, 此监听是持续性的)
 *
 * <p>
 *     由于实现事件驱动仍然基于Watcher(一次性的), 所以还是存在事件丢失:
 *     在watcher触发后导再次watch前发生在节点上的事件会丢失(除了最后一次)
 *     如: 1.触发了节点新增但在exists-watch前又发生了多次节点删除和新增, 当exists watcher到达时
 *     	   如果节点是存在的则发布一次节点新增事件, 否则将不发布任何事件
 *     	   2.触发了节点数据变更但在data-watch前又发生了多次节点数据变更或者节点删除, 当data watcher到达时
 *     	   如果节点是存在的则发布一次节点数据变更(中间的多次数据变更事件丢失), 否则发布节点删除(变更事件全部丢失)
 * <p>
 *     客户端网络故障期间发生的节点状态变更(除了最后一次)也将丢失对应事件
 *
 * @see pers.zyc.tools.zkclient.NodeEventReactor.EventReactor
 * @author zhangyancheng
 */
public interface NodeEventListener extends Listener {

	/**
	 * 节点被删除
	 *
	 * @param path 节点路径
	 */
	void onNodeDeleted(String path);
}
