package pers.zyc.tools.network;

import io.netty.channel.*;
import io.netty.util.AttributeKey;
import io.netty.util.internal.ConcurrentSet;
import pers.zyc.tools.utils.lifecycle.ThreadService;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author zhangyancheng
 */
public class NettyService extends ThreadService implements NetworkService {
	private static final AttributeKey<Queue<ResponsePromise>> RESPONSE_PROMISE_KEY =
			AttributeKey.newInstance("CHANNEL_RESPONSE_PROMISE");

	protected final NetworkConfig networkConfig;

	private final ConcurrentSet<Channel> requestedChannelSet = new ConcurrentSet<>();

	public NettyService(NetworkConfig networkConfig) {
		this.networkConfig = networkConfig;
	}

	@Override
	protected Runnable getRunnable() {
		return new ServiceRunnable() {

			@Override
			protected long getInterval() {
				return 1000;
			}

			@Override
			protected void execute() throws InterruptedException {
				for (Channel channel : requestedChannelSet) {
					Queue<ResponsePromise> responsePromiseQueue = channel.attr(RESPONSE_PROMISE_KEY).get();
					for (ResponsePromise promise : responsePromiseQueue) {
						if (promise.isTimeout()) {
							responsePromiseQueue.remove(promise);
						}
					}
				}
			}
		};
	}

	@Override
	public void oneWaySend(Channel channel, Request request) throws InterruptedException {
		oneWaySend(channel, request, networkConfig.getRequestTimeout());
	}

	@Override
	public void oneWaySend(final Channel channel, Request request, int requestTimeout) throws InterruptedException {
		checkRunning();

		if (request.getHeader().isNeedAck()) {
			throw new IllegalArgumentException("Request need ack");
		}

		final ResponsePromise responsePromise = new ResponsePromise(requestTimeout);
		channel.writeAndFlush(request).addListener(new ChannelFutureListener() {

			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				//单向请求发送后即响应
				responsePromise.response(future.isSuccess() ? null : future.cause());
			}
		});
		responsePromise.get();
	}

	@Override
	public Response syncSend(Channel channel, Request request) throws InterruptedException {
		return asyncSend(channel, request).get();
	}

	@Override
	public Response syncSend(Channel channel, Request request, int requestTimeout) throws InterruptedException {
		return asyncSend(channel, request, requestTimeout).get();
	}

	@Override
	public ResponseFuture asyncSend(Channel channel, Request request) {
		return asyncSend(channel, request, networkConfig.getRequestTimeout());
	}

	@Override
	public ResponseFuture asyncSend(final Channel channel, final Request request, int requestTimeout) {
		checkRunning();

		final ResponsePromise responsePromise = new ResponsePromise(requestTimeout);

		channel.writeAndFlush(request).addListener(new ChannelFutureListener() {

			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				if (future.isSuccess()) {
					requestedChannelSet.add(channel);
					getResponsePromiseQueue(channel).add(responsePromise);
				} else {
					responsePromise.response(future.cause());
				}
			}
		});

		return responsePromise;
	}

	private static Queue<ResponsePromise> getResponsePromiseQueue(Channel channel) {
		Queue<ResponsePromise> responsePromiseQueue = channel.attr(RESPONSE_PROMISE_KEY).get();
		if (responsePromiseQueue == null) {
			Queue<ResponsePromise> promiseQueue = new ConcurrentLinkedQueue<>();
			responsePromiseQueue = channel.attr(RESPONSE_PROMISE_KEY).setIfAbsent(promiseQueue);
			if (responsePromiseQueue == null) {
				responsePromiseQueue = promiseQueue;
			}
		}
		return responsePromiseQueue;
	}

	protected ChannelHandler[] getChannelHandlers() {
		return new ChannelHandler[]{};
	}

	protected abstract class PipelineAssembler extends ChannelInitializer<Channel> {

		@Override
		protected void initChannel(Channel channel) throws Exception {
			assemblePipeline(channel.pipeline());
		}

		protected abstract void assemblePipeline(ChannelPipeline pipeline);
	}
}
