package dev.mortus.test;

import java.util.Scanner;

import dev.mortus.util.data.StableArrayList;

public class StableArrayListTest {
	
	static class Dummy {}
	
	public static void main(String[] args) {
		
		System.out.println("Press enter to continue");
		Scanner sc = new Scanner(System.in);
		sc.nextLine();
		sc.close();
		
		for (int num = 100; num <= 1000000; num += 500) {
			
			StableArrayList<Dummy> all = new StableArrayList<>(Dummy.class, 8);
			for (int i = 0; i < num; i++) {
				Dummy d = new Dummy();
				all.add(d);
			}
			
			long start = 0, end = 0;
			System.gc();
			
			start = System.nanoTime();
			for (int i = 0; i < num; i++) {
				all.remove(i);
			}
			end = System.nanoTime();

			long dur = end-start;
			double time = dur*0.000000001;
			System.out.println(num+", "+time);
		}
	}
	
}
