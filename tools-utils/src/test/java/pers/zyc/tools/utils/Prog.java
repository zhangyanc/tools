package pers.zyc.tools.utils;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

class Prog {
  public static void main(String[] args) {
    Callable<Long> callable = new Callable<Long>() {
      public Long call() throws Exception {
        // Allocate, to create some memory pressure.
        byte[][] bytes = new byte[1024][];
        for (int i = 0; i < 1024; i++) {
          bytes[i] = new byte[1024];
        }

        return 42L;
      }
    };
    for (;;) {
      Executors.newSingleThreadExecutor().submit(callable);
    }
  }
}