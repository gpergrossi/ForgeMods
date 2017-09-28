package com.gpergrossi.test;

import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.Iterator;

import org.junit.Test;

import com.gpergrossi.util.data.interop.InteropManager;
import com.gpergrossi.util.data.interop.annotations.InteropCast;
import com.gpergrossi.util.data.interop.annotations.InteropCast.CastType;
import com.gpergrossi.util.data.interop.annotations.InteropClass;
import com.gpergrossi.util.data.interop.annotations.InteropClass.ClassType;
import com.gpergrossi.util.data.interop.annotations.InteropMethod.ClassProperty;
import com.gpergrossi.util.data.interop.annotations.InteropMethod;

public class InteropTest {

	@InteropClass
	public static abstract class Shape {
		InteropManager<Shape> shapeInterops = new InteropManager<>(Shape.class); // TODO auto-scan for classes
		
		@Override
		public String toString() {
			return "AbstractShape[...]";
		}
		
		@InteropMethod(properties = {ClassProperty.REFLEXIVE})
		public boolean intersects(Shape shape) {
			return shapeInterops.doMethod("intersects", Boolean.class, this, shape);
		}
	}
	
	@InteropClass(type = ClassType.BRIDGE, parent = Shape.class)
	public static class PolygonIntersection {
		@InteropMethod(method = "intersects")
		public static boolean intersects(Polygon a, Polygon b) {
			System.out.println("Intersect check on "+a+"-"+b);
			return true;
		}
	}
	
	@InteropClass(type = ClassType.SUBCLASS, parent = Shape.class)
	public static class Triangle extends Shape {
		Point2D[] verts;
		public Triangle(double x0, double y0, double x1, double y1, double x2, double y2) {
			this.verts = new Point2D.Double[] {
				new Point2D.Double(x0, y0),
				new Point2D.Double(x1, y1),
				new Point2D.Double(x2, y2)
			};
		}
		
		@InteropCast(CastType.AdaptToMethod)
		public Polygon toPolygon() {
			return new Polygon(verts);
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("Triangle[verts={");
			Iterator<Point2D> iter = Arrays.asList(verts).iterator();
			while (iter.hasNext()) {
				Point2D vert = iter.next();
				sb.append('(').append(vert.getX()).append(',').append(vert.getY()).append(")");
				if (iter.hasNext()) sb.append(", ");
			}
			sb.append("}]");
			return sb.toString();
		}
	}

	@InteropClass(type = ClassType.SUBCLASS, parent = Shape.class)
	public static class Rectangle extends Shape {
		double x, y, width, height;
		public Rectangle(double x, double y, double width, double height) {
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
		}
		
		@Override
		public String toString() {
			return "Rectangle[x="+x+", y="+y+", width="+width+", height="+height+"]";
		}
	}

	@InteropSubClass(parent = Shape.class)
	public static class Polygon extends Shape {
		Point2D[] verts;
		public Polygon(Point2D... verts) {
			Point2D[] copy = new Point2D[verts.length];
			System.arraycopy(verts, 0, copy, 0, verts.length);
			this.verts = copy;
		}
		
		@InteropCast(CastType.AdaptFromMethod)
		public static Polygon fromRectangle(Rectangle r) {
			Point2D[] verts = new Point2D[4];
			verts[0] = new Point2D.Double(r.x, r.y);
			verts[1] = new Point2D.Double(r.x+r.width, r.y);
			verts[2] = new Point2D.Double(r.x+r.width, r.y+r.height);
			verts[3] = new Point2D.Double(r.x, r.y+r.height);
			return new Polygon(verts);
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("Polygon[verts={");
			Iterator<Point2D> iter = Arrays.asList(verts).iterator();
			while (iter.hasNext()) {
				Point2D vert = iter.next();
				sb.append('(').append(vert.getX()).append(',').append(vert.getY()).append(")");
				if (iter.hasNext()) sb.append(", ");
			}
			sb.append("}]");
			return sb.toString();
		}
	}
	
	@Test
	public void castTest() {
		InteropManager<Shape> shapeInterops = new InteropManager<>(Shape.class);
		shapeInterops.registerClass(Triangle.class);
		shapeInterops.registerClass(Rectangle.class);
		shapeInterops.registerClass(Polygon.class);
		
		Rectangle rect = new Rectangle(0, 0, 100, 100);
		System.out.println(rect);
		
		Polygon p1 = shapeInterops.adapt(rect, Polygon.class);
		System.out.println(p1);
		
		System.out.println("");
		
		
		Triangle t = new Triangle(0, 0, 50, 20, 20, 50);
		System.out.println(t);
		
		Polygon p2 = shapeInterops.adapt(t, Polygon.class);
		System.out.println(p2);
		
		System.out.println("");
	}
	
}
