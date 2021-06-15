package com.gpergrossi.tasks;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

import com.gpergrossi.tasks.Task.Status;

public class TaskManager {
	
	public static TaskManager create(String name, int numThreads) {
		if (numThreads < 0) throw new IllegalArgumentException("Cannot create a TaskManager with "+numThreads+" threads.");
		if (numThreads == 0) {
			return new TaskManager(name, new ImmediateTaskExecutor());
		} else { 
			return new TaskManager(name, new ThreadPoolTaskExecutor(name, numThreads));
		}
	}
	
	final String name;
	final TaskExecutorService executor;
	final Set<Task> blockingTasks;
	final Set<Task> runningTasks;
	private IOHandler ioHandler;
	
	private int numTasks, numRunning;
	boolean shutdown = false;
	public boolean debug;
	
	public TaskManager(String name, TaskExecutorService executor) {
		this.name = name;
		this.executor = executor;
		this.blockingTasks = new HashSet<>();
		this.runningTasks = new HashSet<>();
		this.ioHandler = new ImmediateIOHandler();
	}

	public void setIOHandler(IOHandler ioHandler) {
		this.ioHandler = ioHandler;
	}
	
	/**
	 * <p>Submits a task. The submitted task will begin running its {@code work} method when 
	 * this TaskManager schedules it (dependent on the {@code TaskExecutorService} used to
	 * construct this TaskManager).</p>
	 * 
	 * <p>If this TaskManager {@code isShutdown} already, then the task's status will be set to
	 * FAILED and this TaskManager's {@code onBadFinish} method will be called.</p>
	 * @param task
	 * @param blockedOn
	 */
	public void submit(Task task) {
		submit(task, false);
	}
	
	/**
	 * <p>Submits a task. The submitted task will begin running its {@code work} method when 
	 * this TaskManager schedules it (dependent on the {@code TaskExecutorService} used to
	 * construct this TaskManager).</p>
	 * 
	 * <p>If this TaskManager {@code isShutdown} already and {@code ignoreShutdown} is false,
	 * then the task's status will be set to FAILED and this TaskManager's {@code onBadFinish}
	 * method will be called.</p>
	 * @param task
	 * @param blockedOn
	 */
	public void submit(Task task, boolean ignoreShutdown) {
		if (shutdown && !ignoreShutdown) {
			task.setException(new RejectedExecutionException("TaskManager \""+this.name+"\" has been shut down!"));
			onBadFinish(task);
			return;
		}
		
		task.setManager(this);
		task.setStatus(Status.WAITING);
		
		incrementTaskCount();
		
		executor.execute(new SubmittedTask(task, Status.WAITING));
	}
	
	/**
	 * <p>Submits a task that is blocked on another task or tasks. The submitted task will begin 
	 * running its {@code work} method when the list of {@code blockedOn} tasks have finished.</p>
	 * 
	 * <p>If this TaskManager {@code isShutdown} already, then the task's status will be set to
	 * FAILED and this TaskManager's {@code onBadFinish} method will be called.</p>
	 * @param task
	 * @param blockedOn
	 */
	public void submitBlocked(Task task, List<Task> blockedOn) {
		submitBlocked(task, blockedOn, false);
	}
	
	/**
	 * <p>Submits a task that is blocked on another task or tasks. The submitted task will begin 
	 * running its {@code work} method when the list of {@code blockedOn} tasks have finished.</p>
	 * 
	 * <p>If this TaskManager {@code isShutdown} already and {@code ignoreShutdown} is false, 
	 * then the task's status will be set to FAILED and this TaskManager's {@code onBadFinish}
	 * method will be called.</p>
	 * @param task
	 * @param blockedOn
	 */
	public void submitBlocked(Task task, List<Task> blockedOn, boolean ignoreShutdown) {
		if (shutdown && !ignoreShutdown) {
			task.setException(new RejectedExecutionException("TaskManager \""+this.name+"\" has been shut down!"));
			onBadFinish(task);
			return;
		}
		
		task.setManager(this);
		task.setStatus(Status.RUNNING);
		task.block(new ArrayList<>(blockedOn), task::work);
		
		incrementTaskCount();
		blockingTasks.add(task);
	}

	Task createIOTask(Runnable io) {		
		return ioHandler.submitIO(io);
	}
	
	/**
	 * Adds the task to the execution queue without incrementing the task count.
	 * Used for resubmitting a task after its priority has changed.
	 */
	void resubmit(Task task) {
		task.setStatus(Status.WAITING);
		executor.execute(new SubmittedTask(task, Status.WAITING));
	}
	
