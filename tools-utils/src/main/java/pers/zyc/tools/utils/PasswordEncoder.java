package pers.zyc.tools.utils;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author zhangyancheng
 */
public class PasswordEncoder {
	private static final int HEX = 16;
	private static final int SALT_LENGTH = 8;

	private final byte[] secret;
	private final String algorithm;

	public PasswordEncoder(String secret) {
		this.secret = secret.getBytes();
		this.algorithm = "SHA";
	}

	public PasswordEncoder(String secret, String algorithm) {
		this.secret = secret.getBytes();
		this.algorithm = algorithm;
		//fast-fail
		createDigest(algorithm);
	}

	private static MessageDigest createDigest(String algorithm) {
		try {
			return MessageDigest.getInstance(algorithm);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
	}

	private static byte[] createSalt(int saltLength) {
		byte[] salt = new byte[saltLength];
		ThreadLocalRandom.current().nextBytes(salt);
		return salt;
	}

	private static String hexEncode(byte[] bytes) {
		char[] chars = new char[bytes.length << 1];
		int i = 0;
		for (int b : bytes) {
			chars[i++] = Character.forDigit((b & 0xf0) >> 4, HEX);
			chars[i++] = Character.forDigit((b & 0x0f), HEX);
		}
		return new String(chars);
	}

	private static byte[] hexDecode(String hexString) {
		char[] chars = hexString.toCharArray();
		byte[] bytes = new byte[chars.length >> 1];
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = (byte) ((Character.digit(chars[i << 1], HEX) << 4) | Character.digit(chars[(i << 1) + 1], HEX));
		}
		return bytes;
	}

	public String encode(String password) {
		return hexEncode(digest(password, createSalt(SALT_LENGTH)));
	}

	public boolean match(String password, String encodedPwd) {
		byte[] decodedBytes = hexDecode(encodedPwd);
		byte[] salt = new byte[SALT_LENGTH];
		System.arraycopy(decodedBytes, decodedBytes.length - SALT_LENGTH, salt, 0, SALT_LENGTH);
		return MessageDigest.isEqual(decodedBytes, digest(password, salt));
	}

	private byte[] digest(String password, byte[] salt) {
		MessageDigest messageDigest = createDigest(algorithm);
		messageDigest.update(salt);
		messageDigest.update(secret);
		messageDigest.update(password.getBytes(Charset.forName("UTF-8")));
		byte[] digest = messageDigest.digest(messageDigest.digest());
		digest = Arrays.copyOf(digest, digest.length + SALT_LENGTH);
		System.arraycopy(salt, 0, digest, digest.length - SALT_LENGTH, SALT_LENGTH);
		return digest;
	}
}
