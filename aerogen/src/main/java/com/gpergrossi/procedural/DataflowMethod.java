package com.gpergrossi.procedural;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.gpergrossi.procedural.annotations.Reentrant;

public abstract class DataflowMethod {
	
	/** LIST_NO_RESULTS is simply an unmodifiable constant empty DataflowResult list */
	public static final List<DataflowResult> LIST_NO_RESULTS = Collections.unmodifiableList(new ArrayList<DataflowResult>(0));
	
	/** LIST_NO_PREREQUISITES is simply an unmodifiable constant empty DataflowResult list */
	public static final List<DataflowResult> LIST_NO_PREREQUISITES = LIST_NO_RESULTS;
	
	
	
	protected DataflowObject<?> owner;
	protected Field field;
	protected Reentrant.Type reentrant = Reentrant.Type.EXCEPTION;
	
	public DataflowMethod() {}
	
	protected void setReentrant(Reentrant.Type value) {
		reentrant = value;
	}
	
	/**
	 * A list of DataflowResults needed to complete the doWork() method.
	 * This should include all DataflowResults that are require()d by any
	 * DataflowObject methods that will be called.<br><br>
	 * 
	 * It is allowed to call methods from doWork() that have require()d
	 * DataflowResults which were not mentioned in the getPrerequisites() 
	 * method. However, this will result in the Task being halted each time 
	 * an unexpected requirement is encountered, thus resulting in terrible
	 * execution speed and parallelism.
	 * 
	 * @return A best-effort list of all DataflowResult requirements that 
	 * will be needed to complete the doWork() method.
	 */
	public abstract List<DataflowResult> getPrerequisites();
	
	/**
	 * Do all necessary work specified by this DataflowMethod. All resulting
	 * changes to state should be entirely encapsulated within the DataflowObject
	 * instance to which this DataflowMethod instance belongs.
	 */
	public abstract void doWork();
	
	/**
	 * Returns a list of DataflowResults that are produced by this DataflowMethod.
	 * This method is called only once, when the DataflowObject containing this
	 * DataflowMethod is initialized. Therefore, the results list returned should
	 * not change after initialization.
	 * 
	 * @return A list of DataflowResult produced by this DataflowMethod.
	 */
	public abstract List<DataflowResult> getResults();
	
}
