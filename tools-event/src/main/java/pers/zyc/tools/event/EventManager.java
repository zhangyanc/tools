package pers.zyc.tools.event;

import pers.zyc.tools.lifecycle.PeriodicService;

import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author zhangyancheng
 */
public class EventManager<E> extends PeriodicService implements EventSource<E> {
    private String name;
    private ListenerInvoker invoker;
    private BlockingQueue<E> eventQueue = null;
    private Set<EventListener> listeners = new CopyOnWriteArraySet<>();

    @Override
    public String getName() {
        return name;
    }

    @Override
    protected long period() {
        return 0;
    }

    @Override
    protected void execute() throws InterruptedException {

    }

    @Override
    public void addListener(EventListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(EventListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void setListenerInvoker(ListenerInvoker listenerInvoker) {

    }

    public static class Builder {
        private String name;

    }
}
