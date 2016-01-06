package com.cva;

import java.util.concurrent.Future;

public class FutureKeeper {
	public FutureKeeper(Future<Object> future, ChunkedWriter chunkedWriter,
			Thread scriptExecutorThread) {
		this.future = future;
		this.chunkedWriter = chunkedWriter;
		this.scriptExecutorThread = scriptExecutorThread;
	}

	public Future<Object> getFuture() {
		return future;
	}

	public ChunkedWriter getChunkedWriter() {
		return chunkedWriter;
	}

	public void stopScriptExecutorThread() {
		//if (scriptExecutorThread.isAlive())
		//	scriptExecutorThread.stop();
	}

	private Future<Object> future;
	private ChunkedWriter chunkedWriter;
	private Thread scriptExecutorThread;
}
