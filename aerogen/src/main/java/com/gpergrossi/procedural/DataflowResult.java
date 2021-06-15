package com.gpergrossi.procedural;

import java.lang.reflect.Field;
import java.util.List;

public class DataflowResult {

	public static enum Status {
		NEW, INITIALIZED, REQUESTED, IN_PROGRESS, FINISHED;
	}
	
	protected DataflowObject<?> owner;
	protected Field field;
	
	protected Status status = Status.NEW;
	protected List<DataflowMethod> productionMethods;

	private boolean success;
	private Throwable failureCause = null;
	
	public DataflowResult() {}
	
}
