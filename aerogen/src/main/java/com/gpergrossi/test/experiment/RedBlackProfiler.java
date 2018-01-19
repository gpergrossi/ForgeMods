package com.gpergrossi.test.experiment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.gpergrossi.util.data.OrderedPair;
import com.gpergrossi.util.data.btree.RedBlackTree;

public class RedBlackProfiler {

	public static void main(String[] args) {
//		System.out.println("Press enter to start");
//		Scanner sc = new Scanner(System.in);
//		sc.nextLine();
		
		doTest(10000000, 40000);
		for (int i = 40000; i <= 1000000; i *= 1.2) {
			doTest(i, 40000);
		}
		
		List<OrderedPair<Double>> results = new ArrayList<>();
		for (int i = 40000; i <= 10000000; i *= 1.2) {
			OrderedPair<Double> result = doTest(i, 40000);
			results.add(result);
		}
		
		for (OrderedPair<Double> result : results) {
			System.out.println(result.first+", "+result.second);
		}
	}

	private static OrderedPair<Double> doTest(int treeSize, int numTests) {
		RedBlackTree<Integer, Boolean> tree = new RedBlackTree<>();
		Random random = new Random();
		
		System.out.println("Testing (Treesize="+treeSize+", numTests="+numTests+"):");
		System.out.println("Building Tree...");
		List<Integer> removeValues = new ArrayList<>();
		for (int i = 0; i < treeSize; i++) {
			tree.put(i, true);
			removeValues.add(i);
		}
		Collections.shuffle(removeValues);
		
		startTimer("Adding/Removing");
		for (int i = 0; i < numTests; i++) {
			tree.put(random.nextInt(), true);
			
			int value = removeValues.get(i);
			tree.remove(value);
		}
		double removeTimeMS = endTimer();
		removeTimeMS /= numTests;
		
		return new OrderedPair<Double>((double) treeSize, removeTimeMS);
	}
	
	private static long startTime;
	private static String label;
	
	private static void startTimer(String string) {
		label = string;
		System.out.println(label+"...");
		startTime = System.nanoTime();
	}
	
	private static double endTimer() {
		long time = System.nanoTime() - startTime;
		System.out.println(label+"... done! ("+time+" ns)");
		
		return time * 0.000001;
	}
	
}
