package pers.zyc.tools.zkclient.listener;

import pers.zyc.tools.utils.event.Listener;

/**
 * @author zhangyancheng
 */
public interface ClientDestroyListener extends Listener {

	void onDestroy();
}
