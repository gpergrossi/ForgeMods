package dev.mortus.util.math.geom;

import java.awt.Shape;
import java.awt.geom.Path2D;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class Polygon {

	final Vec2[] vertices;
	private Vec2 centroid;
	private double area = Double.NaN;
	private Shape shape;
	private Rect bounds;

	public Polygon(Vec2... vertices) {
		Vec2[] verts = new Vec2[vertices.length];
		System.arraycopy(vertices, 0, verts, 0, vertices.length);
		this.vertices = sanitize(verts);
	}

	public Polygon(List<Vec2> vertices) {
		Vec2[] verts = new Vec2[vertices.size()];
		verts = vertices.toArray(verts);
		this.vertices = sanitize(verts);
	}

	/**
	 * Removes duplicates
	 * @param vertices
	 * @return
	 */
	private Vec2[] sanitize(Vec2[] vertices) {
		int numValid = 0;
		for (int i = 0; i < vertices.length; i++) {
			boolean valid = true;
			for (int j = 0; j < i; j++) {
				if (vertices[j] == null) continue;
				if (vertices[j].equals(vertices[i])) valid = false;
			}
			if (valid) numValid++;
			else vertices[i] = null;
		}
		Vec2[] sanitized = new Vec2[numValid];
		int addIndex = 0;
		for (int i = 0; i < vertices.length; i++) {
			if (vertices[i] == null) continue;
			sanitized[addIndex++] = vertices[i];
		}
		return sanitized;
	}
	
	public double getArea() {
		if (Double.isNaN(area)) {
			area = 0;
			for (int i = 0; i < vertices.length; i++) {
				Vec2 a = vertices[i];
				Vec2 b = ((i+1 < vertices.length) ? vertices[i + 1] : vertices[0]);
				area += a.cross(b);
			}
			area /= 2;
		}
		if (area <= 0) System.err.println("Polygon has "+this.area+" area!");
		return area;
	}

	public Vec2 getCentroid() {
		if (centroid == null) {
			double cx = 0, cy = 0;
			double area = 0;
			for (int i = 0; i < vertices.length; i++) {
				Vec2 a = vertices[i];
				Vec2 b = ((i+1 < vertices.length) ? vertices[i + 1] : vertices[0]);
				double cross = a.cross(b);
				area += cross;
				cx += (a.x + b.x) * cross;
				cy += (a.y + b.y) * cross;
			}
			area /= 2;
			this.area = area;
			if (this.area <= 0) {
				System.err.println("Polygon has "+this.area+" area!");
				return null;
			}
			
			cx /= (area * 6);
			cy /= (area * 6);
			this.centroid = new Vec2(cx, cy);
		}
		return this.centroid;
	}

	public Shape getShape2D() {
		if (shape == null) {
			if (vertices.length < 3) {
				System.err.println("Polygon has only has "+vertices.length+" vertices!");
				return null;
			}
			
			Path2D path = new Path2D.Double();
			path.moveTo(vertices[0].x, vertices[0].y);
			for (int i = 1; i < vertices.length; i++) {
				path.lineTo(vertices[i].x, vertices[i].y);
			}
			path.closePath();
			shape = path;
		}
		return shape;
	}
	
	public Rect getBounds() {
		if (bounds == null) {
			double minX = Double.POSITIVE_INFINITY;
			double minY = Double.POSITIVE_INFINITY;
			double maxX = Double.NEGATIVE_INFINITY;
			double maxY = Double.NEGATIVE_INFINITY;
			for (int i = 0; i < vertices.length; i++) {
				Vec2 vert = vertices[i];
				minX = Math.min(minX, vert.x);
				maxX = Math.max(maxX, vert.x);
				minY = Math.min(minY, vert.y);
				maxY = Math.max(maxY, vert.y);
			}
			bounds = new Rect(minX, minY, maxX-minX, maxY-minY);
		}
		return bounds;
	}

	public Iterable<LineSeg> edges() {
		return new Iterable<LineSeg>() {
			public Iterator<LineSeg> iterator() {
				return new Iterator<LineSeg>() {
					int index = 0;
					public boolean hasNext() {
						return index < vertices.length;
					}
					public LineSeg next() {
						if (index >= vertices.length) throw new NoSuchElementException();
						Vec2 pt0 = vertices[index];
						Vec2 pt1 = null;
						index++;
						if (index == vertices.length) pt1 = vertices[0]; 
						else pt1 = vertices[index];
						return new LineSeg(pt0.x, pt0.y, pt1.x, pt1.y);
					}
				};
			}
		};
	}
	
	public boolean contains(double x, double y) {
		return this.getShape2D().contains(x, y);
	}
	
	public boolean intersects(Rect r) {
		return this.getShape2D().intersects(r.getShape2D());
	}
	
	public boolean intersects(Polygon p) {
		if (p.contains(vertices[0].x(), vertices[0].y())) return true;
		if (this.contains(p.vertices[0].x(), p.vertices[0].y())) return true;
		for (LineSeg otherEdge : p.edges()) {
			for (LineSeg myEdge : this.edges()) {
				if (otherEdge.intersect(null, myEdge)) return true;
			}
		}
		return false;
	}

	public boolean contains(Polygon polygon) {
		for (Vec2 v : polygon.vertices) {
			if (!this.contains(v.x, v.y)) return false;
		}
		return true;
	}

	public double distanceToEdge(double x, double y) {
		double dist = Double.POSITIVE_INFINITY;
		Vec2 pt = new Vec2(x, y);
		Vec2 hold = new Vec2(0, 0);
		for (LineSeg edge : edges()) {
			double d = edge.closestPoint(pt, hold);
			dist = Math.min(dist, d);
		}
		return dist;
	}

	public double getPerimeter() {
		double perimeter = 0;
		for (LineSeg side : edges()) {
			perimeter += side.length();
		}
		return perimeter;
	}

}
