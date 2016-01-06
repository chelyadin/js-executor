package com.cva;

import java.io.IOException;
import java.io.StringWriter;

import org.glassfish.jersey.server.ChunkedOutput;

public class ChunkedWriter extends StringWriter{
	ChunkedWriter(ChunkedOutput<String> chunkedOutput){
		this.chunkedOutput = chunkedOutput;
	}
	/*
	@Override
	public void write(String str) {
		try {
			chunkedOutput.write(str);
		//	chunkedOutput.write("</output></script>");
		//	chunkedOutput.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		super.write(str);
	};
	*/
	
	
	
	@Override
	public String toString(){
		try {
			if (!chunkedOutput.isClosed())
				chunkedOutput.write(super.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return super.toString();
	}
	
	public void closeChunkedOutput(){
		try {
			if (!chunkedOutput.isClosed()){
				chunkedOutput.write("</output></script>");
				chunkedOutput.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean isChunkedOutputClosed(){
		return chunkedOutput.isClosed();
	}
	
	private ChunkedOutput<String> chunkedOutput;
}
