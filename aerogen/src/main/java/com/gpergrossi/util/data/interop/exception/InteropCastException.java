package com.gpergrossi.util.data.interop.exception;

import java.lang.reflect.Method;

public class InteropCastException extends RuntimeException {

	private static final long serialVersionUID = -3002182631185934378L;

	Method method;
	Class<?> typeFrom, typeTo;
	Object object;
	
	public InteropCastException(Method method, Class<?> paramType, Class<?> returnType, Object object, String message, Throwable reason) {
		super(message + ". Signature = \""+returnType.getName()+" "+method.getDeclaringClass().getName()+"."+method.getName()+"("+paramType.getName()+")\"", reason);
		this.method = method;
		this.typeFrom = paramType;
		this.typeTo = returnType;
		this.object = object;
	}
	
	public InteropCastException(Method method, Class<?> returnType, Object object, String message, Throwable reason) {
		super(message + ". Signature = \""+returnType.getName()+" (("+method.getDeclaringClass().getName()+") x)."+method.getName()+"()\"", reason);
		this.method = method;
		this.typeFrom = method.getDeclaringClass();
		this.typeTo = returnType;
		this.object = object;
	}
	
	public InteropCastException(Class<?> typeFrom, Class<?> typeTo, Object object, String message) {
		super(message + ". "+typeFrom.getName()+" --> "+typeTo.getName());
		this.method = null;
		this.typeFrom = typeFrom;
		this.typeTo = typeTo;
		this.object = object;
	}
	
}
