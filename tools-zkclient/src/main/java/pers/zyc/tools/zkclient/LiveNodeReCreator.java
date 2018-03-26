package pers.zyc.tools.zkclient;

import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.zyc.tools.zkclient.listener.ConnectionListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author zhangyancheng
 */
class LiveNodeReCreator implements ConnectionListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(LiveNodeReCreator.class);

	private final ZKClient zkClient;
	private final List<RecreateInfo> needToRecreateList = new ArrayList<>();
	private final CopyOnWriteArraySet<RecreateInfo> recreateInfoSet = new CopyOnWriteArraySet<>();

	LiveNodeReCreator(ZKClient zkClient) {
		this.zkClient = zkClient;
		zkClient.addListener(this);
	}

	@Override
	public void onConnected(boolean newSession) {
		Iterator<RecreateInfo> iterator = needToRecreateList.iterator();
		while (iterator.hasNext()) {
			RecreateInfo info = iterator.next();
			try {
				String actualPath = zkClient.createEphemeral(info.path, info.data, info.sequential);
				LOGGER.info("{} recreate success", info.path);
				iterator.remove();
				info.listener.onRecreateSuccess(info.path, actualPath);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return;
			} catch (Exception e) {
				LOGGER.info("{} recreate failed", info.path);
				info.listener.onRecreateFailed(info.path, e);

				if (e instanceof KeeperException.NodeExistsException) {
					iterator.remove();
				}

				if (!zkClient.isConnected()) {
					return;
				}
			}
		}
	}

	@Override
	public void onDisconnected(boolean sessionClosed) {
		if (sessionClosed) {
			needToRecreateList.clear();
			needToRecreateList.addAll(recreateInfoSet);
		}
	}

	void add(String path, byte[] data, boolean sequential, RecreateListener recreateListener) {
		recreateInfoSet.add(new RecreateInfo(path, data, sequential, recreateListener));
	}

	void remove(String path) {
		recreateInfoSet.remove(new RecreateInfo(path, null, false, null));
	}

	private static class RecreateInfo {
		final String path;
		final byte[] data;
		final boolean sequential;
		final RecreateListener listener;

		RecreateInfo(String path, byte[] data, boolean sequential, RecreateListener listener) {
			this.path = path;
			this.data = data;
			this.sequential = sequential;
			this.listener = listener;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			RecreateInfo that = (RecreateInfo) o;
			return Objects.equals(path, that.path);
		}
	}
}
