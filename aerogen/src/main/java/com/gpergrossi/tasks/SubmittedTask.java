package com.gpergrossi.tasks;

import com.gpergrossi.tasks.Task.Status;

public class SubmittedTask implements Runnable, Comparable<SubmittedTask> {
	
	final Task.Priority expectedPriority;
	final Task task;
	final Status expectedStatus;
	
	public SubmittedTask(Task task, Status expectedStatus) {
		this.task = task;
		this.expectedPriority = task.getPriority();
		this.expectedStatus = expectedStatus;
	}
	
	@Override
	public void run() {
		task.lock();
		try {
			if (!shouldRun(task)) {
				if (task.getManager().debug) System.out.println("---- Did not run task "+this);
			} else {
				task.getManager().begin(task);
				task.run();
				task.getManager().stop(task);
			}
		} finally {
			task.unlock();
		}
	}
	
	public boolean shouldRun(Task task) {
		if (task.getStatus() != expectedStatus) return false;
		if (task.getPriority() != expectedPriority) return false;
		return true;
	}
	
	@Override
	public int compareTo(SubmittedTask o) {
		return o.expectedPriority.compareTo(this.expectedPriority);	
	}
	
	@Override
	public String toString() {
		return task.toString()+" (Expected [priority = "+expectedPriority+", status = "+expectedStatus+"])";
	}
	
}
