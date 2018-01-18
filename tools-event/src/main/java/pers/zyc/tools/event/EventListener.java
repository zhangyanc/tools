package pers.zyc.tools.event;

/**
 * @author zhangyancheng
 */
public interface EventListener<E> extends Listener {

    void onEvent(E event);
}
