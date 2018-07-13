package pers.zyc.tools.utils.spi;

/**
 * @author zhangyancheng
 */
public class SumCalculator implements SimpleCalculator {

    private static final String TYPE = "SUM";

    @Override
    public long calc(long op1, long op2) {
        return op1 + op2;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public boolean match(String targetType) {
        return TYPE.equalsIgnoreCase(targetType);
    }
}
