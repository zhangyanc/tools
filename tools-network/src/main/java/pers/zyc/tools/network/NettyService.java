package pers.zyc.tools.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AttributeKey;
import io.netty.util.internal.ConcurrentSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.zyc.tools.utils.GeneralThreadFactory;
import pers.zyc.tools.utils.event.*;
import pers.zyc.tools.utils.lifecycle.ThreadService;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * @author zhangyancheng
 */
class NettyService extends ThreadService implements NetworkService, EventSource<ChannelEvent> {
	/**
	 * Channel保存Promise Map键
	 */
	private static final AttributeKey<Map<Integer, ResponsePromise>> RESPONSE_PROMISE_KEY =
			AttributeKey.newInstance("CHANNEL_RESPONSE_PROMISE");

	/**
	 * 配置
	 */
	protected final NetworkConfig networkConfig;

	private final Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * 请求信号量，用于控制最大并发请求数，为null时表示不控制
	 */
	private final Semaphore requestPermits;

	/**
	 * 所有发送了请求的Channel集合
	 */
	private final ConcurrentSet<Channel> requestedChannelSet = new ConcurrentSet<>();

	/**
	 * Channel事件发布器
	 */
	private final Multicaster<EventListener<ChannelEvent>> channelEventMulticaster;

	/**
	 * 日志记录广播异常
	 */
	private final MulticastExceptionHandler multicastExceptionHandler = new MulticastExceptionHandler() {

		@Override
		public Void handleException(Throwable cause, MulticastDetail detail) {
			logger.error("Multicast error: " + detail, cause);
			return null;
		}
	};

	NettyService(final NetworkConfig networkConfig) {
		this.networkConfig = networkConfig;

		requestPermits = networkConfig.getMaxProcessingRequests() > 0 ?
				new Semaphore(networkConfig.getMaxProcessingRequests()) : null;

		channelEventMulticaster = new Multicaster<EventListener<ChannelEvent>>() {
			{
				setExceptionHandler(multicastExceptionHandler);
			}
		};

		//设置当前服务线程名
		setThreadFactory(new GeneralThreadFactory("TimeoutRequestClear"));
	}

	@Override
	protected Runnable getRunnable() {
		return new ServiceRunnable() {

			@Override
			protected long getInterval() {
				//超时请求检查间隔，超时清理的最大延迟为一个间隔
				return networkConfig.getRequestTimeoutDetectInterval();
			}

			@Override
			protected void execute() throws InterruptedException {
				for (Channel channel : requestedChannelSet) {
					Collection<ResponsePromise> promises = channel.attr(RESPONSE_PROMISE_KEY).get().values();
					for (ResponsePromise promise : promises) {
						if (promise.isTimeout()) {
							logger.info("Request: {} timeout, Channel: {}", promise.request, channel);
							promises.remove(promise);
						}
					}
					if (promises.isEmpty()) {
						requestedChannelSet.remove(channel);
					}
				}
			}
		};
	}

	@Override
	protected void doStop() throws Exception {
		//结束所有等待中的请求
		for (Channel channel : requestedChannelSet) {
			respondAllChannelPromise(channel, new RequestException("Service stopped!"));
		}
	}

	@Override
	public void addListener(EventListener<ChannelEvent> listener) {
		channelEventMulticaster.addListener(listener);
	}

