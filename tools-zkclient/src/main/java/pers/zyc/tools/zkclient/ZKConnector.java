package pers.zyc.tools.zkclient;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.zyc.tools.event.Listenable;
import pers.zyc.tools.event.MulticastExceptionHandler;
import pers.zyc.tools.event.Multicaster;
import pers.zyc.tools.lifecycle.PeriodicService;
import pers.zyc.tools.zkclient.listener.ConnectionListener;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

import static org.apache.zookeeper.Watcher.Event.KeeperState.Disconnected;
import static org.apache.zookeeper.Watcher.Event.KeeperState.SyncConnected;

/**
 * 连接器实现
 * @author zhangyancheng
 */
final class ZKConnector extends PeriodicService implements Listenable<ConnectionListener> {
    private final static Logger LOGGER = LoggerFactory.getLogger(ZKConnector.class);

    /**
     * 监听器回调异常处理器
     */
    private final static MulticastExceptionHandler EXCEPTION_HANDLER = new LogMulticastExceptionHandler(LOGGER);

    /**
     * 连接事件广播器, listener回调的阻塞将影响connector线程对新连接事件的处理
     */
    private final Multicaster<ConnectionListener> multicaster;

    /**
     * 连接事件锁, 用于ZK连接事件的等待和唤醒
     */
    private final Condition incomeEventCondition = serviceLock.newCondition();

    /**
     * 协商后的sessionTimeout, 期望连接事件到达的超时时间
     */
    private int eventWaitTimeout;
    /**
     * session id用来判断是否同一个ZooKeeper实例重连成功
     */
    private long zooKeeperSessionId;
    /**
     * 当前事件, 新入事件(与当前时间比较判断连接状态)
     */
    private Watcher.Event.KeeperState currentState, incomeState;
    /**
     * ZooKeeper实例
     */
    private ZooKeeper zooKeeper;

    private final String connectStr;
    private final int sessionTimeout;

    /**
     * @param connectStr 连接地址
     * @param sessionTimeout 会话超时时间
     */
    ZKConnector(String connectStr, int sessionTimeout) {
        this.connectStr = connectStr;
        this.sessionTimeout = sessionTimeout;

        multicaster = new Multicaster<ConnectionListener>(){};
        multicaster.setExceptionHandler(EXCEPTION_HANDLER);
    }

    @Override
    public void addListener(ConnectionListener connectionListener) {
        multicaster.addListener(connectionListener);
    }

    @Override
    public void removeListener(ConnectionListener connectionListener) {
        multicaster.removeListener(connectionListener);
    }

    @Override
    public String getName() {
        return "ZKConnector";
    }

    @Override
    protected void doStop() throws Exception {
        super.doStop();
        closeZooKeeper();
        multicaster.removeAllListeners();
    }

    boolean isConnected() {
        return isRunning() && zooKeeper != null && zooKeeper.getState().isConnected();
    }

    ZooKeeper getZooKeeper() {
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
            serviceLock.lock();
            try {
                currentState = incomeState = event.getState();
                incomeEventCondition.signal();
            } finally {
                serviceLock.unlock();
            }
        }
    }

    @Override
    protected void execute() throws InterruptedException {
        serviceLock.lockInterruptibly();
        Publisher publisher;
        try {
            if (!isRunning()) {
                return;
            }
            //初始以及会话关闭后的下轮, 创建ZooKeeper
            if (zooKeeper == null) {
                createZooKeeper();
            }

            long timeout = TimeUnit.MILLISECONDS.toNanos(eventWaitTimeout);
            while (this.incomeState == null && timeout > 0) {
                //等待新事件
                timeout = incomeEventCondition.awaitNanos(timeout);
            }
            Watcher.Event.KeeperState incomeState = this.incomeState;
            //清空incomeState(如果有的话), 表示此轮已经消费
            this.incomeState = null;

            /*
             * 状态判断分以下情况:
             *      1. 未收到事件且当前状态为空, 表示当前ZooKeeper尚未连通过, 无需发布事件继续等待连接
             *      1. 未收到事件且当前处于连通状态, 表示正常连通无需发布事件
             *      2. 收到SyncConnected事件, 判断是否是同一个ZooKeeper实例重连成功, 发布connected或者reconnected事件
             *      3. 收到Disconnected事件, 发布suspend事件, 然后等待ZooKeeper的自动重连
             *      4. 其他均为错误(未支持)状态, 主动关闭ZooKeeper, 发布会话关闭事件
             */
            if (incomeState == null &&
               (currentState == null || currentState == SyncConnected)) {
                return;
            }
            if (incomeState == SyncConnected) {
                if (zooKeeperSessionId == zooKeeper.getSessionId()) {
                    publisher = new Publisher() {
                        @Override
                        public void run() {
                            multicaster.listeners.onReconnected();
                        }
                    };
                } else {
                    //更新协商后真正的超时时间
                    eventWaitTimeout = zooKeeper.getSessionTimeout();
                    //保存新会话id, 用于收到SyncConnected后判断是否为新会话
                    zooKeeperSessionId = zooKeeper.getSessionId();
                    publisher = new Publisher() {
                        @Override
                        public void run() {
                            multicaster.listeners.onConnected();
                        }
                    };
                }
            } else if (incomeState == Disconnected) {
                publisher = new Publisher() {
                    @Override
                    public void run() {
                        multicaster.listeners.onSuspend();
                    }
                };
            } else {
                closeZooKeeper();
                publisher = new Publisher() {
                    @Override
                    public void run() {
                        multicaster.listeners.onSessionClosed();
                    }
                };
            }
        } finally {
            serviceLock.unlock();
        }
        //发布事件
        publisher.run();
    }

    private abstract class Publisher implements Runnable {
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
                currentState = incomeState = null;
                eventWaitTimeout = sessionTimeout;
            }
        }
    }
}
