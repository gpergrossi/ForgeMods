package com.gpergrossi.procedural;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.gpergrossi.procedural.annotations.Reentrant;

/**
 * A DataflowClass represents a run-time "compilation" of a DataflowObject class.
 * Upon the first initialization of an instance of a given DataflowObject class,
 * the DataflowClass object will be created. All fields will be scanned to 
 * compile lists of the DataflowResult fields and DataflowMethod fields. 
 * Each DataflowMethod will also be queried for its static list of 
 * DataflowResults, which will be compiled into a map and used to initialize
 * all future instances of the DataflowObject class.
 * 
 * @author Gregary Pergrossi
 *
 * @param <T> - The DataflowObject class that extends {@code DataflowObject<T>}.
 *      (I.E. the class signature should always be of the form {@code class X extends DataflowObject<X>})
 */
public final class DataflowClass<T extends DataflowObject<T>> {
	
	static Map<Class<? extends DataflowObject<?>>, DataflowClass<?>> classMap = new HashMap<>();

	@SuppressWarnings("unchecked")
	public static <T extends DataflowObject<T>> DataflowClass<T> register(Class<T> clazz, T instance) {
		DataflowClass<T> dfclass = (DataflowClass<T>) classMap.get(clazz);
		if (dfclass == null) {
			dfclass = new DataflowClass<T>();
			dfclass.scan(clazz);
			classMap.put(clazz, dfclass);
		}
		
		return dfclass;
	}
	
	
	
	private Class<T> clazz;
	private List<Field> methods;
	private List<Field> results;

	private DataflowClass() {
		this.methods = new ArrayList<>();
		this.results = new ArrayList<>();
	}
	
	protected void buildMessage(String msg) {
		System.out.println("[DataflowClass "+clazz.getName()+"] "+msg);
	}
	
	protected void buildWarning(String msg) {
		System.out.println("[DataflowClass "+clazz.getName()+"] "+msg);
	}
	
	protected void buildError(String msg) {
		System.out.println("[DataflowClass "+clazz.getName()+"] "+msg);
	}
	
	private void scan(Class<T> clazz) {
		for (Field field : clazz.getDeclaredFields()) {
			// Any declared DataflowResult is a candidate for the results list
			if (field.getType() == DataflowResult.class) {
				final int modifiers = field.getModifiers();
				if (!Modifier.isFinal(modifiers) || Modifier.isStatic(modifiers)) {
					buildWarning("Skipping dataflow result "+field.getName()+" in class "+clazz.getName()+" because it is not final and non-static.");
					continue;
				}
				results.add(field);
			} 
			
			// Any declared DataflowMethod is a candidate for the methods list
			else if (field.getType() == DataflowMethod.class) {
				final int modifiers = field.getModifiers();
				if (!Modifier.isFinal(modifiers) || Modifier.isStatic(modifiers)) {
					buildWarning("Skipping dataflow method "+field.getName()+" in class "+clazz.getName()+" because it is not final and non-static.");
					continue;
				}
				methods.add(field);
			}
		}
		
		// Scan parent?
		Class<? super T> parent = clazz.getSuperclass();
		if (parent != null && parent != DataflowObject.class) {
			buildMessage("Parent class? "+parent.getCanonicalName());
			
		}
		
		this.results = Collections.unmodifiableList(results);
		this.methods = Collections.unmodifiableList(methods);
	}

	public void populateInstanceData(DataflowObject<T> instance, Set<DataflowResult> results, Set<DataflowMethod> methods) {		
		for (Field resultField : this.results) {
			DataflowResult resultInstance = null;
			try {
				resultInstance = (DataflowResult) resultField.get(instance);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				buildError(e.getMessage());
				continue;
			}
			resultInstance.field = resultField;
			
			// Add the resultInstance to the output
			results.add(resultInstance);
		}
		
		for (Field methodField : this.methods) {
			DataflowMethod methodInstance = null;
			try {
				methodInstance = (DataflowMethod) methodField.get(instance);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				buildError(e.getMessage());
				continue;
			}
			methodInstance.field = methodField;
			
			// Check for Reentrant annotation
			if (methodField.isAnnotationPresent(Reentrant.class)) {
				Reentrant reentrant = methodField.getAnnotation(Reentrant.class);
				methodInstance.setReentrant(reentrant.value());
			}

			// Add the methodInstance to the output
			methods.add(methodInstance);
		}
	}
	
}
