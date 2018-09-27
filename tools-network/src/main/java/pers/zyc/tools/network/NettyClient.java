package pers.zyc.tools.network;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import pers.zyc.tools.utils.GeneralThreadFactory;
import pers.zyc.tools.utils.lifecycle.ServiceException;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * @author zhangyancheng
 */
public class NettyClient extends NettyService {

	/**
	 * 连接超时时间（ms）
	 */
	private int connectTimeout = 3000;

	private final Bootstrap bootstrap = new Bootstrap();

	public NettyClient() {
	}

	@Override
	protected void doStart() {
		super.doStart();

		bootstrap
				.option(ChannelOption.TCP_NODELAY, isSoTcpNoDelay())
				.option(ChannelOption.SO_REUSEADDR, isSoReuseAddress())
				.option(ChannelOption.SO_KEEPALIVE, isSoKeepAlive())
				.option(ChannelOption.SO_LINGER, getSoLinger())
				.option(ChannelOption.SO_SNDBUF, getSoSendBuffer())
				.option(ChannelOption.SO_RCVBUF, getSoReceiveBuffer())
				.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, getConnectTimeout())
				.group(new NioEventLoopGroup(getSelectors(), new GeneralThreadFactory("IO-SELECTOR-")))
				.channel(NioSocketChannel.class)
				.handler(new PipelineAssembler());
	}

	@Override
	protected void doStop() throws Exception {
		bootstrap.config().group().shutdownGracefully();
		super.doStop();
	}

	public int getConnectTimeout() {
		return connectTimeout;
	}

	public void setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	/**
	 * 创建连接
	 *
	 * @param host 主机
	 * @param port 端口号
	 * @return 连接
	 * @throws InterruptedException 等待连接过程中线程被中断
	 * @throws ServiceException.NotRunningException 服务未运行
	 * @throws NetworkException 连接异常
	 */
	public Channel createChannel(String host, int port) throws InterruptedException {
		return createChannel(InetSocketAddress.createUnresolved(host, port));
	}

	/**
	 * 创建连接
	 *
	 * @param remoteAddress 对端socket地址
	 * @return 连接
	 * @throws InterruptedException 等待连接过程中线程被中断
	 * @throws ServiceException.NotRunningException 服务未运行
	 * @throws NetworkException 连接异常
	 */
	public Channel createChannel(SocketAddress remoteAddress) throws InterruptedException {
		checkRunning();

		ChannelFuture connectFuture = bootstrap.connect(remoteAddress).await();
		if (connectFuture.isSuccess()) {
			Channel channel = connectFuture.channel();
			if (channel.isActive()) {
				return channel;
			}
		}
		throw new NetworkException("Connect exception", connectFuture.cause());
	}
}
