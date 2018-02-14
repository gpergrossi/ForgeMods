package com.gpergrossi.util.data;

import java.util.Iterator;
import java.util.function.Function;

public class IteratorCaster<From, To> implements Iterator<To> {

	private Iterator<From> iterator;
	private Function<From, To> caster;
	
	public IteratorCaster(Iterator<From> iterator, Function<From, To> caster) {
		this.iterator = iterator;
		this.caster = caster;
	}
	
	@Override
	public boolean hasNext() {
		return iterator.hasNext();
	}

	@Override
	public To next() {
		return caster.apply(iterator.next());
	}
	
	@Override
	public void remove() {
		iterator.remove();
	}

}
