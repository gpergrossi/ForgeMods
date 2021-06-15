package com.gpergrossi.procedural;

@FunctionalInterface
public interface IResultListener {

	// TODO make all changes in status trigger an event
	
	public void onResultEvent(DataflowResult result, DataflowResult.Status status);
	
}
