package pers.zyc.tools.utils.event;

import pers.zyc.tools.utils.Pair;
import pers.zyc.tools.utils.TimeMillis;
import pers.zyc.tools.utils.lifecycle.ServiceException;
import pers.zyc.tools.utils.lifecycle.ThreadService;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.*;

/**
 * 事件异步派发
 *
 * @param <E> 事件泛型
 *
 * @author zhangyancheng
 */
public class EventBus<E> extends ThreadService implements Listenable<EventListener<E>> {

	private final String name;
	private final long mergeInterval;
	private final long internalPollTimeout;
	private final BlockingQueue<Ownership> eventQueue;
	private final EventBusIdleCallback<E> idleCallback;
	
	/**
	 * 保存onEvent的方法引用, 用于出错处理
	 */
	private static final Method ON_EVENT_METHOD = EventListener.class.getMethods()[0];
	
	/**
	 * 事件广播器
	 */
	private final Multicaster<EventListener<E>> multicaster = new Multicaster<EventListener<E>>() {};

	/**
	 * 上次事件事件, 用于计算是否空闲
	 */
	private long lastEventTime;

	private EventBus(String name,
					long mergeInterval,
					int eventQueueCapacity,
					long internalPollTimeout,
					EventBusIdleCallback<E> idleCallback) {
		this.name = name;
		this.mergeInterval = mergeInterval;
		this.internalPollTimeout = internalPollTimeout;
		this.idleCallback = idleCallback;
		this.eventQueue = new LinkedBlockingQueue<>(eventQueueCapacity);
	}

	@Override
	public String getName() {
		return name != null ? name : super.getName();
	}

	@Override
	public void addListener(EventListener<E> listener) {
		multicaster.addListener(listener);
	}

	@Override
	public void removeListener(EventListener<E> listener) {
		multicaster.removeListener(listener);
	}

	@Override
	protected void doStart() {
		lastEventTime = TimeMillis.INSTANCE.get();
	}

	@Override
	protected void doStop() throws Exception {
		eventQueue.clear();
		multicaster.removeAllListeners();
		super.doStop();
	}

	@Override
	protected ServiceRunnable getRunnable() {
		return new ServiceRunnable() {

			@Override
			protected long getInterval() {
				return mergeInterval;
			}

			@Override
			protected void execute() throws InterruptedException {
				doDispatch();
			}
		};
	}

	/**
	 * 事件派发
	 *
	 * @throws InterruptedException 线程中断
	 */
	private void doDispatch() throws InterruptedException {
		if (mergeInterval > 0) {
			/*
			 * 尝试取出所有事件, 如果没有取到需要检查是否空闲, 取到则合并发布事件
			 * 专有事件不合并仍按照顺序依次发布, 非专有事件则只发布最后一个(合并)
			 */
			MergedEvents mergedEvents = new MergedEvents();
			if (eventQueue.drainTo(mergedEvents) == 0) {
				checkIdle();
			} else {
				lastEventTime = TimeMillis.INSTANCE.get();

				List<Ownership> events = mergedEvents.events;
				for (int i = 0; i < events.size(); i++) {
					Ownership event = events.get(i);
					//当前是专有事件则发布, 否则比较是否最后一个非专有事件, 如果是则发布
					if (event.value() != null) {
						inform(event.key(), event.value());
					} else if (mergedEvents.mergedEventIndex == i) {
						multicaster.listeners.onEvent(event.key());
					}
				}
			}
		} else {
			Ownership event = eventQueue.poll(internalPollTimeout, TimeUnit.MILLISECONDS);

			if (event == null) {
				checkIdle();
			} else {
				lastEventTime = TimeMillis.INSTANCE.get();
				if (event.value() != null) {
					inform(event.key(), event.value());
				} else {
					multicaster.listeners.onEvent(event.key());
				}
			}
		}
	}

	/**
	 * 检查是否需要触发空闲回调
	 */
	private void checkIdle() {
		if (idleCallback != null) {
			if ((TimeMillis.INSTANCE.get() - lastEventTime) >= idleCallback.getIdleTimeMillis()) {
				idleCallback.onIdle(this);
			}
			lastEventTime = TimeMillis.INSTANCE.get();
		}
	}

