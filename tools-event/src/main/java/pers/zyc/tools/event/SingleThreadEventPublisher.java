package pers.zyc.tools.event;

import pers.zyc.tools.utils.NameThreadFactory;

import java.util.concurrent.Executors;

/**
 * 单线程并行发布
 *
 * @author zhangyancheng
 */
public class SingleThreadEventPublisher extends ParallelEventPublisher {

    public SingleThreadEventPublisher(String name) {
        super(Executors.newSingleThreadExecutor(new NameThreadFactory(name)));
    }

    public SingleThreadEventPublisher(String name, PublishExceptionHandler publishExceptionHandler) {
        super(Executors.newSingleThreadExecutor(new NameThreadFactory(name)), publishExceptionHandler);
    }
}
