package com.gpergrossi.test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.gpergrossi.tasks.Task;
import com.gpergrossi.tasks.TaskManager;
import com.gpergrossi.tasks.Task.Priority;

public class TaskTest {

	public static class TestTask extends Task {

		boolean shouldBlock = false;
		String result;
		
		public TestTask(String name, boolean shouldBlock, String result) {
			super(name);
			this.shouldBlock = shouldBlock;
			this.result = result;
		}
		
		public TestTask(String name) {
			this(name, true, "");
		}
		
		@Override
		public void work() {
			
			if (shouldBlock) {
				List<Task> list = new ArrayList<>();
				list.add(new TestTask(this.name+" Spawn 1", false, "H"));
				list.add(new TestTask(this.name+" Spawn 2", false, "e"));
				list.add(new TestTask(this.name+" Spawn 3", false, "l"));
				list.add(new TestTask(this.name+" Spawn 4", false, "l"));
				list.add(new TestTask(this.name+" Spawn 5", false, "o"));
				list.add(new TestTask(this.name+" Spawn 6", false, " "));
				list.add(new TestTask(this.name+" Spawn 7", false, "W"));
				list.add(new TestTask(this.name+" Spawn 8", false, "o"));
				list.add(new TestTask(this.name+" Spawn 9", false, "r"));
				list.add(new TestTask(this.name+" Spawn 10", false, "l"));
				list.add(new TestTask(this.name+" Spawn 11", false, "d"));
				list.add(new TestTask(this.name+" Spawn 12", false, "!"));
				
				list.forEach(t -> this.getManager().submit(t));

				block(list, this::resume);
				return;
			}

			try {
				long time = (long) (Math.random() * 2000 + 500);
				Thread.sleep(time);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			setFinished();
		}
		
		public void resume() {			
			try {
				long time = (long) (Math.random() * 2000 + 500);
				Thread.sleep(time);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			String result = "";
			List<Object> results = getBlockResults();
			for (Object o : results) {
				result += o;
			}
			
			setFinished();
		}
		
	}
	
	public static void main(String[] args) throws InterruptedException, ExecutionException {
		final TaskManager manager = TaskManager.create("TestManager", 4);

		manager.setTaskMonitorVisible(true);
		
		Task stringTask1 = new TestTask("Daemon 1");
		Task stringTask2 = new TestTask("Daemon 2");
		manager.submit(stringTask1);
		manager.submit(stringTask2);
		
		stringTask1.addListener(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(1000);
					System.out.println(stringTask1.get());
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}
			}
		});
		
		Thread.sleep(1000);
		System.out.println(stringTask2.getResult(Priority.HIGHEST));
		
		manager.shutdown();
		manager.awaitTermination(60, TimeUnit.SECONDS);

		manager.setTaskMonitorVisible(false);
	}
	
}
