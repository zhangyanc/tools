package pers.zyc.tools.zkclient.listener;

import pers.zyc.tools.event.EventListener;

/**
 * 添加成功后如果event source已经准备好, 则触发事件
 *
 * @author zhangyancheng
 */
public interface SpecialEventListener<E> extends EventListener<E> {
}
