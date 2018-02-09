package pers.zyc.tools.zkclient;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.zyc.tools.event.*;
import pers.zyc.tools.lifecycle.PeriodicService;
import pers.zyc.tools.zkclient.event.ConnectionEvent;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * ZooKeeper连接器
 *
 * @author zhangyancheng
 */
class ZKConnector extends PeriodicService implements EventSource<ConnectionEvent> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZKConnector.class);

    private final ConnectorHelper connectorHelper;
    private Set<EventListener<ConnectionEvent>> connectionListeners = new CopyOnWriteArraySet<>();
    private EventPublisher eventPublisher = new SerialEventPublisher(new LogPublishExceptionHandler(LOGGER));

    public ZKConnector(String connectStr, int sessionTimeout) {
        connectorHelper = new ConnectorHelper(serviceLock, connectStr, sessionTimeout);
    }

    @Override
    public void addListener(EventListener<ConnectionEvent> listener) {
        if (connectionListeners.add(Objects.requireNonNull(listener)) && isConnected()) {
            //如果当前处于连接状态则单独发布CONNECTED事件
            serviceLock.lock();
            try {
                eventPublisher.publish(ConnectionEvent.CONNECTED, listener);
            } finally {
                serviceLock.unlock();
            }
        }
    }

    @Override
    public void removeListener(EventListener<ConnectionEvent> listener) {
        connectionListeners.remove(Objects.requireNonNull(listener));
    }

    @Override
    public void setEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = Objects.requireNonNull(eventPublisher);
    }

    @Override
    public EventPublisher getEventPublisher() {
        return this.eventPublisher;
    }

    @Override
    protected void doStop() throws Exception {
        super.doStop();
        connectionListeners.clear();
        connectorHelper.closeZooKeeper();
    }

    boolean isConnected() {
        return connectorHelper.isConnected();
    }

    ZooKeeper getZooKeeper() {
        return connectorHelper.getZooKeeper();
    }

    @Override
    protected long period() {
        return 0;
    }

    @Override
    protected void execute() throws InterruptedException {
        serviceLock.lockInterruptibly();
        try {
            //处理并返回连接事件, 异常后停止连接器
            ConnectionEvent connectionEvent = connectorHelper.process();
            if (connectionEvent != null) {
                LOGGER.info("Publish event {}", connectionEvent);
                eventPublisher.publish(connectionEvent, connectionListeners);
            }
        } finally {
            serviceLock.unlock();
        }
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        LOGGER.error("Connector error!", e);
        super.uncaughtException(t, e);
    }

    /**
     * 连接器辅助, 负责所有ZooKeeper状态处理及事件转换
     */
    private static class ConnectorHelper implements Watcher {
        final Lock lock;
        final Condition eventCondition;

        final String connectStr;
        final int sessionTimeout;
        int eventWaitTimeout;
        long zooKeeperSessionId;
        WatchedEvent incomeEvent;
        WatchedEvent currentEvent;
        volatile ZooKeeper zooKeeper;

        ConnectorHelper(Lock lock, String connectStr, int sessionTimeout) {
            this.lock = lock;
            this.eventCondition = lock.newCondition();
            this.connectStr = connectStr;
            this.sessionTimeout = sessionTimeout;
            this.eventWaitTimeout = sessionTimeout;
        }

        @Override
        public void process(WatchedEvent event) {
            lock.lock();
            try {
                LOGGER.info(event.toString());
                currentEvent = incomeEvent = event;
                eventCondition.signal();
            } finally {
                lock.unlock();
            }
        }

        boolean isConnected() {
            return zooKeeper != null && zooKeeper.getState().isConnected();
        }

        ZooKeeper getZooKeeper() {
            return zooKeeper;
        }

        /**
         * 处理WatchedEvent, 并转换事件
         *
         * @return 需要发布的连接事件, 为null表示无需发布
         */
        ConnectionEvent process() throws InterruptedException {
            if (zooKeeper == null) {
                createZooKeeper();
            }

            long timeout = TimeUnit.MILLISECONDS.toNanos(eventWaitTimeout);
            while (incomeEvent == null && timeout > 0) {
                //等待新事件
                timeout = eventCondition.awaitNanos(timeout);
            }

            /*
             * 收到事件:
             *      1. 连接成功, 判断是现有会话重连成功还是新会话连接成功, 返回CONNECTED或RECONNECTED
             *      2. 断开连接, 等待ZooKeeper自动重连, 返回DISCONNECTED
             *      3. 超时, 关闭ZooKeeper实例, 返回SESSION_CLOSED
             *      4. 其他事件, 不支持其他事件, 抛出异常后关闭当前连接器
             * 未收到事件:
             *      5. 如果当前状态为null, 表示会话初始连接未成功, 不发布事件等待ZooKeeper自动重连
             *      6. 当前状态不为null且不为连接状态, server已经会话过期, 关闭ZooKeeper实例, 返回SESSION_CLOSED
             *      7. 当前状态不为null且为连接状态, 正常连通, 不发布事件
             */
            ConnectionEvent event = null;
            if (incomeEvent != null) {
                Watcher.Event.KeeperState incomeState = incomeEvent.getState();
                incomeEvent = null;
                switch (incomeState) {
                    case SyncConnected:
                        //session id不变表示现有会话重连成功
                        event = zooKeeperSessionId != zooKeeper.getSessionId() ?
                                ConnectionEvent.CONNECTED : ConnectionEvent.RECONNECTED;
                        break;
                    case Disconnected:
                        event = ConnectionEvent.DISCONNECTED;
                        break;
                    case Expired:
                        event = ConnectionEvent.SESSION_CLOSED;
                        break;
                    default:
                        //暂不支持其他类型
                        throw new IllegalStateException("Unexpected state: " + incomeState);
                }
            } else if (currentEvent != null &&
                    currentEvent.getState() != Watcher.Event.KeeperState.SyncConnected) {

                //断开连接后扔未收到事件, 则主动关闭会话
                event = ConnectionEvent.SESSION_CLOSED;
            }

            if (event == ConnectionEvent.CONNECTED) {
                //更新协商后真正的超时时间
                eventWaitTimeout = zooKeeper.getSessionTimeout();
                //保存新会话id, 用于收到SyncConnected后判断是否为新会话
                zooKeeperSessionId = zooKeeper.getSessionId();
            } else if (event == ConnectionEvent.SESSION_CLOSED) {
                closeZooKeeper();
            }
            return event;
        }

        void createZooKeeper() {
            try {
                zooKeeper = new ZooKeeper(connectStr, sessionTimeout, this);
            } catch (IOException e) {
                throw new RuntimeException("New ZooKeeper instance error!", e);
            }
        }

        /**
         * 关闭ZooKeeper及自身的重连机制
         */
        void closeZooKeeper() throws InterruptedException {
            ZooKeeper badClient = zooKeeper;
            if (badClient != null) {
                LOGGER.info("ZooKeeper {} closed!", badClient);
                reset();
                badClient.close();
            }
        }

        /**
         * 重置状态
         */
        void reset() {
            zooKeeper = null;
            incomeEvent = currentEvent = null;
            zooKeeperSessionId = 0;
            eventWaitTimeout = sessionTimeout;
        }
    }
}
