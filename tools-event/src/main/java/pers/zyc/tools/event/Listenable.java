package pers.zyc.tools.event;

/**
 * @author zhangyancheng
 */
public interface Listenable<L extends Listener> {

    void addListener(L listener);

    void removeListener(L listener);
}
