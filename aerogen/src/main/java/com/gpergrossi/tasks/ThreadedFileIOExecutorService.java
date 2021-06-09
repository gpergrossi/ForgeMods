package com.gpergrossi.tasks;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

import net.minecraft.world.storage.IThreadedFileIO;
import net.minecraft.world.storage.ThreadedFileIOBase;
import scala.actors.threadpool.RejectedExecutionException;

public class ThreadedFileIOExecutorService extends TaskExecutorService implements IThreadedFileIO {

	private final PriorityBlockingQueue<SubmittedTask> workQueue;
	private SubmittedTask currentlyRunning = null;
	private boolean shutdown = false;
	private final ThreadedFileIOBase ioBase;
	
	public ThreadedFileIOExecutorService() {
		ioBase = ThreadedFileIOBase.getThreadedIOInstance();
		workQueue = new PriorityBlockingQueue<SubmittedTask>();
	}
	
	@Override
	public void submit(SubmittedTask submitted) {
		if (shutdown) submitted.task.setException(new RejectedExecutionException());
		
		workQueue.add(submitted);		
		ioBase.queueIO(this);
	}
	
	@Override
	public void shutdown() {
		this.shutdown = true;
	}
	
	@Override
	public List<Runnable> shutdownNow() {
		List<Runnable> tasksKilled = new ArrayList<>();
		while (!workQueue.isEmpty()) {
			SubmittedTask submitted = workQueue.poll();
			submitted.task.cancel(true);
			tasksKilled.add(submitted);
		}
		if (currentlyRunning != null) {
			currentlyRunning.task.cancel(true);
			tasksKilled.add(currentlyRunning);
		}
		return tasksKilled;
	}

	@Override
	public boolean isShutdown() {
		return shutdown;
	}
	
	@Override
	public boolean isTerminated() {
		return shutdown && currentlyRunning == null;
	}

	@Override
	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
		final boolean[] waiting = new boolean[] {true};
		Date endTime = new Date();
		endTime.setTime(endTime.getTime() + unit.toMillis(timeout));
		
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				System.out.println("Times up!");
				waiting[0] = false;
			}
		}, endTime);
		
		System.out.println("Awaiting...");
		while (waiting[0] && !isTerminated()) {
			Thread.sleep(500);
		}

		timer.cancel();
		
		return isTerminated();
	}

	@Override
	public Iterator<SubmittedTask> getWaitingTasks() {
		return workQueue.iterator();
	}

	@Override
	public boolean writeNextIO() {
		if (workQueue.isEmpty()) return false;
		
		currentlyRunning = workQueue.poll();
		currentlyRunning.run();
		currentlyRunning = null;
		return true;
	}
	
	public void flush() {
		while (writeNextIO());
	}

}
