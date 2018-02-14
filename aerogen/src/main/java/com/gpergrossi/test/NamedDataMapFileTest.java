package com.gpergrossi.test;

import static org.junit.Assert.assertEquals;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.Test;

import com.gpergrossi.util.io.IStreamHandler;
import com.gpergrossi.util.io.ndmf.NamedDataMapFile;

public class NamedDataMapFileTest {
	
	public static final IStreamHandler.FixedSize<String> nameHandler = new IStreamHandler.FixedSize<String>() {
		
		public static final int MAX_LENGTH = 6;
		
		public final Writer<String> WRITER = new Writer<String>() {
			@Override
			public void write(OutputStream os, String obj) throws IOException {
				if (obj.length() > MAX_LENGTH) throw new IllegalArgumentException("String is too long!");
				
				DataOutputStream dos = new DataOutputStream(os);
				dos.writeUTF(obj);
			}
		};
		
		public final Reader<String> READER = new Reader<String>() {
			@Override
			public String read(InputStream is) throws IOException {
				DataInputStream dis = new DataInputStream(is);
				return dis.readUTF();
			}
		};
		
		@Override
		public Writer<String> getWriter() {
			return WRITER;
		}
		
		@Override
		public Reader<String> getReader() {
			return READER;
		}
		
		@Override
		public int getStreamedSize() {
			return 2+MAX_LENGTH;
		}
	};
	
	private static class Data {
		
	}
	
	public static final IStreamHandler<Data> dataHandler = new IStreamHandler<Data>() {
		public final Writer<Data> WRITER = new Writer<Data>() {
			@Override
			public void write(OutputStream os, Data obj) throws IOException {
			}
		};
		
		public final Reader<Data> READER = new Reader<Data>() {
			@Override
			public Data read(InputStream is) throws IOException {
				return new Data();
			}
		};
		
		@Override
		public Writer<Data> getWriter() {
			return WRITER;
		}
		
		@Override
		public Reader<Data> getReader() {
			return READER;
		}
	};
	
	//@Test
	public void testCreation() throws IOException {
		NamedDataMapFile<String, Data> ndmf = new NamedDataMapFile<>(nameHandler, dataHandler, 64);
		File file = new File("NDMFTest");
		if (file.exists()) file.delete();
		
		ndmf.open(file);
	}
	
	//@Test
	public void testWriteRead() throws IOException {
		NamedDataMapFile<String, Data> ndmf = new NamedDataMapFile<>(nameHandler, dataHandler, 64);
		File file = new File("NDMFTest");
		if (file.exists()) file.delete();

		ndmf.open(file);
		System.out.println();
		System.out.println("======== WRITING ========");
		for (int i = 1; i < 32; i++) {
			ndmf.storedNames.put(":"+i, i+0xF000);
		}
		System.out.println();
		System.out.println("======== READING ========");
		for (int i = 1; i < 32; i++) {
			assertEquals(Integer.valueOf(i+0xF000), ndmf.storedNames.get(":"+i));
		}
		ndmf.close();
	}
	
	//@Test
	public void testWriteCloseRead() throws IOException {
		NamedDataMapFile<String, Data> ndmf = new NamedDataMapFile<>(nameHandler, dataHandler, 64);
		File file = new File("NDMFTest");
		if (file.exists()) file.delete();

		System.out.println();
		System.out.println("======== WRITING ========");
		ndmf.open(file);
		for (int i = 1; i < 32; i++) {
			ndmf.storedNames.put(":"+i, i+0xF000);
		}
		ndmf.close();
		
		System.out.println();
		System.out.println("======== READING ========");
		ndmf.open(file);
		for (int i = 1; i < 32; i++) {
			assertEquals(Integer.valueOf(i+0xF000), ndmf.storedNames.get(":"+i));
		}
		ndmf.close();
	}

	@Test
	public void testOverwriteCloseRead() throws IOException {
		NamedDataMapFile<String, Data> ndmf = new NamedDataMapFile<>(nameHandler, dataHandler, 64);
		File file = new File("NDMFTest");
		if (file.exists()) file.delete();

		System.out.println();
		System.out.println("======== WRITING ========");
		ndmf.open(file);
		for (int i = 1; i < 32; i++) {
			ndmf.storedNames.put(":"+i, i+0x2000);
		}
		
		System.out.println();
		System.out.println("======== OVERWRITING ========");
		for (int i = 1; i < 32; i++) {
			ndmf.storedNames.put(":"+i, i+0x1000);
		}
		ndmf.close();
		
		System.out.println();
		System.out.println("======== READING ========");
		ndmf.open(file);
		for (int i = 1; i < 32; i++) {
			assertEquals(Integer.valueOf(i+0x1000), ndmf.storedNames.get(":"+i));
		}
		ndmf.close();
	}
	
}
