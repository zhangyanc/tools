package pers.zyc.tools.zkclient;

import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author zhangyancheng
 */
class ZKCli extends ZooKeeperMain implements Closeable {

	private static final Logger logger = LoggerFactory.getLogger(ZKCli.class);

	ZKCli(String connectString) throws IOException {
		super(new ZooKeeper(connectString, 30000, new Watcher() {
			@Override
			public void process(WatchedEvent event) {
				logger.debug(event.toString());
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
