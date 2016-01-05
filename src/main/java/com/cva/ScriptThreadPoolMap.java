package com.cva;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
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
		scriptMap = new ConcurrentHashMap<UUID, Future<Object>>();
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
	public void registerScript(UUID uuid, String javascript) {
		Future<Object> scriptFuture = executorService
				.submit(new ScriptExecutor(javascript));
		scriptMap.put(uuid, scriptFuture);
	}

	/**
	 * Returns JavaScript calculation result
	 * @param uuid
	 * @return Object calculation result
	 */
	public Object getScriptResult(UUID uuid) {
		Future<Object> future = scriptMap.get(uuid);
		Object resultObject = null;
		try {
			resultObject = future.get();
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return resultObject;
	}

	/**
	 * Returns flag that calculation is finished
	 * @param uuid
	 * @return  boolean calculation finished
	 */
	public boolean isScriptDone(UUID uuid) {
		Future<Object> future = scriptMap.get(uuid);
		return future.isDone();
	}

	/**
	 * Removes Thread calculated script
	 * @param uuid
	 */
	public void removeScript(UUID uuid) {
		Future<Object> future = scriptMap.get(uuid);
		future.cancel(true);
		scriptMap.remove(uuid);
	}

	/**
	 * Returns iterator from ConcurrentHashMap which contains futures
	 * @return iterator from ConcurrentHashMap 
	 */
	public Iterator<Entry<UUID, Future<Object>>> getScriptsIterator() {
		Set<Entry<UUID, Future<Object>>> set = scriptMap.entrySet();
		Iterator<Entry<UUID, Future<Object>>> iterator = set.iterator();
		return iterator;
	}

	private static volatile ScriptThreadPoolMap instance;
	private volatile ExecutorService executorService;
	private Map<UUID, Future<Object>> scriptMap;
}
