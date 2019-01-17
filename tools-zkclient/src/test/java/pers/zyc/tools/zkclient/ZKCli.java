package pers.zyc.tools.zkclient;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooKeeperMain;

import java.io.Closeable;
import java.util.concurrent.CountDownLatch;

/**
 * @author zhangyancheng
 */
class ZKCli extends ZooKeeperMain implements Closeable {

	ZKCli(String connectString) throws Exception {
		super(new ZooKeeper(connectString, 30000, null));
		final CountDownLatch latch = new CountDownLatch(1);
		zk.register(new Watcher() {
			@Override
			public void process(WatchedEvent event) {
				if (event.getState() == Event.KeeperState.SyncConnected) {
					latch.countDown();
				}
			}
		});
		latch.await();
	}

	@Override
	public void close() {
		try {
			zk.close();
		} catch (InterruptedException ignored) {
		}
	}

	ZooKeeper getZooKeeper() {
		return zk;
	}
}
