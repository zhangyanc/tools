package pers.zyc.tools.zkclient;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.zyc.tools.event.*;
import pers.zyc.tools.zkclient.event.ConnectionEvent;
import pers.zyc.tools.zkclient.event.PathEvent;
import pers.zyc.tools.zkclient.listener.ConnectionListener;

/**
 * @author zhangyancheng
 */
public abstract class AbstractZKEventConverter<E extends PathEvent> implements EventSource<E>, ConnectionListener {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private LogMulticastExceptionHandler exceptionHandler = new LogMulticastExceptionHandler(logger);

    private String path;
    private EventMulticaster eventMulticaster = new SyncEventMulticaster(exceptionHandler);
    private EventManager<WatchedEvent> eventManager = new EventManager.Builder()
            .multicastExceptionHandler(exceptionHandler).build();

    protected final PathEventWatcher pathEventWatcher = new PathEventWatcher();

    public AbstractZKEventConverter(String path) {
        this.path = path;
    }

    @Override
    public void addListener(EventListener<E> listener) {
        if (eventMulticaster.addListener(listener)) {

        }
    }

    @Override
    public void removeListener(EventListener<E> listener) {
        eventMulticaster.removeListener(listener);
    }

    @Override
    public void onEvent(ConnectionEvent event) {

    }

    private class PathEventProcessor implements EventListener<WatchedEvent> {

        @Override
        public void onEvent(WatchedEvent event) {

        }
    }

    private class PathEventWatcher implements Watcher {

        @Override
        public void process(WatchedEvent event) {
            eventManager.add(event);
        }
    }
}
