package com.gpergrossi.tasks;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.gpergrossi.util.data.Iterators;

public class ThreadPoolTaskExecutor extends TaskExecutorService {

	final String name;
	final PriorityBlockingQueue<Runnable> taskQueue;
	final ThreadPoolExecutor executor;
	private int numThreads;
	
	public ThreadPoolTaskExecutor(String name, int numThreads) {
		this.name = name;
		this.taskQueue = new PriorityBlockingQueue<>(32);
		
		this.executor = new ThreadPoolExecutor(numThreads, numThreads, 0l, TimeUnit.SECONDS, taskQueue);
		ThreadFactory threadFactory = new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread thread = new Thread(r);
				thread.setName(ThreadPoolTaskExecutor.this.name + " Worker Thread #"+(ThreadPoolTaskExecutor.this.numThreads++));
				return thread;
			}
		};
		executor.setThreadFactory(threadFactory);
	}

	@Override
	public void shutdown() {
		executor.shutdown();		
	}

	@Override
	public List<Runnable> shutdownNow() {
		return executor.shutdownNow();
	}

	@Override
	public boolean isShutdown() {
		return executor.isShutdown();
	}

	@Override
	public boolean isTerminated() {
		return executor.isTerminated();
	}

	@Override
	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
		return executor.awaitTermination(timeout, unit);
	}

	@Override
	public void submit(SubmittedTask task) {
		executor.execute(task);
	}

	@Override
	public Iterator<SubmittedTask> getWaitingTasks() {
		return Iterators.cast(taskQueue.iterator(), r -> ((SubmittedTask) r));
	}

	
	
}
