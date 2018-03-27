package pers.zyc.tools.zkclient;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.List;
import static org.apache.zookeeper.AsyncCallback.*;

/**
 * ZooKeeper原始api
 *
 * 因为实现可能使用了反射、重试、状态检查等, 由此引出的异常统一为ClientException
 *
 * @author zhangyancheng
 */
interface IZookeeper {

	String createPersistent(String path, byte[] data, boolean sequential) throws
			KeeperException, InterruptedException, ClientException;

	String createEphemeral(String path, byte[] data, boolean sequential) throws
			KeeperException, InterruptedException, ClientException;

	void delete(String path) throws KeeperException, InterruptedException, ClientException;

	void delete(String path, int version) throws KeeperException, InterruptedException, ClientException;

	void delete(String path, int version, VoidCallback cb, Object ctx) throws ClientException;

	boolean exists(String path) throws KeeperException, InterruptedException, ClientException;

	Stat exists(String path, Watcher watcher) throws KeeperException, InterruptedException, ClientException;

	void exists(String path, Watcher watcher, StatCallback cb, Object ctx) throws ClientException;

	byte[] getData(String path) throws KeeperException, InterruptedException, ClientException;

	byte[] getData(String path, Watcher watcher) throws KeeperException, InterruptedException, ClientException;

	byte[] getData(String path, Watcher watcher, Stat stat) throws
			KeeperException, InterruptedException, ClientException;

	void getData(String path, Watcher watcher, DataCallback cb, Object ctx) throws ClientException;

	Stat setData(String path, byte[] data, int version) throws KeeperException, InterruptedException, ClientException;

	void setData(String path, byte data[], int version, StatCallback cb, Object ctx) throws ClientException;

	List<String> getChildren(String path) throws KeeperException, InterruptedException, ClientException;

	List<String> getChildren(String path, Watcher watcher) throws
			KeeperException, InterruptedException, ClientException;

	void getChildren(String path, Watcher watcher, ChildrenCallback cb, Object ctx) throws ClientException;

	void getChildren(String path, Watcher watcher, Children2Callback cb, Object ctx) throws ClientException;

	void sync(final String path, VoidCallback cb, Object ctx) throws ClientException;

	Transaction transaction() throws ClientException;

	List<OpResult> multi(Iterable<Op> ops) throws InterruptedException, KeeperException, ClientException;
}