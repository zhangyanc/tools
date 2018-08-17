package pers.zyc.tools.redis.client;

import pers.zyc.tools.redis.client.util.ByteUtil;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author zhangyancheng
 */
public abstract class Request {

	private static final CommandBytesCache COMMAND_BYTES_CACHE = new CommandBytesCache();

	protected final LinkedList<byte[]> parts;
	private final AtomicBoolean finished = new AtomicBoolean();

	protected Request(byte[]... args) {
		parts = new LinkedList<>(Arrays.asList(args));
		parts.addFirst(getCmd());
	}

	private byte[] getCmd() {
		return COMMAND_BYTES_CACHE.getCommandBytes(getCommand());
	}

	protected String getCommand() {
		return getClass().getSimpleName().toUpperCase();
	}

	int partSize() {
		return parts.size();
	}

	byte[] nextPart() {
		return parts.poll();
	}

	@Override
	public String toString() {
		return "Request{" + getCommand() + "}";
	}

	private static class CommandBytesCache {
		ConcurrentMap<String, byte[]> commandBytesCacheMap = new ConcurrentHashMap<>();

		byte[] getCommandBytes(String command) {
			byte[] commandBytes = commandBytesCacheMap.get(command);
			if (commandBytes == null) {
				commandBytes = ByteUtil.toByteArray(command);
				commandBytesCacheMap.put(command, commandBytes);
			}
			return commandBytes;
		}
	}

	boolean finish() {
		return !finished.get() && finished.compareAndSet(false, true);
	}
}
