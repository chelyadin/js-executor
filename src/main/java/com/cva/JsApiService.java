package com.cva;

import java.io.IOException;
import java.net.HttpRetryException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.sun.research.ws.wadl.Response;

/**
 * Class is a RootResource. It contains Thread Pool for running several
 * JavaScripts in different threads. Runs like
 * "127.0.0.1:8080/JsExecutor/api/execute/". JavaScript sends like a parameter
 * inside POST method body
 * 
 * @author CVA
 *
 */
@Path("/execute")
public class JsApiService {

	public JsApiService() {
		// TODO Auto-generated constructor stub
		poolMap = ScriptThreadPoolMap.getInstance();
	}

	// Testing method, not complete in use
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String sayPlainTextHello() {
	
		Set<Entry<HttpServletRequest, Future<Object>>> set = poolMap.getScriptMap().entrySet();
		Iterator<Entry<HttpServletRequest, Future<Object>>> iter = set.iterator();
		StringBuilder sb = new StringBuilder();
		while(iter.hasNext()){
			Entry<HttpServletRequest, Future<Object>> entry = iter.next();
			sb.append(entry.getKey() + " - " + (entry.getValue().isDone() ? "Done":"Not Done") + "\n");
		}
		System.out.println("---\n" +sb.toString());
		return sb.toString();
	}

	/**
	 * Method takes a JavaScript string as a parameter from POST method body
	 * 
	 * @return String JavaScript result for client application
	 */
	@POST
	@Consumes("text/plain")
	@Produces("text/xml")
	public String getJs() {

		String javascript = "";
		Scanner scanner = null;
		Object resultObject = null;
		String resultString = null;

		try {
			scanner = new Scanner(request.getInputStream(), "UTF-8");
			scanner.useDelimiter("\\A");

			// Getting JavaScript String from POST BODY
			javascript = scanner.hasNext() ? scanner.next() : "";
			// Setting request and JavaScript into poolMat for calculating
			poolMap.registerScript(request, javascript);
			// Retrieving result String
			Future<Object> resultFuture = poolMap.getScriptResult(request);

			try {
				resultObject = resultFuture.get();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (resultObject != null)
				resultString = resultObject.toString();

			return resultString != null ? resultString : String
					.valueOf(HttpServletResponse.SC_BAD_REQUEST);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			scanner.close();
		}
		// return "-1";
		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		return String.valueOf(HttpServletResponse.SC_BAD_REQUEST);
	}

	@Context
	private HttpServletRequest request;
	@Context
	private HttpServletResponse response;

	ScriptThreadPoolMap poolMap;

}