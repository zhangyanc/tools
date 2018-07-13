package pers.zyc.tools.utils.lifecycle;

/**
 * @author zhangyancheng
 */
public interface Lifecycle {

    void start();

    void stop();

    boolean isRunning();
}
