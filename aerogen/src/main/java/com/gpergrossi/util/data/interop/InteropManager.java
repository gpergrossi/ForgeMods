package com.gpergrossi.util.data.interop;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gpergrossi.util.data.OrderedPair;
import com.gpergrossi.util.data.interop.annotations.InteropCast;
import com.gpergrossi.util.data.interop.annotations.InteropClass;
import com.gpergrossi.util.data.interop.exception.InteropCastException;
import com.gpergrossi.util.data.interop.exception.InteropTagException;
import com.gpergrossi.util.data.interop.interfaces.InteropAdapter;

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import io.github.lukehutch.fastclasspathscanner.matchprocessor.ClassAnnotationMatchProcessor;

public class InteropManager<T> {

	public static Map<Class<?>, InteropTypeClass<?>> subclasses;
	
	Class<T> typeClass;
	Map<OrderedPair<Class<? extends T>>, InteropAdapter<T>> adapters;
	
	boolean debug;
	
	public InteropManager(Class<T> typeClass) {
		this.typeClass = typeClass;
		this.adapters = new HashMap<>();
		this.scanForSubclasses();
	}
	
	private void scanForSubclasses() {
		FastClasspathScanner classScan = new FastClasspathScanner("-io.github.lukehutch", "-net.minecraft");
		classScan.addClassLoader(classLoader);
		classScan.matchClassesWithAnnotation(
			InteropClass.class,
			new ClassAnnotationMatchProcessor() {
				public void processMatch(Class<?> arg0) {
					
				}
			}
		);
	}

	@SuppressWarnings("unchecked")
	public <K extends T> K adapt(T objectFrom, Class<K> classTo) {
		Class<? extends T> classFrom = (Class<? extends T>) objectFrom.getClass();
		OrderedPair<Class<? extends T>> classPair = new OrderedPair<>(classFrom, classTo);
		InteropAdapter<T> adapter = adapters.get(classPair);
		if (adapter == null) throw new InteropCastException(classFrom, classTo, objectFrom, "No adapter found");
		return (K) adapter.adapt(objectFrom);
	}
	
	public void registerAdapter(Class<? extends T> classFrom, Class<? extends T> classTo, InteropAdapter<T> adapter, boolean overwrite) {
		OrderedPair<Class<? extends T>> classPair = new OrderedPair<>(classFrom, classTo);
		if (!adapters.containsKey(classPair)) {
			if (debug) System.out.println("new adapter: "+classFrom.getName()+" --> "+classTo.getName());
			adapters.put(classPair, adapter);
		} else {
			if (debug) System.out.println("duplicate adapter: "+classFrom.getName()+" --> "+classTo.getName()+(overwrite ? " (overwritten)" : " (discarded)"));
			if (overwrite) adapters.put(classPair, adapter);
		}
	}
	
	public void registerClass(Class<? extends T> clazz) throws InteropTagException {
		Method[] methods = clazz.getMethods();
		for (Method method : methods) {
			if (method.isAnnotationPresent(InteropCast.class)) registerMethod(method);
		}
	}
	
	private void registerMethod(final Method method) throws InteropTagException {
		InteropCast tag = method.getAnnotation(InteropCast.class);
		if (tag == null) throw new InteropTagException(method, tag, "Cannot register method because it does not have an InteropTag type.");
		switch (tag.value()) {
			case AdaptFromMethod: {
				// Verify method (must be static, have one parameter T, and a return type T)
				requireStatic(method, tag);
				final Class<? extends T> parameterTypeT = requireOneParameterT(method, tag);
				final Class<? extends T> returnTypeT = requireReturnT(method, tag);
				
				// Create adapter
				this.registerAdapter(parameterTypeT, returnTypeT, (param) -> {
					try {
						return returnTypeT.cast(method.invoke(null, param));
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						throw new InteropCastException(method, parameterTypeT, returnTypeT, param, "Cast operation failed", e);
					}
				}, false);
				if (debug) System.out.println("Registered "+method.getDeclaringClass().getName()+"."+method.getName()+"() as adapter");
				return;
			}
			case AdaptToMethod: {
				// Verify method (must be non-static, have no parameters, and a return type T)
				requireNoParameters(method, tag);
				final Class<? extends T> instanceT = requireNonStaticT(method, tag);
				final Class<? extends T> returnTypeT = requireReturnT(method, tag);
				
				// Create adapter
				this.registerAdapter(instanceT, returnTypeT, (param) -> {
					try {
						return returnTypeT.cast(method.invoke(param));
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						throw new InteropCastException(method, returnTypeT, param, "Cast operation failed", e);
					}
				}, false);
				if (debug) System.out.println("Registered "+method.getDeclaringClass().getName()+"."+method.getName()+"() as adapter");
				return;
			}
		}		
	}

