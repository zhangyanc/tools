package pers.zyc.tools.network;

import pers.zyc.tools.utils.event.Listener;

/**
 * @author zhangyancheng
 */
public interface ResponseFutureListener extends Listener {

	void responseReceived(Request request, Response response);

	void exceptionCaught(Request request, NetworkException re);
}
