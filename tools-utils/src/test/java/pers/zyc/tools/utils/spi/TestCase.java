package pers.zyc.tools.utils.spi;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

/**
 * @author zhangyancheng
 */
public class TestCase {

    @Test
    public void case0() {
        ArrayList<SimpleCalculator> calculators = new ArrayList<>();
        for (SimpleCalculator calculator : SpiPluginUtil.loadPlugins(SimpleCalculator.class)) {
            calculators.add(calculator);
        }
        Assert.assertTrue(calculators.size() == 2);
    }

    @Test
    public void case1() {
        SumCalculator sumCalculator = SpiPluginUtil.getByType(SimpleCalculator.class, "sum");
        Assert.assertNotNull(sumCalculator);
        Assert.assertTrue(sumCalculator.calc(1, 1) == 2);
        Assert.assertTrue(sumCalculator.calc(1, -1) == 0);
        Assert.assertTrue(sumCalculator.calc(3, 7) == 10);

        AvgCalculator avgCalculator = SpiPluginUtil.getByType(SimpleCalculator.class, "avg");
        Assert.assertNotNull(avgCalculator);
        Assert.assertTrue(avgCalculator.calc(1, 1) == 1);
        Assert.assertTrue(avgCalculator.calc(1, -1) == 0);
        Assert.assertTrue(avgCalculator.calc(3, 7) == 5);
    }

    @Test
    public void case2() {
        try {
            SumCalculator sumCalculator = SpiPluginUtil.getByType(SimpleCalculator.class, "avg");
            Assert.fail("Error type!");
            sumCalculator.calc(1, 1);
        } catch (Exception e) {
            Assert.assertTrue(e.getClass().isAssignableFrom(ClassCastException.class));
        }
    }
}
