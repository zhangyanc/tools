package pers.zyc.tools.event;

/**
 * @author zhangyancheng
 */
public interface EventListener<E> extends Listener, java.util.EventListener {

    void onEvent(E event);
}
