package com.cva;

import java.util.concurrent.Callable;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.servlet.http.HttpServletRequest;

/**
 * Class implements Callable interface, method call() returns Object which is a
 * result of JavaScript execution by Naschorn. Source JavaScript sets in
 * constructor.
 * 
 * @author CVA
 *
 */
public class ScriptExecutor implements Callable<Object> {

	public ScriptExecutor(HttpServletRequest request, String javascript) {
		// TODO Auto-generated constructor stub
		this.javascript = javascript;
	}

	@Override
	public Object call() {
		// TODO Auto-generated method stub
		ScriptEngineManager engineManager = new ScriptEngineManager();
		ScriptEngine engine = engineManager.getEngineByName("nashorn");
		
		
		// Part for long calculation emulation Start-->
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		// Part for long calculation emulation -->End
		
		
		try {
			return engine.eval(javascript);
		} catch (ScriptException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	String javascript;
}
