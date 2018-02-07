package pers.zyc.tools.zkclient;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.zyc.tools.event.EventListener;
import pers.zyc.tools.event.EventSource;
import pers.zyc.tools.event.ListenerInvoker;
import pers.zyc.tools.lifecycle.PeriodicService;
import pers.zyc.tools.zkclient.event.ConnectionEvent;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

/**
 * @author zhangyancheng
 */
class ZKConnector extends PeriodicService implements EventSource<ConnectionEvent>, Watcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZKConnector.class);
    private String connectStr;
    private int sessionTimeout;

    private WatchedEvent lastEvent;
    private WatchedEvent currentEvent;
    private volatile ZooKeeper zooKeeper;
    private long zooKeeperSessionId = -1;
    private ListenerInvoker listenerInvoker;
    private Condition connectionEventCondition = serviceLock.newCondition();
    private Set<EventListener<ConnectionEvent>> connectionListeners = new CopyOnWriteArraySet<>();

    public ZKConnector(String connectStr, int sessionTimeout) {
        this.connectStr = connectStr;
        this.sessionTimeout = sessionTimeout;
    }

    @Override
    public void addListener(EventListener<ConnectionEvent> listener) {
        connectionListeners.add(Objects.requireNonNull(listener));
    }

    @Override
    public void removeListener(EventListener<ConnectionEvent> listener) {
        connectionListeners.remove(Objects.requireNonNull(listener));
    }

    @Override
    public void setListenerInvoker(ListenerInvoker listenerInvoker) {
        this.listenerInvoker = Objects.requireNonNull(listenerInvoker);
    }

    @Override
    public ListenerInvoker getListenerInvoker() {
        return this.listenerInvoker;
    }

    @Override
    protected void doStop() throws Exception {
        super.doStop();
        connectionListeners.clear();
        closeZooKeeper();
    }

    boolean isConnected() {
        return zooKeeper != null && zooKeeper.getState().isConnected();
    }

    ZooKeeper getZooKeeper() {
        return zooKeeper;
    }

    @Override
    protected long period() {
        return 0;
    }

    @Override
    protected void execute() throws InterruptedException {
        serviceLock.lock();
        try {
            if (zooKeeper == null) {
                try {
                    zooKeeper = new ZooKeeper(connectStr, sessionTimeout, this);
                } catch (IOException e) {
                    throw new RuntimeException("New ZooKeeper instance error!", e);
                }
            }

            ConnectionEvent connectionEvent = null;
            long timeout = TimeUnit.MILLISECONDS.toNanos(sessionTimeout);
            while (currentEvent == null && timeout > 0) {
                timeout = connectionEventCondition.awaitNanos(timeout);
            }
            if (currentEvent != null) {
                switch (currentEvent.getState()) {
                    case SyncConnected:
                        if (zooKeeperSessionId != zooKeeper.getSessionId()) {
                            zooKeeperSessionId = zooKeeper.getSessionId();
                            connectionEvent = ConnectionEvent.CONNECTED;
                        } else {
                            //重连成功
                            connectionEvent = ConnectionEvent.RECONNECTED;
                        }
                        break;
                    case Disconnected:
                        connectionEvent = ConnectionEvent.DISCONNECTED;
                        break;
                    case Expired:
                        closeZooKeeper();
                        connectionEvent = ConnectionEvent.SESSION_CLOSED;
                        break;
                    default:
                        throw new IllegalStateException("Unexpected event: " + currentEvent);
                }
                currentEvent = null;
            } else if (lastEvent != null && lastEvent.getState() != Event.KeeperState.SyncConnected) {
                closeZooKeeper();
                connectionEvent = ConnectionEvent.SESSION_CLOSED;
            }
            if (connectionEvent != null) {
                listenerInvoker.invoke(connectionEvent, connectionListeners);
            }
        } finally {
            serviceLock.unlock();
        }
    }

    private void closeZooKeeper() throws InterruptedException {
        if (zooKeeper != null) {
            zooKeeperSessionId = -1;
            zooKeeper.close();
        }
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        LOGGER.error("Connect error!", e);
        super.uncaughtException(t, e);
    }

    @Override
    public void process(WatchedEvent event) {
        serviceLock.lock();
        try {
            if (isRunning()) {
                lastEvent = currentEvent = event;
                connectionEventCondition.signal();
            }
        } finally {
            serviceLock.unlock();
        }
    }
}
