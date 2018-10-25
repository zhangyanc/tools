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
public class NetServer extends NetService {

	/**
	 * 是否使用epoll
	 */
	private boolean useEPoll;

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
				.option(ChannelOption.SO_BACKLOG, getBacklog())
				.option(ChannelOption.SO_REUSEADDR, isSoReuseAddress())
				.option(ChannelOption.SO_RCVBUF, getSoReceiveBuffer())
				.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, getConnectTimeout());

		ThreadFactory acceptorThreadFactory = new GeneralThreadFactory("IO-ACCEPTOR"),
					  selectorThreadFactory = new GeneralThreadFactory("IO-SELECTOR-");

		EventLoopGroup acceptorLoopGroup, selectorLoopGroup;
		Class<? extends ServerSocketChannel> channelClass;
		if (useEPoll && Epoll.isAvailable()) {
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

		bootstrap.bind().syncUninterruptibly();
	}

	@Override
	protected void doStop() throws Exception {
		bootstrap.config().group().shutdownGracefully();
		bootstrap.config().childGroup().shutdownGracefully();
		super.doStop();
	}

	public boolean isUseEPoll() {
		return useEPoll;
	}

	public void setUseEPoll(boolean useEPoll) {
		this.useEPoll = useEPoll;
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
