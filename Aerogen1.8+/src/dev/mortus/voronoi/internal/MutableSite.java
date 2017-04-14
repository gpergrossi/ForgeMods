package dev.mortus.voronoi.internal;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import dev.mortus.util.math.geom.Vec2;
import dev.mortus.voronoi.diagram.Edge;
import dev.mortus.voronoi.diagram.Site;
import dev.mortus.voronoi.diagram.Vertex;
import dev.mortus.voronoi.diagram.Voronoi;

public class MutableSite extends Site {

	protected boolean isFinished;
	
	MutableSite(Voronoi voronoi, int id, Vec2 pos) {
		super(voronoi, id, pos.getX(), pos.getY());
		this.isFinished = false;
		this.isClosed = false;
	}
	
	public boolean isClosed() {
		if (isFinished) return isClosed;
		if (vertices == null) return false;
		for (Vertex v : vertices) {
			if (v.isBoundary()) return false;
		}
		return true;
	}

	void addVertex(MutableVertex vertex) {
		vertices.add(vertex);
	}

	void addEdge(MutableEdge edge) {
		edges.add(edge);
	}

	void sortVertices(Comparator<Vertex> order) {
		vertices.sort(order);
	}

	void sortEdges(Comparator<Edge> order) {
		edges.sort(order);
	}

	MutableVertex getLastVertex() {
		if (vertices.size() == 0) return null;
		return (MutableVertex) vertices.get(vertices.size()-1);
	}

	Iterable<MutableVertex> getVertexIterable() {
		Iterator<Vertex> iter = vertices.iterator();
		
		return new Iterable<MutableVertex>() {
			@Override
			public Iterator<MutableVertex> iterator() {
				return new Iterator<MutableVertex>() {

					@Override
					public boolean hasNext() {
						return iter.hasNext();
					}

					@Override
					public MutableVertex next() {
						return (MutableVertex) iter.next();
					}
					
				};
			}
		};
	}

	void makeListsUnmodifiable() {
		edges = Collections.unmodifiableList(edges);
		vertices = Collections.unmodifiableList(vertices);
	}

}
