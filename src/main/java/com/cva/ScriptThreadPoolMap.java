package com.cva;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.ws.rs.NotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jersey.repackaged.com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 * Singleton Contains ConcurrentHashMap<HttpServletRequest, Future<Object>> 
 * with ScriptExecutors
 * 
 * @author CVA
 *
 */
public class ScriptThreadPoolMap {
	private ScriptThreadPoolMap() {
		executorService = Executors.newFixedThreadPool(1, 
				new ThreadFactoryBuilder()
				.setDaemon(true)
				.setNameFormat("js-executor-pool-%d")
				.build());
		scriptMap = new ConcurrentHashMap<>();
	}

	/**
	 * Returns singleton instance of this class
	 * @return singleton
	 */
	synchronized public static ScriptThreadPoolMap getInstance() {
		ScriptThreadPoolMap localInstance = instance;
		if (localInstance == null) {
			synchronized (ScriptThreadPoolMap.class) {
				localInstance = instance;
				if (localInstance == null)
					instance = localInstance = new ScriptThreadPoolMap();
			}
		}
		return localInstance;
	}

	/**
	 * Registers scripts as value with key as uuid
	 * @param uuid
	 * @param javascript
	 * @return FutureKeeper 
	 */
	public FutureKeeper registerScript(String uuid, String javascript, Writer writer) {
		ScriptExecutor scriptExecutor = new ScriptExecutor(javascript, writer);
		// TODO - are you sure you get the right thread here?
		Thread scriptExecutorThread = scriptExecutor.getThread();
		
		Future<Object> scriptFuture = executorService.submit(scriptExecutor);
		
		FutureKeeper futureKeeper = new FutureKeeper(uuid, javascript, scriptFuture, (ChunkedWriter)writer, scriptExecutorThread);
		log.debug("registerScript " + futureKeeper);
		scriptMap.put(uuid, futureKeeper);
		return futureKeeper;
	}

	/**
	 * Returns JavaScript calculation result
	 * @param uuid
	 * @return Object calculation result
	 */
	public Object getScriptResult(String uuid) {
		
		ChunkedWriter chunkedWriter = scriptMap.get(uuid).chunkedWriter;
		String output = chunkedWriter.toString();
		return output;
		
		/*
		Future<Object> future = scriptMap.get(uuid).getFuture();
		Object resultObject = null;
		try {
			resultObject = future.get();
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			resultObject = e;
		}
		return resultObject;
		*/
	}

	/**
	 * Returns flag that script exists
	 * @param uuid
	 * @return boolean flag script existence
	 */
	public boolean isScriptExist(String uuid) {
		return scriptMap.containsKey(uuid);
	}
	
	/**
	 * Returns flag that calculation is finished
	 * @param uuid
	 * @return  boolean calculation finished
	 */
	public boolean isScriptDone(String uuid) {
		FutureKeeper futureKeeper = scriptMap.get(uuid);
		if (futureKeeper==null) throw new NotFoundException("Script not found: " + uuid);
		return futureKeeper.future.isDone();
	}

	/**
	 * Removes Thread calculated script
	 * @param uuid
	 * @throws IOException 
	 */
	public String removeScript(String uuid) throws IOException {
		
		Future<Object> future = null;
		FutureKeeper futureKeeper = scriptMap.get(uuid);
		if (futureKeeper==null) throw new NotFoundException("Script not found: " + uuid);
		future = futureKeeper.future;
		ChunkedWriter chunkedWriter = scriptMap.get(uuid).chunkedWriter;
		String output = chunkedWriter.toString();
		chunkedWriter.close();
		future.cancel(true);
		futureKeeper.stopScriptExecutorThread();
		if (future.isCancelled()||future.isDone()){
			scriptMap.remove(uuid);
		}
		return output;
	}

	/**
	 * Returns iterator from ConcurrentHashMap which contains futures
	 * @return iterator from ConcurrentHashMap 
	 */
	public Iterator<FutureKeeper> getScriptsIterator() {
		return scriptMap.values().iterator();
	}

	private static volatile ScriptThreadPoolMap instance;
	private final ExecutorService executorService;
	private final Map<String, FutureKeeper> scriptMap;
	final static Logger log = LoggerFactory.getLogger(ScriptThreadPoolMap.class);
}
