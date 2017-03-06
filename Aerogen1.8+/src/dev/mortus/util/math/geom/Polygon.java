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
		if (area == Double.NaN) {
			area = 0;
			for (int i = 0; i < vertices.length; i++) {
				Vec2 a = vertices[i];
				Vec2 b = vertices[0];
				if (i + 1 < vertices.length)
					b = vertices[i + 1];
				area += a.getX() * b.getY() - b.getX() * a.getY();
			}
			area = area / 2;
		}
		return area;
	}

	public Vec2 getCentroid() {
		if (centroid == null) {
			double cx = 0, cy = 0;
			double area = 0;
			for (int i = 0; i < vertices.length; i++) {
				Vec2 a = vertices[i];
				Vec2 b = vertices[0];
				if (i + 1 < vertices.length)
					b = vertices[i + 1];
				double cross = a.cross(b);
				area += cross;
				cx += (a.getX() + b.getX()) * cross;
				cy += (a.getY() + b.getY()) * cross;
			}
			area /= 2;
			this.area = area;
			
			cx /= (area * 6);
			cy /= (area * 6);
			this.centroid = Vec2.create(cx, cy);
			if (this.area == 0) System.err.println("Polygon has 0 area!");
		}
		return this.centroid;
	}

	public Shape getShape2D() {
		if (shape == null) {
			Path2D path = new Path2D.Double();
			boolean first = true;
			for (Vec2 v : vertices) {
				if (first) {
					path.moveTo(v.getX(), v.getY());
					first = false;
				} else {
					path.lineTo(v.getX(), v.getY());
				}
			}
			if (!first) {
				path.closePath();
			}
			if (vertices.length == 0) System.err.println("Polygon has no vertices!");
			//System.out.println("Creating shape with " + vertices.length + " vertices");
			shape = path;
		}
		return shape;
	}

}
