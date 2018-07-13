package pers.zyc.tools.zkclient.listener;


import pers.zyc.tools.utils.event.Listener;

/**
 * @author zhangyancheng
 */
public interface ConnectionListener extends Listener {

	/**
	 * 当连接连通时回调
	 *
	 * @param newSession 是否为新会话, 区分是否为同一个ZooKeeper实例自动重连成功
	 */
	void onConnected(boolean newSession);

	/**
	 * 当连接断开时回调
	 *
	 * @param sessionClosed 会话是否关闭了(会话超时)
	 */
	void onDisconnected(boolean sessionClosed);
}
