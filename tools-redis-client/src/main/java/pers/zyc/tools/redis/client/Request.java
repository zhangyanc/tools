package pers.zyc.tools.redis.client;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author zhangyancheng
 */
public abstract class Request {

	private final CommandBytesCache commandBytesCache = new CommandBytesCache();

	private final byte[][] args;

	protected Request(byte[]... args) {
		this.args = args;
	}

	byte[] getCmd() {
		return commandBytesCache.getCommandBytes(getCommand());
	}

	protected String getCommand() {
		return getClass().getSimpleName().toUpperCase();
	}

	byte[][] getArgs() {
		return args;
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
