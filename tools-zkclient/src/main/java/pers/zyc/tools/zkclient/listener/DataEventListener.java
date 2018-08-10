package pers.zyc.tools.zkclient.listener;

import org.apache.zookeeper.data.Stat;

/**
 * 节点数据监听器, 注册后可持续监听节点数据变更
 *
 * @see pers.zyc.tools.zkclient.NodeEventDurableWatcher.DataEventReWatcher
 * @author zhangyancheng
 */
public interface DataEventListener extends NodeEventListener {

	/**
	 * 节点数据变更
	 *
	 * @param path 节点路径
	 * @param stat 节点stat(与reactor引用相同实例, 实现只能读)
	 * @param data 节点数据(与reactor引用相同实例, 实现只能读)
	 */
	void onDataChanged(String path, Stat stat, byte[] data);
}
