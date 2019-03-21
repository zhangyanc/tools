package pers.zyc.tools.jmxclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.zyc.tools.utils.event.EventListener;
import pers.zyc.tools.utils.event.EventSource;
import pers.zyc.tools.utils.event.Multicaster;

import javax.management.*;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author zhangyancheng
 */
public class JmxClient implements EventSource<ConnectionEvent> {
	private static final Logger LOGGER = LoggerFactory.getLogger(JmxClient.class);

	static final NotificationListener JMX_CONNECTION_LISTENER = new NotificationListener() {
		@Override
		public void handleNotification(Notification notification, Object handback) {
			JmxClient jmxClient = (JmxClient) handback;
			LOGGER.info("Notification -> {}, host: {}", notification.getType(), jmxClient.jmxHost.getHost());
			jmxClient.multicaster.listeners.onEvent(new ConnectionEvent(jmxClient, notification.getType()));
		}
	};

	private final JmxHost jmxHost;
	private final JmxUser jmxUser;
	private final JMXServiceURL serviceURL;
	private final int connectionTimeout;
	private final ConnectorGuarder connectorGuarder;
	private final ConcurrentMap<ObjectName, Object> mbeanMap = new ConcurrentHashMap<>();
	private final Multicaster<EventListener<ConnectionEvent>> multicaster =
			new Multicaster<EventListener<ConnectionEvent>>() {};

	public JmxClient(JmxHost jmxHost) {
		this(jmxHost, null);
	}

	public JmxClient(JmxHost jmxHost, JmxUser jmxUser) {
		this(jmxHost, jmxUser, 3000);
	}

	public JmxClient(JmxHost jmxHost, JmxUser jmxUser, int connectionTimeout) {
		this.jmxHost = jmxHost;
		try {
			serviceURL = new JMXServiceURL("rmi", "", 0, "/jndi/rmi://" + jmxHost.getHost() + "/jmxrmi");
		} catch (MalformedURLException e) {
			throw new JmxException(e);
		}
		this.jmxUser = jmxUser;
		this.connectionTimeout = connectionTimeout;
		connectorGuarder = new ConnectorGuarder(this);
	}

	@Override
	public void addListener(EventListener<ConnectionEvent> listener) {
		multicaster.addListener(listener);
	}

	@Override
	public void removeListener(EventListener<ConnectionEvent> listener) {
		multicaster.removeListener(listener);
	}

	public void connect() {
		connectorGuarder.connect();
	}

	public void syncConnect() {
		connect();
		connectorGuarder.getConnector();
	}

	public void disconnect() {
		connectorGuarder.disconnect();
		mbeanMap.clear();
	}

	public MBeanServerConnection getConnection() {
		try {
			return connectorGuarder.getConnector().getMBeanServerConnection();
		} catch (IOException e) {
			throw new JmxException("Get host[" + jmxHost.getHost() + "] connection error", e);
		}
	}

	public Set<ObjectName> queryNames(ObjectName name) {
		return queryNames(name, null);
	}

	public Set<ObjectName> queryNames(ObjectName name, QueryExp query) {
		try {
			return getConnection().queryNames(name, query);
		} catch (IOException e) {
			throw new JmxException("Query names error, host:" + jmxHost.getHost() + ", name: " + name);
		}
	}

	public <T> T getMBean(Class<T> clazz, String objectName) {
		return getMBean(clazz, getObjectName(objectName));
	}

	@SuppressWarnings("unchecked")
	public <T> T getMBean(Class<T> clazz, ObjectName objectName) {
		Objects.requireNonNull(clazz);
		Object mbean = mbeanMap.get(objectName);
		if (mbean == null) {
			Object mBeanProxy = createProxy(getConnection(), clazz, objectName);
			mbean = mbeanMap.putIfAbsent(objectName, mBeanProxy);
			if (mbean == null) {
				mbean = mBeanProxy;
			}
		}
		return (T) mbean;
	}

	private static <M> M createProxy(MBeanServerConnection connection, Class<M> interfaceClass, ObjectName objectName) {
		Object proxy = Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class<?>[] {interfaceClass},
				new MBeanServerInvocationHandler(connection, objectName, JMX.isMXBeanInterface(interfaceClass)));
		return interfaceClass.cast(proxy);
	}

	ObjectName getObjectName(String objectName) {
		try {
			return ObjectName.getInstance(objectName);
		} catch (MalformedObjectNameException e) {
			throw new JmxException(e);
		}
	}

	public JmxHost getJmxHost() {
		return jmxHost;
	}

	public JmxUser getJmxUser() {
		return jmxUser;
	}

	public JMXServiceURL getServiceURL() {
		return serviceURL;
	}

	public int getConnectionTimeout() {
		return connectionTimeout;
	}
}
