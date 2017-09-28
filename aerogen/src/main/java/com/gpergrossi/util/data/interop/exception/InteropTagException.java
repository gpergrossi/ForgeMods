package com.gpergrossi.util.data.interop.exception;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class InteropTagException extends RuntimeException {

	private static final long serialVersionUID = -5245919691251027033L;
	
	Method offenderMethod;
	Annotation offenderAnnotation;
	
	public InteropTagException(Method method, Annotation annotation, String message) {
		super(message);
		this.offenderMethod = method;
		this.offenderAnnotation = annotation;
	}
	
}
