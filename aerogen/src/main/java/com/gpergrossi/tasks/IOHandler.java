package com.gpergrossi.tasks;

import java.io.Closeable;

public interface IOHandler extends Closeable {

	/**
	 * Takes a runnable that needs IO to be done and returns a Task
	 * which will be finished when this IOHandler does the IO.
	 */
	public Task submitIO(Runnable io);
	
	public void flush();
	
	@Override
	public void close();
	
}
