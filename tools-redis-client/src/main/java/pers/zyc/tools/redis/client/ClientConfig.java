package pers.zyc.tools.redis.client;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import pers.zyc.tools.utils.Regex;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * @author zhangyancheng
 */
public class ClientConfig {
	private boolean ssl;
	private String host;
	private String password;
	private int port;
	private int db;
	private int connectionTimeout;
	private int requestTimeout;
	private int netWorkers;
	private GenericObjectPoolConfig poolConfig;

	public ClientConfig(boolean ssl, String host, String password, int port, int db,
						int connectionTimeout, int requestTimeout, int netWorkers,
						GenericObjectPoolConfig poolConfig) {
		this.ssl = ssl;
		this.host = host;
		this.password = password;
		this.port = port;
		this.db = db;
		this.connectionTimeout = connectionTimeout;
		this.requestTimeout = requestTimeout;
		this.netWorkers = netWorkers;
		this.poolConfig = poolConfig;
	}

	public ClientConfig(String connectStr) {
		this(connectStr, new GenericObjectPoolConfig());
	}

	public ClientConfig(String connectStr, GenericObjectPoolConfig poolConfig) {
		this(URI.create(connectStr), poolConfig);
	}

	public ClientConfig(URI uri, GenericObjectPoolConfig poolConfig) {
		this.ssl = getSsl(uri);
		this.host = getHost(uri);
		this.password = getPassword(uri);
		this.port = getPort(uri);
		this.db = getDbIndex(uri);
		this.poolConfig = poolConfig;

		Map<String, String> queries = getQueries(uri);
		String val;
		if ((val = queries.get("connectionTimeout")) != null) {
			this.connectionTimeout = Integer.parseInt(val);
		}
		if ((val = queries.get("requestTimeout")) != null) {
			this.requestTimeout = Integer.parseInt(val);
		}
		if ((val = queries.get("netWorkers")) != null) {
			this.netWorkers = Integer.parseInt(val);
		}
	}

	public boolean isSsl() {
		return ssl;
	}

	public String getHost() {
		return host;
	}

	public String getPassword() {
		return password;
	}

	public int getPort() {
		return port;
	}

	public int getDb() {
		return db;
	}

	public int getConnectionTimeout() {
		return connectionTimeout;
	}

	public int getRequestTimeout() {
		return requestTimeout;
	}

	public int getNetWorkers() {
		return netWorkers;
	}

	public GenericObjectPoolConfig getPoolConfig() {
		return poolConfig;
	}

	private static boolean getSsl(URI uri) {
		String scheme = uri.getScheme();
		switch (scheme) {
			case "redis":
				return false;
			case "rediss":
				return true;
			default:
				throw new IllegalArgumentException("Illegal scheme: " + scheme);
		}
	}

	private static String getHost(URI uri) {
		String host = uri.getHost();
		if (!Regex.DOMAIN.matches(host)) {
			throw new IllegalArgumentException("Illegal host: " + host);
		}
		return host;
	}

	private static int getPort(URI uri) {
		int port = uri.getPort();
		if (port == -1) {
			throw new IllegalArgumentException("Illegal port: " + port);
		}
		return port;
	}

	private static String getPassword(URI uri) {
		String userInfo = uri.getUserInfo();
		if (userInfo != null) {
			return userInfo.split(":", 2)[1];
		}
		return null;
	}

	private static int getDbIndex(URI uri) {
		String[] pathSplit = uri.getPath().split("/", 2);
		if (pathSplit.length > 1) {
			String dbIndexStr = pathSplit[1];
			if (dbIndexStr.isEmpty()) {
				return 0;
			}
			return Integer.parseInt(dbIndexStr);
		} else {
			return 0;
		}
	}

	private static Map<String, String> getQueries(URI uri) {
		Map<String, String> queries = new HashMap<>();

		if (uri.getQuery() == null) {
			return queries;
		}

		Matcher queriesMatcher = Regex.URI_QUERY_PAIR.pattern().matcher(uri.getQuery());
		while (queriesMatcher.find()) {
			queries.put(queriesMatcher.group(1), queriesMatcher.group(2));
		}
		return queries;
	}
}
