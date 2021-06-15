package com.gpergrossi.tasks;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.ReentrantLock;

public abstract class Task extends AbstractFuture<Void> implements Runnable {

	public static Task createFinished(String name) {
		Task task = new Task(name) {
			@Override
			public void work() {
				throw new RuntimeException("A completed task should not be executed!");
			}
		};
		task.set(null);
		return task;
	}
	
	public static enum Priority implements Comparable<Priority> {
		LOWEST, LOW, NORMAL, HIGH, HIGHEST;
	}
	
	public static enum Status {
		UNINITIALIZED, WAITING, RUNNING, BLOCKED, FINISHED, CANCELLED, INTERRUPTED, FAILURE;
		
		public boolean isDone() {
			return this.ordinal() >= FINISHED.ordinal();
		}
		
		public boolean isGood() {
			return this.ordinal() <= FINISHED.ordinal();
		}
	}
	
	private TaskManager manager;
	private List<Runnable> listeners;	
	
	private volatile Status status = Status.UNINITIALIZED;
	private Priority priority = Priority.NORMAL;
	protected final String name;
	
	private List<? extends Task> blockTasks;
	private Runnable blockReturn;
	private ListenableFuture<List<Object>> blockFuture;
	private List<Object> blockResults;
	
	private final ReentrantLock lock;
	private Thread lockOwner;
	private Throwable exception;
	
	public Task(String name) {
		this(name, Priority.NORMAL);
	}
	
	public Task(String name, Priority priority) {
		this.name = name;
		this.lock = new ReentrantLock();
	}
	
	void setManager(TaskManager manager) {
		if (this.manager != null) throw new IllegalStateException("Cannot submit a task that has already been submitted!");
		this.manager = manager;
		if (this.listeners != null) {
			for (Runnable listener : listeners) {
				this.addListener(listener);
			}
			this.listeners = null;
		}
	}
	
	protected TaskManager getManager() {
		return manager;
	}
	
	public abstract void work();
	
	@Override
	public void run() {		
		Runnable todo = this::work;
		
		if (blockReturn != null) {
			try {
				if (blockFuture.isCancelled()) this.cancel(false);
				
				Iterator<? extends Task> taskIter = blockTasks.iterator();
				while (taskIter.hasNext()) {
					Task task = taskIter.next();
					if (task.getStatus() == Status.FINISHED) taskIter.remove(); 
				}
				if (blockTasks.size() > 0) {
					StringBuilder err = new StringBuilder();
					err.append("One or more tasks on which this task was blocked did not succeed:\n");
					blockTasks.forEach(t -> {
						err.append("  ").append(t);
						if (((Task) t).status == Status.FAILURE) {
							err.append(" : ").append(t.getException().getMessage());
						}
						err.append("\n");
					});
					this.setException(new RuntimeException(err.toString()));
				}
				
				blockResults = blockFuture.get();
			} catch (InterruptedException | ExecutionException e) {
				setException(e);
				return;
			}
			todo = blockReturn;
			blockReturn = null;
		}
		
		if (!this.isCancelled() && !this.isDone()) {
			todo.run();
		}
	}

	/**
	 * Use this call from inside the {@code work} method of this task to pause the task
	 * awaiting the completion of all {@code blockTasks}. When the {@code blockTasks}
	 * have finished, this task will be resubmitted to the TaskManager and upon running
	 * will call {@code blockReturn}.
	 * @param blockTasks
	 * @param blockReturn
	 */
	protected void block(List<? extends Task> blockTasks, Runnable blockReturn) {
		lock();
		try {
			if (blockReturn == null) throw new IllegalArgumentException("blockReturn must be non-null!");
			if (status != Status.RUNNING) throw new IllegalStateException("Thread cannot block() unless it is running! (Status = "+status+")");
			blockTasks.forEach(task -> {
				if (task == this) throw new IllegalArgumentException("A Task cannot wait on itself!");
				if (((Task) task).priority.compareTo(this.priority) < 0) task.changePriority(priority);
			});
			
			this.setStatus(Status.BLOCKED);
			if (status != Status.BLOCKED) return;
			
			this.blockTasks = blockTasks;
			this.blockReturn = blockReturn;
			this.blockFuture = Futures.successfulAsList(blockTasks);
			this.blockFuture.addListener(new SubmittedTask(this, Status.BLOCKED), this.manager.executor);
		} finally {
			unlock();
		}
	}
	
	protected void blockIO(Runnable io, Runnable blockReturn) {
		List<Task> tasks = new ArrayList<Task>();
		tasks.add(this.manager.createIOTask(io));
		this.block(tasks, blockReturn);
	}

	protected List<Object> getBlockResults() {
		if (blockResults == null) throw new IllegalStateException("Cannot get block results because the task has not blocked!");
		return blockResults;
	}
	
