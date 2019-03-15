package pers.zyc.tools.jmxclient;

import pers.zyc.tools.utils.GeneralThreadFactory;

import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author zhangyancheng
 */
public class JMXConnection {
	private static final ExecutorService CONNECT_EXECUTOR;

	static {
		GeneralThreadFactory threadFactory = new GeneralThreadFactory("jmx-connector-");
		threadFactory.setDaemon(true);
		CONNECT_EXECUTOR = Executors.newCachedThreadPool(threadFactory);
	}

	private final JmxUser jmxUser;
	private final JMXServiceURL serviceURL;
	private final JMXConnector connector;

	public JMXConnection(JmxHost jmxHost, JmxUser jmxUser, int connectionTimeout) {
		try {
			this.serviceURL = new JMXServiceURL("rmi", jmxHost.getIp(), jmxHost.getPort(), "");
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException(e);
		}
		this.jmxUser = jmxUser;
		this.connector = connect(this, connectionTimeout);
		connector.addConnectionNotificationListener(new NotificationListener() {
			@Override
			public void handleNotification(Notification notification, Object handback) {

			}
		}, null, null);
	}

	private static JMXConnector connect(final JMXConnection jmxConnection, int connectionTimeout) {
		Future<JMXConnector> connectFuture = CONNECT_EXECUTOR.submit(new Callable<JMXConnector>() {
			@Override
			public JMXConnector call() throws Exception {
				Map<String, Object> env = null;
				if (jmxConnection.jmxUser != null) {
					env = new HashMap<>();
					env.put("jmx.remote.credentials", new String[]{jmxConnection.jmxUser.getUser(),
							jmxConnection.jmxUser.getPassword()});
				}
				return JMXConnectorFactory.connect(jmxConnection.serviceURL, env);
			}
		});
		try {
			return connectFuture.get(connectionTimeout, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new JmxException("Thread interrupt");
		} catch (ExecutionException | TimeoutException e) {
			throw new JmxException(e);
		}
	}
}
