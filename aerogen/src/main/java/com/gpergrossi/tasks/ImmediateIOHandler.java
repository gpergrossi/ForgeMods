package com.gpergrossi.tasks;

public class ImmediateIOHandler implements IOHandler {

	@Override
	public Task submitIO(Runnable io) {
		io.run();
		return Task.createFinished("IOTask");
	}

	@Override
	public void flush() {}

	@Override
	public void close() {}

}
