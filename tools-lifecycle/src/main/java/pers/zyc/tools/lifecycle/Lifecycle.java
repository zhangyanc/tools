package pers.zyc.tools.lifecycle;

/**
 * @author zhangyancheng
 */
public interface Lifecycle {

    void start();

    void stop();

    boolean isRunning();
}