	/**
	 * Mark a task as running. Called by the {@code run} method in SubmittedTask when
	 * a the TaskExecutorService executes it.
	 */
	synchronized void begin(Task task) {
		debug("---> "+Thread.currentThread().getName()+" began working on "+task);
		if (task.getStatus() == Status.BLOCKED) blockingTasks.remove(task);
		task.setStatus(Status.RUNNING);
		runningTasks.add(task);
		numRunning++;
	}
	
	/**
	 * Mark a task as stopped. Called by the {@code run} method in SubmittedTask when
	 * a the TaskExecutorService finishes executing it. The status of the task
	 * will ideally be {@code FINISHED} or {@code BLOCKED} at this point. Any other
	 * status indicates an error and this TaskManager's onBadFinish method will be called. 
	 */
	synchronized void stop(Task task) {
		if (task.getStatus() == Status.WAITING || task.getStatus() == Status.UNINITIALIZED) {
			throw new IllegalStateException("Cannot stop task "+task+". Bad status!");
		}
		
		numRunning--;
		runningTasks.remove(task);

		if (task.getStatus() == Status.RUNNING) {
			task.setException(new RuntimeException("Task did not call set() or block()!"));
			onBadFinish(task);
			return;
		}
		
		if (task.getStatus() == Status.BLOCKED) {
			blockingTasks.add(task);
			debug("<--- "+Thread.currentThread().getName()+" stopped working on "+task);
			return;
		}
		
		if (task.getStatus().isDone()) {
			if (!task.getStatus().isGood()) onBadFinish(task);
			
			decrementTaskCount();
			
			debug("<--- "+Thread.currentThread().getName()+" finished working on "+task);
		}
	}
	
	/**
	 * Called for any tasks that does not stop running with a status of {@code FINISHED} or {@code BLOCKED}.
	 */
	void onBadFinish(Task task) {
		final Status status = task.getStatus();
		switch (status) {
			case FAILURE: throw new RuntimeException("Task failed: "+task+" "+task.getException().getMessage());
			case CANCELLED: System.err.println("Task cancelled: "+task); break;
			case INTERRUPTED: System.err.println("Task interrupted: "+task); break;
			default:
		}
	}

	/**
	 * Safely increments the number of tasks in this TaskManager.
	 */
	synchronized void incrementTaskCount() {
		numTasks++;
		debug("Task Manager \""+name+"\" has "+numTasks+" tasks");
	}

	/**
	 * Safely decrements the number of tasks in this TaskManager.
	 * If the number of tasks reaches 0 and this manager {@code isShutdown}
	 * then the {@link terminate} method will be called.
	 */
	synchronized void decrementTaskCount() {
		numTasks--;
		debug("Task Manager \""+name+"\" has "+numTasks+" tasks");
		if (numTasks < 0) throw new IllegalStateException("Current number of tasks is "+numTasks+"!");
		if (shutdown && numTasks == 0) terminate();
	}

	/**
	 * Returns the number of unfinished tasks in this TaskManager.
	 */
	public synchronized int getTaskCount() {
		return numTasks;
	}
	
	/**
	 * Marks this TaskManager as shutdown. All further task submissions without
	 * the {@code ignoreShutdown = true} argument will immediately fail. When all
	 * tasks finish, this TaskManager's {@link terminate} method will be called.
	 */
	public void shutdown() {
		shutdown = true;
		synchronized (this) {
			if (numTasks == 0) terminate();
		}
	}

	/**
	 * Called when this TaskManager {@code isShutdown} and all tasks have finished.
	 * Calls {@code shutdown} on the underlying {@code TaskExecutorService} and
	 * {@code close} on this Manager's {@code IOHandler}.
	 */
	private void terminate() {
		executor.shutdown();
		ioHandler.close();
	}

	/**
	 * Kills all running tasks and returns a list of Tasks that did not finish.
	 */
	public List<Task> shutdownNow() {
		this.shutdown();

		List<Task> dead = new ArrayList<>();
		if (numTasks == 0) return dead;
		
		// Kill blocking tasks
		for (Task blocked : blockingTasks) {
			blocked.cancel(true);
			dead.add(blocked);
		}
		blockingTasks.clear();
		
		// Ask TaskExecutorService to kill all waiting/running tasks
		List<Runnable> runnables = executor.shutdownNow();
		for (Runnable r : runnables) {
			if (r instanceof Task) {
				Task task = (Task) r;
				dead.add(task);
			} else {
				throw new RuntimeException("Encountered non-task in shutdownNow list!");
			}
		}
		
		return dead;		
	}
	
