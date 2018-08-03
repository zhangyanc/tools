package pers.zyc.tools.zkclient;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * IZooKeeper实现, 代理ZKClient的所有IZookeeper操作, 加锁、状态检查
 *
 * @author zhangyancheng
 */
class DefaultZooKeeper implements ZooKeeperOperations, InvocationHandler {

	final Logger logger = LoggerFactory.getLogger(getClass());

	final ZKClient zkClient;

	DefaultZooKeeper(ZKClient zkClient) {
		this.zkClient = zkClient;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		return invoke(method, args);
	}

	/**
	 * 反射调用到当前对象的具体方法(当前对象作为IZookeeper的实现再调用ZooKeeper执行远程操作)
	 *
	 * @param method 调用方法
	 * @param args 调用参数
	 * @return 执行结果
	 * @throws Exception 反射调用异常
	 */
	protected Object invoke(Method method, Object[] args) throws Exception {
		try {
			return method.invoke(this, args);
		} catch (InvocationTargetException e) {
			//抛出ZooKeeper对象的调用异常(避免包装成UndeclaredThrowableException)
			throw (Exception) e.getTargetException();
		} catch (IllegalAccessException e) {
			throw new ClientException(e);
		}
	}

	@Override
	public String create(String path, byte[] data, CreateMode createMode) throws KeeperException, InterruptedException {
		return zkClient.getZooKeeper().create(path, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, createMode);
	}

	@Override
	public void delete(String path, int version) throws KeeperException, InterruptedException {
		zkClient.getZooKeeper().delete(path, version);
	}

	@Override
	public boolean exists(String path) throws KeeperException, InterruptedException {
		return zkClient.getZooKeeper().exists(path, null) != null;
	}

	@Override
	public Stat exists(String path, Watcher watcher) throws KeeperException, InterruptedException {
		return zkClient.getZooKeeper().exists(path, watcher);
	}

	@Override
	public byte[] getData(String path, Watcher watcher) throws KeeperException, InterruptedException {
		return zkClient.getZooKeeper().getData(path, watcher, null);
	}

	@Override
	public byte[] getData(String path, Watcher watcher, Stat stat) throws KeeperException, InterruptedException {
		return zkClient.getZooKeeper().getData(path, watcher, stat);
	}

	@Override
	public Stat setData(String path, byte[] data, int version) throws KeeperException, InterruptedException {
		return zkClient.getZooKeeper().setData(path, data, version);
	}

	@Override
	public List<String> getChildren(String path, Watcher watcher) throws KeeperException, InterruptedException {
		return zkClient.getZooKeeper().getChildren(path, watcher);
	}

	@Override
	public List<OpResult> multi(Iterable<Op> ops) throws InterruptedException, KeeperException {
		return zkClient.getZooKeeper().multi(ops);
	}
}
