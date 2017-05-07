package dev.mortus.test;

import java.util.Random;
import java.util.Scanner;

import dev.mortus.util.math.geom.Vec2;
import dev.mortus.voronoi.diagram.Voronoi;
import dev.mortus.voronoi.diagram.VoronoiBuilder;
import dev.mortus.voronoi.internal.Worker;

public class VoronoiBruteTest {

	private static Random r = new Random();
	
	public static void main(String[] args) {
		
		Scanner s = new Scanner(System.in);
		String line = s.nextLine();
		s.close();
		
		boolean verbose = line.contains("v");
		
		if (line.contains("s")) {
			test(10, verbose);
			if (line.contains("o")) test2(10, verbose);
			System.exit(0);
		}
		
		if (line.contains("l")) {
			test(1000000, verbose);
			if (line.contains("o")) test2(1000000, verbose);
			System.exit(0);
		}
		
		if (line.contains("o")) {
			for (int num = 500000; num > 0;  num /= 1.5) test2(num, verbose);
			System.exit(0);
		}
		
		for (int num = 500000; num > 0;  num /= 1.5) test(num, verbose);
		
	}
	
	private static void test(int num, boolean verbose) {		
		boolean success = false;
		long start = 0, end = 0;
		long update = 0;

		Voronoi.DEBUG_FINISH = verbose;
		VoronoiBuilder vb = new VoronoiBuilder();
		Worker w = null;
		
		while (success == false) {
			if (verbose) System.out.println("GC...");
			System.gc();
			
			try {
				if (verbose) System.out.println("Generating "+num+" points...");
				for (int i = 0; i < num; i++) {
					vb.addSite(Vec2.create(r.nextDouble()*10000.0, r.nextDouble()*10000.0));
				}
				
				if (verbose) System.out.println("Constructing diagram...");
				update = System.currentTimeMillis();
				start = System.nanoTime();
				
				int numResponses = 0;
				w = vb.getBuildWorker();
				while (!w.isDone()) {
					w.doWork(1000);
					if (verbose) {
						numResponses++;
						if (System.currentTimeMillis() - update > 500) {
							System.out.println("Progress: "+w.getProgressEstimate()+" ("+numResponses+" returns)");
							update = System.currentTimeMillis();
							numResponses = 0;
						}
					}
				}
				
				end = System.nanoTime();
				success = true;
			} catch (RuntimeException re) {
				System.out.println("FAIL");
				re.printStackTrace();
			}
		}

		Voronoi.DEBUG_FINISH = false;
		Voronoi v = w.getResult();
		
		long dur = end-start;
		double time = dur*0.000000001;
		System.out.println(num+", "+time+"     [sites="+v.getSites().size()+", verts="+v.getVertices().size()+", edges="+v.getEdges().size());
		System.out.println(Vec2.Direct.ALLOCATION_COUNT+" Vec2's allocated");
	}
	
	private static void test2(int num, boolean verbose) {
		be.humphreys.simplevoronoi.Voronoi vor = new be.humphreys.simplevoronoi.Voronoi(0.0001);
		boolean success = false;
		long start = 0, end = 0;
		
		while (success == false) {
			System.gc();
			try {
				if (verbose) System.out.println("Generating "+num+" points...");
				double[] xValuesIn = new double[num];
				double[] yValuesIn = new double[num];
				for (int i = 0; i < num; i++) {
					xValuesIn[i] = r.nextDouble()*10000.0;
					yValuesIn[i] = r.nextDouble()*10000.0;
				}

				if (verbose) System.out.println("Constructing diagram...");
				start = System.nanoTime();
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
