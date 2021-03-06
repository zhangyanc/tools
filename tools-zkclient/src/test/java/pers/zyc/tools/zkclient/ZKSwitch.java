package pers.zyc.tools.zkclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 控制启动和关闭zk服务
 *
 * @author zhangyancheng
 */
class ZKSwitch {
	private static final Logger LOGGER = LoggerFactory.getLogger(ZKSwitch.class);

	private interface CommandCallback<V> {
		V execute(Process process);
	}

	private class WaitCallback implements CommandCallback<Void> {
		private int sleep;

		public WaitCallback(int sleep) {
			this.sleep = sleep;
		}

		@Override
		public Void execute(Process process) {
			try {
				TimeUnit.MILLISECONDS.sleep(sleep);
			} catch (InterruptedException ignored) {
			}
			return null;
		}
	}

	private class StartZooKeeperCallback implements CommandCallback<Integer> {

	    final CountDownLatch startedLatch;

        private StartZooKeeperCallback(CountDownLatch startedLatch) {
            this.startedLatch = startedLatch;
        }

        @Override
		public Integer execute(Process process) {
			try {
				BufferedReader bufferedReader = new BufferedReader(
						new InputStreamReader(process.getInputStream(), "GBK"));
				String line;
				boolean counted = false;
				while ((line = bufferedReader.readLine()) != null){
					//System.err.println(line);
					if (!counted && line.contains("binding to port")) {
                        startedLatch.countDown();
                        counted = true;
                    }
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			return null;
		}
	}

	private String zkBaseDir;
	private int zkPid = -1;

	ZKSwitch(String zkBaseDir) {
		this.zkBaseDir = zkBaseDir;
	}

	private static void exec(String cmd) {
		exec(cmd, null);
	}

	private static <V> V exec(String cmd, CommandCallback<V> callback) {
		Process process = null;
		try {
			process = Runtime.getRuntime().exec(cmd);
			return callback != null ? callback.execute(process) : null;
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (process != null) {
				process.destroy();
			}
		}
	}

	private static Integer getPidByListenPort(final int listenPort) {
		return exec("netstat -ano", new CommandCallback<Integer>() {
			@Override
			public Integer execute(Process process) {
				try {
					BufferedReader bufferedReader = new BufferedReader(
							new InputStreamReader(process.getInputStream(), "GBK"));

					String line;
					while ((line = bufferedReader.readLine()) != null){
						if (line.contains(":" + listenPort)) {
							//[, TCP, 0.0.0.0:2181, 0.0.0.0:0, LISTENING, 409792]
							String[] array = line.split("\\s+");
							return Integer.parseInt(array[array.length - 1]);
						}
					}
					throw new RuntimeException("Can not find zookeeper pid for listen port: " + listenPort);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		});
	}

	private static Integer getPidByProcessName(final String processName, final boolean forced) {
		return exec("jps -l", new CommandCallback<Integer>() {
			@Override
			public Integer execute(Process process) {
				try {
					BufferedReader bufferedReader = new BufferedReader(
							new InputStreamReader(process.getInputStream(), "GBK"));

					String line;
					while ((line = bufferedReader.readLine()) != null){
						if (line.contains(processName)) {
							return Integer.parseInt(line.split(" ")[0]);
						}
					}
					if (forced) {
						throw new RuntimeException("Can not find zookeeper pid for execute name: " + processName);
					}
					return 0;
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		});
	}

	void open() throws InterruptedException {
        zkPid = getPidByProcessName("QuorumPeerMain", false);
        if (zkPid > 0) {
            return;
        }

        final CountDownLatch startedLatch = new CountDownLatch(1);
		Thread starter = new Thread(new Runnable() {
			@Override
			public void run() {
				exec(zkBaseDir + "/bin/zkServer.cmd", new StartZooKeeperCallback(startedLatch));
			}
		});
        starter.setDaemon(true);
		starter.start();
        startedLatch.await();
		//获取zk server的进程pid
		zkPid = getPidByProcessName("QuorumPeerMain", true);
		LOGGER.debug("ZooKeeper start success, pid: {}", zkPid);
	}

	void close() {
		if (zkPid != -1) {
			exec("taskkill /f /pid " + zkPid, new WaitCallback(1000));
			LOGGER.debug("ZooKeeper close success.");
			zkPid = -1;
		}
	}

	public static void main(String[] args) throws Exception {
		ZKSwitch zkSwitch = new ZKSwitch("E:/Tools/zookeeper-3.4.6");
		zkSwitch.open();
		System.out.println(zkSwitch.zkPid);
		zkSwitch.close();
		System.out.println(zkSwitch.zkPid);
	}
}
