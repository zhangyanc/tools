package pers.zyc.tools.zkclient;

/**
 * @author zhangyancheng
 */
public interface Elector {

	/**
	 * @return member节点数据
	 */
	byte[] getMemberData();

	/**
	 * @return 选主模式
	 */
	ElectorMode getElectorMode();
}
