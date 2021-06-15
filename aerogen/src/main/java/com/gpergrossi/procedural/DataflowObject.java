package com.gpergrossi.procedural;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.gpergrossi.procedural.DataflowResult.Status;
import com.gpergrossi.procedural.exceptions.DataflowObjectNotInitializedException;

public abstract class DataflowObject<T extends DataflowObject<T>> {
	
	DataflowClass<T> clazz = null;
	Set<DataflowResult> results;
	Set<DataflowMethod> methods;
	
	protected DataflowObject() {}
	
	@SuppressWarnings("unchecked")
	protected void initialize() {
		this.clazz = DataflowClass.register((Class<T>) this.getClass(), (T) this);
				
		this.results = new HashSet<>();
		this.methods = new HashSet<>();
		this.clazz.populateInstanceData(this, results, methods);
		this.results = Collections.unmodifiableSet(results);
		this.methods = Collections.unmodifiableSet(methods);
		
		for (DataflowMethod method : methods) {
			method.owner = this;
			for (DataflowResult methodResult : method.getResults()) {
				if (!results.contains(methodResult)) {
					this.clazz.buildError("DataflowMethod \""+method.field.getName()+"\" references a DataflowResult that was not declared as a member of this class.");
					continue;
				}
				if (methodResult.productionMethods == null) {
					methodResult.productionMethods = new ArrayList<>();
				}
				methodResult.productionMethods.add(method);
			}
		}
		
		for (DataflowResult result : results) {
			result.owner = this;
			result.status = Status.INITIALIZED;
			if (result.productionMethods == null) {
				this.clazz.buildError("DataflowResult \""+result.field.getName()+"\" is not produced by any DataflowMethod declared as a member of this class.");
			}
		}
	}

	protected boolean require(DataflowExecutor executor, DataflowResult... requirements) {
		if (clazz == null) throw new DataflowObjectNotInitializedException();
		
		/**
		 * TODO Take list of requirements:
		 * For each:
		 *   If requirement is complete, do nothing
		 *   Else, locate the DataflowMethod that satisfies the requirement
		 *     Send it to the executor
		 *
		 * Wait for all requirements
		 * Return true if no errors
		 */
		
		return false;
	}
	
}