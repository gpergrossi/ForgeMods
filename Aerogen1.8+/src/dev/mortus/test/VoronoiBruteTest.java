package dev.mortus.test;

import java.util.Random;
import java.util.Scanner;

import be.humphreys.simplevoronoi.Voronoi;
import dev.mortus.util.math.geom.Vec2;
import dev.mortus.voronoi.diagram.VoronoiBuilder;

public class VoronoiBruteTest {

	public static void main(String[] args) {
		
		Scanner s = new Scanner(System.in);
		String line = s.nextLine();
		s.close();
		
		if (line.contains("o")) {
			other();
			System.exit(0);
		}
		
		Random r = new Random();
		for (int num = 100; num <= 30000; num += 100) {
			VoronoiBuilder v = new VoronoiBuilder();
			
			boolean success = false;
			
			long start = 0, end = 0;
			System.gc();
			while (success == false) {
				try {
					start = System.nanoTime();
					for (int i = 0; i < num; i++) {
						v.addSite(new Vec2(r.nextDouble()*10000.0, r.nextDouble()*10000.0));
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
	
	public static void other() {
		Random r = new Random();
		for (int num = 100; num <= 30000; num += 100) {
			be.humphreys.simplevoronoi.Voronoi vor = new Voronoi(0.0000001);
			
			boolean success = false;
			
			long start = 0, end = 0;
			System.gc();
			while (success == false) {
				try {
					start = System.nanoTime();
					double[] xValuesIn = new double[num];
					double[] yValuesIn = new double[num];
					for (int i = 0; i < num; i++) {
						xValuesIn[i] = r.nextDouble()*10000.0;
						yValuesIn[i] = r.nextDouble()*10000.0;
					}
					vor.generateVoronoi(xValuesIn, yValuesIn, 0, 10000.0, 0, 10000.0);
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
