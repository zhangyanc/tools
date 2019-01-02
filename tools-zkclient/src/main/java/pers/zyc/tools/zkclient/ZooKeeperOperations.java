package pers.zyc.tools.zkclient;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.List;

/**
 * ZooKeeper Api
 *
 * @author zhangyancheng
 */
interface ZooKeeperOperations {

	String create(final String path, byte data[], CreateMode createMode)  throws KeeperException, InterruptedException;

	void delete(String path) throws KeeperException, InterruptedException;

	void delete(String path, int version) throws KeeperException, InterruptedException;

	boolean exists(String path) throws KeeperException, InterruptedException;

	Stat exists(String path, Watcher watcher) throws KeeperException, InterruptedException;

	byte[] getData(String path) throws KeeperException, InterruptedException;

	byte[] getData(String path, Stat stat) throws KeeperException, InterruptedException;

	byte[] getData(String path, Watcher watcher) throws KeeperException, InterruptedException;

	byte[] getData(String path, Watcher watcher, Stat stat) throws KeeperException, InterruptedException;

	Stat setData(String path, byte[] data, int version) throws KeeperException, InterruptedException;

	List<String> getChildren(String path) throws KeeperException, InterruptedException;

	List<String> getChildren(String path, Watcher watcher) throws KeeperException, InterruptedException;

	List<OpResult> multi(Iterable<Op> ops) throws InterruptedException, KeeperException;
}