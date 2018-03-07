package pers.zyc.tools.event;

/**
 * @author zhangyancheng
 */
public interface EventHandler<E> extends EventListener {

    void handleEvent(E event);
}
