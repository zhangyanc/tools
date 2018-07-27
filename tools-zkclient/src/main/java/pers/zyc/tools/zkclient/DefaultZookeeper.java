package pers.zyc.tools.zkclient;

import org.apache.zookeeper.AsyncCallback.*;
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
class DefaultZookeeper implements IZookeeper, InvocationHandler {

	final Logger logger = LoggerFactory.getLogger(getClass());

	final ZKConnector connector;

	DefaultZookeeper(ZKConnector connector) {
		this.connector = connector;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		connector.checkRunning();
		return doInvoke(method, args);
	}

	/**
	 * 反射调用到当前对象的具体方法(当前对象作为IZookeeper的实现再调用ZooKeeper执行远程操作)
	 *
	 * @param method 调用方法
	 * @param args 调用参数
	 * @return 执行结果
	 * @throws Exception 反射调用异常
	 */
	protected Object doInvoke(Method method, Object[] args) throws Exception {
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
	public String createPersistent(String path, byte[] data, boolean sequential) throws
			KeeperException, InterruptedException {

		return connector.zooKeeper.create(path, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.fromFlag(sequential ? 2 : 0));
	}

	@Override
	public String createEphemeral(String path, byte[] data, boolean sequential) throws
			KeeperException, InterruptedException {

		return connector.zooKeeper.create(path, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.fromFlag(sequential ? 3 : 1));
	}

	@Override
	public void delete(String path) throws KeeperException, InterruptedException {
		delete(path, -1);
	}

	@Override
	public void delete(String path, int version) throws KeeperException, InterruptedException {
		connector.zooKeeper.delete(path, version);
	}

	@Override
	public void delete(String path, int version, VoidCallback cb, Object ctx) {
		connector.zooKeeper.delete(path, version, cb, ctx);
	}

	@Override
	public boolean exists(String path) throws KeeperException, InterruptedException {
		return exists(path, null) != null;
	}

	@Override
	public Stat exists(String path, Watcher watcher) throws KeeperException, InterruptedException {
		return connector.zooKeeper.exists(path, watcher);
	}

	@Override
	public void exists(String path, Watcher watcher, StatCallback cb, Object ctx) {
		connector.zooKeeper.exists(path, watcher, cb, ctx);
	}

	@Override
	public byte[] getData(String path) throws KeeperException, InterruptedException {
		return getData(path, null);
	}

	@Override
	public byte[] getData(String path, Watcher watcher) throws KeeperException, InterruptedException {
		return connector.zooKeeper.getData(path, watcher, null);
	}

	@Override
	public byte[] getData(String path, Watcher watcher, Stat stat) throws KeeperException, InterruptedException {
		return connector.zooKeeper.getData(path, watcher, stat);
	}

	@Override
	public void getData(String path, Watcher watcher, DataCallback cb, Object ctx) {
		connector.zooKeeper.getData(path, watcher, cb, ctx);
	}

	@Override
	public Stat setData(String path, byte[] data, int version) throws KeeperException, InterruptedException {
		return connector.zooKeeper.setData(path, data, version);
	}

	@Override
	public void setData(String path, byte[] data, int version, StatCallback cb, Object ctx) {
		connector.zooKeeper.setData(path, data, version, cb, ctx);
	}

	@Override
	public List<String> getChildren(String path) throws KeeperException, InterruptedException {
		return getChildren(path, null);
	}

	@Override
	public List<String> getChildren(String path, Watcher watcher) throws KeeperException, InterruptedException {
		return connector.zooKeeper.getChildren(path, watcher);
	}

	@Override
	public void getChildren(String path, Watcher watcher, ChildrenCallback cb, Object ctx) {
		connector.zooKeeper.getChildren(path, watcher, cb, ctx);
	}

	@Override
	public void getChildren(String path, Watcher watcher, Children2Callback cb, Object ctx) {
		connector.zooKeeper.getChildren(path, watcher, cb, ctx);
	}

	@Override
	public void sync(String path, VoidCallback cb, Object ctx) {
		connector.zooKeeper.sync(path, cb, ctx);
	}

	@Override
	public Transaction transaction() {
		return connector.zooKeeper.transaction();
	}

	@Override
	public List<OpResult> multi(Iterable<Op> ops) throws InterruptedException, KeeperException {
		return connector.zooKeeper.multi(ops);
	}
}
