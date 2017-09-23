package com.gpergrossi.util.geom.shapes;

import java.awt.geom.Line2D;

import com.gpergrossi.util.geom.vectors.Double2D;

public class LineSeg extends Line {
	
	public LineSeg(Double2D pt0, Double2D pt1) {
		this(pt0.x(), pt0.y(), pt1.x(), pt1.y());
	}
	
	public LineSeg(double x0, double y0, double x1, double y1) {
		this.x = x0;
		this.y = y0;
		this.dx = (x1-x0);
		this.dy = (y1-y0);
	}

	public LineSeg copy() {
		return new LineSeg(x, y, dx, dy);
	}
	
	public LineSeg toSegment(double maxExtent) {
		return this.copy();
	}
	
	public double tmin() {
		return 0;
	}
	
	public double tmax() {
		return 1;
	}

	public double length() {
		return Double2D.distance(0, 0, dx, dy);
	}
	
	public Line toLine() {
		return new Line(x, y, dx, dy);
	}

	public Ray toRay() {
		return new Ray(x, y, dx, dy);
	}
	
	@Override
	public LineSeg outset(double amount) {
		return this.inset(-amount);
	}

	@Override
	public LineSeg inset(double amount) {
		Double2D.Mutable work = this.getDirection().mutable();
		work.perpendicular().normalize().multiply(amount);
		return new LineSeg(getStartX() + work.x(), getStartY() + work.y(), getEndX() + work.x(), getEndY() + work.y());
	}
	
	@Override
	public Line2D asAWTShape() {
		return new Line2D.Double(getX(tmin()), getY(tmin()), getX(tmax()), getY(tmax()));
	}
	
}
