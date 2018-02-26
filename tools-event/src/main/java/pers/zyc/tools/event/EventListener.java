package pers.zyc.tools.event;

/**
 * @author zhangyancheng
 */
public interface EventListener<E extends Event> extends java.util.EventListener, Listener {

    void onEvent(E event);
}
