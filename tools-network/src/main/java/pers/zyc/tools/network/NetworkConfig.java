package pers.zyc.tools.network;

/**
 * @author zhangyancheng
 */
public class NetworkConfig {
	static final int MAX_FRAME_LENGTH = 16777216;//16M

	private boolean tcpNoDelay = true;
	private boolean reuseAddress = true;
	private boolean keepAlive = true;
	private int linger = -1;
	private int sendBuffer = 8 * 1024;
	private int receiveBuffer  = 8 * 1024;

	private int selectors = 0;
	private int requestTimeout = 3000;
	private int maxFrameLength = MAX_FRAME_LENGTH;

	private int channelReadTimeout = 60000;
	private int channelWriteTimeout = 20000;

	private BufAllocator bufAllocator;

	private CommandFactory commandFactory;

	private RequestHandlerFactory requestHandlerFactory;

	public boolean isTcpNoDelay() {
		return tcpNoDelay;
	}

	public boolean isReuseAddress() {
		return reuseAddress;
	}

	public boolean isKeepAlive() {
		return keepAlive;
	}

	public int getLinger() {
		return linger;
	}

	public int getSendBuffer() {
		return sendBuffer;
	}

	public int getReceiveBuffer() {
		return receiveBuffer;
	}

	public int getSelectors() {
		return selectors;
	}

	public int getRequestTimeout() {
		return requestTimeout;
	}

	public int getMaxFrameLength() {
		return maxFrameLength;
	}

	public int getChannelReadTimeout() {
		return channelReadTimeout;
	}

	public int getChannelWriteTimeout() {
		return channelWriteTimeout;
	}

	public BufAllocator getBufAllocator() {
		return bufAllocator;
	}

	public CommandFactory getCommandFactory() {
		return commandFactory;
	}

	public RequestHandlerFactory getRequestHandlerFactory() {
		return requestHandlerFactory;
	}
}
