package com.gpergrossi.test.experiment;

import java.util.ArrayList;

/**
 * The data collected from this test (which I admit isn't perfect)
 * clearly indicates that ArrayList.contains() has a linear scaling time.
 * 
 * The results of the test show a very clean quadratic ascent, resulting from
 * the number of objects growing linearly as well as the time it takes to find
 * each one growing linearly.
 * 
 * Of course, these results may be obvious to almost everybody. But I decided
 * to test it instead of making a fairly safe assumption, as the test required
 * very little effort.
 * 
 * One can, of course, use a HashSet to get "constant" time scaling for the contains()
 * operation, but that constant time is larger than it needs to be. My findings led
 * me to create the "storage" classes in com.gpergrossi.data.storage. Items stored
 * in a Storage implement the StorageItem interface and record their own index.
 * This allows for constant time and faster-than-hash lookup of items. It also
 * provides 
 * 
 * @author Gregary Pergrossi
 */
public class ArrayListTest {

	static class Dummy {}
	
	public static void main(String[] args) {
				
		for (int num = 100; num <= 1000000; num += 100) {
						
			ArrayList<Dummy> all = new ArrayList<>();
			ArrayList<Dummy> toFind = new ArrayList<>();
			for (int i = 0; i < num; i++) {
				Dummy d = new Dummy();
				all.add(d);
				toFind.add(d);
			}
			
			long start = 0, end = 0;
			System.gc();
			
			int i = 0;
			start = System.nanoTime();
			for (Dummy d : toFind) {
				if (all.contains(d)) i++;
			}
			if (i != all.size()) System.err.println("test error");
			end = System.nanoTime();

			long dur = end-start;
			double time = dur*0.000000001;
			System.out.println(num+", "+time);
		}
	}
	
}
