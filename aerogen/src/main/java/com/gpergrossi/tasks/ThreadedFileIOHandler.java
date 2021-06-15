package com.gpergrossi.tasks;

import com.gpergrossi.tasks.Task.Priority;

public class ThreadedFileIOHandler implements IOHandler {

	private TaskManager manager;
	private ThreadedFileIOExecutorService service;
	
	public ThreadedFileIOHandler() {
		this.service = new ThreadedFileIOExecutorService();
		this.manager = new TaskManager("IOManager", service);
	}

	@Override
	public Task submitIO(Runnable io) {
		Task task = new Task("IOTask", Priority.NORMAL) {
			@Override
			public void work() {
				io.run();
				this.setFinished();
			}
		};
		manager.submit(task);
		return task;
	}

	@Override
	public void flush() {
		service.flush();
	}

	@Override
	public void close() {
		manager.shutdown();
		service.flush();
	}

}
