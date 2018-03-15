package pers.zyc.tools.zkclient.event;

/**
 * @author zhangyancheng
 */
public enum NodeEventType {
	//节点状态变更类型(从org.apache.zookeeper.Watcher.Event.EventType复制)
	Created, Deleted, DataChanged, ChildrenChanged;
}
