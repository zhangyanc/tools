package pers.zyc.tools.event;

import pers.zyc.tools.utils.NameThreadFactory;

import java.util.concurrent.Executors;

/**
 * @author zhangyancheng
 */
public class SingleThreadInvoker extends ParallelInvoker {

    public SingleThreadInvoker(String name) {
        super(Executors.newSingleThreadExecutor(new NameThreadFactory(name)));
    }
}
