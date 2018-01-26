package pers.zyc.tools.event;

/**
 * @author zhangyancheng
 */
public class SerialInvoker extends BaseInvoker {

    @Override
    @SuppressWarnings("unchecked")
    protected void doInvoke(Object event, EventListener listener) throws Exception {
        listener.onEvent(event);
    }
}
