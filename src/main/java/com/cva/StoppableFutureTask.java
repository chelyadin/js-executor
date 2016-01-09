package com.cva;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

/**
 * Specialization of @see FutureTask
 * TODO consider extending or using it to execute scripts
 * Created by zdv on 23.12.15.
 */
public class StoppableFutureTask<V> extends FutureTask<V> {

    private volatile Thread runner = null;
    private Lock runnerMonitor = new ReentrantLock();
    private Condition isStopped = runnerMonitor.newCondition();
    private final Optional<Consumer<StoppableFutureTask<V>>> completionListener;

    /**
     * Creates new future task, which can be executed with @see java.util.concurrent.Executor
     * @param callable to execute
     * @param completionListener is called, when task is completed;
     */
    public StoppableFutureTask(Callable<V> callable, Consumer<StoppableFutureTask<V>> completionListener) {
        super(callable);
        this.completionListener = Optional.ofNullable(completionListener);
    }

    /**
     * this(callable, null)
     * @param callable to execute
     */
    public StoppableFutureTask(Callable<V> callable) {
        this(callable, null);
    }

    /**
     * Tries to cancel this task, and then if it does not stop gracefully in 1 second, stops thread, in which this task is executed.
     * @throws InterruptedException when current thread is interrupted while awaiting for completion
     * @throws IllegalStateException when thread executing this task cannot be stopped
     */
    @SuppressWarnings("deprecation")
    public void stop() throws InterruptedException, IllegalStateException {
        if (runner != null && runner.isAlive()) {
            if( !isCancelled() ) {
                cancel(true);
            }
            runnerMonitor.lock();
            try {
                int attempts = 2;
                isStopped.await(1, TimeUnit.SECONDS);
                while (runner.isAlive() && attempts-- > 0) {
                    runner.stop();
                    isStopped.await(1, TimeUnit.SECONDS);
                }
                if (runner.isAlive()) {
                    throw new IllegalStateException("Cannot forcibly stop thread " + runner.getName() );
                }
            } finally {
                runnerMonitor.unlock();
            }

        }
    }

    /**
     * @see FutureTask#run()
     */
    @Override
    public void run() {
       runnerMonitor.lock();
       try {
           this.runner = Thread.currentThread();
       } finally {
           runnerMonitor.unlock();
       }
       try {
           super.run();
       } finally {
           runnerMonitor.lock();
           isStopped.signalAll();
           runnerMonitor.unlock();
       }
    }

    @Override
    protected void done() {
        super.done();
        if (completionListener.isPresent()) {
            completionListener.get().accept(this);
        }
    }
}
