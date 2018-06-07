package pers.zyc.tools.redis.client;

import pers.zyc.tools.event.Listener;

/**
 * @author zhangyancheng
 */
public interface ResponseListener extends Listener {

	void onResponseReceived(Object response);

	void onSocketException(Exception e);
}
