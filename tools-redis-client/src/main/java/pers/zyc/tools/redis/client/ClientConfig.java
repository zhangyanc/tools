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
	private int connectionTimeout = 60 * 1000;
	private int requestTimeout = 60 * 1000;
	private int netWorkers = 1;
	private int requestTimeoutDetectInterval = 1000;

	private boolean needPreparePool = false;
	private int maxConnectionTotal = GenericObjectPoolConfig.DEFAULT_MAX_TOTAL;
	private int minConnectionIdle = GenericObjectPoolConfig.DEFAULT_MIN_IDLE;

	public ClientConfig(boolean ssl,
						String host,
						String password,
						int port,
						int db,
						int connectionTimeout,
						int requestTimeout,
						int netWorkers,
						int requestTimeoutDetectInterval,
						boolean needPreparePool,
						int maxConnectionTotal,
						int minConnectionIdle) {
		this.ssl = ssl;
		this.host = host;
		this.password = password;
		this.port = port;
		this.db = db;
		this.connectionTimeout = connectionTimeout;
		this.requestTimeout = requestTimeout;
		this.netWorkers = netWorkers;
		this.requestTimeoutDetectInterval = requestTimeoutDetectInterval;
		this.needPreparePool = needPreparePool;
		this.maxConnectionTotal = maxConnectionTotal;
		this.minConnectionIdle = minConnectionIdle;

		validate();
	}

	public ClientConfig(String connectStr) {
		this(URI.create(connectStr));
	}

	public ClientConfig(URI uri) {
		this.ssl = getSsl(uri);
		this.host = getHost(uri);
		this.password = getPassword(uri);
		this.port = getPort(uri);
		this.db = getDbIndex(uri);

		String query = uri.getQuery();
		if (query == null) {
			return;
		}

		Map<String, String> queries = getQueries(query);
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
		if ((val = queries.get("requestTimeoutDetectInterval")) != null) {
			this.requestTimeoutDetectInterval = Integer.parseInt(val);
		}
		if ((val = queries.get("needPreparePool")) != null) {
			this.needPreparePool = Boolean.parseBoolean(val);
		}
		if ((val = queries.get("maxConnectionTotal")) != null) {
			this.maxConnectionTotal = Integer.parseInt(val);
		}
		if ((val = queries.get("minConnectionIdle")) != null) {
			this.minConnectionIdle = Integer.parseInt(val);
		}

		validate();
	}

	private void validate() {
		if (host == null) {
			throw new IllegalArgumentException("host can't be null");
		}
		if (port < 0 || port > 0xFFFF) {
			throw new IllegalArgumentException("port out of range: " + port);
		}
		if (db < 0) {
			throw new IllegalArgumentException("db can't be negative: " + db);
		}
		if (connectionTimeout <= 0) {
			throw new IllegalArgumentException("connectionTimeout must be positive: " + connectionTimeout);
		}
		if (requestTimeout <= 0) {
			throw new IllegalArgumentException("requestTimeout must be positive: " + requestTimeout);
		}
		if (netWorkers <= 0) {
			throw new IllegalArgumentException("netWorkers must be positive: " + netWorkers);
		}
		if (requestTimeoutDetectInterval <= 0) {
			throw new IllegalArgumentException("requestTimeoutDetectInterval must be positive: " + requestTimeoutDetectInterval);
		}
		if (maxConnectionTotal <= 0) {
			throw new IllegalArgumentException("maxConnectionTotal must be positive: " + maxConnectionTotal);
		}
		if (minConnectionIdle <= 0) {
			throw new IllegalArgumentException("minConnectionIdle must be positive: " + minConnectionIdle);
		}
	}

	GenericObjectPoolConfig createPoolConfig() {
		GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
		poolConfig.setMaxWaitMillis(getConnectionTimeout());
		poolConfig.setMaxTotal(getMaxConnectionTotal());
		poolConfig.setMaxIdle(getMaxConnectionTotal());
		poolConfig.setMinIdle(getMinConnectionIdle());

		poolConfig.setTestWhileIdle(true);
		poolConfig.setTimeBetweenEvictionRunsMillis(1000 * 30);
		return poolConfig;
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

	public int getRequestTimeoutDetectInterval() {
		return requestTimeoutDetectInterval;
	}

	public boolean isNeedPreparePool() {
		return needPreparePool;
	}

	public int getMaxConnectionTotal() {
		return maxConnectionTotal;
	}

	public int getMinConnectionIdle() {
		return minConnectionIdle;
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
		return uri.getUserInfo();
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

	private static Map<String, String> getQueries(String query) {
		Map<String, String> queries = new HashMap<>();
		Matcher queriesMatcher = Regex.URI_QUERY_PAIR.pattern().matcher(query);
		while (queriesMatcher.find()) {
			queries.put(queriesMatcher.group(1), queriesMatcher.group(2));
		}
		return queries;
	}
}
