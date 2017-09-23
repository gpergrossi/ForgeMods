package com.gpergrossi.util.math.func2d;

import com.gpergrossi.util.data.ranges.Int2DRange;

public class FiniteFeature implements Function2D {

	Int2DRange.Floats feature;
	
	double radius;

	public FiniteFeature(Int2DRange.Floats details) {
		this.feature = details;
		
		double dx = details.width / 2.0;
		double dy = details.height / 2.0;
		this.radius = Math.sqrt(dx*dx + dy*dy);
	}
	
	@Override
	public double getValue(double x, double y) {
		return feature.lerp((float) x, (float) y, 0);
	}
	
	public double getCenterX() {
		return (feature.maxX + feature.minX) / 2.0;
	}
	
	public double getCenterY() {
		return (feature.maxY + feature.minY) / 2.0;
	}
	
	public int getWidth() {
		return feature.width;
	}
	
	public int getHeight() {
		return feature.height;
	}
	
	public double getRadius() {
		return radius;
	}
	
}
