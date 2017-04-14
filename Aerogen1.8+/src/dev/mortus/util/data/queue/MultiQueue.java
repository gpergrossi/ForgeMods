package dev.mortus.util.data.queue;

import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;

public class MultiQueue<T extends Comparable<T>> extends AbstractQueue<T> {
	
	List<Queue<T>> queues;
	
	public MultiQueue() {
		this.queues = new ArrayList<>();
	}
	
	@Override
	public boolean offer(T e) {
		throw new UnsupportedOperationException("You cannot offer items to a MultiQueue, instead offer them to the underlying queues.");
	}

	public void addQueue(Queue<T> queue) {
		this.queues.add(queue);
	}
	
	public void removeQueue(Queue<T> queue) {
		this.queues.remove(queue);
	}
	
	private Queue<T> getSmallestQueue() {
		Queue<T> winner = null;
		T winningElement = null;
		for (Queue<T> queue : queues) {
			T element = queue.peek();
			if (element == null) return null;
			if (winningElement == null || winningElement.compareTo(element) > 0) {
				winningElement = element;
				winner = queue;
			}
		}
		return winner;
	}
	
	@Override
	public T poll() {
		Queue<T> smallest = getSmallestQueue();
		if (smallest == null) return null;
		return smallest.poll();
	}

	@Override
	public T peek() {
		Queue<T> smallest = getSmallestQueue();
		if (smallest == null) return null;
		return smallest.peek();
	}

	@Override
	public Iterator<T> iterator() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int size() {
		int size = 0;
		for (Queue<T> queue : queues) {
			size += queue.size();
		}
		return size;
	}

}
