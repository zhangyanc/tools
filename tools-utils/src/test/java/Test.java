import java.util.concurrent.Callable;

/**
 * @author zhangyancheng
 */
public class Test implements Callable<Long> {

    volatile boolean closed = false;

    @Override
    public Long call() throws Exception {
        byte[] ls = new byte[1024 * 1024];
        if (closed) {
            throw new Exception("Closed!");
        }
        return 0L;
    }

    public void close() {
        closed = true;
    }

    static class Delegate implements Callable<Long> {

        private Test test;

        public Delegate(Test test) {
            this.test = test;
        }

        @Override
        protected void finalize() throws Throwable {
            test.close();
        }

        @Override
        public Long call() throws Exception {
            Long result = test.call();
            return result;
        }
    }

    private static Callable create() {
        return new Delegate(new Test());
    }

    public static void main(String[] args) throws Exception {
        while (true) {
            create().call();
        }
    }
}
