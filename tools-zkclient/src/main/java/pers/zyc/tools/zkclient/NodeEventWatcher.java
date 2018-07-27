package pers.zyc.tools.zkclient;

import pers.zyc.tools.zkclient.listener.ChildrenEventListener;
import pers.zyc.tools.zkclient.listener.DataEventListener;
import pers.zyc.tools.zkclient.listener.ExistsEventListener;

/**
 * @author zhangyancheng
 */
public interface NodeEventWatcher {

	/**
	 * @return 节点路径
	 */
	String getWatchedNodePath();

	/**
	 * 添加存在监听器, 节点存在状态变更后触发回调
	 *
	 * @param existsEventListener 存在监听器
	 */
	void addListener(ExistsEventListener existsEventListener);

	/**
	 * 移除监听器, 不再触发事件回调
	 *
	 * @param existsEventListener 存在监听器
	 */
	void removeListener(ExistsEventListener existsEventListener);

	/**
	 * 添加节点数据监听器, 节点数据变更后触发回调
	 *
	 * @param dataEventListener 数据监听器
	 */
	void addListener(DataEventListener dataEventListener);

	/**
	 * 移除监听器, 不再触发事件回调
	 *
	 * @param dataEventListener 数据监听器
	 */
	void removeListener(DataEventListener dataEventListener);

	/**
	 * 添加子节点监听器, 子节点变更后触发回调
	 *
	 * @param childrenEventListener 子节点监听器
	 */
	void addListener(ChildrenEventListener childrenEventListener);

	/**
	 * 移除监听器, 不再触发事件回调
	 *
	 * @param childrenEventListener 子节点监听器
	 */
	void removeListener(ChildrenEventListener childrenEventListener);
}
