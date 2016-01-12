package com.cva;

import java.io.IOException;
import java.io.StringWriter;

import org.glassfish.jersey.server.ChunkedOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChunkedWriter extends StringWriter{
	
	ChunkedWriter(ChunkedOutput<String> chunkedOutput){
		this.chunkedOutput = chunkedOutput;
		stringBuilder = new StringBuilder("<script><output>");
		finishedWithException = false;
	}

	@Override
	public void write(String str) {
		log.debug("write#String " + str);
		super.write(str);
		/*
		try {
			chunkedOutput.write(str);
		} catch (IOException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
		*/
		stringBuilder.append(str);
	}

	@Override
	public void write(int c) {
		log.debug("write#int " + (char)c);
		super.write(c);
		/*
		try {
			chunkedOutput.write(String.valueOf(c));
		} catch (IOException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
		*/
		stringBuilder.append(c);
	}

	@Override
	public void write(char[] cbuf, int off, int len) {
		log.debug("write#charArray " + new String(cbuf, off, len));
		super.write(cbuf, off, len);
		/*
		try {
			chunkedOutput.write(new String(cbuf, off, len));
		} catch (IOException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
		*/
		stringBuilder.append(cbuf, off, len);
	}

	@Override
	public void write(String str, int off, int len) {
		log.debug("write#StrOffset " + str.substring(off, off+len));
		super.write(str, off, len);
		/*
		try {
			chunkedOutput.write(str.substring(off, off+len));
		} catch (IOException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
		*/
		stringBuilder.append(str.substring(off, off+len));		
	}

	@Override
	public void close() throws IOException {
		try {
			super.close();
			if (!chunkedOutput.isClosed()){
				stringBuilder.append("</output><status>");
				stringBuilder.append(isFinishedWithException() ? "Finished with exception" : "Done");
				stringBuilder.append("</status></script>");
				chunkedOutput.write(stringBuilder.toString());
			}
		} finally {
			chunkedOutput.close();
		}
	}
	
	public void setFinishedWithException(boolean finishedWithException){
		this.finishedWithException = finishedWithException;
	}
	
	public boolean isFinishedWithException(){
		return finishedWithException;
	}

	private final ChunkedOutput<String> chunkedOutput;
	transient static final Logger log = LoggerFactory.getLogger(ChunkedWriter.class);
	private StringBuilder stringBuilder;
	private boolean finishedWithException;
}
