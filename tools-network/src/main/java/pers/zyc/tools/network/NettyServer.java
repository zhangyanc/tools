package pers.zyc.tools.network;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import pers.zyc.tools.utils.GeneralThreadFactory;

import java.util.concurrent.ThreadFactory;

/**
 * @author zhangyancheng
 */
public class NettyServer extends NettyService {

	/**
	 * 是否使用epoll
	 */
	private boolean useEpoll = Epoll.isAvailable();

	/**
	 * 等待接受连接队列大小
	 */
	private int backlog = 1024;

	/**
	 * 服务端口号
	 */
	private int port;

	/**
	 * 服务端Netty引导器
	 */
	private final ServerBootstrap bootstrap = new ServerBootstrap();

	@Override
	protected void doStart() {
		super.doStart();

		bootstrap
				.option(ChannelOption.TCP_NODELAY, isSoTcpNoDelay())
				.option(ChannelOption.SO_BACKLOG, getBacklog())
				.option(ChannelOption.SO_REUSEADDR, isSoReuseAddress())
				.option(ChannelOption.SO_KEEPALIVE, isSoKeepAlive())
				.option(ChannelOption.SO_LINGER, getSoLinger())
				.option(ChannelOption.SO_SNDBUF, getSoSendBuffer())
				.option(ChannelOption.SO_RCVBUF, getSoReceiveBuffer());

		ThreadFactory acceptorThreadFactory = new GeneralThreadFactory("IO-ACCEPTOR"),
					  selectorThreadFactory = new GeneralThreadFactory("IO-SELECTOR-");

		EventLoopGroup acceptorLoopGroup, selectorLoopGroup;
		Class<? extends ServerSocketChannel> channelClass;
		if (isUseEpoll()) {
			acceptorLoopGroup = new EpollEventLoopGroup(1, acceptorThreadFactory);
			selectorLoopGroup = new EpollEventLoopGroup(getSelectors(), selectorThreadFactory);
			channelClass = EpollServerSocketChannel.class;
		} else {
			acceptorLoopGroup = new NioEventLoopGroup(1, acceptorThreadFactory);
			selectorLoopGroup = new NioEventLoopGroup(getSelectors(), selectorThreadFactory);
			channelClass = NioServerSocketChannel.class;
		}

		bootstrap.group(acceptorLoopGroup, selectorLoopGroup)
				.channel(channelClass)
				.localAddress(getPort())
				.childHandler(new PipelineAssembler());

		boolean started = false;
		try {
			started = bootstrap.bind().await().isSuccess();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}

		if (!started) {
			throw new IllegalStateException("Server bind to " + getPort() + " start failed.");
		}
	}

	@Override
	protected void doStop() throws Exception {
		bootstrap.config().group().shutdownGracefully();
		bootstrap.config().childGroup().shutdownGracefully();
		super.doStop();
	}

	public boolean isUseEpoll() {
		return useEpoll;
	}

	public void setUseEpoll(boolean useEpoll) {
		this.useEpoll = useEpoll;
	}

	public int getBacklog() {
		return backlog;
	}

	public void setBacklog(int backlog) {
		this.backlog = backlog;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
}
