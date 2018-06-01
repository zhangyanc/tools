package pers.zyc.tools.redis.client.request;

import pers.zyc.tools.redis.client.Protocol;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author zhangyancheng
 */
public abstract class Request {

	private final CommandBytesCache commandBytesCache = new CommandBytesCache();

	private final byte[][] args;

	Request(byte[]... args) {
		this.args = args;
	}

	public byte[] getCmd() {
		return commandBytesCache.getCommandBytes(getCommand());
	}

	String getCommand() {
		return getClass().getSimpleName().toUpperCase();
	}

	public byte[][] getArgs() {
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
