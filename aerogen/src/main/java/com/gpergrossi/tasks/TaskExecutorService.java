package com.gpergrossi.tasks;

import java.util.Iterator;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public abstract class TaskExecutorService extends AbstractExecutorService {
		
	public abstract void submit(SubmittedTask task);
	
	@Override
	public final void execute(Runnable task) {
		submit((SubmittedTask) task);
	}
	
	@Override
	public final <T> Future<T> submit(Callable<T> task) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public final Future<?> submit(Runnable task) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public final <T> Future<T> submit(Runnable task, T result) {
		throw new UnsupportedOperationException();
	}
	
	public abstract Iterator<SubmittedTask> getWaitingTasks();
	
}
