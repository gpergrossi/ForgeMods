package com.gpergrossi.util.math.func2d;

import java.util.Random;

public class SimplexNoise2D implements Function2D {

	public static double MAX_OFFSET = 16777216.0; //2^24
	
	//Manipulation of noise to allow seeded noise generation
	int xDir = 1;
	int yDir = 1;
	double xOff = 0;
	double yOff = 0;
	
	double frequency = 1.0/64.0;
	
	public SimplexNoise2D(long seed, double frequency) {
		Random random = new Random(seed);
		
		xDir = random.nextInt(2);
		if(xDir == 0) xDir = 1;
		yDir = random.nextInt(2);
		if(yDir == 0) yDir = 1;
		
		this.xOff = random.nextDouble()*MAX_OFFSET*xDir;
		this.yOff = random.nextDouble()*MAX_OFFSET*yDir;
		
		this.xDir = random.nextInt(2);
		if(this.xDir == 0) this.xDir = 1;
		this.yDir = random.nextInt(2);
		if(this.yDir == 0) this.yDir = 1;
		
		this.frequency = frequency;
	}

	public double getValue(double x, double y) {
		return SimplexNoise.noise(x*xDir*frequency+xOff, y*yDir*frequency+yOff);
	}

}
