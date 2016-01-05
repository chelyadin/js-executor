package com.cva;


import java.util.Iterator;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.Future;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.server.ManagedAsync;



/**
 * Class is a RootResource. It services client request for JavaScript
 * execution by Nashorn. Runs like
 * "127.0.0.1:8080/js-executor/api/execute/script".
 * 
 * Available methods:
 * POST /script/ - creates new JavaScript executor, assigns UUID, returns link for script
 * GET /script/ - returns links for all available launched scripts
 * GET /script/UUID - returns script status and result
 * DELETE /script/UUID - deletes script with returning status and result
 * 
 * @author CVA
 *
 */
@Path("/execute")
public class JsApiService {

	public JsApiService() {
		// TODO Auto-generated constructor stub
		scriptThreadPoolMap = ScriptThreadPoolMap.getInstance();
	}

	/**
	 * Shows all links for available scripts
	 * @param asyncResponse
	 */
	@GET
	@ManagedAsync
	@Path("/script/")
	@Produces("text/xml")
	public void getScriptsStatus(@Suspended final AsyncResponse asyncResponse) {

		Iterator<Entry<UUID, Future<Object>>> iterator = scriptThreadPoolMap
				.getScriptsIterator();

		StringBuilder sb = new StringBuilder("<?xml version=\"1.0\"?>"
				+ "<links>");
		while (iterator.hasNext()) {
			Entry<UUID, Future<Object>> entry = iterator.next();
			sb.append("<link><uuid>" + entry.getKey() + "</uuid><status>"
					+ (entry.getValue().isDone() ? "Done" : "Undone")
					+ "</status></link>");
		}
		sb.append("</links>");
		asyncResponse.resume(sb.toString());
	}

	/**
	 * Shows status and result of script by certain uuid
	 * @param asyncResponse
	 * @param uuidString
	 */
	@GET
	@ManagedAsync
	@Path("/script/{uuidString}")
	@Produces("text/xml")
	public void getScript(@Suspended final AsyncResponse asyncResponse,
			@PathParam("uuidString") String uuidString) {

		String statusString = null;
		String responseString = null;
		boolean isDone = scriptThreadPoolMap.isScriptDone(UUID
				.fromString(uuidString));

		// Building statusString
		if (isDone) {
			if (scriptThreadPoolMap
					.getScriptResult(UUID.fromString(uuidString)) instanceof Exception) {
				statusString = "Finished with exception";
			} else {
				statusString = "Done";
			}

		} else {
			statusString = "Undone";
		}

		// Building responseString
		responseString = "<?xml version=\"1.0\"?>"
				+ "<script><status>"
				+ statusString
				+ "</status><result>"
				+ (isDone ? scriptThreadPoolMap.getScriptResult(UUID
						.fromString(uuidString)) : "") + "</result></script>";

		asyncResponse.resume(responseString);
	}
	
	/**
	 * Method takes a JavaScript string as a parameter from POST method body
	 * @param body
	 * @return Response with appropriate status and message
	 */
	@POST
	@Path("/script")
	@Consumes("text/plain")
	@Produces("text/xml")
	public Response setJsString(String body) {

		// If a body is empty then return an error BAD_REQUEST
		if (body.equals(""))
			return Response.status(Status.BAD_REQUEST).entity("Empty script").type("text/xml").build();
	
		UUID uuid = UUID.randomUUID();
		
		// Registering request and JavaScript into poolMat for calculating
		scriptThreadPoolMap.registerScript(uuid, body);
		return Response.status(Status.ACCEPTED).entity("<location>script/" + uuid + "</location>").type("text/xml").build();
	}


/*
	@POST
	@ManagedAsync 
	@Path("/script")
	@Consumes("text/plain")
	@Produces("text/xml")
	public void executeJsString(@Suspended final AsyncResponse asyncResponse,
			@QueryParam("blocking") String blocking,
			String body) {

		// If a body is empty then return an error BAD_REQUEST
//		if (body.equals(""))
//			return Response.status(Status.BAD_REQUEST).entity("Empty script").type("text/xml").build();

		UUID uuid = UUID.randomUUID();
		
		// Registering request and JavaScript into poolMat for calculating
		scriptThreadPoolMap.registerScript(uuid, body);
		
		String statusString = null;
		String responseString = null;
		boolean isDone = scriptThreadPoolMap.isScriptDone(uuid);

		// Building statusString
		if (isDone) {
			if (scriptThreadPoolMap
					.getScriptResult(uuid) instanceof Exception) {
				statusString = "Finished with exception";
			} else {
				statusString = "Done";
			}

		} else {
			statusString = "Undone";
		}

		// Building responseString
		responseString = "<?xml version=\"1.0\"?>"
				+ "<script><status>"
				+ statusString
				+ "</status><result>"
				+ scriptThreadPoolMap.getScriptResult(uuid) + "</result></script>";

		asyncResponse.resume(responseString);
		
		//return Response.status(Status.ACCEPTED).entity("<location>script/" + uuid + "</location>").type("text/xml").build();
	
	}
*/
	
	@DELETE
	@ManagedAsync
	@Path("/script/{uuidString}")
	@Produces("text/xml")
	public void delete(@Suspended final AsyncResponse asyncResponse,
			@PathParam("uuidString") String uuidString) {
		String statusString = null;
		String responseString = null;
		boolean isDone = scriptThreadPoolMap.isScriptDone(UUID
				.fromString(uuidString));

		// Building statusString
		if (isDone) {
			if (scriptThreadPoolMap
					.getScriptResult(UUID.fromString(uuidString)) instanceof Exception) {
				statusString = "Finished with exception";
			} else {
				statusString = "Done";
			}

		} else {
			statusString = "Interrupted";
		}

		// Building responseString
		responseString = "<?xml version=\"1.0\"?>"
				+ "<deleted><status>"
				+ statusString
				+ "</status><result>"
				+ (isDone ? scriptThreadPoolMap.getScriptResult(UUID
						.fromString(uuidString)) : "") + "</result></deleted>";

		asyncResponse.resume(responseString);

		scriptThreadPoolMap.removeScript(UUID.fromString(uuidString));
	}

	private ScriptThreadPoolMap scriptThreadPoolMap;

}