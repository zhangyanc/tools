package pers.zyc.tools.event;

import pers.zyc.tools.lifecycle.PeriodicService;
import pers.zyc.tools.utils.Pair;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * 事件管理器
 *
 * @author zhangyancheng
 */
public class EventManager<E> extends PeriodicService implements EventSource<E> {
    private String name;
    private long idleTime;
    private long mergeInterval;
    private ListenerInvoker listenerInvoker;
    private BlockingQueue<Ownership> eventQueue = new LinkedBlockingDeque<>();
    private Set<EventListener<E>> listeners = new CopyOnWriteArraySet<>();
    private long lastEventTime;

    @Override
    public String getName() {
        return name;
    }

    @Override
    protected long period() {
        return mergeInterval;
    }

    @Override
    protected void execute() throws InterruptedException {
        if (mergeInterval > 0) {
            if (eventQueue.isEmpty()) {
                if (idleTime > 0) {

                }

                return;
            }
            int size = eventQueue.size();
            Ownership event;
            while (size-- > 0) {
                event = eventQueue.poll();
            }
        } else {

        }
    }

    private void publish(Ownership event) {
        EventListener<E> eventOwner = event.value();
        if (eventOwner == null) {
            listenerInvoker.invoke(event.key(), listeners);
        } else {
            listenerInvoker.invoke(event.key(), eventOwner);
        }
    }

    @Override
    public void addListener(EventListener<E> listener) {
        listeners.add(Objects.requireNonNull(listener));
    }

    @Override
    public void removeListener(EventListener<E> listener) {
        listeners.remove(Objects.requireNonNull(listener));
    }

    @Override
    public void setListenerInvoker(ListenerInvoker listenerInvoker) {
        this.listenerInvoker = Objects.requireNonNull(listenerInvoker);
    }

    public void add(E event) {
        add(event, null);
    }

    public void add(E event, EventListener<E> owner) {
        Objects.requireNonNull(event);

        Ownership ownership = new Ownership();
        ownership.key(event);
        ownership.value(owner);

        eventQueue.offer(ownership);
    }

    private class Ownership extends Pair<E, EventListener<E>> {
    }

    public static class Builder {
        private String name;

    }
}
