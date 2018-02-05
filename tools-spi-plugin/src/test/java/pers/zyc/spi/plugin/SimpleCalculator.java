package pers.zyc.spi.plugin;

/**
 * @author zhangyancheng
 */
public interface SimpleCalculator extends CommonSpiPlugin {

    long calc(long op1, long op2);
}
