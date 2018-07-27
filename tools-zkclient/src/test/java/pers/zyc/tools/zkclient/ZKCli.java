package pers.zyc.tools.zkclient;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooKeeperMain;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author zhangyancheng
 */
class ZKCli extends ZooKeeperMain implements Closeable {

	ZKCli(String connectString) throws IOException {
		super(new ZooKeeper(connectString, 30000, new Watcher() {
			@Override
			public void process(WatchedEvent event) {
			}
		}));
	}

	public void close() {
		try {
			zk.close();
		} catch (InterruptedException ignored) {
		}
	}
}
