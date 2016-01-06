package com.cva;

import java.io.Writer;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Singleton Contains ConcurrentHashMap<HttpServletRequest, Future<Object>> 
 * with ScriptExecutors
 * 
 * @author CVA
 *
 */
public class ScriptThreadPoolMap {
	private ScriptThreadPoolMap() {
		executorService = Executors.newCachedThreadPool();
		scriptMap = new ConcurrentHashMap<UUID, FutureKeeper>();
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
	 */
	public void registerScript(UUID uuid, String javascript, Writer writer) {
		ScriptExecutor scriptExecutor = new ScriptExecutor(javascript, writer);
		Thread scriptExecutorThread = scriptExecutor.getThread();
		
		Future<Object> scriptFuture = executorService.submit(scriptExecutor);
		
		scriptMap.put(uuid, new FutureKeeper(scriptFuture, (ChunkedWriter)writer, scriptExecutorThread));
	}

	/**
	 * Returns JavaScript calculation result
	 * @param uuid
	 * @return Object calculation result
	 */
	public Object getScriptResult(UUID uuid) {
		
		ChunkedWriter chunkedWriter = scriptMap.get(uuid).getChunkedWriter();
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
	public boolean isScriptExist(UUID uuid) {
		Future<Object> future = null;
		FutureKeeper futureKeeper = scriptMap.get(uuid);
		if (futureKeeper!=null){
			future = futureKeeper.getFuture();
			if (future!=null){
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}	
	}
	
	/**
	 * Returns flag that calculation is finished
	 * @param uuid
	 * @return  boolean calculation finished
	 */
	public boolean isScriptDone(UUID uuid) {
		Future<Object> future = null;
		FutureKeeper futureKeeper = scriptMap.get(uuid);
		if (futureKeeper!=null){
			future = futureKeeper.getFuture();
		}
		
		if (future!=null){
			return future.isDone();
		} else {
			return false;
		}
			
	}

	/**
	 * Removes Thread calculated script
	 * @param uuid
	 */
	public String removeScript(UUID uuid) {
		
		Future<Object> future = null;
		FutureKeeper futureKeeper = scriptMap.get(uuid);
		if (futureKeeper!=null){
			future = futureKeeper.getFuture();		ChunkedWriter chunkedWriter = scriptMap.get(uuid).getChunkedWriter();
			String output = chunkedWriter.toString();
			chunkedWriter.closeChunkedOutput();
			future.cancel(true);
			futureKeeper.stopScriptExecutorThread();
			if (future.isCancelled()||future.isDone()){
				scriptMap.remove(uuid);
			}

			return output;
		} else {
			return "Not exists";
		}
		
	}

	/**
	 * Returns iterator from ConcurrentHashMap which contains futures
	 * @return iterator from ConcurrentHashMap 
	 */
	public Iterator<Entry<UUID, FutureKeeper>> getScriptsIterator() {
		Set<Entry<UUID, FutureKeeper>> set = scriptMap.entrySet();
		Iterator<Entry<UUID, FutureKeeper>> iterator = set.iterator();
		return iterator;
	}

	private static volatile ScriptThreadPoolMap instance;
	private volatile ExecutorService executorService;
	private Map<UUID, FutureKeeper> scriptMap;
}
