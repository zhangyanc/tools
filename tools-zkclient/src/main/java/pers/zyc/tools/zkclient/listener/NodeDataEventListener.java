package pers.zyc.tools.zkclient.listener;

import org.apache.zookeeper.data.Stat;

/**
 * @author zhangyancheng
 */
public interface NodeDataEventListener extends NodeListener {

	void onStatChanged(String path, Stat stat);

	void onDataChanged(String path, byte[] data);
}
