package com.cva;


import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.server.ChunkedOutput;




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
	@Path("/script/")
	@Produces("text/xml")
	public Response getScriptsStatus() {

		Iterator<Entry<UUID, FutureKeeper>> iterator = scriptThreadPoolMap
				.getScriptsIterator();

		StringBuilder sb = new StringBuilder("<?xml version=\"1.0\"?>"
				+ "<links>");
		while (iterator.hasNext()) {
			Entry<UUID, FutureKeeper> entry = iterator.next();
			sb.append("<link><uuid>" + entry.getKey() + "</uuid><status>"
					+ (entry.getValue().getFuture().isDone() ? "Done" : "Undone")
					+ "</status></link>");
		}
		sb.append("</links>");
		return Response.status(Status.OK).entity(sb.toString()).build();
	}

	/**
	 * Shows status and result of script by certain uuid
	 * @param asyncResponse
	 * @param uuidString
	 */
	@GET
	@Path("/script/{uuidString}")
	@Produces("text/xml")
	public Response getScript(@PathParam("uuidString") String uuidString) {

		String statusString = null;
		String responseString = null;
		boolean isExists = scriptThreadPoolMap.isScriptExist(UUID
				.fromString(uuidString));
		boolean isDone = scriptThreadPoolMap.isScriptDone(UUID
				.fromString(uuidString));

		// Building statusString
		if (!isExists){
			statusString = "Not exists";
		} else {
				if (isDone) {
					statusString = "Done";
			} else {
				statusString = "Undone";
			}
		}
		
		// Building responseString
		responseString = "<?xml version=\"1.0\"?>"
				+ "<script><status>"
				+ statusString
				+ "</status><output>"
				+ (isDone ? scriptThreadPoolMap.getScriptResult(UUID
						.fromString(uuidString)) : "") + "</output></script>";

		return Response.status(Status.OK).entity(responseString).build();
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
		scriptThreadPoolMap.registerScript(uuid, body, new ChunkedWriter(new ChunkedOutput<String>(String.class)));
		return Response.status(Status.ACCEPTED).entity("<location>script/" + uuid + "</location>").type("text/xml").build();
	}


	@POST
	@Path("/script/blocking")
	@Consumes("text/plain")
	@Produces("text/xml")
    public ChunkedOutput<String> getChunkedResponse(String body) {
		
        final ChunkedOutput<String> chunkedOutput = new ChunkedOutput<String>(String.class);

        UUID uuid = UUID.randomUUID();
		
		try {
			chunkedOutput.write("<script><uuid>" + uuid + "</uuid>"	+ "<output>");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		scriptThreadPoolMap.registerScript(uuid, body, new ChunkedWriter(chunkedOutput));
		
		return chunkedOutput;
		
    }
 

	/**
	 * 
	 * @param uuidString
	 * @return Response with status and body
	 */
	@DELETE
	@Path("/script/{uuidString}")
	@Produces("text/xml")
	public Response delete(@PathParam("uuidString") String uuidString) {
		String statusString = null;
		boolean isExist = scriptThreadPoolMap.isScriptExist(UUID
				.fromString(uuidString));
		boolean isDone = scriptThreadPoolMap.isScriptDone(UUID
				.fromString(uuidString));
		
		// Building statusString
		if (!isExist){
			statusString = "Not exists";
		} else {
				if (isDone) {
					statusString = "Done";
			} else {
				statusString = "Undone";
			}
		}
		
		String outputString = scriptThreadPoolMap.removeScript(UUID.fromString(uuidString));
		return Response.status(Status.OK).
				entity("<deleted><status>" + statusString + "</status><output>"
						+ outputString + "</output></deleted>").type("text/xml").build();
	}
	
	private ScriptThreadPoolMap scriptThreadPoolMap;

}