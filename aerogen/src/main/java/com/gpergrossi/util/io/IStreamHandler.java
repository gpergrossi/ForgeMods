package com.gpergrossi.util.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface IStreamHandler<T> {
	
	@FunctionalInterface
	public static interface Writer<T> {
		public void write(OutputStream os, T obj) throws IOException;
	}
	
	@FunctionalInterface
	public static interface Reader<T> {
		public T read(InputStream is) throws IOException;
	}
	
	public static interface FixedSize<T> extends IStreamHandler<T> {
		public int getStreamedSize();
	}
	
	public Writer<T> getWriter();
	public Reader<T> getReader();
	
}
