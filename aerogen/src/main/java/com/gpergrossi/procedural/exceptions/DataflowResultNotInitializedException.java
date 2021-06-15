package com.gpergrossi.procedural.exceptions;

import com.gpergrossi.procedural.DataflowResult;

public class DataflowResultNotInitializedException extends RuntimeException {

	private static final long serialVersionUID = 6781146296517776896L;
	
	private DataflowResult result;
	
	public DataflowResultNotInitializedException(DataflowResult result) {
		this.result = result;
	}
	
	public String getMessage() {
		return "DataflowResult not initialized! "+result;
	}
	
}
