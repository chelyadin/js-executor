package com.cva;

import java.io.IOException;
import java.util.Iterator;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.server.ChunkedOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class is a RootResource. It services client request for JavaScript execution
 * by Nashorn. Runs like "127.0.0.1:8080/js-executor/api/execute/script".
 * 
 * Available methods: POST /script/ - creates new JavaScript executor, assigns
 * UUID, returns link for script GET /script/ - returns links for all available
 * launched scripts GET /script/UUID - returns script status and result DELETE
 * /script/UUID - deletes script with returning status and result
 * 
 * @author CVA
 *
 */
@Path("/execute")
public class JsApiService {

	public JsApiService() {
		scriptThreadPoolMap = ScriptThreadPoolMap.getInstance();
	}

	/**
	 * Shows all links for available scripts
	 * 
	 * @param asyncResponse
	 */
	@GET
	@Path("/script/")
	@Produces("text/xml")
	public Response getScriptsStatus() {

		Iterator<FutureKeeper> iterator = scriptThreadPoolMap
				.getScriptsIterator();

		StringBuilder sb = new StringBuilder("<?xml version=\"1.0\"?>"
				+ "<links>");
		while (iterator.hasNext()) {
			FutureKeeper entry = iterator.next();
			sb.append("<link><uuid>" + entry.uuid + "</uuid><status>"
					+ (entry.future.isDone() ? "Done" : "Undone")
					+ "</status></link>");
		}
		sb.append("</links>");
		return Response.status(Status.OK).entity(sb.toString()).build();
	}

	/**
	 * Shows status and result of script by certain uuid
	 * 
	 * @param asyncResponse
	 * @param uuidString
	 */
	@GET
	@Path("/script/{uuidString}")
	@Produces("text/xml")
	public Response getScriptResult(@PathParam("uuidString") String uuidString) {

		String statusString = null;
		String responseString = null;

		boolean isExists = scriptThreadPoolMap.isScriptExist(uuidString);
		boolean isDone;
		if (isExists) {
			isDone = scriptThreadPoolMap.isScriptDone(uuidString);
		} else {
			isDone = false;
		}

		// Building statusString
		if (!isExists) {
			responseString = "<?xml version=\"1.0\"?>"
					+ "<script><output></output>"
					+ "<status>NOT_FOUND</status></script>";
			return Response.status(Status.NOT_FOUND).entity(responseString)
					.type("text/xml").build();
		} else {
			if (isDone) {
				statusString = "Done";
			} else {
				statusString = "Undone";
			}
		}

		// Handling errors. Exception message was added to ChunkedWriter
		ChunkedWriter resultObject = (ChunkedWriter) scriptThreadPoolMap
				.getScriptResult(uuidString);
		if (resultObject.isFinishedWithException())
			statusString = "Finished with exception";

		// Building responseString
		responseString = "<?xml version=\"1.0\"?>" + "<script><output>"
				+ resultObject.toString() + "</output>" + "<status>"
				+ statusString + "</status></script>";

		// Building Response
		return Response.status(Status.OK).entity(responseString).build();
	}

	/**
	 * Method takes a JavaScript string as a parameter from POST method body
	 * 
	 * @param body
	 * @return Response with appropriate status and message
	 */

	@POST
	@Path("/script")
	@Consumes("text/plain")
	@Produces("text/xml")
	public Response postScript(String body) {

		// If a body is empty then return an error BAD_REQUEST
		if (body.equals(""))
			return Response.status(Status.BAD_REQUEST).entity("Empty script")
					.type("text/xml").build();

		UUID uuid = UUID.randomUUID();

		// Registering request and JavaScript into poolMap for calculating
		scriptThreadPoolMap.registerScript(uuid.toString(), body,
				new ChunkedWriter(new ChunkedOutput<String>(String.class)));
		// Sending location as URI in Location header
		return Response.status(Status.ACCEPTED)
				.entity("<location>script/" + uuid + "</location>")
				.type("text/xml").header("Location", "execute/script/" + uuid)
				.build();
	}

	@SuppressWarnings("resource")
	@POST
	@Path("/script/blocking")
	@Consumes("text/plain")
	@Produces("text/xml")
	public Object postScript(String body,
			@QueryParam("blocking") boolean blocking) throws IOException {

		final ChunkedOutput<String> chunkedOutput = new ChunkedOutput<String>(
				String.class);

		UUID uuid = UUID.randomUUID();

		scriptThreadPoolMap.registerScript(uuid.toString(), body,
				new ChunkedWriter(chunkedOutput));

		if (blocking) {
			log.debug("postScript blocking " + uuid);
			return chunkedOutput;

		}

		log.debug("postScript nonblocking " + uuid);
		return Response.status(Status.ACCEPTED)
				.entity("<location>script/" + uuid + "</location>")
				.type("text/xml").header("Location", "execute/script/" + uuid)
				.build();

	}

	/**
	 * 
	 * @param uuidString
	 * @return Response with status and body
	 * @throws IOException
	 */
	@DELETE
	@Path("/script/{uuidString}")
	@Produces("text/xml")
	public Response deleteScript(@PathParam("uuidString") String uuidString) {

		String statusString = null;
		String responseString = null;

		boolean isExists = scriptThreadPoolMap.isScriptExist(uuidString);
		boolean isDone;
		if (isExists) {
			isDone = scriptThreadPoolMap.isScriptDone(uuidString);
		} else {
			isDone = false;
		}

		// Building statusString
		if (!isExists) {
			statusString = "Not exists";
			responseString = "<?xml version=\"1.0\"?>"
					+ "<script><output></output>"
					+ "<status>NOT_FOUND</status></script>";
			return Response.status(Status.NOT_FOUND).entity(responseString)
					.type("text/xml").build();
		} else {
			if (isDone) {
				statusString = "Done";
			} else {
				statusString = "Undone";
			}
		}

		// Getting resultObject
		ChunkedWriter resultObject;
		try {
			resultObject = (ChunkedWriter) scriptThreadPoolMap
					.removeScript(uuidString);
		} catch (IOException e) {
			responseString = "<?xml version=\"1.0\"?>"
					+ "<script><output></output>"
					+ "<status>NOT_FOUND</status></script>";
			return Response.status(Status.NOT_FOUND).entity(responseString)
					.type("text/xml").build();
		} catch (InterruptedException | IllegalStateException e) {
			responseString = "<?xml version=\"1.0\"?>"
					+ "<script><output></output>"
					+ "<status>FORBIDDEN</status></script>";
			return Response.status(Status.FORBIDDEN).entity(responseString)
					.type("text/xml").build();
		}

		// Handling errors. Exception message was added to ChunkedWriter
		if (resultObject.isFinishedWithException())
			statusString = "Finished with exception";

		// Building responseString
		responseString = "<?xml version=\"1.0\"?>" + "<deletedScript><output>"
				+ resultObject.toString() + "</output>" + "<status>"
				+ statusString + "</status></deletedScript>";

		// Building Response
		return Response.status(Status.OK).entity(responseString).build();

	}

	private ScriptThreadPoolMap scriptThreadPoolMap;
	transient static final Logger log = LoggerFactory
			.getLogger(JsApiService.class);

}