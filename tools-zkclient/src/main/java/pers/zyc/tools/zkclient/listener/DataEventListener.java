package pers.zyc.tools.zkclient.listener;

import org.apache.zookeeper.data.Stat;

/**
 * @author zhangyancheng
 */
public interface DataEventListener extends NodeEventListener {

	void onDataChanged(String path, Stat stat, byte[] data);
}
