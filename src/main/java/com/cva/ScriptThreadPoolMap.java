package com.cva;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.servlet.http.*;

/**
 * Singleton Contains Map Map<HttpServletRequest, Future<Object>> with
 * ScriptExecutors
 * 
 * @author CVA
 *
 */
public class ScriptThreadPoolMap {
	private ScriptThreadPoolMap() {
		executorService = Executors.newCachedThreadPool();
		scriptMap = new HashMap<HttpServletRequest, Future<Object>>();
	}

	// Getter for singleton
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

	// Registration scripts as value with key as request
	synchronized public void registerScript(HttpServletRequest request,
			String javascript) {
		Future<Object> scriptFuture = executorService
				.submit(new ScriptExecutor(request, javascript));

		scriptMap.put(request, scriptFuture);

	}

	// Getter of JavaScript calculation result
	synchronized public Future<Object> getScriptResult(
			HttpServletRequest request) {
		Future<Object> future = scriptMap.get(request);
		if (future.isDone()){
			scriptMap.remove(request);
		}
		System.out.println("scriptMap.size(): " + scriptMap.size());
		
		return future;
	}

	// Getter for testing goals. Have to be deleted.
	public ExecutorService getExecutorService() {
		return executorService;
	}
	
	// Getter for testing goals. Have to be deleted.
	public Map<HttpServletRequest, Future<Object>> getScriptMap(){
		return scriptMap;
	}

	private ExecutorService executorService;
	private static volatile ScriptThreadPoolMap instance;
	private Map<HttpServletRequest, Future<Object>> scriptMap;
}
