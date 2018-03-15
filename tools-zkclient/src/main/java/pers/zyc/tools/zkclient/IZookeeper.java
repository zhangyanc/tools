package pers.zyc.tools.zkclient;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.List;
import static org.apache.zookeeper.AsyncCallback.*;

interface IZookeeper {
	String createPersistent(String path, byte[] data, boolean sequential) throws KeeperException, InterruptedException;
	String createEphemeral(String path, byte[] data, boolean sequential)
			throws KeeperException, InterruptedException;
	String createLive(String path, byte[] data, boolean sequential, RecreationListener recreationListener)
			throws KeeperException, InterruptedException;

	void delete(String path) throws KeeperException, InterruptedException;
	void delete(String path, int version) throws KeeperException, InterruptedException;
	void delete(String path, int version, VoidCallback cb, Object ctx);
	boolean exists(String path) throws KeeperException, InterruptedException;
	Stat exists(String path, Watcher watcher) throws KeeperException, InterruptedException;
	void exists(String path, Watcher watcher, StatCallback cb, Object ctx);
	byte[] getData(String path) throws KeeperException, InterruptedException;
	byte[] getData(String path, Watcher watcher) throws KeeperException, InterruptedException;
	void getData(String path, Watcher watcher, DataCallback cb, Object ctx);
	Stat setData(String path, byte[] data, int version) throws KeeperException, InterruptedException;
	void setData(String path, byte data[], int version, StatCallback cb, Object ctx);
	List<String> getChildren(String path) throws KeeperException, InterruptedException;
	List<String> getChildren(String path, Watcher watcher) throws KeeperException, InterruptedException;
	void getChildren(String path, Watcher watcher, ChildrenCallback cb, Object ctx);
	void getChildren(String path, Watcher watcher, Children2Callback cb, Object ctx);
	void sync(final String path, VoidCallback cb, Object ctx);
	Transaction transaction();
	List<OpResult> multi(Iterable<Op> ops) throws InterruptedException, KeeperException;
}