package dev.mortus.util.math.geom;

import java.awt.Shape;
import java.awt.geom.Path2D;
import java.util.List;

public class Polygon {

	final Vec2[] vertices;
	private Vec2 centroid;
	private double area = Double.NaN;
	private Shape shape;

	public Polygon(Vec2... vertices) {
		this.vertices = new Vec2[vertices.length];
		System.arraycopy(vertices, 0, this.vertices, 0, vertices.length);
	}

	public Polygon(List<Vec2> vertices) {
		this.vertices = new Vec2[vertices.size()];
		int i = 0;
		for (Vec2 v : vertices) {
			this.vertices[i] = v;
			i++;
		}
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

}
