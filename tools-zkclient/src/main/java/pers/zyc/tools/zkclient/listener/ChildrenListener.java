package pers.zyc.tools.zkclient.listener;

import java.util.List;

/**
 * @author zhangyancheng
 */
public interface ChildrenListener extends NodeListener {

	void onChildrenChanged(String path, List<String> children);
}
