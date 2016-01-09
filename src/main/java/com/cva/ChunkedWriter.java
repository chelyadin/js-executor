package com.cva;

import java.io.IOException;
import java.io.StringWriter;

import org.glassfish.jersey.server.ChunkedOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChunkedWriter extends StringWriter{
	
	ChunkedWriter(ChunkedOutput<String> chunkedOutput){
		this.chunkedOutput = chunkedOutput;
	}

	@Override
	public void write(String str) {
		log.debug("write#String " + str);
		super.write(str);
		try {
			chunkedOutput.write(str);
		} catch (IOException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	@Override
	public void write(int c) {
		log.debug("write#int " + (char)c);
		super.write(c);
		try {
			chunkedOutput.write(String.valueOf(c));
		} catch (IOException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	@Override
	public void write(char[] cbuf, int off, int len) {
		log.debug("write#charArray " + new String(cbuf, off, len));
		super.write(cbuf, off, len);
		try {
			chunkedOutput.write(new String(cbuf, off, len));
		} catch (IOException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	@Override
	public void write(String str, int off, int len) {
		log.debug("write#StrOffset " + str.substring(off, off+len));
		super.write(str, off, len);
		try {
			chunkedOutput.write(str.substring(off, off+len));
		} catch (IOException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	@Override
	public void close() throws IOException {
		try {
			super.close();
			if (!chunkedOutput.isClosed()){
				// TODO it's a bad practice to write opening tags in one class and closing in another,
				// TODO in same way opening and closing closeable resources needs to be done
				// in same try/catch/finally block
				chunkedOutput.write("</output></script>");
			}
		} finally {
			chunkedOutput.close();
		}
	}

	private final ChunkedOutput<String> chunkedOutput;
	transient static final Logger log = LoggerFactory.getLogger(ChunkedWriter.class);
}
