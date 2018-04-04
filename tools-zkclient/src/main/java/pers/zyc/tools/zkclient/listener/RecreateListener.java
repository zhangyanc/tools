package pers.zyc.tools.zkclient.listener;

/**
 * 临时节点重建监听器
 *
 * @see pers.zyc.tools.zkclient.EphemeralNodeReCreator
 * @author zhangyancheng
 */
public interface RecreateListener {

	/**
	 * 临时节点重建成功
	 *
	 * @param path 节点路径
	 * @param actualPath 实际路径
	 */
	void onRecreateSuccessful(String path, String actualPath);

	/**
	 * 临时节点重建失败
	 *
	 * @param path 节点路径
	 * @param exception 异常
	 */
	void onRecreateFailed(String path, Exception exception);
}
