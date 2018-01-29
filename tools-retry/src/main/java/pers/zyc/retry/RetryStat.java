package pers.zyc.retry;

import java.util.Objects;

/**
 * @author zhangyancheng
 */
public class RetryStat {
    private long startTime;
    private int alreadyRetryCounts;
    private long loopTime;
    private Exception exception;

    void loop(Exception exception) {
        this.exception = Objects.requireNonNull(exception);
        loopTime = System.currentTimeMillis();
        if (startTime == 0) {
            startTime = loopTime;
        } else {
            alreadyRetryCounts++;
        }
    }

    Exception exception() {
        return exception;
    }

    public long startTime() {
        return startTime;
    }

    public int alreadyRetryCounts() {
        return alreadyRetryCounts;
    }

    public long loopTime() {
        return loopTime;
    }
}
