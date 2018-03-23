package pers.zyc.tools.zkclient.listener;

import java.util.List;

/**
 * @author zhangyancheng
 */
public interface ChildrenListener extends NodeEventListener {

	void onChildrenChanged(String path, List<String> children);
}
