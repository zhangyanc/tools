package pers.zyc.tools.event;

/**
 * @author zhangyancheng
 */
public interface EventListener<E extends Event> extends Listener {

    void onEvent(E event);
}
