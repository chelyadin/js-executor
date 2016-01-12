package com.cva;

import java.io.IOException;
import java.io.Writer;
import java.util.concurrent.Callable;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class implements Callable interface, method call() returns Object which is a
 * result of JavaScript execution by Naschorn. Source JavaScript sets in
 * constructor.
 * 
 * @author CVA
 *
 */
public class ScriptExecutor implements Callable<Object> {

	static final ScriptEngineManager engineManager = new ScriptEngineManager();

	public ScriptExecutor(String javascript, Writer writer) {
		this.javascript = javascript;
		this.writer = writer;
		this.callingThread = null;
	}

	public Thread getThread() {
		return callingThread;
	}

	
	@Override
	public Object call() throws IOException { // throws ScriptException,

		this.callingThread = Thread.currentThread();
		log.debug("callingThread " + callingThread.getName());

		try {
			ScriptEngine engine = engineManager.getEngineByName("nashorn");
			if (null == engine)
				throw new IllegalStateException("no nashorn engine found");

			ScriptContext context = engine.getContext();
			context.setWriter(writer);
			context.setErrorWriter(writer);

			engine.eval(javascript);
		} catch (ScriptException e) {
			((ChunkedWriter)writer).setFinishedWithException(true);
			writer.write(e.getMessage());
		} finally {
			writer.close();
		}
		return writer;

	}

	final String javascript;
	final Writer writer;
	private Thread callingThread;
	final static Logger log = LoggerFactory.getLogger(ScriptExecutor.class);
}
