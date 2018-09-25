package pers.zyc.tools.network;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
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

	private final ServerBootstrap bootstrap = new ServerBootstrap();

	public NettyServer(NettyServerConfig serverConfig) {
		super(serverConfig);
	}

	@Override
	protected void doStart() {
		super.doStart();

		final NettyServerConfig serverConfig = (NettyServerConfig) networkConfig;
		bootstrap
				//
				.option(ChannelOption.TCP_NODELAY, serverConfig.isTcpNoDelay())
				.option(ChannelOption.SO_BACKLOG, serverConfig.getBacklog())
				.option(ChannelOption.SO_REUSEADDR, serverConfig.isReuseAddress())
				.option(ChannelOption.SO_KEEPALIVE, serverConfig.isKeepAlive())
				.option(ChannelOption.SO_LINGER, serverConfig.getLinger())
				.option(ChannelOption.SO_SNDBUF, serverConfig.getSendBuffer())
				.option(ChannelOption.SO_RCVBUF, serverConfig.getReceiveBuffer());

		ThreadFactory acceptorThreadFactory = new GeneralThreadFactory("IO-ACCEPTOR"),
					  selectorThreadFactory = new GeneralThreadFactory("IO-SELECTOR-");

		EventLoopGroup acceptorLoopGroup, selectorLoopGroup;
		Class<? extends ServerSocketChannel> channelClass;
		if (serverConfig.isUseEpoll()) {
			acceptorLoopGroup = new EpollEventLoopGroup(1, acceptorThreadFactory);
			selectorLoopGroup = new EpollEventLoopGroup(serverConfig.getSelectors(), selectorThreadFactory);
			channelClass = EpollServerSocketChannel.class;
		} else {
			acceptorLoopGroup = new NioEventLoopGroup(1, acceptorThreadFactory);
			selectorLoopGroup = new NioEventLoopGroup(serverConfig.getSelectors(), selectorThreadFactory);
			channelClass = NioServerSocketChannel.class;
		}

		bootstrap.group(acceptorLoopGroup, selectorLoopGroup)
				.channel(channelClass)
				.localAddress(serverConfig.getPort())
				.childHandler(new PipelineAssembler());

		boolean started = false;
		try {
			started = bootstrap.bind().await().isSuccess();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}

		if (!started) {
			throw new IllegalStateException("Server bind to " + serverConfig.getPort() + " start failed.");
		}
	}

	@Override
	protected void doStop() throws Exception {
		bootstrap.config().group().shutdownGracefully();
		bootstrap.config().childGroup().shutdownGracefully();
	}
}
