package com.cva;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FutureKeeper {
	
	transient static final Logger log = LoggerFactory.getLogger(FutureKeeper.class);

	final ChunkedWriter chunkedWriter;
	final Future<Object> future;
	final String scriptBody;
//	final Thread scriptExecutorThread;
	final ScriptExecutor scriptExecutor;
	final String uuid;
	private Lock runnerMonitor = new ReentrantLock();
	private Condition isStopped = runnerMonitor.newCondition();
	
	public FutureKeeper(String uuid, String scriptBody, Future<Object> future, ChunkedWriter chunkedWriter,
			ScriptExecutor scriptExecutor) {
		this.uuid = uuid;
		this.scriptBody = scriptBody;
		this.future = future;
		this.chunkedWriter = chunkedWriter;
	//	this.scriptExecutorThread = scriptExecutorThread;
		this.scriptExecutor = scriptExecutor;
	}

	@SuppressWarnings("deprecation")
	public void stopScriptExecutorThread() throws IllegalStateException, InterruptedException {
		Thread runner = scriptExecutor.getThread();
		if (runner != null && runner.isAlive()) {
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
	
	@Override
	public String toString() {
		return "FutureKeeper [uuid=" + uuid + ", scriptBody=\n" + scriptBody + "\n, future=" + future
				+ ", scriptExecutorThread=" + ((scriptExecutor != null) && (scriptExecutor.getThread() != null) ? scriptExecutor.getThread().getName() : "") + "]";
	}
}
