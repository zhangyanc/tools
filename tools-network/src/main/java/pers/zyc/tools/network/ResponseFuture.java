package pers.zyc.tools.network;

/**
 * @author zhangyancheng
 */
public interface ResponseFuture {

	boolean isDown();

	Response get() throws InterruptedException;
}
