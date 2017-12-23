package com.gpergrossi.util.data.interop.interfaces;

import com.gpergrossi.util.data.interop.exception.InteropCastException;

@FunctionalInterface
public interface IInteropAdapter<T> {

	public T adapt(T other) throws InteropCastException;
	
}
