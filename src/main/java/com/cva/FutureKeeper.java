package com.cva;

import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FutureKeeper {
	
	transient static final Logger log = LoggerFactory.getLogger(FutureKeeper.class);

	final ChunkedWriter chunkedWriter;
	final Future<Object> future;
	final String scriptBody;
	final Thread scriptExecutorThread;
	final String uuid;
	
	public FutureKeeper(String uuid, String scriptBody, Future<Object> future, ChunkedWriter chunkedWriter,
			Thread scriptExecutorThread) {
		this.uuid = uuid;
		this.scriptBody = scriptBody;
		this.future = future;
		this.chunkedWriter = chunkedWriter;
		this.scriptExecutorThread = scriptExecutorThread;
	}

	@SuppressWarnings("deprecation")
	public void stopScriptExecutorThread() {
		if (scriptExecutorThread.isAlive()) {
			log.debug("stopScriptExecutorThread " + scriptExecutorThread.getName());
			scriptExecutorThread.stop();
		}
	}
	
	@Override
	public String toString() {
		return "FutureKeeper [uuid=" + uuid + ", scriptBody=\n" + scriptBody + "\n, future=" + future
				+ ", scriptExecutorThread=" + scriptExecutorThread + "]";
	}
}
