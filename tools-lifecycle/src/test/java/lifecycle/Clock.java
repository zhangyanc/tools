package lifecycle;

import pers.zyc.tools.lifecycle.PeriodicService;
import pers.zyc.tools.lifecycle.ServiceException;
import pers.zyc.tools.lifecycle.ServiceState;

/**
 * @author zhangyancheng
 */
public class Clock extends PeriodicService {

    private Clock() {
    }

    private volatile long timeMillis = 0;

    @Override
    protected Periodic createPeriodic() {
        return new Periodic() {

            @Override
            protected long getPeriod() {
                return 1;
            }

            @Override
            protected void execute() throws InterruptedException {
                timeMillis = System.currentTimeMillis();
            }
        };
    }

    @Override
    public String getName() {
        return "CLOCK";
    }

    @Override
    protected void doStart() throws Exception {
        //标记一下当前时间, 不然start后可能读到0(periodic未工作前)
        timeMillis = System.currentTimeMillis();
    }

    private static final Clock CLOCK = new Clock();

    public static Clock getClock() {
        return CLOCK;
    }

    public long now() {
        checkRunning();
        return timeMillis;
    }

    public static void main(String[] args) throws InterruptedException {
        Clock clock = Clock.getClock();
        try {
            clock.now();
            throw new RuntimeException();
        } catch (ServiceException.NotRunningException must) {
        }
        if (clock.getState() != ServiceState.NEW) {
            throw new RuntimeException();
        }

        clock.start();
        clock.checkRunning();
        clock.now();

        for (int i = 0; i < 10; i++) {
            System.out.println(clock.now() + " <= " + System.currentTimeMillis());
            Thread.sleep((long) ((Math.random() + 0.0001) * 10000));
        }

        clock.stop();
        if (clock.getState() != ServiceState.STOPPED) {
            throw new RuntimeException();
        }
        try {
            clock.now();
        } catch (ServiceException.NotRunningException must) {
        }
    }
}
