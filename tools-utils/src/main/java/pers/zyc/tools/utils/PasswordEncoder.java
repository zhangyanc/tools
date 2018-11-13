package pers.zyc.tools.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author zhangyancheng
 */
public class PasswordEncoder {
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

	private MessageDigest createDigest(String algorithm) {
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
		char[] chars = new char[bytes.length * 2];
		int i = 0;
		for (int b : bytes) {
			chars[i++] = Character.forDigit((b & 0xff) >> 4, 16);
			chars[i++] = Character.forDigit((b & 0x0f), 16);
		}
		return new String(chars);
	}

	private static byte[] hexDecode(String hexString) {
		char[] chars = hexString.toCharArray();
		byte[] bytes = new byte[chars.length / 2];
		for (int i = 0; i < bytes.length; i++) {
			int j = i * 2;
			bytes[i] = (byte) ((Character.digit(chars[j], 16) << 4) | Character.digit(chars[j + 1], 16));
		}
		return bytes;
	}

	public String encode(String originalPwd) {
		return hexEncode(digest(originalPwd.getBytes(), createSalt(SALT_LENGTH)));
	}

	public boolean match(String originalPwd, String encodedPwd) {
		byte[] decodedBytes = hexDecode(encodedPwd);
		byte[] salt = new byte[SALT_LENGTH];
		System.arraycopy(decodedBytes, decodedBytes.length - 8, salt, 0, SALT_LENGTH);
		return Arrays.equals(decodedBytes, digest(originalPwd.getBytes(), salt));
	}

	private byte[] digest(byte[] pwdBytes, byte[] salt) {
		byte[] digest = new byte[pwdBytes.length + secret.length + salt.length];
		System.arraycopy(pwdBytes, 0, digest, 0, pwdBytes.length);
		System.arraycopy(secret, 0, digest, pwdBytes.length, secret.length);
		System.arraycopy(salt, 0, digest, pwdBytes.length + secret.length, salt.length);
		MessageDigest messageDigest = createDigest(algorithm);
		for (int i = 0; i < 3; i++) {
			digest = messageDigest.digest(digest);
		}
		digest = Arrays.copyOf(digest, digest.length + SALT_LENGTH);
		System.arraycopy(salt, 0, digest, digest.length - SALT_LENGTH, SALT_LENGTH);
		return digest;
	}
}