	/**
	 * 专有事件通知
	 *
	 * @param event 事件
	 * @param eventOwner 事件专有监听器
	 */
	private void inform(final E event, final EventListener<E> eventOwner) {
		multicaster.getMulticastExecutor().execute(new Runnable() {
			@Override
			public void run() {
				try {
					eventOwner.onEvent(event);
				} catch (Throwable throwable) {
					if (multicaster.getExceptionHandler() != null) {
						try {
							multicaster.getExceptionHandler().handleException(throwable,
									new MulticastDetail(eventOwner, ON_EVENT_METHOD, new Object[]{ event }));
						} catch (Throwable ignored) {
						}
					}
				}
			}
		});
    }

	/**
	 * 添加事件, 如果队列满则阻塞至队列有空余或者被中断
	 *
	 * @param event 待添加事件
	 * @throws InterruptedException 等待过程中被中断
     * @throws NullPointerException event为null
	 * @throws ServiceException.NotRunningException 未启动
	 */
	public void add(E event) throws InterruptedException {
		add(event, null);
	}

	/**
	 * 添加事件, 如果队列满则阻塞至队列空余或者被中断
	 *
	 * @param event 待添加事件
	 * @param owner 事件所属监听器, 如果为null表示事件通知给所有监听器,
	 *              否则表示当前事件为专有事件只发布给当前监听器
	 * @throws InterruptedException 等待过程中被中断
     * @throws NullPointerException event为null时
	 * @throws ServiceException.NotRunningException 未启动
	 */
	public void add(E event, EventListener<E> owner) throws InterruptedException {
		checkRunning();
		eventQueue.put(new Ownership(Objects.requireNonNull(event), owner));
	}
    
    /**
     * 添加事件, 如果当前队列满则返回添加失败
     *
     * @param event 待添加事件
     * @return 是否添加成功
     * @throws NullPointerException event为null
	 * @throws ServiceException.NotRunningException 未启动
     */
    public boolean offer(E event) {
		return offer(event, null);
	}

	/**
	 * 添加事件, 如果队列满则阻塞至队列空余直到超时或者被中断
	 *
	 * @param event 待添加事件
	 * @param timeout 超时毫秒
	 * @return 是否添加成功
	 * @throws InterruptedException 等待过程中被中断
	 * @throws NullPointerException event为null
	 * @throws ServiceException.NotRunningException 未启动
	 */
	public boolean offer(E event, long timeout) throws InterruptedException {
		return offer(event, timeout, null);
	}
    
    /**
     * 添加事件, 如果当前队列满则返回添加失败
     *
     * @param event 待添加事件
     * @param owner 事件专有监听器, 如果为null表示事件通知给所有监听器,
     *              否则表示当前事件为专有事件只发布给当前监听器
     * @return 是否添加成功
     * @throws NullPointerException event为null
	 * @throws ServiceException.NotRunningException 未启动
     */
    public boolean offer(E event, EventListener<E> owner) {
    	checkRunning();
		return eventQueue.offer(new Ownership(Objects.requireNonNull(event), owner));
	}

	/**
	 * 添加事件, 如果队列满则阻塞至队列空余直到超时或者被中断
	 *
	 * @param event 待添加事件
	 * @param timeout 超时毫秒
	 * @param owner 事件专有监听器, 如果为null表示事件通知给所有监听器,
	 *              否则表示当前事件为专有事件只发布给当前监听器
	 * @return 是否添加成功
	 * @throws InterruptedException
	 * @throws NullPointerException event为null
	 * @throws ServiceException.NotRunningException 未启动
	 */
	public boolean offer(E event, long timeout, EventListener<E> owner) throws InterruptedException {
		checkRunning();
		return eventQueue.offer(new Ownership(Objects.requireNonNull(event), owner), timeout, TimeUnit.MILLISECONDS);
	}
    
    /**
     * 清空事件队列
     */
    public void clearEvents() {
        eventQueue.clear();
    }

