package pers.zyc.tools.zkclient.listener;

import org.apache.zookeeper.data.Stat;

/**
 * @author zhangyancheng
 */
public interface ExistsEventListener extends NodeEventListener {

	void onNodeCreated(String path, Stat nodeStat);

	void onNodeDeleted(String path);
}
