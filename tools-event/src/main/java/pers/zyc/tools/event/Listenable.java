package pers.zyc.tools.event;

/**
 * @author zhangyancheng
 */
public interface Listenable<L> {

    void addListener(L listener);

    void removeListener(L listener);
}
