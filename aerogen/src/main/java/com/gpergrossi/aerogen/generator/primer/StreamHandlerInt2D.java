package com.gpergrossi.aerogen.generator.primer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.gpergrossi.util.geom.vectors.Int2D;
import com.gpergrossi.util.io.IStreamHandlerFixedSize;

public class StreamHandlerInt2D implements IStreamHandlerFixedSize<Int2D> {

	public static StreamHandlerInt2D INSTANCE = new StreamHandlerInt2D();
	
	private StreamHandlerInt2D() { }
	
	public static final Writer<Int2D> WRITER = new Writer<Int2D>() {
		@Override
		public void write(OutputStream os, Int2D obj) throws IOException {
			DataOutputStream dos = new DataOutputStream(os);
			dos.writeInt(obj.x());
			dos.writeInt(obj.y());
		}
	};
	
	public static final Reader<Int2D> READER = new Reader<Int2D>() {
		@Override
		public Int2D read(InputStream is) throws IOException {
			DataInputStream dis = new DataInputStream(is);
			final int x = dis.readInt();
			final int y = dis.readInt();
			return new Int2D(x, y);
		}
	};
	
	@Override
	public Writer<Int2D> getWriter() {
		return WRITER;
	}

	@Override
	public Reader<Int2D> getReader() {
		return READER;
	}

	@Override
	public int getMaxSize() {
		return 8;
	}

	
	
}
