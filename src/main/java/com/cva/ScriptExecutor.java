package com.cva;


import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.concurrent.Callable;

import javax.naming.Binding;
import javax.script.Bindings;
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
	
	public ScriptExecutor(String javascript, Writer writer) {
		// TODO Auto-generated constructor stub
		this.javascript = javascript;
		this.writer = writer;
	}

	public Thread getThread(){
		return Thread.currentThread();
	}
	
	@Override
	public Object call() throws ScriptException {
				
		// TODO Auto-generated method stub
		ScriptEngineManager engineManager = new ScriptEngineManager();
		ScriptEngine engine = engineManager.getEngineByName("nashorn");

		engine.getContext().setWriter(writer);
		
		Writer errorWriter = new StringWriter();
		engine.getContext().setErrorWriter(errorWriter);
		
		// Part for long calculation emulation Start-->
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		// Part for long calculation emulation -->End

		Object resultObject;

		engine.eval(javascript);
		
		resultObject = writer.toString();
		if (!((ChunkedWriter)writer).isChunkedOutputClosed())
			((ChunkedWriter)writer).closeChunkedOutput();
		return resultObject;
	}

	String javascript;
	Writer writer;
}
