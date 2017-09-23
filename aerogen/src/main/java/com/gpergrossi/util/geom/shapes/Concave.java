package com.gpergrossi.util.geom.shapes;

import java.awt.Shape;

import com.gpergrossi.util.geom.vectors.Double2D;

public class Concave implements IShape {

	@Override
	public IShape copy() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getArea() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getPerimeter() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Double2D getCentroid() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IShape outset(double amount) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IShape inset(double amount) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean contains(Double2D pt) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean contains(IShape other) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean intersects(IShape other) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Line clip(Line line) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Convex toPolygon(int numSides) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Rect getBounds() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Shape asAWTShape() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