	/**
	 * 事件-监听器元组
	 */
	private class Ownership extends Pair<E, EventListener<E>> {
		Ownership(E event, EventListener<E> listener) {
			key(event);
			value(listener);
		}
	}
	
	private class MergedEvents extends AbstractCollection<Ownership> {
		//最后一个非专有事件位置
        int mergedEventIndex;
        List<Ownership> events = new ArrayList<>();
        
        @Override
        public boolean add(Ownership ownership) {
            if (ownership.value() == null) {
            	//记录最后一个非专有事件位置
                mergedEventIndex = events.size();
            }
            events.add(ownership);
            return true;
        }
        
        @Override
        public Iterator<Ownership> iterator() {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public int size() {
            throw new UnsupportedOperationException();
        }
    }

    public static class Builder<E> {
		private boolean autoStart;
		private String name;
		private int eventQueueCapacity = Integer.MAX_VALUE;
		private EventBusIdleCallback<E> idleCallback;
		private long mergeInterval;
		private long internalPollTimeout = 1000;
		private Executor multicastExecutor;
		private MulticastExceptionHandler multicastExceptionHandler;

		/**
		 * 构建事件总线，构建后总线配置属性将无法再被修改
		 *
		 * @return 事件总线
		 */
		public EventBus<E> build() {
			EventBus<E> eventBus = new EventBus<>(name, mergeInterval, eventQueueCapacity,
					internalPollTimeout, idleCallback);
			if (multicastExecutor != null) {
				eventBus.multicaster.setMulticastExecutor(multicastExecutor);
			}
			if (multicastExceptionHandler != null) {
				eventBus.multicaster.setExceptionHandler(multicastExceptionHandler);
			}
			if (autoStart) {
				eventBus.start();
			}
			return eventBus;
		}

		/**
		 * 设置构建后自动启动事件总线
		 */
		public Builder<E> autoStart() {
			this.autoStart = true;
			return this;
		}

		/**
		 * 设置EventBus名称, 同时也是派发线程名
		 */
		public Builder<E> name(String name) {
			this.name = name;
			return this;
		}

		/**
		 * 设置事件队列容量
		 *
		 * @param eventQueueCapacity 事件队列容量, 默认为无界队列
		 */
		public Builder<E> eventQueueCapacity(int eventQueueCapacity) {
			this.eventQueueCapacity = eventQueueCapacity;
			return this;
		}

		/**
		 * 设置空闲回调
		 *
		 * @param idleCallback 空闲回调
		 */
		public Builder<E> idleCallback(EventBusIdleCallback<E> idleCallback) {
			this.idleCallback = idleCallback;
			return this;
		}

		/**
		 * 设置事件合并间隔(ms), 间隔段内如果有多个事件默认只发布最后一个（默认不合并）
		 *
		 * @param mergeInterval 事件合并间隔(ms), 大于0为有效值
		 */
		public Builder<E> mergeInterval(long mergeInterval) {
			this.mergeInterval = mergeInterval;
			return this;
		}

		/**
		 * 设置派发线程单次从阻塞队列中获取事件的超时时间
		 *
		 * @param internalPollTimeout 派发线程单次从阻塞队列中获取事件的超时时间(ms), 默认为1000ms
		 */
		public Builder<E> internalPollTimeout(long internalPollTimeout) {
			this.internalPollTimeout = internalPollTimeout;
			return this;
		}

		/**
		 * 设置事件广播执行器
		 *
		 * @param multicastExecutor 广播执行器, 默认为同步执行器，即在派发线程中执行事件广播
		 * @see Multicaster
		 */
		public Builder<E> multicastExecutor(Executor multicastExecutor) {
			this.multicastExecutor = multicastExecutor;
			return this;
		}

		/**
		 * 设置事件广播异常处理器
		 *
		 * @param multicastExceptionHandler 事件广播异常处理器, 默认不处理异常
		 */
		public Builder<E> multicastExceptionHandler(MulticastExceptionHandler multicastExceptionHandler) {
			this.multicastExceptionHandler = multicastExceptionHandler;
			return this;
		}
	}
}
