package com.gpergrossi.procedural;

@FunctionalInterface
public interface ITaskListener {

	// TODO make all changes in status trigger an event
	
	public void onTaskEvent(DataflowTask task, DataflowTask.Status status);
	
}
