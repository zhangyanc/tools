package pers.zyc.tools.zkclient;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.zyc.tools.event.EventListener;
import pers.zyc.tools.event.EventMulticaster;
import pers.zyc.tools.event.MulticastExceptionHandler;
import pers.zyc.tools.event.SyncEventMulticaster;
import pers.zyc.tools.lifecycle.PeriodicService;
import pers.zyc.tools.zkclient.event.ConnectionEvent;
import pers.zyc.tools.zkclient.listener.SpecialEventListener;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static pers.zyc.tools.zkclient.event.ConnectionEvent.EventType.*;

/**
 * 连接器实现
 * @author zhangyancheng
 */
final class ZKConnectorImpl extends PeriodicService implements ZKConnector {
    private final static Logger LOGGER = LoggerFactory.getLogger(ZKConnectorImpl.class);

    /**
     * 监听器回调异常处理器
     */
    private final static MulticastExceptionHandler EXCEPTION_HANDLER = new LogMulticastExceptionHandler(LOGGER);

    private final String connectStr;
    private final int sessionTimeout;

    /**
     * 同步广播事件给所有监听器, listener回调的阻塞将影响connector线程对新连接事件的处理
     */
    private final EventMulticaster eventMulticaster = new SyncEventMulticaster(EXCEPTION_HANDLER);
    private final ReentrantLock eventLock = new ReentrantLock();

    /**
     * 连接事件锁, 用于ZK连接事件的等待和唤醒
     */
    private final Condition eventCondition = eventLock.newCondition();

    /**
     * 协商后的sessionTimeout, 期望连接事件到达的超时时间
     */
    private int eventWaitTimeout;
    /**
     * session id用来判断是否同一个ZooKeeper实例重连成功
     */
    private long zooKeeperSessionId;
    /**
     * 新入事件
     */
    private WatchedEvent incomeEvent;
    /**
     * 当前事件(对比新入等同于老的事件), 用于比较新入事件来判断ZooKeeper状态
     */
    private WatchedEvent currentEvent;
    private volatile ZooKeeper zooKeeper;

    /**
     * @param connectStr 连接地址
     * @param sessionTimeout 会话超时时间
     */
    ZKConnectorImpl(String connectStr, int sessionTimeout) {
        this.connectStr = connectStr;
        this.sessionTimeout = sessionTimeout;
    }

    @Override
    public void addListener(EventListener<ConnectionEvent> listener) {
        eventMulticaster.addListener(listener);
        if (listener instanceof SpecialEventListener &&
            isConnected() && eventLock.tryLock()) {
            try {
                listener.onEvent(new ConnectionEvent(this, CONNECTED));
            } finally {
                eventLock.unlock();
            }
        }
    }

    @Override
    public void removeListener(EventListener<ConnectionEvent> listener) {
        eventMulticaster.removeListener(listener);
    }

    @Override
    public String getName() {
        return "ZKConnector";
    }

    @Override
    protected void doStop() throws Exception {
        super.doStop();
        closeZooKeeper();
        eventMulticaster.removeAllListeners();
    }

    @Override
    public boolean isConnected() {
        return zooKeeper != null && zooKeeper.getState().isConnected();
    }

    @Override
    public ZooKeeper getZooKeeper() {
        return zooKeeper;
    }

    @Override
    protected long period() {
        return 0;
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        LOGGER.error("Connector error!", e);
        super.uncaughtException(t, e);
    }

    private class ConnectionWatcher implements Watcher {

        @Override
        public void process(WatchedEvent event) {
            eventLock.lock();
            try {
                currentEvent = incomeEvent = event;
                eventCondition.signal();
            } finally {
                eventLock.unlock();
            }
        }
    }

    @Override
    protected void execute() throws InterruptedException {
        eventLock.lockInterruptibly();
        ConnectionEvent connectionEvent;
        try {
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
            ConnectionEvent.EventType eventType;
            if (incomeEvent != null) {
                Watcher.Event.KeeperState incomeState = incomeEvent.getState();
                incomeEvent = null;
                switch (incomeState) {
                    case SyncConnected:
                        //session id不变表示现有会话重连成功
                        eventType = zooKeeperSessionId != zooKeeper.getSessionId() ? CONNECTED : RECONNECTED;
                        break;
                    case Disconnected:
                        eventType = DISCONNECTED;
                        break;
                    case Expired:
                        eventType = SESSION_CLOSED;
                        break;
                    default:
                        //暂不支持其他类型
                        throw new IllegalStateException("Unexpected state: " + incomeState);
                }
            } else if (currentEvent != null &&
                       currentEvent.getState() != Watcher.Event.KeeperState.SyncConnected) {

                //断开连接后扔未收到事件, 则主动关闭会话
                eventType = SESSION_CLOSED;
            } else {
                //正常连通, 无需发布事件
                eventType = null;
            }

            if (eventType == CONNECTED) {
                //更新协商后真正的超时时间
                eventWaitTimeout = zooKeeper.getSessionTimeout();
                //保存新会话id, 用于收到SyncConnected后判断是否为新会话
                zooKeeperSessionId = zooKeeper.getSessionId();
            } else if (eventType == SESSION_CLOSED) {
                closeZooKeeper();
            }
            connectionEvent = eventType == null ? null : new ConnectionEvent(this, eventType);
        } finally {
            eventLock.unlock();
        }

        if (connectionEvent != null && isAlive()) {
            LOGGER.info("Publish connection event: ", connectionEvent);
            eventMulticaster.multicastEvent(connectionEvent);
        }
    }

    private void createZooKeeper() {
        try {
            zooKeeper = new ZooKeeper(connectStr, sessionTimeout, new ConnectionWatcher());
        } catch (IOException e) {
            throw new RuntimeException("New ZooKeeper instance error!", e);
        }
    }

    /**
     * 关闭ZooKeeper及自身的重连机制
     */
    private void closeZooKeeper() throws InterruptedException {
        if (zooKeeper != null) {
            LOGGER.info("ZooKeeper closed, {}", zooKeeper);
            try {
                zooKeeper.close();
            } finally {
                zooKeeper = null;
                zooKeeperSessionId = 0;
                incomeEvent = currentEvent = null;
                eventWaitTimeout = sessionTimeout;
            }
        }
    }
}
