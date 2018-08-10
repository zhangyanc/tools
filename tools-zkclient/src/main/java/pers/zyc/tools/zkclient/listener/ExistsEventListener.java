package pers.zyc.tools.zkclient.listener;

import org.apache.zookeeper.data.Stat;

/**
 * 节点存在监听器, 注册后可持续监听节点存在状态变更
 *
 * @see pers.zyc.tools.zkclient.NodeEventDurableWatcher.ExistsEventReWatcher
 * @author zhangyancheng
 */
public interface ExistsEventListener extends NodeEventListener {

	void onNodeCreated(String path, Stat stat);
}
