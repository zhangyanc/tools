package pers.zyc.tools.zkclient;

import pers.zyc.tools.utils.event.SourcedEvent;

/**
 * @author zhangyancheng
 */
public class ElectionEvent extends SourcedEvent<Elector> {

	public final Election.EventType eventType;

	ElectionEvent(Elector source, Election.EventType eventType) {
		super(source);
		this.eventType = eventType;
	}
}
