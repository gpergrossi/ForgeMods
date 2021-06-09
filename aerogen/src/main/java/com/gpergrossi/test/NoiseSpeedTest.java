package com.gpergrossi.test;

import com.gpergrossi.util.math.SimplexNoise;
import com.gpergrossi.util.math.func2d.IFunction2D;
import net.minecraft.world.gen.NoiseGeneratorSimplex;

public class NoiseSpeedTest {

	public static void main(String[] args) {
		
		IFunction2D mine = new IFunction2D() {
			@Override
			public double getValue(double x, double y) {
				return SimplexNoise.noise(x, y);
			}
		};
		IFunction2D theirs = new IFunction2D() {
			private NoiseGeneratorSimplex gen = new NoiseGeneratorSimplex();
			@Override
			public double getValue(double x, double y) {
				return gen.getValue(x, y);
			}
		};
		
		long warmupIters = 20000000;
		long testIters = 2000000;
		
		double v = 0;

		System.out.println("Warming up...");
		for (long i = 0; i < 100; i++) {
			for (long j = 0; j < warmupIters; j++) {
				mine.getValue(v, v);
				theirs.getValue(v, v);
				v += 0.1;
			}
			System.out.println(i+"%");
		}
		
		long start0 = System.nanoTime();
		System.out.println("Testing mine...");
		for (long i = 0; i < 100; i++) {
			for (long j = 0; j < testIters; j++) {
				mine.getValue(v, v);
				v += 0.1;
			}
			System.out.println(i+"%");
		}
		double msDur0 = (System.nanoTime()-start0) / (1000000);

		long start1 = System.nanoTime();
		System.out.println("Testing theirs...");
		for (long i = 0; i < 100; i++) {
			for (long j = 0; j < testIters; j++) {
				theirs.getValue(v, v);
				v += 0.1;
			}
			System.out.println(i+"%");
		}
		double msDur1 = (System.nanoTime()-start1) / (1000000);
		
		System.out.println("Results:");
		System.out.println("My noise took "+msDur0+" ms");
		System.out.println("Their noise took "+msDur1+" ms");
		
	}
	
}
