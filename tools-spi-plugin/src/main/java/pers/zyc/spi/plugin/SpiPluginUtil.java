package pers.zyc.spi.plugin;

import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author zhangyancheng
 */
public class SpiPluginUtil {

    private static final ConcurrentMap<Class, ServiceLoader> serviceLoaders = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public static <P extends SpiPlugin> Iterable<P> loadPlugins(Class<P> pClass) {
        ServiceLoader<P> serviceLoader = serviceLoaders.get(pClass);
        if (serviceLoader == null) {
            ServiceLoader<P> loader = ServiceLoader.load(pClass);
            ServiceLoader<P> perv = serviceLoaders.putIfAbsent(pClass, loader);
            serviceLoader = perv == null ? loader : perv;
        } else {
            serviceLoader.reload();
        }
        final Iterator<P> iterator = serviceLoader.iterator();
        return new Iterable<P>() {
            @Override
            public Iterator<P> iterator() {
                return iterator;
            }
        };
    }

    /**
     * 加载指定类型插件, 如果不存在则返回null
     */
    public static <P extends TypeSpiPlugin<T>, T> P getByType(Class<P> pClass, T type) {
        for (P plugin : loadPlugins(pClass)) {
            if (plugin.match(type)) {
                return plugin;
            }
        }
        return null;
    }
}
