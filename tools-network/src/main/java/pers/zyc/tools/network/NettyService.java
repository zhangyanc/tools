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
import pers.zyc.tools.utils.event.*;
import pers.zyc.tools.utils.lifecycle.ThreadService;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhangyancheng
 */
public class NettyService extends ThreadService implements NetworkService, EventSource<ChannelEvent> {
	private static final AttributeKey<Map<Integer, ResponsePromise>> RESPONSE_PROMISE_KEY =
			AttributeKey.newInstance("CHANNEL_RESPONSE_PROMISE");

	protected final NetworkConfig networkConfig;

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final CommandProcessor commandProcessor = new CommandProcessor();

	private final ConcurrentSet<Channel> requestedChannelSet = new ConcurrentSet<>();

	private final Multicaster<EventListener<ChannelEvent>> channelEventMulticaster;

	public NettyService(NetworkConfig networkConfig) {
		this.networkConfig = networkConfig;

		channelEventMulticaster = new Multicaster<EventListener<ChannelEvent>>() {
			{
				setExceptionHandler(new MulticastExceptionHandler() {

					@Override
					public Void handleException(Throwable cause, MulticastDetail detail) {
						logger.error("{} multicast error, {}: {}", detail.args[0], detail.listener, cause.getMessage());
						return null;
					}
				});
			}
		};
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
					Collection<ResponsePromise> responsePromises = channel.attr(RESPONSE_PROMISE_KEY).get().values();
					for (ResponsePromise promise : responsePromises) {
						if (promise.isTimeout()) {
							responsePromises.remove(promise);
						}
					}
				}
			}
		};
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
					getResponsePromiseQueue(channel).put(request.getId(), responsePromise);
				} else {
					responsePromise.response(future.cause());
				}
			}
		});

		return responsePromise;
	}

	private static Map<Integer, ResponsePromise> getResponsePromiseQueue(Channel channel) {
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

	protected ChannelHandler[] getChannelHandlers() {
		return new ChannelHandler[]{
				new Encoder(),
				new Decoder(),
				new IdleStateHandler(networkConfig.getChannelReadTimeout(), networkConfig.getChannelWriteTimeout(), 0),
				commandProcessor
		};
	}

	protected abstract class PipelineAssembler extends ChannelInitializer<Channel> {

		@Override
		protected void initChannel(Channel channel) throws Exception {
			assemblePipeline(channel.pipeline());
		}

		protected abstract void assemblePipeline(ChannelPipeline pipeline);
	}

	@ChannelHandler.Sharable
	protected class CommandProcessor extends SimpleChannelInboundHandler<Command> {

		@Override
		protected void channelRead0(ChannelHandlerContext ctx, Command command) throws Exception {
			final Channel channel = ctx.channel();
			switch (command.getHeader().getType()) {
				case Header.REQUEST:
					if (command.getType() != CommandFactory.HEARTBEAT) {
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
								channel.writeAndFlush(response).addListener(new ChannelFutureListener() {

									@Override
									public void operationComplete(ChannelFuture future) throws Exception {
										response.writeComplete(future.isSuccess());
									}
								});
							} catch (Throwable throwable) {
								channel.writeAndFlush(createErrorResponse(throwable));
							}
						}
					});
					break;
				case Header.RESPONSE:
					Response response = (Response) command;

					ResponsePromise responsePromise = channel.attr(RESPONSE_PROMISE_KEY).get()
							.remove(response.getRequestId());

					if (responsePromise != null) {
						responsePromise.response(response);
					}
					break;
				default: throw new RuntimeException("UnKnown type: " + command.getHeader().getType());
			}
		}

		Response createErrorResponse(Throwable error) {
			return null;
		}
	}

	protected class ConnectionHandler extends ChannelDuplexHandler {

		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			channelEventMulticaster.listeners.onEvent(new ChannelEvent(NettyService.this, ctx.channel(),
					ChannelEvent.EventType.CONNECT));
		}

		@Override
		public void channelInactive(ChannelHandlerContext ctx) throws Exception {
			channelEventMulticaster.listeners.onEvent(new ChannelEvent(NettyService.this, ctx.channel(),
					ChannelEvent.EventType.CLOSE));
		}

		@Override
		public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
			final Channel channel = ctx.channel();

			if (evt == IdleStateEvent.READER_IDLE_STATE_EVENT) {
				logger.warn("Read idle, {} will be closed", channel);

				channel.close().awaitUninterruptibly();

				channelEventMulticaster.listeners.onEvent(new ChannelEvent(NettyService.this, channel,
						ChannelEvent.EventType.READ_IDLE));
			} else if (evt == IdleStateEvent.WRITER_IDLE_STATE_EVENT) {
				logger.info("Write idle, {} will send heartbeat", channel);

				channel.writeAndFlush(new Heartbeat());

				channelEventMulticaster.listeners.onEvent(new ChannelEvent(NettyService.this, channel,
						ChannelEvent.EventType.WRITE_IDLE));
			}
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
			logger.warn(ctx.channel() + " exception caught", cause);

			ctx.channel().close().awaitUninterruptibly();

			channelEventMulticaster.listeners.onEvent(new ChannelEvent(NettyService.this, ctx.channel(),
					ChannelEvent.EventType.EXCEPTION));
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
