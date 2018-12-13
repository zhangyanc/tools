package pers.zyc.tools.utils;

import java.util.concurrent.Callable;

/**
 * @author zhangyancheng
 */
public class Prog2 implements Callable<Long> {

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

        private Prog2 prog2;

        public Delegate(Prog2 prog2) {
            this.prog2 = prog2;
        }

        @Override
        protected void finalize() throws Throwable {
            prog2.close();
        }

        @Override
        public Long call() throws Exception {
            Long result = prog2.call();
            return result;
        }
    }

    private static Callable create() {
        return new Delegate(new Prog2());
    }

    public static void main(String[] args) throws Exception {
        while (true) {
            create().call();
        }
    }
}
