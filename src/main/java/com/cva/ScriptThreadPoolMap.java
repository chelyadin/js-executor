package com.cva;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
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

	// Getter for JavaScript calculation result
	synchronized public Future<Object> getScriptResult(
			HttpServletRequest request) {
		Future<Object> future = scriptMap.get(request);
		if (future.isDone()) {
			scriptMap.remove(request);
		}

		return future;
	}

	// Setter that removes Thread calculated script
	synchronized public void removeScript(HttpServletRequest request) {
		scriptMap.remove(request);
	}

	// Getter for testing goals. Has to be deleted.
	public ExecutorService getExecutorService() {
		return executorService;
	}

	// Getter for testing goals. Has to be deleted.
	public Iterator<Entry<HttpServletRequest, Future<Object>>> getScriptsIterator() {
		Set<Entry<HttpServletRequest, Future<Object>>> set = scriptMap
				.entrySet();
		Iterator<Entry<HttpServletRequest, Future<Object>>> iterator = set
				.iterator();

		return iterator;
	}

	private ExecutorService executorService;
	private static volatile ScriptThreadPoolMap instance;
	private Map<HttpServletRequest, Future<Object>> scriptMap;
}