	/**
	 * Returns true if this TaskManager has been shutdown by either the
	 * {@code shutdown} or {@code shutdownNow} methods.
	 * @return
	 */
	public boolean isShutdown() {
		return shutdown;
	}

	/**
	 * Returns true if this TaskManager has been shutdown by either the
	 * {@code shutdown} or {@code shutdownNow} methods and all tasks have
	 * finished running.
	 */
	public boolean isTerminated() {
		return executor.isTerminated();
	}

	/**
	 * Waits for {@link isTerminated} to be true.
	 */
	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
		return executor.awaitTermination(timeout, unit);
	}
	
	/**
	 * Returns a formatted string listing all known tasks and their statuses act the
	 * current moment. The status may not list all tasks or their statuses correctly
	 * as it is not thread-synchronized and will not wait or cause waiting.
	 * @return
	 */
	public String dumpStatus() {
		final int[] running = new int[1];
		final int[] blocking = new int[1];
		final int[] waiting = new int[1];
		final int numTasks = this.numTasks;
		final int numRunning = this.numRunning;
		
		StringBuilder sb = new StringBuilder();
		sb.append("===============================\n");
		sb.append("TaskManager \"").append(name).append("\" status:\n");
		sb.append("There are ").append(numTasks).append(" tasks (").append(numRunning).append(" running)\n\n");
		
		sb.append("Running:\n");
		try {
			runningTasks.forEach(t -> {
				sb.append("  ").append(t.toString()).append("\n");
				running[0]++;
			});
		} catch (ConcurrentModificationException e) {}
		
		sb.append("\nBlocking:\n");
		try {
			blockingTasks.forEach(t -> {
				List<? extends Task> tasks = t.getBlockTasks();
				List<String> taskNames = tasks.stream()
					.filter(s -> s != null && s.peekStatus() != Status.FINISHED)
					.map(s -> s.shortString()).collect(Collectors.toList());
				
				sb.append("  ").append(t.toString()).append(": {\n");
				taskNames.forEach(s -> sb.append("    ").append(s).append("\n"));
				sb.append("  }\n");
				
				blocking[0]++;
			});
		} catch (ConcurrentModificationException e) {}
		
		sb.append("\nWaiting:\n");
		try {
			executor.getWaitingTasks().forEachRemaining(t -> {
				SubmittedTask submitted = t;
				
				if (submitted.task.peekStatus() != submitted.expectedStatus) return;
				if (submitted.task.peekPriority() != submitted.expectedPriority) return;
				
				sb.append("  ").append(submitted.task.toString()).append("\n");
				waiting[0]++;
			});
		} catch (ConcurrentModificationException e) {}
		
		sb.append("\nAccounted for: ").append(running[0]).append("/").append(numRunning).append(" running, ");
		sb.append(blocking[0]).append(" blocking, ").append(waiting[0]).append(" waiting.  ");
		sb.append(running[0]+blocking[0]+waiting[0]).append("/").append(numTasks).append(" total.\n");
		
		sb.append("===============================\n");
		return sb.toString();
	}
	
	
	
	private JFrame monitor;
	private Timer monitorTimer;
	
	public void setTaskMonitorVisible(boolean visible) {
		
		if (visible && monitor == null) {
			monitor = new JFrame("Task Monitor");
			monitor.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			
			JTextArea output = new JTextArea("");
			output.setPreferredSize(new Dimension(600, 400));
			output.setFont(new Font("Courier New", Font.PLAIN, 12));
			
			JScrollPane scrollPane = new JScrollPane(output);
			scrollPane.setPreferredSize(new Dimension(600, 400));
			
			monitor.add(scrollPane);
			monitor.pack();
			monitor.setVisible(true);
			
			monitor.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					super.windowClosing(e);
					setTaskMonitorVisible(false);
				}
			});
			
			monitorTimer = new Timer();
			monitorTimer.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					output.setText(dumpStatus());
				}
			}, 500, 500);
			return;
		}
		
		if (!visible && monitor != null) {
			monitorTimer.cancel();
			if (monitor.isVisible()) monitor.dispose();
			monitor = null;
			return;
		}
	}

	public void debug(String message) {
		if (!debug) return;
		System.out.println(this.name+" [DEBUG]: "+message);
	}

	public IOHandler getIOHandler() {
		return ioHandler;
	}
	
}
