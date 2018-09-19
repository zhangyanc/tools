package pers.zyc.tools.network;

import io.netty.channel.Channel;
import pers.zyc.tools.utils.lifecycle.Lifecycle;

/**
 * @author zhangyancheng
 */
public interface NetworkService extends Lifecycle {

	void oneWaySend(Channel channel, Request request) throws InterruptedException, RequestException;

	void oneWaySend(Channel channel, Request request, int requestTimeout) throws InterruptedException, RequestException;

	Response syncSend(Channel channel, Request request) throws InterruptedException, RequestException;

	Response syncSend(Channel channel, Request request, int requestTimeout) throws InterruptedException, RequestException;

	ResponseFuture asyncSend(Channel channel, Request request);

	ResponseFuture asyncSend(Channel channel, Request request, int requestTimeout);
}
