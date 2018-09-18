package pers.zyc.tools.network;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import pers.zyc.tools.utils.GeneralThreadFactory;

/**
 * @author zhangyancheng
 */
public class NettyClient extends NettyService {

	private final Bootstrap bootstrap = new Bootstrap();

	public NettyClient(NettyClientConfig clientConfig) {
		super(clientConfig);
	}

	@Override
	protected void doStart() {
		super.doStart();

		final NettyClientConfig clientConfig = ((NettyClientConfig) networkConfig);
		bootstrap
				//
				.option(ChannelOption.TCP_NODELAY, clientConfig.isTcpNoDelay())
				.option(ChannelOption.SO_REUSEADDR, clientConfig.isReuseAddress())
				.option(ChannelOption.SO_KEEPALIVE, clientConfig.isKeepAlive())
				.option(ChannelOption.SO_LINGER, clientConfig.getLinger())
				.option(ChannelOption.SO_SNDBUF, clientConfig.getSendBuffer())
				.option(ChannelOption.SO_RCVBUF, clientConfig.getReceiveBuffer())
				//
				.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, clientConfig.getConnectTimeout())
				//
				.group(new NioEventLoopGroup(clientConfig.getSelectors(), new GeneralThreadFactory("IO-SELECTOR-")))
				//
				.channel(NioSocketChannel.class)
				//
				.handler(new PipelineAssembler() {
					@Override
					protected void assemblePipeline(ChannelPipeline pipeline) {
						pipeline.addLast(getChannelHandlers());
					}
				});
	}

	@Override
	protected void doStop() throws Exception {
		super.doStop();
	}
}
