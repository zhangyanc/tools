package pers.zyc.spi.plugin;

/**
 * @author zhangyancheng
 */
public interface TypeSpiPlugin<T> extends SpiPlugin {

    /**
     * @return 插件类型
     */
    T getType();

    /**
     * 检查是否匹配目标类型
     */
    boolean match(T targetType);
}
