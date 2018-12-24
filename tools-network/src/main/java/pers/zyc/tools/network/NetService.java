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
import pers.zyc.tools.utils.SystemMillis;
import pers.zyc.tools.utils.event.*;
import pers.zyc.tools.utils.lifecycle.ServiceException;
import pers.zyc.tools.utils.lifecycle.ThreadService;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * @author zhangyancheng
 */
public class NetService extends ThreadService implements EventSource<ChannelEvent> {
	/**
	 * Channel保存Promise Map键
	 */
	private static final AttributeKey<Map<Integer, ResponsePromise>> RESPONSE_PROMISE_KEY =
			AttributeKey.newInstance("CHANNEL_RESPONSE_PROMISE");

	/**
	 * 连接超时时间（ms）
	 */
	private int connectTimeout = 3000;

	/**
	 * {@link java.net.Socket#setReuseAddress(boolean)}
	 */
	private boolean soReuseAddress = true;

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
	 * 连接读空闲时间（ms）
	 */
	private int channelReadTimeout = 60000;

	/**
	 * 连接写空闲时间（ms）
	 */
	private int channelWriteTimeout = 20000;

	/**
	 * 连接读写都空闲时间（ms）
	 */
	private int channelAllTimeout = 0;

	/**
	 * 请求超时清理周期（ms）
	 */
	private int requestTimeoutDetectInterval = 1000;

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
	private ExecutorService multicastExecutor;

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
			logger.error("Multicast error on method " + detail.method.getName(), cause);
			return null;
		}
	};

	/**
	 * Channel事件广播器
	 */
	private final Multicaster<EventListener<ChannelEvent>> channelEventMulticaster =
			new Multicaster<EventListener<ChannelEvent>>() {};

	@Override
	protected void doStart() {
		if (multicastExecutor == null) {
			multicastExecutor = Executors.newSingleThreadExecutor(new GeneralThreadFactory("Multicaster") {
				{
					setDaemon(true);
				}
			});
		}

		channelEventMulticaster.setMulticastExecutor(multicastExecutor);
		channelEventMulticaster.setExceptionHandler(multicastExceptionHandler);

		requestPermits = maxProcessingRequests > 0 ? new Semaphore(maxProcessingRequests) : null;

		//设置当前服务线程名
		setThreadFactory(new GeneralThreadFactory("TimeoutRequestCleaner"));

		//添加连接事件监听器，异常时关闭连接，连接关闭时清理所有通过此连接发送的请求
		addListener(new EventListener<ChannelEvent>() {
			@Override
			public void onEvent(ChannelEvent event) {
				switch (event.eventType) {
					case EXCEPTION:
						event.channel.close();
						break;
					case CLOSE:
						respondAllChannelPromise(event.channel, new NetworkException("Channel closed!"));
						break;
					default:
				}
			}
		});
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
						if (promise.deadline <= SystemMillis.current()) {
							logger.debug("Request: {} timeout, Channel: {}", promise.request, channel);
							respondPromise(promise, new NetworkException.TimeoutException());
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
	 * @param request 请求（必须是无需ack类型）
	 * @throws InterruptedException 发送过程中线程被中断
	 * @throws ServiceException.NotRunningException 服务未运行
	 * @throws NetworkException.TimeoutException 请求超时
	 * @throws NetworkException.TooMuchRequestException 请求过多
	 * @throws NetworkException 发送失败
	 */
	public void sendOneWay(Request request) throws InterruptedException {
		sendOneWay(request, requestTimeout);
	}

	/**
	 * 单向发送请求
	 *
	 * @param request 请求（必须是无需ack类型）
	 * @param requestTimeout 请求超时（ms）
	 * @throws InterruptedException 发送过程中线程被中断
	 * @throws ServiceException.NotRunningException 服务未运行
	 * @throws NetworkException.TimeoutException 请求超时
	 * @throws NetworkException.TooMuchRequestException 请求过多
	 * @throws NetworkException 发送失败
	 */
	public void sendOneWay(Request request, int requestTimeout) throws InterruptedException {
		if (!(requestTimeout > 0)) {
			throw new IllegalArgumentException("requestTimeout " + requestTimeout + " <= 0");
		}
		checkRunning();

		if (request.getHeader().isNeedAck()) {
			throw new IllegalArgumentException("Request need ack");
		}

		Channel channel = request.getChannel();
		final ResponsePromise responsePromise = acquirePromise(request, requestTimeout);
		channel.writeAndFlush(request).addListeners(new CommandSendFutureListener(request) {
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				super.operationComplete(future);
				//单向请求发送后响应
				respondPromise(responsePromise, future.isSuccess() ? null : future.cause());
			}
		});
		responsePromise.get();
	}

	/**
	 * 同步发送请求
	 *
	 * @param request 请求
	 * @return 响应
	 * @throws InterruptedException 发送过程中线程被中断
	 * @throws ServiceException.NotRunningException 服务未运行
	 * @throws NetworkException.TimeoutException 请求超时
	 * @throws NetworkException.TooMuchRequestException 请求过多
	 * @throws NetworkException 发送失败
	 */
	public Response sendSync(Request request) throws InterruptedException {
		return sendAsync(request).get();
	}

	/**
	 * 同步发送请求
	 *
	 * @param request 请求
	 * @param requestTimeout 请求超时（ms）
	 * @return 响应
	 * @throws InterruptedException 发送过程中线程被中断
	 * @throws ServiceException.NotRunningException 服务未运行
	 * @throws NetworkException.TimeoutException 请求超时
	 * @throws NetworkException.TooMuchRequestException 请求过多
	 * @throws NetworkException 其他网络异常
	 */
	public Response sendSync(Request request, int requestTimeout) throws InterruptedException {
		return sendAsync(request, requestTimeout).get();
	}

	/**
	 * 异步发送请求
	 *
	 * @param request 请求
	 * @return 响应Future
	 * @throws ServiceException.NotRunningException 服务未运行
	 * @throws NetworkException.TooMuchRequestException 请求过多
	 */
	public ResponseFuture sendAsync(Request request) {
		return sendAsync(request, requestTimeout);
	}

	/**
	 * 异步发送请求
	 *
	 * @param request 请求
	 * @param requestTimeout 请求超时（ms）
	 * @return 响应Future
	 * @throws ServiceException.NotRunningException 服务未运行
	 * @throws NetworkException.TooMuchRequestException 请求过多
	 */
	public ResponseFuture sendAsync(final Request request, int requestTimeout) {
		if (!(requestTimeout > 0)) {
			throw new IllegalArgumentException("requestTimeout " + requestTimeout + " <= 0");
		}
		checkRunning();

		final Channel channel = request.getChannel();
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
					//发送失败后响应
					respondPromise(responsePromise, future.cause());
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

	private void respondPromise(ResponsePromise promise, Object response) {
		if (promise.response(response)) {
			if (requestPermits != null) {
				requestPermits.release();
			}
		}
	}

	private ResponsePromise acquirePromise(Request request, int requestTimeout) {
		//如果设置了请求许可则必需获取许可才能发送请求
		if (requestPermits != null && !requestPermits.tryAcquire()) {
			throw new NetworkException.TooMuchRequestException();
		}
		return new ResponsePromise(request, requestTimeout, new Multicaster<ResponseFutureListener>() {
			{
				setMulticastExecutor(multicastExecutor);
				setExceptionHandler(multicastExceptionHandler);
				setEventListeners(new HashSet<ResponseFutureListener>());
			}
		});
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

	/**
	 * 请求处理异常
	 *
	 * @param channel 连接
	 * @param request 请求
	 * @param e 异常
	 */
	protected void requestHandleFailed(Channel channel, Request request, Exception e) {
		logger.error(request + " handle failed, Channel: " + channel, e);
	}

	/**
	 * write监听器
	 */
	private class CommandSendFutureListener implements ChannelFutureListener {

		final Command command;

		CommandSendFutureListener(Command command) {
			this.command = command;
		}

		@Override
		public void operationComplete(ChannelFuture future) throws Exception {
			command.writeComplete(future.isSuccess());

			if (!future.isSuccess()) {
				logger.error("{} send failed, error: {}, Channel{}",
						command, future.cause().getMessage(), future.channel());
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
					new Encoder(),
					new Decoder(),
					new IdleStateHandler(
							channelReadTimeout,
							channelWriteTimeout,
							channelAllTimeout,
							TimeUnit.MILLISECONDS
					),
					commandHandler,
					channelStateHandler
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
					final Request request = (Request) command;
					final RequestHandler requestHandler = requestHandlerFactory.getHandler(request.getType());
					requestHandler.getExecutor().execute(new Runnable() {
						@Override
						public void run() {
							request.setChannel(channel);
							Response response;
							try {
								response = requestHandler.handle(request);
							} catch (Exception e) {
								requestHandleFailed(channel, request, e);
								return;
							}

							if (!request.getHeader().isNeedAck() || response == null) {
								return;
							}
							channel.writeAndFlush(response).addListener(new CommandSendFutureListener(response));
						}
					});
					break;
				case Header.RESPONSE:
					Response response = (Response) command;

					Map<Integer, ResponsePromise> responsePromiseMap = channel.attr(RESPONSE_PROMISE_KEY).get();
					ResponsePromise responsePromise = responsePromiseMap.remove(response.getId());

					if (responsePromiseMap.isEmpty()) {
						requestedChannelSet.remove(channel);
					}
					if (responsePromise != null) {
						//收到response后响应
						respondPromise(responsePromise, response);
					} else {
						logger.warn("ID[{}] {} not matched!", response.getId(), response);
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
	protected class ChannelStateHandler extends ChannelDuplexHandler implements ChannelFutureListener {

		@Override
		public void operationComplete(ChannelFuture future) throws Exception {
			if (!future.isSuccess()) {
				logger.error(future.channel() + " write error", future.cause());
				publishChannelEvent(future.channel(), ChannelEvent.EventType.EXCEPTION);
			}
		}

		@Override
		public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
			ctx.write(msg, promise.addListener(this));
		}

		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			super.channelActive(ctx);
			logger.debug("Channel active, Channel: {}", ctx.channel());
			publishChannelEvent(ctx.channel(), ChannelEvent.EventType.CONNECT);
		}

		@Override
		public void channelInactive(ChannelHandlerContext ctx) throws Exception {
			super.channelInactive(ctx);
			logger.debug("Channel inactive, Channel: {}", ctx.channel());
			publishChannelEvent(ctx.channel(), ChannelEvent.EventType.CLOSE);
		}

		@Override
		public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
			super.userEventTriggered(ctx, evt);
			logger.debug("User event triggered, Channel: {}, Event: {}", ctx.channel(), evt);

			if (evt instanceof IdleStateEvent) {
				ChannelEvent.EventType eventType;
				switch (((IdleStateEvent) evt).state()) {
					case READER_IDLE:
						eventType = ChannelEvent.EventType.READ_IDLE;
						break;
					case WRITER_IDLE:
						eventType = ChannelEvent.EventType.WRITE_IDLE;
						break;
					case ALL_IDLE:
						eventType = ChannelEvent.EventType.ALL_IDLE;
						break;
					default:
						throw new Error();
				}
				publishChannelEvent(ctx.channel(), eventType);
			}
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
			logger.error(ctx.channel() + " exception caught", cause);
			publishChannelEvent(ctx.channel(), ChannelEvent.EventType.EXCEPTION);
		}

		private void publishChannelEvent(Channel channel, ChannelEvent.EventType eventType) {
			channelEventMulticaster.listeners.onEvent(new ChannelEvent(NetService.this, channel, eventType));
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

	public int getConnectTimeout() {
		return connectTimeout;
	}

	public void setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	public boolean isSoReuseAddress() {
		return soReuseAddress;
	}

	public void setSoReuseAddress(boolean soReuseAddress) {
		this.soReuseAddress = soReuseAddress;
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

	public int getChannelAllTimeout() {
		return channelAllTimeout;
	}

	public void setChannelAllTimeout(int channelAllTimeout) {
		this.channelAllTimeout = channelAllTimeout;
	}

	public int getRequestTimeoutDetectInterval() {
		return requestTimeoutDetectInterval;
	}

	public void setRequestTimeoutDetectInterval(int requestTimeoutDetectInterval) {
		this.requestTimeoutDetectInterval = requestTimeoutDetectInterval;
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

	public ExecutorService getMulticastExecutor() {
		return multicastExecutor;
	}

	public void setMulticastExecutor(ExecutorService multicastExecutor) {
		this.multicastExecutor = multicastExecutor;
	}
}
