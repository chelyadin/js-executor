package com.cva;

import org.junit.Test;

import java.util.concurrent.*;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by zdv on 23.12.15.
 */

public class StoppableFutureTaskTest {

    public static final int EXECUTION_TIMEOUT_SECONDS = 5;

    @Test
    public void testStoppableFuture() throws ExecutionException, InterruptedException {
        StoppableFutureTask<Integer> task = new StoppableFutureTask<>( () -> {
            int i = 0;
            while(i < 100_000_000) {
                try {
                    i++;
                    // comment out next line to see Thread.stop() in action. Thread.sleep() is interruptible, whereas tight calculation cycle isn't
                    // Thread.sleep(100);
                    System.out.println(i);
//                } catch (InterruptedException e) {
//                    System.err.println("Interrupted " + Thread.currentThread().getName());
//                    e.printStackTrace(System.err);
//                    throw e;
                } catch (ThreadDeath d) {
                    System.err.println("Stopped " + Thread.currentThread().getName());
                    d.printStackTrace(System.err);
                    throw d;
                }
            }
            return i;
        });
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(task);
        try {
            task.get(EXECUTION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            assertFalse("We expect exception to be thrown", true);
        } catch (TimeoutException e) {
            task.stop();
        }
        assertTrue(task.isCancelled());
    }
}
