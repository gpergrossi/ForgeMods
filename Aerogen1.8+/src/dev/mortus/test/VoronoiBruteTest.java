package dev.mortus.test;

import java.awt.geom.Point2D;
import java.util.Random;

import dev.mortus.voronoi.Voronoi;

public class VoronoiBruteTest {

	public static void main(String[] args) {
		Random r = new Random();
		for (int num = 100; num <= 30000; num += 100) {
			Voronoi v = new Voronoi();
			
			boolean success = false;
			
			long start = 0, end = 0;
			System.gc();
			while (success == false) {
				try {
					start = System.nanoTime();
					for (int i = 0; i < num; i++) {
						v.addSite(new Point2D.Double(r.nextDouble()*10000.0, r.nextDouble()*10000.0));
					}
					v.build();
					end = System.nanoTime();
					success = true;
				} catch (RuntimeException re) {
					System.out.println("FAIL");
				}
			}

			long dur = end-start;
			double time = dur*0.000000001;
			System.out.println(num+", "+time);
		}
	}
	
}
