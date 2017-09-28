package com.gpergrossi.util.data.interop.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface InteropClass {

	public static enum ClassType {
		CLASS, SUBCLASS, BRIDGE;
	}
	
	public static final class NoParent {}	
	
	public ClassType type() default ClassType.CLASS;
	public Class<?> parent() default NoParent.class;
	
}
