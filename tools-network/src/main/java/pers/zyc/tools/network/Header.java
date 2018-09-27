package pers.zyc.tools.network;

import io.netty.buffer.ByteBuf;

/**
 * @author zhangyancheng
 */
public class Header implements Protocol {

	private static final int REQUEST_HEADER_LENGTH = 1 + 1 + 1 + 4 + 4 + 8;

	static final int REQUEST = 0;
	static final int RESPONSE = 1;

	/**
	 * 命令头类型，标识命令是请求（0）还是响应（1）
	 */
	private int headerType;

	/**
	 * 是否需要ack，标识命令是否需要应答，一般来说请求需要应答，响应不需要应答
	 */
	private boolean needAck;

	/**
	 * 命令ID，每个请求需要有唯一id，请求的响应id需要与请求id相同
	 */
	private int commandId;

	/**
	 * 命令类型
	 */
	private int commandType;

	/**
	 * 命令版本号
	 */
	private int commandVersion;

	/**
	 * 命令时间
	 */
	private long commandTime;

	private int status;
	private String error;

	public Header headerType(int headerType) {
		setHeaderType(headerType);
		return this;
	}

	public Header needAck(boolean needAck) {
		setNeedAck(needAck);
		return this;
	}

	public Header commandId(int commandId) {
		setCommandId(commandId);
		return this;
	}

	public Header commandType(int commandType) {
		setCommandType(commandType);
		return this;
	}

	public Header commandVersion(int commandVersion) {
		setCommandVersion(commandVersion);
		return this;
	}

	public Header commandTime(long commandTime) {
		setCommandTime(commandTime);
		return this;
	}

	public Header status(int status) {
		setStatus(status);
		return this;
	}

	public Header error(String error) {
		setError(error);
		return this;
	}

	public boolean isRequest() {
		return headerType == REQUEST;
	}

	@Override
	public int getType() {
		return headerType;
	}

	@Override
	public int getEstimatedSize() {
		if (isRequest()) {
			return REQUEST_HEADER_LENGTH;
		}
		int size = REQUEST_HEADER_LENGTH + 4;
		if (error != null) {
			size += error.length();
		}
		return size;
	}

	@Override
	public void validate() throws Exception {
		if (headerType != REQUEST && headerType != RESPONSE) {
			throw new IllegalArgumentException("Unknown header type: " + headerType);
		}
	}

	@Override
	public void encode(ByteBuf byteBuf) throws Exception {
		validate();

		byteBuf.writeByte(headerType);
		byteBuf.writeBoolean(needAck);
		byteBuf.writeByte(commandVersion);
		byteBuf.writeInt(commandId);
		byteBuf.writeInt(commandType);
		byteBuf.writeLong(commandTime);

		if (!isRequest()) {
			byteBuf.writeInt(status);
			if (error == null) {
				byteBuf.writeInt(0);
			} else {
				byte[] errorMsg = error.getBytes(UTF_8);
				byteBuf.writeInt(errorMsg.length);
				byteBuf.writeBytes(errorMsg);
			}
		}
	}

	@Override
	public void decode(ByteBuf byteBuf) throws Exception {
		this.headerType(byteBuf.readByte())
			.needAck(byteBuf.readBoolean())
			.commandVersion(byteBuf.readByte())
			.commandId(byteBuf.readInt())
			.commandType(byteBuf.readInt())
			.commandTime(byteBuf.readLong());

		if (!isRequest()) {
			setStatus(byteBuf.readInt());
			int errorMsgLength = byteBuf.readInt();
			if (errorMsgLength > 0) {
				byte[] errorMsg = new byte[errorMsgLength];
				byteBuf.readBytes(errorMsg);
				setError(new String(errorMsg, UTF_8));
			}
		}

		validate();
	}

	public int getHeaderType() {
		return headerType;
	}

	public void setHeaderType(int headerType) {
		this.headerType = headerType;
	}

	public boolean isNeedAck() {
		return needAck;
	}

	public void setNeedAck(boolean needAck) {
		this.needAck = needAck;
	}

	public int getCommandId() {
		return commandId;
	}

	public void setCommandId(int commandId) {
		this.commandId = commandId;
	}

	public int getCommandType() {
		return commandType;
	}

	public void setCommandType(int commandType) {
		this.commandType = commandType;
	}

	public int getCommandVersion() {
		return commandVersion;
	}

	public void setCommandVersion(int commandVersion) {
		this.commandVersion = commandVersion;
	}

	public long getCommandTime() {
		return commandTime;
	}

	public void setCommandTime(long commandTime) {
		this.commandTime = commandTime;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}
}
