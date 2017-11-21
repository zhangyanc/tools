package pers.zyc.tools.utils;

import java.util.concurrent.locks.Lock;

/**
 * @author zhangyancheng
 */
public interface Locker<L extends Lock> {
    L getLock();
}
