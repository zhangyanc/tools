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
import pers.zyc.tools.utils.lifecycle.ServiceException;
import pers.zyc.tools.utils.lifecycle.ThreadService;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * @author zhangyancheng
 */
class NetService extends ThreadService implements EventSource<ChannelEvent> {
	/**
	 * Channel保存Promise Map键
	 */
	private static final AttributeKey<Map<Integer, ResponsePromise>> RESPONSE_PROMISE_KEY =
			AttributeKey.newInstance("CHANNEL_RESPONSE_PROMISE");

	/**
	 * {@link java.net.Socket#setTcpNoDelay(boolean)}
	 */
	private boolean soTcpNoDelay = true;

	/**
	 * {@link java.net.Socket#setReuseAddress(boolean)}
	 */
	private boolean soReuseAddress = true;

	/**
	 * {@link java.net.Socket#setKeepAlive(boolean)}
	 */
	private boolean soKeepAlive = false;

	/**
	 * {@link java.net.Socket#setSoLinger(boolean, int)}
	 */
	private int soLinger = -1;

	/**
	 * {@link java.net.Socket#setSendBufferSize(int)}
	 */
	private int soSendBuffer = 8 * 1024;

	/**
	 * {@link java.net.Socket#setReceiveBufferSize(int)}
	 */
	private int soReceiveBuffer = 8 * 1024;

	/**
	 * 多路复用器个数（IO线程数），为0表示使用netty默认值
	 */
	private int selectors = 0;

	/**
	 * 默认请求超时时间（ms）
	 */
	private int requestTimeout = 3000;

	/**
	 * 网络包最大大小
	 *
	 * {@link io.netty.handler.codec.LengthFieldBasedFrameDecoder#maxFrameLength}
	 */
	private int maxFrameLength = Command.MAX_FRAME_LENGTH;

	/**
	 * 连接读空闲时间（ms），双向心跳模式中，一旦连接读空闲则对端已宕机，则关闭连接
	 */
	private int channelReadTimeout = 60000;

	/**
	 * 连接写空闲时间（ms），双向心跳模式中，连接写空闲则需要发送心跳包
	 */
	private int channelWriteTimeout = 20000;

	/**
	 * 请求超时清理周期（ms）
	 */
	private int requestTimeoutDetectInterval = 1000;

	/**
	 * 心跳命令类型，心跳为框架自带命令，配置类型避免与用户命令类型冲突
	 */
	private int heartbeatCommandType = 999;

	/**
	 * 允许同时处理的最大请求数（小于0表示不做最大限制）
	 */
	private int maxProcessingRequests = -1;

	/**
	 * 用户命令工厂，解码时通过命令类型从工厂中获取一个命令实体解码出命令内容
	 */
	private CommandFactory commandFactory;

	/**
	 * 请求处理器工厂，处理请求时通过请求类型从工厂中获取请求处理器处理请求
	 */
	private RequestHandlerFactory requestHandlerFactory;

	/**
	 * 异步请求时，发送响应回调的执行器，为null表示在收到响应的线程中执行
	 */
	private Executor responseMulticastExecutor;


	/**
	 * 请求信号量，用于控制最大并发请求数，为null时表示不控制
	 */
	private Semaphore requestPermits;

	/**
	 * 所有发送了请求的Channel集合
	 */
	private final ConcurrentSet<Channel> requestedChannelSet = new ConcurrentSet<>();

	private final Logger logger = LoggerFactory.getLogger(getClass());

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

	/**
	 * Channel事件发布器
	 */
	private final Multicaster<EventListener<ChannelEvent>> channelEventMulticaster =
			new Multicaster<EventListener<ChannelEvent>>() {
				{
					setExceptionHandler(multicastExceptionHandler);
				}
			};

	@Override
	protected void doStart() {
		requestPermits = maxProcessingRequests > 0 ? new Semaphore(maxProcessingRequests) : null;
		//设置当前服务线程名
		setThreadFactory(new GeneralThreadFactory("TimeoutRequestClear"));
		logger.info(getName() + " started");
	}

