package com.gpergrossi.util.data.interop.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface InteropCast {
		
	public static enum CastType {
		AdaptToMethod, AdaptFromMethod;
	}
	
	CastType value();
	
}
