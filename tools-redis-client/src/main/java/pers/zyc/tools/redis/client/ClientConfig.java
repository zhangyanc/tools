package pers.zyc.tools.redis.client;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

/**
 * @author zhangyancheng
 */
public class ClientConfig {
	private String host;
	private String password;
	private int port;
	private int db;
	private int connectionTimeout;
	private int requestTimeout;
	private int netWorkers;
	private GenericObjectPoolConfig poolConfig;

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getDb() {
		return db;
	}

	public void setDb(int db) {
		this.db = db;
	}

	public int getConnectionTimeout() {
		return connectionTimeout;
	}

	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	public int getRequestTimeout() {
		return requestTimeout;
	}

	public void setRequestTimeout(int requestTimeout) {
		this.requestTimeout = requestTimeout;
	}

	public int getNetWorkers() {
		return netWorkers;
	}

	public void setNetWorkers(int netWorkers) {
		this.netWorkers = netWorkers;
	}

	public GenericObjectPoolConfig getPoolConfig() {
		return poolConfig;
	}

	public void setPoolConfig(GenericObjectPoolConfig poolConfig) {
		this.poolConfig = poolConfig;
	}
}
