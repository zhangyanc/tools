package pers.zyc.tools.event;

import pers.zyc.tools.lifecycle.PeriodicService;
import pers.zyc.tools.utils.Pair;
import pers.zyc.tools.utils.TimeMillis;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.*;

/**
 * 事件管理器
 *
 * @author zhangyancheng
 */
public class EventManager<E> extends PeriodicService implements EventSource<E> {
    private static final ListenerInvoker DEFAULT_INVOKER = new SerialInvoker();

    private String name;
    private long idleTime;
    private long mergeInterval;
    private long lastEventTime;
    private IdleCallback idleCallback;
    private ListenerInvoker listenerInvoker = DEFAULT_INVOKER;
    private Set<EventListener<E>> listeners = new CopyOnWriteArraySet<>();
    private BlockingQueue<Ownership> eventQueue = new LinkedBlockingDeque<>();

    private EventManager(String name,
                         long idleTime,
                         long mergeInterval,
                         IdleCallback idleCallback) {

        this.name = name;
        this.idleTime = idleTime;
        this.mergeInterval = mergeInterval;
        this.idleCallback = idleCallback;
    }

    @Override
    public String getName() {
        return name != null ? name : super.getName();
    }

    @Override
    protected long period() {
        return mergeInterval;
    }

    @Override
    protected void afterStart() {
        super.afterStart();
        lastEventTime = TimeMillis.get();
    }

    private class Ownership extends Pair<E, EventListener<E>> {
    }

    @Override
    protected void execute() throws InterruptedException {
        Ownership event = null;

        if (mergeInterval > 0) {
            int size = eventQueue.size();
            while (size-- > 0) {
                event = eventQueue.poll();
            }
        } else {
            event = eventQueue.poll(200, TimeUnit.MILLISECONDS);
        }

        if (event != null) {
            lastEventTime = TimeMillis.get();
            EventListener<E> eventOwner = event.value();
            if (eventOwner == null) {
                listenerInvoker.invoke(event.key(), listeners);
            } else {
                listenerInvoker.invoke(event.key(), eventOwner);
            }
        } else if (idleTime > 0 && idleCallback != null &&
                (TimeMillis.get() - lastEventTime) >= idleTime) {

            lastEventTime = TimeMillis.get();
            idleCallback.onIdle();
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

    /**
     * 添加事件
     *
     * @param event 事件
     */
    public void add(E event) {
        add(event, null);
    }

    /**
     * 添加事件
     * @param event 事件
     * @param owner 事件所属监听器, 如果为null表示事件通知给所有监听器
     */
    public void add(E event, EventListener<E> owner) {
        Objects.requireNonNull(event);

        Ownership ownership = new Ownership();
        ownership.key(event);
        ownership.value(owner);

        eventQueue.offer(ownership);
    }

    public interface IdleCallback {
        void onIdle();
    }

    public static class Builder {
        private String name;
        private long idleTime;
        private long mergeInterval;
        private IdleCallback idleCallback;

        /**
         * @param name 名称
         */
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * 设置空闲回调
         *
         * @param idleTime 空闲时间
         * @param idleCallback 空闲回调
         */
        public Builder idle(long idleTime, IdleCallback idleCallback) {
            this.idleTime = idleTime;
            this.idleCallback = idleCallback;
            return this;
        }

        /**
         * @param mergeInterval 事件合并时间
         */
        public Builder merge(long mergeInterval) {
            this.mergeInterval = mergeInterval;
            return this;
        }

        /**
         * 创建事件管理器
         */
        public <E> EventManager<E> build() {
            return new EventManager<>(name, idleTime, mergeInterval, idleCallback);
        }
    }
}
