package pers.zyc.tools.zkclient.event;

import pers.zyc.tools.event.SourcedEvent;
import pers.zyc.tools.zkclient.AbstractZKEventConverter;

/**
 * @author zhangyancheng
 */
public class PathEvent extends SourcedEvent<AbstractZKEventConverter> {

    public PathEvent(AbstractZKEventConverter source) {
        super(source);
    }
}