	/**
	 * Makes sure that this method is static.
	 * @param method - the method in question
	 * @param tag - the tag being examined, used for exception report
	 * @throws InteropTagException if the condition fails
	 */
	private void requireStatic(final Method method, InteropCast tag) throws InteropTagException {
		if (!Modifier.isStatic(method.getModifiers())) throw new InteropTagException(method, tag, "Method must be static");
	}

	/**
	 * Makes sure that this method has exactly one parameter from which this {@code InteropManager}'s {@code typeClass} is assignable.
	 * Returns a {@code Class<? extends T>} representing the parameter type's class.
	 * @param method - the method in question
	 * @param tag - the tag being examined, used for exception report
	 * @return a {@code Class<? extends T>} representing the parameter type's class.
	 * @throws InteropTagException if the condition fails
	 */
	@SuppressWarnings("unchecked")
	private Class<? extends T> requireOneParameterT(final Method method, InteropCast tag) throws InteropTagException {
		Class<?>[] parameterTypes = method.getParameterTypes();
		if (parameterTypes.length != 1) throw new InteropTagException(method, tag, "Method must have exactly one parameter");
		if (!typeClass.isAssignableFrom(parameterTypes[0])) throw new InteropTagException(method, tag, "Parameter type does not agree with the generic type for this InteropManager");
		Class<? extends T> parameterTypeT = (Class<? extends T>) parameterTypes[0];
		return parameterTypeT;
	}

	/**
	 * Makes sure that this method is both non-static and implemented in a class from which this {@code InteropManager}'s {@code typeClass} is assignable.
	 * Returns a {@code Class<? extends T>} representing the declaring class.
	 * @param method - the method in question
	 * @param tag - the tag being examined, used for exception report
	 * @return a {@code Class<? extends T>} representing the declaring class.
	 * @throws InteropTagException if the condition fails
	 */
	@SuppressWarnings("unchecked")
	private Class<? extends T> requireNonStaticT(Method method, InteropCast tag) throws InteropTagException {
		if (Modifier.isStatic(method.getModifiers())) throw new InteropTagException(method, tag, "Method must not be static");
		if (!typeClass.isAssignableFrom(method.getDeclaringClass())) throw new InteropTagException(method, tag, "Declaring class does not agree with the generic type for this InteropManager");
		return (Class<? extends T>) method.getDeclaringClass();
	}
	
	/**
	 * Makes sure that this method has no parameters.
	 * @param method - the method in question
	 * @param tag - the tag being examined, used for exception report
	 * @throws InteropTagException if the condition fails
	 */
	private void requireNoParameters(Method method, InteropCast tag) throws InteropTagException {
		Class<?>[] parameterTypes = method.getParameterTypes();
		if (parameterTypes.length != 0) throw new InteropTagException(method, tag, "Method must not have any parameters");
	}
	
	/**
	 * Makes sure that this method returns a type from which this {@code InteropManager}'s {@code typeClass} is assignable.
	 * Returns a {@code Class<? extends T>} representing the return type's class.
	 * @param method - the method in question
	 * @param tag - the tag being examined, used for exception report
	 * @return a {@code Class<? extends T>} representing the return type's class.
	 * @throws InteropTagException if the condition fails
	 */
	@SuppressWarnings("unchecked")
	private Class<? extends T> requireReturnT(final Method method, InteropCast tag) throws InteropTagException {
		// Verify method (must return a type <T>)
		Class<?> returnType = method.getReturnType();
		if (!typeClass.isAssignableFrom(returnType)) throw new InteropTagException(method, tag, "Return type does not agree with the generic type for this InteropManager");
		Class<? extends T> returnTypeT = (Class<? extends T>) returnType;
		return returnTypeT;
	}

	public <K> K doMethod(String method, Class<K> returnType, T self, Object... args) {
		try {
			return returnType.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
