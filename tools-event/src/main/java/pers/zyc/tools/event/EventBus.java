package pers.zyc.tools.event;

import pers.zyc.tools.lifecycle.PeriodicService;
import pers.zyc.tools.utils.Pair;
import pers.zyc.tools.utils.TimeMillis;

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
public class EventBus<E> extends PeriodicService implements Listenable<EventListener<E>> {
	/**
	 * 派发线程名
	 */
	private String name;
	/**
	 * 空闲时间(ms, 默认不检查空闲), 超过空闲时间没有事件触发onIdle
	 */
	private long idleTime;
	/**
	 * 事件合并周期(ms, 默认不进行合并), 周期内如果有多个事件, 只发布最后一个
	 */
	private long mergeInterval;
	/**
	 * 派发线程单次从阻塞队列中获取事件的超时时间(ms)
	 */
	private long internalPollTimeout = 1000;

	/**
	 * 上次事件事件, 用于计算是否空闲
	 */
	private long lastEventTime;
	/**
	 * 事件队列
	 */
	private BlockingQueue<Ownership> eventQueue = new LinkedBlockingDeque<>();
	
	/**
	 * 保存onEvent的方法引用, 用于出错处理
	 */
	private static final Method ON_EVENT_METHOD = EventListener.class.getMethods()[0];
	
	/**
	 * 事件广播器
	 */
	private final Multicaster<EventListener<E>> multicaster = new Multicaster<EventListener<E>>() {};

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
		lastEventTime = TimeMillis.get();
		super.doStart();
	}

	@Override
	protected void doStop() throws Exception {
		eventQueue.clear();
		multicaster.removeAllListeners();
		super.doStop();
	}

	@Override
	protected long getInterval() {
		return mergeInterval;
	}

	@Override
	protected void execute() throws InterruptedException {
		if (mergeInterval > 0) {
			/*
			 * 尝试取出所有事件, 如果没有取到需要检查是否空闲, 取到则合并发布事件
			 * 专有事件不合并仍按照顺序依次发布, 非专有事件则只发布最后一个(合并)
			 */
			MergedEvents mergedEvents = new MergedEvents();
            if (eventQueue.drainTo(mergedEvents) == 0) {
	            checkIdle();
            } else {
	            lastEventTime = TimeMillis.get();

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
				lastEventTime = TimeMillis.get();
				if (event.value() != null) {
					inform(event.key(), event.value());
				} else {
					multicaster.listeners.onEvent(event.key());
				}
			}
		}
	}

	/**
	 * 检查是否需要触发空闲
	 */
	protected void checkIdle() {
		if (idleTime > 0 && (TimeMillis.get() - lastEventTime) >= idleTime) {
			lastEventTime = TimeMillis.get();
			onIdle();
		}
	}

	/**
	 * 专有事件通知
	 * @param event 事件
	 * @param eventOwner 事件专有监听器
	 */
	protected void inform(final E event, final EventListener<E> eventOwner) {
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
	 * 空闲, 由子类制定逻辑
	 */
	protected void onIdle() {
	}

	/**
	 * 添加事件, 如果队列满则阻塞至队列空闲或者被中断
	 *
	 * @param event 待添加事件
	 * @throws InterruptedException 等待过程中被中断
     * @throws NullPointerException event为null时抛出
	 */
	public void add(E event) throws InterruptedException {
		add(event, null);
	}

	/**
	 * 添加事件, 如果队列满则阻塞至队列空闲或者被中断
	 *
	 * @param event 待添加事件
	 * @param owner 事件所属监听器, 如果为null表示事件通知给所有监听器,
	 *              否则表示当前事件为专有事件只发布给当前监听器
	 * @throws InterruptedException 等待过程中被中断
     * @throws NullPointerException event为null时抛出
	 */
	public void add(E event, EventListener<E> owner) throws InterruptedException {
		eventQueue.put(new Ownership(Objects.requireNonNull(event), owner));
	}
    
    /**
     * 添加事件, 如果当前队列满则返回添加失败
     *
     * @param event 待添加事件
     * @return 是否添加成功
     * @throws NullPointerException event为null时抛出
     */
    public boolean offer(E event) {
		return offer(event, null);
	}
    
    /**
     * 添加事件, 如果当前队列满则返回添加失败
     *
     * @param event 待添加事件
     * @param owner 事件专有监听器, 如果为null表示事件通知给所有监听器,
     *              否则表示当前事件为专有事件只发布给当前监听器
     * @return 是否添加成功
     * @throws NullPointerException event为null时抛出
     */
    public boolean offer(E event, EventListener<E> owner) {
		return eventQueue.offer(new Ownership(Objects.requireNonNull(event), owner));
	}
    
    /**
     * 清空事件队列
     */
    public void clearEvents() {
        eventQueue.clear();
    }

	/**
	 * 设置EventBus名称, 同时也是派发线程名
	 */
	public EventBus<E> name(String name) {
		this.name = name;
		return this;
	}

	/**
	 * 设置事件队列容量
	 * @param eventQueueCapacity 事件队列容量, 默认为无界队列
	 */
	public EventBus<E> eventQueueCapacity(int eventQueueCapacity) {
		if (eventQueueCapacity > 0) {
			this.eventQueue = new ArrayBlockingQueue<>(eventQueueCapacity);
		}
		return this;
	}

	/**
	 * 设置空闲时间, 空闲时间段内无事件触发空闲
	 * @param idleTime 空闲时间(ms), 大于0为有效值
	 */
	public EventBus<E> idleTime(long idleTime) {
		this.idleTime = idleTime;
		return this;
	}

	/**
	 * 设置事件合并间隔(ms), 间隔段内如果有多个事件默认只发布最后一个
	 * @param mergeInterval 事件合并间隔(ms), 大于0为有效值
	 */
	public EventBus<E> mergeInterval(long mergeInterval) {
		this.mergeInterval = mergeInterval;
		return this;
	}

	/**
	 * 设置派发线程单次从阻塞队列中获取事件的超时时间
	 * @param internalPollTimeout 派发线程单次从阻塞队列中获取事件的超时时间(ms), 默认为1000ms
	 */
	public EventBus<E> internalPollTimeout(long internalPollTimeout) {
		this.internalPollTimeout = internalPollTimeout;
		return this;
	}

	/**
	 * 设置事件广播执行器
	 * @param multicastExecutor 广播执行器, 默认为同步执行器
	 * @see Multicaster
	 */
	public EventBus<E> multicastExecutor(Executor multicastExecutor) {
		this.multicaster.setMulticastExecutor(multicastExecutor);
		return this;
	}

	/**
	 * 设置事件广播异常处理器
	 * @param multicastExceptionHandler 事件广播异常处理器, 默认不处理异常
	 */
	public EventBus<E> multicastExceptionHandler(MulticastExceptionHandler multicastExceptionHandler) {
		this.multicaster.setExceptionHandler(multicastExceptionHandler);
		return this;
	}

	/**
	 * 添加多个监听器
	 * @param listeners 监听器
	 */
	@SuppressWarnings("unchecked")
	public EventBus<E> addListeners(EventListener<E>... listeners) {
		for (EventListener<E> listener : listeners) {
			addListener(listener);
		}
		return this;
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
}
