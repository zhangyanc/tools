package pers.zyc.tools.redis.client;

import redis.clients.jedis.Protocol;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.util.RedisOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author zhangyancheng
 */
class JedisEncoder {

	static byte[] encode(final byte[] command,
						 final byte[]... args) {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		RedisOutputStream ros = new RedisOutputStream(baos);

		sendCommand(ros, command, args);
		try {
			ros.flush();
		} catch (IOException e) {
			throw new JedisConnectionException(e);
		}

		return baos.toByteArray();
	}

	private static void sendCommand(final RedisOutputStream os, final byte[] command,
									final byte[]... args) {
		try {
			os.write(Protocol.ASTERISK_BYTE);
			os.writeIntCrLf(args.length + 1);
			os.write(Protocol.DOLLAR_BYTE);
			os.writeIntCrLf(command.length);
			os.write(command);
			os.writeCrLf();

			for (final byte[] arg : args) {
				os.write(Protocol.DOLLAR_BYTE);
				os.writeIntCrLf(arg.length);
				os.write(arg);
				os.writeCrLf();
			}
		} catch (IOException e) {
			throw new JedisConnectionException(e);
		}
	}
}