	@Override
	public void removeListener(EventListener<ChannelEvent> listener) {
		channelEventMulticaster.removeListener(listener);
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

		final ResponsePromise responsePromise = acquirePromise(request, requestTimeout);
		channel.writeAndFlush(request).addListeners(new CommandSendFutureListener(request) {
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				super.operationComplete(future);
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

		final ResponsePromise responsePromise = acquirePromise(request, requestTimeout);

		channel.writeAndFlush(request).addListener(new CommandSendFutureListener(request) {
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				super.operationComplete(future);

				if (future.isSuccess()) {
					//请求发送成功（写入网络）后记录Promise，等待响应或者超时
					requestedChannelSet.add(channel);
					getChannelResponsePromiseMap(channel).put(request.getId(), responsePromise);
				} else {
					responsePromise.response(future.cause());
				}
			}
		});
		return responsePromise;
	}

	private static Map<Integer, ResponsePromise> getChannelResponsePromiseMap(Channel channel) {
		Map<Integer, ResponsePromise> responsePromiseMap = channel.attr(RESPONSE_PROMISE_KEY).get();
		if (responsePromiseMap == null) {
			Map<Integer, ResponsePromise> promiseMap = new ConcurrentHashMap<>();
			responsePromiseMap = channel.attr(RESPONSE_PROMISE_KEY).setIfAbsent(promiseMap);
			if (responsePromiseMap == null) {
				responsePromiseMap = promiseMap;
			}
		}
		return responsePromiseMap;
	}

	private ResponsePromise acquirePromise(Request request, int requestTimeout) {
		ResponsePromise responsePromise = new ResponsePromise(requestTimeout, request, requestPermits);

		responsePromise.multicaster.setExceptionHandler(multicastExceptionHandler);

		if (networkConfig.getResponseMulticastExecutor() != null) {
			responsePromise.multicaster.setMulticastExecutor(networkConfig.getResponseMulticastExecutor());
		}
		return responsePromise;
	}

	private void respondAllChannelPromise(Channel channel, Throwable cause) {
		Collection<ResponsePromise> promises = channel.attr(RESPONSE_PROMISE_KEY).get().values();
		for (ResponsePromise promise : promises) {
			promise.response(cause);
		}
		requestedChannelSet.remove(channel);
	}

	protected void requestHandleFailed(Channel channel, Request request, Throwable throwable) {
		logger.error(request + " handle failed, Channel: " + channel, throwable);
	}

	protected Heartbeat createHeartbeat() {
		return new Heartbeat(networkConfig.getHeartbeatCommandType());
	}

	private class CommandSendFutureListener implements ChannelFutureListener {

		final Command command;

		CommandSendFutureListener(Command command) {
			this.command = command;
		}

		@Override
		public void operationComplete(ChannelFuture future) throws Exception {
			command.writeComplete(future.isSuccess());
			if (!future.isSuccess()) {
				logger.error(command + " send failed, Channel: " + future.channel(), future.cause());
				future.channel().close();
			}
		}
	}

	protected class PipelineAssembler extends ChannelInitializer<Channel> {

		final CommandHandler commandHandler = new CommandHandler();
		final NettyService.ChannelStateHandler channelStateHandler = new ChannelStateHandler();

		@Override
		protected void initChannel(Channel channel) throws Exception {
			assemblePipeline(channel.pipeline());
		}

		protected void assemblePipeline(ChannelPipeline pipeline) {
			pipeline.addLast(
					new Encoder(), new Decoder(),
					new IdleStateHandler(networkConfig.getChannelReadTimeout(),
							networkConfig.getChannelWriteTimeout(), 0, TimeUnit.MILLISECONDS),
					channelStateHandler, commandHandler
			);
		}
	}

	@ChannelHandler.Sharable
	protected class CommandHandler extends SimpleChannelInboundHandler<Command> {

		@Override
		protected void channelRead0(ChannelHandlerContext ctx, Command command) throws Exception {
			final Channel channel = ctx.channel();
			switch (command.getHeader().getType()) {
				case Header.REQUEST:
					if (command.getType() != networkConfig.getHeartbeatCommandType()) {
						return;
					}

					final Request request = (Request) command;
					final RequestHandler requestHandler = networkConfig.getRequestHandlerFactory()
							.getHandler(request.getType());

					requestHandler.getExecutor().execute(new Runnable() {
						@Override
						public void run() {
							try {
								final Response response = requestHandler.handle(request);

								if (!request.getHeader().isNeedAck() || response == null) {
									return;
								}

								channel.writeAndFlush(response).addListener(new CommandSendFutureListener(response));
							} catch (Throwable throwable) {
								requestHandleFailed(channel, request, throwable);
							}
						}
					});
					break;
				case Header.RESPONSE:
					Response response = (Response) command;

					Map<Integer, ResponsePromise> responsePromiseMap = channel.attr(RESPONSE_PROMISE_KEY).get();
					ResponsePromise responsePromise = responsePromiseMap.remove(response.getRequestId());

					if (responsePromiseMap.isEmpty()) {
						requestedChannelSet.remove(channel);
					}
					if (responsePromise != null) {
						responsePromise.response(response);
					}
					break;
				default: throw new RuntimeException("UnKnown type: " + command.getHeader().getType());
			}
		}
	}

	@ChannelHandler.Sharable
	protected class ChannelStateHandler extends ChannelInboundHandlerAdapter {

		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			super.channelActive(ctx);
			logger.info("Channel active, Channel: {}", ctx.channel());
			channelEventMulticaster.listeners.onEvent(new ChannelEvent(NettyService.this, ctx.channel(),
					ChannelEvent.EventType.CONNECT));
		}

		@Override
		public void channelInactive(ChannelHandlerContext ctx) throws Exception {
			super.channelInactive(ctx);
			logger.info("Channel inactive, Channel: {}", ctx.channel());
			channelEventMulticaster.listeners.onEvent(new ChannelEvent(NettyService.this, ctx.channel(),
					ChannelEvent.EventType.CLOSE));

			respondAllChannelPromise(ctx.channel(), new RequestException("Channel inactive"));
		}

		@Override
		public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
			super.userEventTriggered(ctx, evt);
			logger.info("User event triggered, Channel: {}, Event: {}", ctx.channel(), evt);

			if (evt == IdleStateEvent.READER_IDLE_STATE_EVENT) {
				channelEventMulticaster.listeners.onEvent(new ChannelEvent(NettyService.this, ctx.channel(),
						ChannelEvent.EventType.READ_IDLE));

				logger.info("On read idle event, Close Channel");
				ctx.channel().close();
			} else if (evt == IdleStateEvent.WRITER_IDLE_STATE_EVENT) {
				channelEventMulticaster.listeners.onEvent(new ChannelEvent(NettyService.this, ctx.channel(),
						ChannelEvent.EventType.WRITE_IDLE));

				logger.info("On write idle event, Send heartbeat");
				Heartbeat heartbeat = createHeartbeat();
				ctx.channel().writeAndFlush(heartbeat).addListener(new CommandSendFutureListener(heartbeat));
			}
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
			super.exceptionCaught(ctx, cause);
			logger.error(ctx.channel() + " exception caught", cause);

			channelEventMulticaster.listeners.onEvent(new ChannelEvent(NettyService.this, ctx.channel(),
					ChannelEvent.EventType.EXCEPTION));

			logger.info("On exception caught, Close Channel");
			ctx.channel().close();
		}
	}

	protected class Encoder extends MessageToByteEncoder<Command> {

		@Override
		protected void encode(ChannelHandlerContext ctx, Command command,
							  ByteBuf out) throws Exception {
			command.encode(out);
		}

		@Override
		protected ByteBuf allocateBuffer(ChannelHandlerContext ctx, Command command,
										 boolean preferDirect) throws Exception {
			return networkConfig.getBufAllocator() == null ?
					super.allocateBuffer(ctx, command, preferDirect) :
					networkConfig.getBufAllocator().allocate(ctx, command, preferDirect);
		}
	}

	protected class Decoder extends LengthFieldBasedFrameDecoder {

		Decoder() {
			super(Math.min(networkConfig.getMaxFrameLength(), NetworkConfig.MAX_FRAME_LENGTH), 0, 3, -3, 3);
		}

		@Override
		protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
			ByteBuf frame = (ByteBuf) super.decode(ctx, in);
			if (frame == null) {
				//未完整包
				return null;
			}
			Header header = new Header();
			header.decode(frame);

			Command command = networkConfig.getCommandFactory().createByType(header.getCommandType());
			command.decode(frame);

			return command;
		}

		@Override
		protected ByteBuf extractFrame(ChannelHandlerContext ctx, ByteBuf buffer, int index, int length) {
			//只读一次, 无需retain
			return buffer.slice(index, length);
		}
	}
}
