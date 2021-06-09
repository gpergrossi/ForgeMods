package com.gpergrossi.procedural;

import java.util.HashSet;
import java.util.Set;

import com.gpergrossi.procedural.DataflowResult.Status;
import com.gpergrossi.procedural.exceptions.DataflowResultNotInitializedException;

public class DataflowExecutor {

	// This class should:
	// - Specify who shall execute DataflowObject getter pre-requisites
	// - Specify whether the calling thread is willing to block for results or would prefer an immediate-fail return
	// - (Optional) Collect a list of failed pre-requisites (e.g. for use in some kind of probe executor)
	
	Set<DataflowResult> requested;
	
	public DataflowExecutor() {
		this.requested = new HashSet<>();
	}
	
	protected void request(DataflowResult result) {
		synchronized (result) {
			if (result.status == Status.NEW) throw new DataflowResultNotInitializedException(result);
			
			result.status = Status.REQUESTED;
			synchronized (requested) {
				requested.add(result);
			}
		}
	}
	
}
