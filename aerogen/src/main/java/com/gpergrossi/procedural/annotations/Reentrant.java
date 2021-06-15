package com.gpergrossi.procedural.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to let the DataflowExecutor know that it is allowed to start
 * a DataflowMethod over again when an unexpected requirement is found.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Reentrant {

	public static enum Type {
		ALLOWED, WARN, EXCEPTION
	}
	
	Type value() default Type.ALLOWED;
	
}
