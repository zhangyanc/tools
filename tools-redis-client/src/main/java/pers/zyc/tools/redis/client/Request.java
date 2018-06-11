package pers.zyc.tools.redis.client;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author zhangyancheng
 */
public abstract class Request {

	private final CommandBytesCache commandBytesCache = new CommandBytesCache();

	private final LinkedList<byte[]> parts;

	protected Request(byte[]... args) {
		parts = new LinkedList<>(Arrays.asList(args));
		parts.addFirst(getCmd());
	}

	private byte[] getCmd() {
		return commandBytesCache.getCommandBytes(getCommand());
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

	private static class CommandBytesCache {
		ConcurrentMap<String, byte[]> commandBytesCacheMap = new ConcurrentHashMap<>();

		byte[] getCommandBytes(String command) {
			byte[] commandBytes = commandBytesCacheMap.get(command);
			if (commandBytes == null) {
				commandBytes = Protocol.toByteArray(command);
				commandBytesCacheMap.put(command, commandBytes);
			}
			return commandBytes;
		}
	}
}
