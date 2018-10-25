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
public class NetClient extends NetService {

	/**
	 * {@link java.net.Socket#setTcpNoDelay(boolean)}
	 */
	private boolean soTcpNoDelay = true;

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

	private final Bootstrap bootstrap = new Bootstrap();

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

	public boolean isSoTcpNoDelay() {
		return soTcpNoDelay;
	}

	public void setSoTcpNoDelay(boolean soTcpNoDelay) {
		this.soTcpNoDelay = soTcpNoDelay;
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

	/**
	 * 创建连接
	 *
	 * @param host 主机
	 * @param port 端口号
	 * @return 连接
	 * @throws ServiceException.NotRunningException 服务未运行
	 * @throws NetworkException 连接异常
	 */
	public Channel createChannel(String host, int port) {
		return createChannel(InetSocketAddress.createUnresolved(host, port));
	}

	/**
	 * 创建连接
	 *
	 * @param remoteAddress 对端socket地址
	 * @return 连接
	 * @throws ServiceException.NotRunningException 服务未运行
	 * @throws NetworkException 连接异常
	 */
	public Channel createChannel(SocketAddress remoteAddress) {
		checkRunning();

		ChannelFuture connectFuture = bootstrap.connect(remoteAddress).awaitUninterruptibly();
		if (connectFuture.isSuccess()) {
			Channel channel = connectFuture.channel();
			if (channel.isActive()) {
				return channel;
			}
			throw new NetworkException("Channel created, but isn't active, remote: " + remoteAddress.toString());
		}
		throw new NetworkException("Channel create failed: " + connectFuture.cause().getMessage() +
				", remote: " + remoteAddress.toString());
	}
}
