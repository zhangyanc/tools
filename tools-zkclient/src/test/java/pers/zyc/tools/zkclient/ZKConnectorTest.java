package pers.zyc.tools.zkclient;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import pers.zyc.tools.zkclient.event.ConnectionEvent;
import pers.zyc.tools.zkclient.listener.ConnectionListener;

import java.util.LinkedList;

/**
 * @author zhangyancheng
 */
public class ZKConnectorTest {

    private static String ZK_ADDRESS = "localhost:2181";

    private static class TestConnectorConnectionListener implements ConnectionListener {

        LinkedList<ConnectionEvent> receivedEvents = new LinkedList<>();

        @Override
        public void onEvent(ConnectionEvent event) {
            receivedEvents.add(event);
        }

        boolean unReceivedEvent() {
            return receivedEvents.isEmpty();
        }

        ConnectionEvent lastEvent() {
            return receivedEvents.getLast();
        }
    }

    private ZKConnector connector;
    private TestConnectorConnectionListener testListener;

    @Before
    public void setUp() {
        connector = new ZKConnector(ZK_ADDRESS, 30000);
        testListener = new TestConnectorConnectionListener();
    }

    /**
     * 测试未启动状态
     */
    @Test
    public void case_unStarted() throws Exception {
        Assert.assertFalse(connector.isConnected());
        Assert.assertNull(connector.getZooKeeper());
    }

    /**
     * 测试事件发布主流程
     *
     * 测试步骤:
     *        1. 运行测试前启动zk服务
     *        2. 添加监听器(不会收到事件)
     *        3. 启动connector(监听到CONNECTED事件)
     *        4. 关闭zk(监听到DISCONNECTED事件)
     *        5. 在sessionTimeout之前重启zk(监听到RECONNECTED事件)
     *        6. 关闭zk(监听到DISCONNECTED事件)
     *        7. 在sessionTimeout之前未重启zk(监听到SESSION_CLOSED事件)
     *        8. 重启zk(监听到CONNECTED事件)
     */
    @Test
    public void case_mainFlow() throws Exception {
        connector.addListener(testListener);
        Thread.sleep(200);
        Assert.assertTrue(testListener.unReceivedEvent());

        connector.start();
        Thread.sleep(200);
        Assert.assertEquals(ConnectionEvent.CONNECTED, testListener.lastEvent());
        Assert.assertTrue(connector.isConnected());


    }
}
