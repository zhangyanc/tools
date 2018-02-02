package pers.zyc.spi.plugin;

import java.util.Collection;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author zhangyancheng
 */
public class SpiPluginUtil {

    private static final ConcurrentMap<Class, ServiceLoader> serviceLoaders = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public static <P extends SpiPlugin> Collection<P> loadPlugins(Class<P> pClass) {
        ServiceLoader<P> serviceLoader = serviceLoaders.get(pClass);
        if (serviceLoader == null) {
            ServiceLoader<P> loader = ServiceLoader.load(pClass);
            ServiceLoader<P> perv = serviceLoaders.putIfAbsent(pClass, loader);
            serviceLoader = perv == null ? loader : perv;
        } else {
            serviceLoader.reload();
        }


        return null;
    }
}
