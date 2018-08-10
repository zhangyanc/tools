package pers.zyc.tools.zkclient.listener;

import java.util.List;

/**
 * 节点子节点监听器, 注册后可持续监听子节点变更
 *
 * @see pers.zyc.tools.zkclient.NodeEventDurableWatcher.ChildrenEventReWatcher
 * @author zhangyancheng
 */
public interface ChildrenEventListener extends NodeEventListener {

	/**
	 * 子节点变更
	 *
	 * @param path 节点路径
	 * @param children 节点子节点列表(与reactor引用相同实例, 实现只能读)
	 */
	void onChildrenChanged(String path, List<String> children);
}
