package dev.mortus.util.data.queue;

import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;

/**
 * A queue based on an array of a given size, if too many items are added to the queue, a larger array
 * (2x the size) is created to store the new items. However, to avoid copying overhead, the old array 
 * will remain in memory until its items are completely used up. This provides a constant add and remove 
 * time where the cost of growth is quite small in terms of CPU time. In exchange this no-copy scheme 
 * uses 50% more memory than is strictly necessary.
 * 
 * In some cases even more memory is required because the new array may become filled before its items
 * have started to empty out. In this case the original array (which is still not empty) as well as
 * a second array at 2x the original size, a third array of 4x the size, and on and on, could all
 * exist simultaneously. While this situation is not ideal, it can be avoided by providing an accurate
 * initialCapacity value. In the worst case, the memory usage is no more than double what it needs to be.
 * 
 * @author Gregary
 */
public class GrowingArrayQueue implements Queue<Integer> {

	
	
	public public GrowingArrayQueue(int initialCapacity) {
		
	}
	
	@Override
	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean contains(Object o) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Iterator<Integer> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object[] toArray() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T[] toArray(T[] a) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean remove(Object o) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean addAll(Collection<? extends Integer> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean add(Integer e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean offer(Integer e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Integer remove() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer poll() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer element() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer peek() {
		// TODO Auto-generated method stub
		return null;
	}

}
