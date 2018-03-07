package pers.zyc.tools.event;

import pers.zyc.tools.lifecycle.PeriodicService;
import pers.zyc.tools.utils.Pair;
import pers.zyc.tools.utils.TimeMillis;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

/**
 * 事件管理器
 *
 * @author zhangyancheng
 */
public final class EventBus<E> extends PeriodicService implements Listenable<EventHandler<E>> {
    private String name;
    private long idleTime;
    private long mergeInterval;
    private long lastEventTime;
    private IdleCallback idleCallback;
    private Set<EventHandler<E>> eventHandlers = new CopyOnWriteArraySet<>();
    private BlockingQueue<Ownership> eventQueue = new LinkedBlockingDeque<>();

    private EventBus(String name,
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
    protected void afterStart() {
        super.afterStart();
        lastEventTime = TimeMillis.get();
    }

    @Override
    protected void doStop() throws Exception {
        super.doStop();
        eventQueue.clear();
    }

    @Override
    protected long period() {
        return mergeInterval;
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
            EventHandler<E> eventOwner = event.value();
            E realEvent = event.key();
            if (eventOwner != null) {
                inform(eventOwner, realEvent);
            } else {
                for (EventHandler<E> eventHandler : eventHandlers) {
                    inform(eventHandler, realEvent);
                }
            }
        } else if (idleTime > 0 && idleCallback != null &&
                (TimeMillis.get() - lastEventTime) >= idleTime) {

            lastEventTime = TimeMillis.get();
            idleCallback.onIdle();
        }
    }

    private void inform(EventHandler<E> eventHandler, E event) {
        try {
            eventHandler.handleEvent(event);
        } catch (Exception ignored) {
        }
    }

    @Override
    public void addListener(EventHandler<E> listener) {
        eventHandlers.add(Objects.requireNonNull(listener));
    }

    @Override
    public void removeListener(EventHandler<E> listener) {
        eventHandlers.remove(listener);
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
     *
     * @param event 事件
     * @param owner 事件所属监听器, 如果为null表示事件通知给所有监听器
     */
    public void add(E event, EventHandler<E> owner) {
        Objects.requireNonNull(event);

        Ownership ownership = new Ownership();
        ownership.key(event);
        ownership.value(owner);

        eventQueue.offer(ownership);
    }

    private class Ownership extends Pair<E, EventHandler<E>> {
    }

    public interface IdleCallback {
        void onIdle();
    }

    public static class Builder {
        private String name;//事件管理器名
        private long idleTime;
        private long mergeInterval;
        private IdleCallback idleCallback;

        /**
         * 设置事件管理器名称
         */
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * 连续无事件表示空闲, 空闲事件超时触发空闲回调
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
         * 设置事件合并间隔, 间隔段内的事件将只发布最后一个
         */
        public Builder mergeInterval(long mergeInterval) {
            this.mergeInterval = mergeInterval;
            return this;
        }

        /**
         * 创建事件管理器
         */
        public <E> EventBus<E> build() {
            return new EventBus<>(name, idleTime, mergeInterval, idleCallback);
        }
    }
}