	protected boolean setFinished() {
		return this.set(null);
	}
	
	@Override
	protected boolean set(Void value) {
		boolean finished = super.set(value);
		if (finished) setStatus(Status.FINISHED);
		return finished;
	}
	
	/**
	 * Wait for the task to complete
	 */
	public void wait(Priority priority) throws InterruptedException, ExecutionException {
		if (!this.isDone())	this.changePriority(priority);
		super.get();
	}
	
	@Override
	protected boolean setFuture(ListenableFuture<? extends Void> future) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	protected boolean setException(Throwable throwable) {
		this.exception = throwable;
		setStatus(Status.FAILURE);
		boolean excepted = super.setException(throwable);
		if (!excepted) {
			this.cancel(true);
			System.err.println(throwable.getMessage());
		}
		return true;
	}
	
	public Throwable getException() {
		return exception;
	}
	
	@Override
	protected void interruptTask() {
		quickSetStatus(Status.INTERRUPTED);
	}
	
	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		boolean cancelled = super.cancel(mayInterruptIfRunning); 
		if (cancelled) quickSetStatus(Status.CANCELLED);
		return cancelled;
	}
	
	/**
	 * Adds a listener to be executed when this task is finished.
	 * The Executor will be the TaskManager this task was assigned to.
	 */
	public void addListener(Runnable listener) {
		if (this.manager == null) {
			if (this.listeners == null) this.listeners = new ArrayList<>();
			listeners.add(listener);			
		} else {			
			if (listener instanceof Task) throw new IllegalArgumentException("Tasks cannot listen to other Tasks. Use TaskManager.submitBlocked().");
			if (listener instanceof SubmittedTask) throw new IllegalArgumentException("SubmittedTasks cannot listen to other Tasks. Use TaskManager.submitBlocked().");
			
			// Create a task for this runnable
			final Task task = new Task("Listening Runnable", Priority.NORMAL) {
				@Override
				public void work() {
					listener.run();
					set(null);
				}
			};
			
			// Block the new task on this task
			List<Task> blockedOn = new ArrayList<>();
			blockedOn.add(this);
			this.manager.submitBlocked(task, blockedOn);
		}
	}
	
	private void quickSetStatus(Status status) {
		if (this.status.isDone()) {
			if (status.compareTo(this.status) > 0) this.status = status;
		} else {
			this.status = status;
		}
	}
	
	void setStatus(Status status) {
		lock();
		try {
			quickSetStatus(status);
		} finally {
			unlock();
		}
	}
	
	public Status getStatus() {
		lock();
		try {
			return status;
		} finally {
			unlock();
		}
	}
	
	public Priority getPriority() {
		lock();
		try {
			return priority;
		} finally {
			unlock();
		}
	}

	/**
	 * Not thread safe! Just looks at the current value.
	 */
	public Status peekStatus() {
		return status;
	}
	
	/**
	 * Not thread safe! Just looks at the current value.
	 */
	public Priority peekPriority() {
		return priority;
	}

	void changePriority(Priority priority) {
		if (this.isDone()) return;

		manager.debug("Attempting to chang priority on "+this.shortString()+" to "+priority);
		
		Status status;

		lock();
		try {
			
			if (this.priority == priority) return;
			
			// Threads will not pick up an unexpected priority level,
			// so setting the priority on a WAITING or BLOCKED task
			// guarantees that it remains WAITING or BLOCKED
			this.priority = priority;
			status = this.status;
			
		} finally {
			unlock();
		}
		
		if (this.manager == null) return;
		
		if (status == Status.WAITING) {
			
			this.manager.resubmit(this);
			
		} else if (status == Status.BLOCKED) {
			
			// Resubmit blockFuture listener
			this.blockFuture.addListener(new SubmittedTask(this, Status.BLOCKED), this.manager.executor);
			
			// Raise the priority of tasks on which this task is blocked
			for (Task task : blockTasks) {
				if (task.priority.compareTo(priority) < 0) task.changePriority(priority);
			}
			
		}
	}
	
	@Override
	public String toString() {
		return "Task[name=\""+name+"\", priority="+priority+", status="+status+"]";
	}
	
	public String shortString() {
		return name+" ("+status+")";
	}

	public List<? extends Task> getBlockTasks() {
		return blockTasks;
	}

	public void lock() {
		if (!this.lock.tryLock()) {
			manager.debug(Thread.currentThread().getName()+": Lock held by "+(lockOwner == null ? "nobody" : lockOwner.getName())+", waiting...");
			this.lock.lock();
		}
		lockOwner = Thread.currentThread();
	}
	
	public void unlock() {
		this.lock.unlock();
	}
	
}
