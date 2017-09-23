package com.gpergrossi.util.geom.shapes;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import com.gpergrossi.util.geom.vectors.Double2D;

public abstract class Polygon implements IShape {

	/**
	 * Removes duplicates and converts mutables to immutables, modifying
	 * the given vertex array and potentially overwriting some elements
	 * @param vertices - array of vertices
	 * @return number of vertices remaining
	 */
	public static int sanitize(Double2D[] vertices) {	
		int numValid = 0;
		for (int i = 0; i < vertices.length; i++) {
			if (vertices[i] == null) continue;
			
			boolean valid = true;
			for (int j = 0; j < i; j++) {
				if (vertices[j] == null) continue;
				if (vertices[j].equals(vertices[i])) valid = false;
			}
			if (!valid) continue;
			
			if (vertices[i] instanceof Double2D.Mutable) {
				vertices[i] = ((Double2D.Mutable) vertices[i]).immutable();
			}
			vertices[numValid++] = vertices[i];
		}		
		return numValid;
	}
	
	/**
	 * Checks if the given vertex array is convex and ensures that it is in counter-clockwise order.
	 * The input vertex array may be flipped as a result.
	 * @param verts
	 * @return
	 */
	public static boolean checkConvex(Double2D[] vertices, int count) {
		if (vertices.length < count) throw new IllegalArgumentException("The array must have at least as many elements as count describes");
		if (count < 3) throw new NoSuchElementException("Vertex array must have at least 3 vertices");
		
		Double2D prevVert = vertices[count-1];
		Double2D prevEdge = prevVert.subtract(vertices[count-2]);
		
		for (int i = 0; i < count; i++) {
			Double2D currVert = vertices[i];
			Double2D currEdge = currVert.subtract(prevVert);
			
			// A negative cross product indicates the next edge bent clockwise, I.E. concave
			if (prevEdge.cross(currEdge) < 0) return false;
			
			prevVert = currVert;
			prevEdge = currEdge;			
		}
		
		return true;
	}
	
	/**
	 * This method is UNSAFE, because it uses the given vertex array directly.
	 * The vertices will not be sanitized and a duplicate array will not be created.
	 * Therefore, after this array has been given to the Polygon, it should not be
	 * read or written anywhere else.
	 * @param rawVerts - an array to use as the internal vertex array of this polygon
	 * @return a polygon
	 */
	public static Convex createPolygonDirect(Double2D[] rawVerts) {
		return new Convex(rawVerts);
	}
	
	/**
	 * This method creates a polygon after sanitizing the input vertices.
	 * If this method was called with a vertex array, the vertex array will
	 * be modified.
	 * @param vertices - input vertices or vertex array (which will be modified)
	 * @return a polygon
	 */
	public static Convex createPolygon(Double2D... vertices) {
		int count = sanitize(vertices);
		Double2D[] verts = new Double2D[count];
		System.arraycopy(vertices, 0, verts, 0, count);
		return new Convex(verts);
	}
	
	/**
	 * Creates a polygon from this list of vertices. This method is safe but wasteful. 
	 * It creates a copy of the given list before sanitizing the input and finally producing
	 * a Polygon object. An intermediate array is created and discarded.
	 * @param vertices - list of vertices (will not be modified)
	 * @return a polygon
	 */
	public static Convex createPolygon(List<Double2D> vertices) {
		Double2D[] array = new Double2D[vertices.size()];
		vertices.toArray(array);
		return createPolygon(array);
	}
	
	public abstract int getNumSides();
	public abstract LineSeg getSide(int i);
	
	public abstract int getNumVertices();
	public abstract Double2D getVertex(int i);
	
	public abstract boolean isConvex();
	public abstract int getNumConvexParts();
	public abstract Convex getConvexPart(int i);
	
	public Iterable<LineSeg> getSides() {
		return new Iterable<LineSeg>() {
			public Iterator<LineSeg> iterator() {
				return new Iterator<LineSeg>() {
					int index = 0;
					public boolean hasNext() {
						return index < getNumSides();
					}
					public LineSeg next() {
						if (index >= getNumSides()) throw new NoSuchElementException();
						return getSide(index++);
					}
				};
			}
		};
	}
	
	public Iterable<Double2D> getVertices() {
		return new Iterable<Double2D>() {
			public Iterator<Double2D> iterator() {
				return new Iterator<Double2D>() {
					int index = 0;
					public boolean hasNext() {
						return index < getNumVertices();
					}
					public Double2D next() {
						if (index >= getNumVertices()) throw new NoSuchElementException();
						return getVertex(index);
					}
				};
			}
		};
	}
	
	public Iterable<Convex> getConvexParts() {
		return new Iterable<Convex>() {
			public Iterator<Convex> iterator() {
				return new Iterator<Convex>() {
					int index = 0;
					public boolean hasNext() {
						return index < getNumConvexParts();
					}
					public Convex next() {
						if (index >= getNumConvexParts()) throw new NoSuchElementException();
						return getConvexPart(index);
					}
				};
			}
		};
	}
	
}
