package com.gpergrossi.util.geom.shapes;

import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Random;

import com.gpergrossi.util.data.Pair;
import com.gpergrossi.util.geom.vectors.Double2D;

public final class Rect implements IShape {

	protected double x, y;
	protected double width, height;
	
	public Rect(Rectangle2D rect2d) {
		this(rect2d.getX(), rect2d.getY(), rect2d.getWidth(), rect2d.getHeight());
	}
	
	public Rect(Double2D pos, Double2D size) {
		this(pos.x(), pos.y(), size.x(), size.y());
	}
	
	public Rect(double x, double y, Double2D size) {
		this(x, y, size.x(), size.y());
	}
	
	public Rect(Double2D pos, double width, double height) {
		this(pos.x(), pos.y(), width, height);
	}
	
	public Rect(double x, double y, double width, double height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	
	public void union(Rect r) {
		double minX = Math.min(minX(), r.minX());
		double minY = Math.min(minY(), r.minY());
		double maxX = Math.max(maxX(), r.maxX());
		double maxY = Math.max(maxY(), r.maxY());
		this.x = minX;
		this.y = minY;
		this.width = maxX - minX;
		this.height = maxY - minY;
	}

	// Using the Liang-Barsky approach
	private static Pair<Double> getIntersectTValues(Rect rect, Line line) {		
		double[] p = new double[] { -line.dx, line.dx, -line.dy, line.dy };
		double[] q = new double[] { line.x - rect.minX(), rect.maxX() - line.x, line.y - rect.minY(), rect.maxY() - line.y };
		double t0 = line.tmin();
		double t1 = line.tmax();
		
		for (int i = 0; i < 4; i++) {
			if (p[i] == 0) {
				if (q[i] < 0) return null;
				continue;
			}
			double t = q[i] / p[i];
			if (p[i] < 0 && t0 < t) t0 = t;
			else if (p[i] > 0 && t1 > t) t1 = t;
		}
		
		if (t0 > t1) return null;
		return new Pair<Double>(t0, t1);
	}

	public Iterable<LineSeg> edges() {
		return new Iterable<LineSeg>() {
			public Iterator<LineSeg> iterator() {
				return new Iterator<LineSeg>() {
					int index = 0;
					Double2D[] vertices;
					{
						 vertices = new Double2D[4];
						 vertices[0] = new Double2D(minX(), minY());
						 vertices[1] = new Double2D(minX(), maxY());
						 vertices[2] = new Double2D(maxX(), maxY());
						 vertices[3] = new Double2D(maxX(), minY());
					}
					public boolean hasNext() {
						return index < vertices.length;
					}
					public LineSeg next() {
						if (index >= vertices.length) throw new NoSuchElementException();
						Double2D pt0 = vertices[index];
						Double2D pt1 = null;
						index++;
						if (index == vertices.length) pt1 = vertices[0]; 
						else pt1 = vertices[index];
						return new LineSeg(pt0.x(), pt0.y(), pt1.x(), pt1.y());
					}
				};
			}
		};
	}

	public double minX() {
		return x;
	}
	
	public double maxX() {
		return x+width;
	}
	
	public double minY() {
		return y;
	}
	
	public double maxY() {
		return y+height;
	}

	public double width() {
		return width;
	}
	
	public double height() {
		return height;
	}
	
	public double centerX() {
		return x + width/2;
	}
	
	public double centerY() {
		return y + height/2;
	}

	public void rountToInt() {
		this.roundToGrid(1, 1);
	}

	public void roundToGrid(int gridWidth, int gridHeight) {
		double minX = Math.floor(minX()/gridWidth)*gridWidth;
		double minY = Math.floor(minY()/gridHeight)*gridHeight;
		double maxX = Math.ceil(maxX()/gridWidth)*gridWidth;
		double maxY = Math.ceil(maxY()/gridHeight)*gridHeight;
		this.x = minX;
		this.y = minY;
		this.width = maxX-minX;
		this.height = maxY-minY;
	}

	public Double2D getRandomPoint(Random random) {
		double x = this.x + random.nextDouble()*width;
		double y = this.y + random.nextDouble()*height;
		return new Double2D(x, y);
	}


	@Override
	public String toString() {
		return "Rect[x0="+minX()+", y0="+minY()+", x1="+maxX()+", y1="+maxY()+"]";
	}
	
	@Override
	public Rect copy() {
		return new Rect(x, y, width, height);
	}
	
	@Override
	public double getArea() {
		return width*height;
	}
	
	@Override
	public double getPerimeter() {
		return 2*width + 2*height;
	}

	@Override
	public Double2D getCentroid() {
		return new Double2D(centerX(), centerY());
	}

	@Override
	public Rect outset(double amount) {
		Rect copy = this.copy();
		copy.expand(amount);
		return copy; 
	}
	
	public void expand(double padding) {
		this.x -= padding;
		this.y -= padding;
		this.width += padding*2;
		this.height += padding*2;
	}
	
	@Override
	public Rect inset(double amount) {
		return outset(-1);
	}

	@Override
	public boolean contains(Double2D pt) {
		return contains(pt.x(), pt.y());
	}
	
	public boolean contains(double x, double y) {
		if (x < minX() || y < minY()) return false;
		if (x > maxX() || y > maxY()) return false;
		return true;
	}

	@Override
	public boolean contains(IShape other) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean intersects(IShape other) {
		if (other instanceof Circle) this.intersects((Circle) other);
		if (other instanceof Line) this.intersects((Line) other);
		if (other instanceof Rect) this.intersects((Rect) other);
		if (other instanceof Convex) this.intersects((Convex) other);
		throw new UnsupportedOperationException();
	}
	
	public boolean intersects(Circle circ) {
		return circ.intersects(this);
	}
	
	public boolean intersects(Line line) {
		Double2D.Mutable result = new Double2D.Mutable();

		// Check intersection with rectangle edges
		for (LineSeg seg : this.edges()) {
			if (seg.intersect(result, line)) return true;	
		}
		
		// Line segments could be inside the rectangle
		if (line.length() < Double.POSITIVE_INFINITY) {
			line.getStart(result);				
			if (this.contains(result)) return true;
			line.getEnd(result);
			if (this.contains(result)) return true;
		}
		
		return false;
	}

	public boolean intersects(Rect other) {
		if (this.maxX() < other.minX()) return false;
		if (this.minX() > other.maxX()) return false;
		if (this.maxY() < other.minY()) return false;
		if (this.minY() > other.maxY()) return false;
		return true;
	}
	
	public boolean intersects(Convex poly) {
		return poly.intersects(this);
	}
	
	@Override
	public LineSeg clip(Line line) {
		Pair<Double> tValues = getIntersectTValues(this, line);
		if (tValues == null) return null;
		double t0 = tValues.first;
		double t1 = tValues.second;
		return new LineSeg(line.getX(t0), line.getY(t0), line.getX(t1), line.getY(t1));
	}
	
	@Override
	public Convex toPolygon(int numSides) {
		Double2D[] verts = new Double2D[4];
		verts[0] = new Double2D(minX(), minY());
		verts[1] = new Double2D(minX(), maxY());
		verts[2] = new Double2D(maxX(), maxY());
		verts[3] = new Double2D(maxX(), minY());
		return Convex.createPolygonDirect(verts);
	}

	@Override
	public Rect getBounds() {
		return this.copy();
	}

	@Override
	public Rectangle2D asAWTShape() {
		return new Rectangle2D.Double(x, y, width, height);
	}
	
}
