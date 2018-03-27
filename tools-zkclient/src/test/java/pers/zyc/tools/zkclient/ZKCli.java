package pers.zyc.tools.zkclient;

import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author zhangyancheng
 */
class ZKCli extends ZooKeeperMain {

	private static Logger logger = LoggerFactory.getLogger(ZKCli.class);

	ZKCli(String connectString) throws IOException {
		super(new ZooKeeper(connectString, 30000, new Watcher() {
			@Override
			public void process(WatchedEvent event) {
				logger.info(event.toString());
			}
		}));
	}
}
