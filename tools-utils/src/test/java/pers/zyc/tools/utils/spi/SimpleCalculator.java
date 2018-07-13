package pers.zyc.tools.utils.spi;

/**
 * @author zhangyancheng
 */
public interface SimpleCalculator extends CommonSpiPlugin {

    long calc(long op1, long op2);
}