	@Override
	protected Runnable getRunnable() {
		return new ServiceRunnable() {

			@Override
			protected long getInterval() {
				//超时请求检查间隔，超时清理的最大延迟为一个间隔
				return requestTimeoutDetectInterval;
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
			respondAllChannelPromise(channel, new NetworkException("Service stopped!"));
		}
		super.doStop();
		logger.info(getName() + " stopped");
	}

	@Override
	public void addListener(EventListener<ChannelEvent> listener) {
		channelEventMulticaster.addListener(listener);
	}

	@Override
	public void removeListener(EventListener<ChannelEvent> listener) {
		channelEventMulticaster.removeListener(listener);
	}

	/**
	 * 单向发送请求
	 *
	 * @param channel 连接
	 * @param request 请求（必须是无需ack类型）
	 * @throws IllegalArgumentException 参数错误
	 * @throws NullPointerException 连接或者请求为空
	 * @throws InterruptedException 发送过程中线程被中断
	 * @throws ServiceException.NotRunningException 服务未运行
	 * @throws NetworkException.TimeoutException 请求超时
	 * @throws NetworkException.TooMuchRequestException 请求过多
	 * @throws NetworkException 发送失败
	 */
	public void oneWaySend(Channel channel, Request request) throws InterruptedException {
		oneWaySend(channel, request, requestTimeout);
	}

	/**
	 * 单向发送请求
	 *
	 * @param channel 连接
	 * @param request 请求（必须是无需ack类型）
	 * @param requestTimeout 请求超时（ms）
	 * @throws IllegalArgumentException 参数错误
	 * @throws NullPointerException 连接或者请求为空
	 * @throws InterruptedException 发送过程中线程被中断
	 * @throws ServiceException.NotRunningException 服务未运行
	 * @throws NetworkException.TimeoutException 请求超时
	 * @throws NetworkException.TooMuchRequestException 请求过多
	 * @throws NetworkException 发送失败
	 */
	public void oneWaySend(final Channel channel, Request request, int requestTimeout) throws InterruptedException {
		if (!(requestTimeout > 0)) {
			throw new IllegalArgumentException("requestTimeout " + requestTimeout + " <= 0");
		}

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

	/**
	 * 同步发送请求
	 *
	 * @param channel 连接
	 * @param request 请求
	 * @return 响应
	 * @throws IllegalArgumentException 参数错误
	 * @throws NullPointerException 连接或者请求为空
	 * @throws InterruptedException 发送过程中线程被中断
	 * @throws ServiceException.NotRunningException 服务未运行
	 * @throws NetworkException.TimeoutException 请求超时
	 * @throws NetworkException.TooMuchRequestException 请求过多
	 * @throws NetworkException 发送失败
	 */
	public Response syncSend(Channel channel, Request request) throws InterruptedException {
		return asyncSend(channel, request).get();
	}

	/**
	 * 同步发送请求
	 *
	 * @param channel 连接
	 * @param request 请求
	 * @param requestTimeout 请求超时（ms）
	 * @return 响应
	 * @throws IllegalArgumentException 参数错误
	 * @throws NullPointerException 连接或者请求为空
	 * @throws InterruptedException 发送过程中线程被中断
	 * @throws ServiceException.NotRunningException 服务未运行
	 * @throws NetworkException.TimeoutException 请求超时
	 * @throws NetworkException.TooMuchRequestException 请求过多
	 * @throws NetworkException 其他网络异常
	 */
	public Response syncSend(Channel channel, Request request, int requestTimeout) throws InterruptedException {
		return asyncSend(channel, request, requestTimeout).get();
	}

	/**
	 * 异步发送请求
	 *
	 * @param channel 连接
	 * @param request 请求
	 * @return 响应Future
	 * @throws IllegalArgumentException 参数错误
	 * @throws NullPointerException 连接或者请求为空
	 * @throws ServiceException.NotRunningException 服务未运行
	 * @throws NetworkException.TooMuchRequestException 请求过多
	 */
	public ResponseFuture asyncSend(Channel channel, Request request) {
		return asyncSend(channel, request, requestTimeout);
	}

	/**
	 * 异步发送请求
	 *
	 * @param channel 连接
	 * @param request 请求
	 * @param requestTimeout 请求超时（ms）
	 * @return 响应Future
	 * @throws IllegalArgumentException 参数错误
	 * @throws NullPointerException 连接或者请求为空
	 * @throws ServiceException.NotRunningException 服务未运行
	 * @throws NetworkException.TooMuchRequestException 请求过多
	 */
	public ResponseFuture asyncSend(final Channel channel, final Request request, int requestTimeout) {
		if (!(requestTimeout > 0)) {
			throw new IllegalArgumentException("requestTimeout " + requestTimeout + " <= 0");
		}
		checkRunning();

		final ResponsePromise responsePromise = acquirePromise(Objects.requireNonNull(request), requestTimeout);

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

		if (responseMulticastExecutor != null) {
			responsePromise.multicaster.setMulticastExecutor(responseMulticastExecutor);
		}
		return responsePromise;
	}

	private void respondAllChannelPromise(Channel channel, Throwable cause) {
		Map<Integer, ResponsePromise> responsePromiseMap = channel.attr(RESPONSE_PROMISE_KEY).get();
		if (responsePromiseMap != null) {
			for (ResponsePromise promise : responsePromiseMap.values()) {
				promise.response(cause);
			}
			requestedChannelSet.remove(channel);
		}
	}

	protected void requestHandleFailed(Channel channel, Request request, Throwable throwable) {
		logger.error(request + " handle failed, Channel: " + channel, throwable);
	}

	protected Heartbeat createHeartbeat() {
		return new Heartbeat(heartbeatCommandType);
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

	/**
	 * Pipeline装配器
	 */
	protected class PipelineAssembler extends ChannelInitializer<Channel> {

		final ChannelHandler commandHandler = new CommandHandler();
		final ChannelHandler channelStateHandler = new ChannelStateHandler();

		@Override
		protected void initChannel(Channel channel) throws Exception {
			assemblePipeline(channel.pipeline());
		}

		protected void assemblePipeline(ChannelPipeline pipeline) {
			pipeline.addLast(
					new Encoder(), new Decoder(),
					new IdleStateHandler(channelReadTimeout, channelWriteTimeout, 0, TimeUnit.MILLISECONDS),
					channelStateHandler, commandHandler
			);
		}
	}

	/**
	 * 命令处理器，处理入站数据
	 *
	 * 线程安全，可装配到多个ChannelPipeline
	 */
	@ChannelHandler.Sharable
	protected class CommandHandler extends SimpleChannelInboundHandler<Command> {

		@Override
		protected void channelRead0(ChannelHandlerContext ctx, Command command) throws Exception {
			final Channel channel = ctx.channel();
			switch (command.getHeader().getType()) {
				case Header.REQUEST:
					if (command.getType() == heartbeatCommandType) {
						return;
					}

					final Request request = (Request) command;
					try {
						final RequestHandler requestHandler = requestHandlerFactory.getHandler(request.getType());
						requestHandler.getExecutor().execute(new Runnable() {
							@Override
							public void run() {
								Response response;
								try {
									response = requestHandler.handle(request);
								} catch (Throwable throwable) {
									requestHandleFailed(channel, request, throwable);
									return;
								}

								if (!request.getHeader().isNeedAck() || response == null) {
									return;
								}
								channel.writeAndFlush(response).addListener(new CommandSendFutureListener(response));
							}
						});
					} catch (Exception e) {
						logger.error("Request handler execute error", e);
					}
					break;
				case Header.RESPONSE:
					Response response = (Response) command;

					Map<Integer, ResponsePromise> responsePromiseMap = channel.attr(RESPONSE_PROMISE_KEY).get();
					ResponsePromise responsePromise = responsePromiseMap.remove(response.getId());

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

	/**
	 * 连接（Channel）状态处理器
	 *
	 * 线程安全，可装配到多个ChannelPipeline
	 */
	@ChannelHandler.Sharable
	protected class ChannelStateHandler extends ChannelInboundHandlerAdapter {

		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			super.channelActive(ctx);
			logger.debug("Channel active, Channel: {}", ctx.channel());
			channelEventMulticaster.listeners.onEvent(new ChannelEvent(NetService.this, ctx.channel(),
					ChannelEvent.EventType.CONNECT));
		}

		@Override
		public void channelInactive(ChannelHandlerContext ctx) throws Exception {
			super.channelInactive(ctx);
			logger.debug("Channel inactive, Channel: {}", ctx.channel());
			channelEventMulticaster.listeners.onEvent(new ChannelEvent(NetService.this, ctx.channel(),
					ChannelEvent.EventType.CLOSE));

			respondAllChannelPromise(ctx.channel(), new NetworkException("Channel inactive"));
		}

		@Override
		public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
			super.userEventTriggered(ctx, evt);
			logger.debug("User event triggered, Channel: {}, Event: {}", ctx.channel(), evt);

			if (evt == IdleStateEvent.READER_IDLE_STATE_EVENT) {
				channelEventMulticaster.listeners.onEvent(new ChannelEvent(NetService.this, ctx.channel(),
						ChannelEvent.EventType.READ_IDLE));

				logger.debug("On read idle event, Close Channel");
				ctx.channel().close();
			} else if (evt == IdleStateEvent.WRITER_IDLE_STATE_EVENT) {
				channelEventMulticaster.listeners.onEvent(new ChannelEvent(NetService.this, ctx.channel(),
						ChannelEvent.EventType.WRITE_IDLE));

				logger.debug("On write idle event, Send heartbeat");
				Heartbeat heartbeat = createHeartbeat();
				ctx.channel().writeAndFlush(heartbeat).addListener(new CommandSendFutureListener(heartbeat));
			}
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
			super.exceptionCaught(ctx, cause);
			logger.error(ctx.channel() + " exception caught", cause);

			channelEventMulticaster.listeners.onEvent(new ChannelEvent(NetService.this, ctx.channel(),
					ChannelEvent.EventType.EXCEPTION));

			logger.debug("On exception caught, Close Channel");
			ctx.channel().close();
		}
	}

	/**
	 * 命令编码器
	 */
	protected class Encoder extends MessageToByteEncoder<Command> {

		@Override
		protected void encode(ChannelHandlerContext ctx, Command command,
							  ByteBuf out) throws Exception {
			command.encode(out);//编码命令
		}

		@Override
		protected ByteBuf allocateBuffer(ChannelHandlerContext ctx, Command command,
										 boolean preferDirect) throws Exception {
			return command.allocateBuffer(ctx, preferDirect);
		}
	}

	/**
	 * 命令解码器
	 */
	protected class Decoder extends LengthFieldBasedFrameDecoder {

		Decoder() {
			super(Math.min(maxFrameLength, Command.MAX_FRAME_LENGTH), 0,
					Command.LENGTH_FIELD_LENGTH,
					-Command.LENGTH_FIELD_LENGTH,
					Command.LENGTH_FIELD_LENGTH);
		}

		@Override
		protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
			ByteBuf frame = (ByteBuf) super.decode(ctx, in);
			if (frame == null) {
				//未完整包
				return null;
			}

			Header header = new Header();
			header.decode(frame);//解码命令头

			Command command = commandFactory.createByType(header);
			command.decode(frame);//解码命令体
			return command;
		}

		@Override
		protected ByteBuf extractFrame(ChannelHandlerContext ctx, ByteBuf buffer, int index, int length) {
			//只读一次, 无需retain
			return buffer.slice(index, length);
		}
	}


	public boolean isSoTcpNoDelay() {
		return soTcpNoDelay;
	}

	public void setSoTcpNoDelay(boolean soTcpNoDelay) {
		this.soTcpNoDelay = soTcpNoDelay;
	}

	public boolean isSoReuseAddress() {
		return soReuseAddress;
	}

	public void setSoReuseAddress(boolean soReuseAddress) {
		this.soReuseAddress = soReuseAddress;
	}

	public boolean isSoKeepAlive() {
		return soKeepAlive;
	}

	public void setSoKeepAlive(boolean soKeepAlive) {
		this.soKeepAlive = soKeepAlive;
	}

	public int getSoLinger() {
		return soLinger;
	}

	public void setSoLinger(int soLinger) {
		this.soLinger = soLinger;
	}

	public int getSoSendBuffer() {
		return soSendBuffer;
	}

	public void setSoSendBuffer(int soSendBuffer) {
		this.soSendBuffer = soSendBuffer;
	}

	public int getSoReceiveBuffer() {
		return soReceiveBuffer;
	}

	public void setSoReceiveBuffer(int soReceiveBuffer) {
		this.soReceiveBuffer = soReceiveBuffer;
	}

	public int getSelectors() {
		return selectors;
	}

	public void setSelectors(int selectors) {
		this.selectors = selectors;
	}

	public int getRequestTimeout() {
		return requestTimeout;
	}

	public void setRequestTimeout(int requestTimeout) {
		this.requestTimeout = requestTimeout;
	}

	public int getMaxFrameLength() {
		return maxFrameLength;
	}

	public void setMaxFrameLength(int maxFrameLength) {
		this.maxFrameLength = maxFrameLength;
	}

	public int getChannelReadTimeout() {
		return channelReadTimeout;
	}

	public void setChannelReadTimeout(int channelReadTimeout) {
		this.channelReadTimeout = channelReadTimeout;
	}

	public int getChannelWriteTimeout() {
		return channelWriteTimeout;
	}

	public void setChannelWriteTimeout(int channelWriteTimeout) {
		this.channelWriteTimeout = channelWriteTimeout;
	}

	public int getRequestTimeoutDetectInterval() {
		return requestTimeoutDetectInterval;
	}

	public void setRequestTimeoutDetectInterval(int requestTimeoutDetectInterval) {
		this.requestTimeoutDetectInterval = requestTimeoutDetectInterval;
	}

	public int getHeartbeatCommandType() {
		return heartbeatCommandType;
	}

	public void setHeartbeatCommandType(int heartbeatCommandType) {
		this.heartbeatCommandType = heartbeatCommandType;
	}

	public int getMaxProcessingRequests() {
		return maxProcessingRequests;
	}

	public void setMaxProcessingRequests(int maxProcessingRequests) {
		this.maxProcessingRequests = maxProcessingRequests;
	}

	public CommandFactory getCommandFactory() {
		return commandFactory;
	}

	public void setCommandFactory(CommandFactory commandFactory) {
		this.commandFactory = commandFactory;
	}

	public RequestHandlerFactory getRequestHandlerFactory() {
		return requestHandlerFactory;
	}

	public void setRequestHandlerFactory(RequestHandlerFactory requestHandlerFactory) {
		this.requestHandlerFactory = requestHandlerFactory;
	}

	public Executor getResponseMulticastExecutor() {
		return responseMulticastExecutor;
	}

	public void setResponseMulticastExecutor(Executor responseMulticastExecutor) {
		this.responseMulticastExecutor = responseMulticastExecutor;
	}
}
