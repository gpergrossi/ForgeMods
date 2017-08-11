package dev.mortus.test;

import java.util.Random;

import dev.mortus.util.math.vectors.Double3D;

public class Double3DRotateTest {

	public static void main(String[] args) {
				
		Random random = new Random();
		for (int i = 0; i < 4000000; i++) {
			double vx = (random.nextDouble()*5+1) * Math.signum(random.nextDouble()-0.5);
			double vy = (random.nextDouble()*5+1) * Math.signum(random.nextDouble()-0.5);
			double vz = (random.nextDouble()*5+1) * Math.signum(random.nextDouble()-0.5);
			Double3D vector = new Double3D(vx, vy, vz);
			double length = vector.length();
			
			Double3D vector2 = new Double3D(vz, vx, vy);
			double dist = vector.distanceTo(vector2);
			
			double ax = (random.nextDouble()*5+1) * Math.signum(random.nextDouble()-0.5);
			double ay = (random.nextDouble()*5+1) * Math.signum(random.nextDouble()-0.5);
			double az = (random.nextDouble()*5+1) * Math.signum(random.nextDouble()-0.5);
			Double3D axis = new Double3D(ax, ay, az);
			double theta = Math.random()*Math.PI*2.0;
			
			Double3D rotated = vector.rotate(axis,  theta);
			double rotatedLength = rotated.length();
			
			Double3D rotated2 = vector.rotate(axis,  theta);
			double rotatedDist = rotated.distanceTo(rotated2);
			
			if (Math.abs(length - rotatedLength) > 0.0001 || Math.abs(dist - rotatedDist) > 0.0001) {
				System.err.println("WRONG:");
				System.err.println("Vector = "+vector);
				System.err.println("Axis = "+axis);
				System.err.println("Theta = "+theta);
			}
		}
		
	}

}
