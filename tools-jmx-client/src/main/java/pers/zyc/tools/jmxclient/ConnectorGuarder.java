package pers.zyc.tools.jmxclient;

import pers.zyc.tools.utils.GeneralThreadFactory;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author zhangyancheng
 */
class ConnectorGuarder extends FutureTask<JMXConnector> {

	private static final ExecutorService CONNECT_EXECUTOR;
	static {
		GeneralThreadFactory threadFactory = new GeneralThreadFactory("jmx-guarder-");
		threadFactory.setDaemon(true);
		CONNECT_EXECUTOR = Executors.newCachedThreadPool(threadFactory);
	}

	private final JmxClient jmxClient;

	ConnectorGuarder(final JmxClient jmxClient) {
		super(new Callable<JMXConnector>() {
			@Override
			public JMXConnector call() throws Exception {
				JmxUser user = jmxClient.getJmxUser();
				Map<String, Object> env = null;
				if (user != null) {
					env = new HashMap<>();
					env.put("jmx.remote.credentials", new String[]{user.getUser(), user.getPassword()});
				}
				JMXConnector connector = JMXConnectorFactory.newJMXConnector(jmxClient.getServiceURL(), env);
				connector.addConnectionNotificationListener(JmxClient.JMX_CONNECTION_LISTENER, null, jmxClient);
				connector.connect(env);
				return connector;
			}
		});
		this.jmxClient = jmxClient;
	}

	void connect() {
		CONNECT_EXECUTOR.execute(this);
	}

	void disconnect() {
		if (!cancel(true)) {
			CONNECT_EXECUTOR.execute(new Runnable() {
				@Override
				public void run() {
					try {
						getConnector().close();
					} catch (Exception ignored) {
					}
				}
			});
		}
	}

	JMXConnector getConnector() {
		try {
			return get(jmxClient.getConnectionTimeout(), TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new JmxException("Thread interrupted");
		} catch (ExecutionException e) {
			throw new JmxException("Connect host[" + jmxClient.getJmxHost().getHost() + "] error", e.getCause());
		} catch (TimeoutException e) {
			throw new JmxException("Connect host[" + jmxClient.getJmxHost().getHost() + "] timeout, " +
					"used more than " + jmxClient.getConnectionTimeout() + "ms");
		}
	}
}
