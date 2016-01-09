package com.cva;

import java.io.IOException;
import java.io.Writer;
import java.util.concurrent.Callable;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

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
	}

	public Thread getThread() {
		// TODO try storing script's thread from call()
		return Thread.currentThread();
	}

	@Override
	public Object call() throws ScriptException, IOException {

		try {
			ScriptEngine engine = engineManager.getEngineByName("nashorn");
			if (null == engine)
				throw new IllegalStateException("no nashorn engine found");

			ScriptContext context = engine.getContext();
			context.setWriter(writer);
			context.setErrorWriter(writer);

			engine.eval(javascript);

			return writer.toString();
		} finally {
			writer.close();
		}
	}

	final String javascript;
	final Writer writer;
}
