package dev.mortus.test;

import java.util.Queue;
import java.util.function.IntFunction;

import dev.mortus.util.data.queue.FixedSizeArrayQueue;

public class QueueTest {


	public static void main(String[] args) {
		
		IntFunction<String[]> allocator = (size -> { return new String[size]; });
		
		Queue<String> queue = new FixedSizeArrayQueue<>(allocator, 10);
		
		test(queue);
		
		System.out.println("All tests passed");
	}

	public static void test(Queue<String> q) {

		for (int j = 0; j < 2; j++) {
			q.clear();
			assertEquals(q.size(), 0);
			
			q.offer("1");
			q.offer("2");
			q.offer("3");
			q.offer("4");
			q.offer("5");
			q.offer("6");
			q.offer("7");
			q.offer("8");
			q.offer("9");
			q.offer("10");
			
			assertEquals(q.size(), 10);
			assertFalse(q.offer("too full"));
			
			assertEquals(q.poll(), "1");
			assertTrue(q.offer("11"));
	
			assertEquals(q.poll(), "2");
			assertEquals(q.poll(), "3");
			assertEquals(q.poll(), "4");
			assertEquals(q.poll(), "5");
			assertEquals(q.poll(), "6");
			assertEquals(q.poll(), "7");
			assertEquals(q.poll(), "8");
			assertEquals(q.poll(), "9");
			assertEquals(q.poll(), "10");
			assertEquals(q.poll(), "11");
			
			for (int i = 0; i < 100; i++) {
				assertEquals(q.poll(), null);
			}
			
		}
		
	}
	
    private static void assertFalse(boolean cond) {
		if (cond) throw new RuntimeException("Assertion failed");
	}
    
    private static void assertTrue(boolean cond) {
		if (!cond) throw new RuntimeException("Assertion failed");
	}
    
	private static void assertEquals(Object i, Object j) {
		if ((i == null || j == null)) {
			if (i != j) throw new RuntimeException("Assertion failed, expected "+j+" got "+i);
			return;
		}
		if (!i.equals(j)) throw new RuntimeException("Assertion failed, expected "+j+" got "+i);
	}

	private static void assertEquals(int i, int j) {
		if (i != j) throw new RuntimeException("Assertion failed, expected "+j+" got "+i);
	}
	
}
