package com.gpergrossi.procedural.exceptions;

import com.gpergrossi.procedural.DataflowObject;

public class DataflowObjectNotInitializedException extends RuntimeException {

	private static final long serialVersionUID = -1943908515292207179L;

	private DataflowObject<?> object;
	
	public DataflowObjectNotInitializedException(DataflowObject<?> object) {
		this.object = object;
	}
	
	@Override
	public String getMessage() {
		return "DataflowObject not initialized! "+object;
	}
	
}
